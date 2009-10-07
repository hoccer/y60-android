#!/usr/bin/ruby

class AndroidProject < Project

  attr_reader :manifest_xml

  def initialize pj_name
    super pj_name
  
    manifest_file = "#{path}/AndroidManifest.xml"
    puts "reading manifest for #{name} from #{manifest_file}"
    @manifest_xml = REXML::Document.new(File.new(manifest_file))
    
  end

  def create_build_env
    super
    merge_source_folders
    generate_properties
  end
  
  def merge_source_folders
    puts "merging source folders for #{name}"
    File.makedirs "#{path}/merged_src"
    
    if File.exists?("#{path}/src")
      run "merging source folders", <<-EOT
        cp -r #{path}/src/* #{path}/merged_src
      EOT
    end
    
    # copy source code from tests only if it exists
    if File.exists?("#{path}/tests")
      run "merging source folders", <<-EOT
        cp -r #{path}/tests/* #{path}/merged_src
      EOT
    end
  end
  
  def generate_properties
    generate_from_template "default.properties"
    generate_from_template "build.properties"
  end

  def uninstall device_id = ""
    s = "-s #{device_id}" unless device_id.nil? || device_id.empty?

    puts "uninstalling #{name} on device id #{device_id}"
    package = @manifest_xml.root.attributes["package"]
    puts "uninstalling package: #{package}"
    run "uninstalling with adb", <<-EOT
      adb #{s} uninstall #{package} 
    EOT
  end

  def install device_id=""
    s = "-s #{device_id}" unless device_id.nil? || device_id.empty?

    apk_dir = "#{path}/bin"
    puts "installing #{name} #{device_id}, looking for apk in #{apk_dir}"
    Dir["#{apk_dir}/*.apk"].each do |apk|
      run "installing", <<-EOT
        adb #{s} push #{apk} /data/local/
        adb shell pm install /data/local/#{File.basename apk}
        adb shell rm /data/local/#{File.basename apk}
      EOT
    end
  end

  def reinstall device_id=""
    uninstall device_id
    install device_id
  end

  # run adroid instumentation tests; returns true if succsessfull
  def test
    puts "testing #{name}"    
   
    # check the manifest instrumentation test definiton
    node = REXML::XPath.first(@manifest_xml, "*/instrumentation")
    return {:was_succsessful => true} unless node
    
    package = node.attributes["targetPackage"]
    testrunner = node.attributes["name"]
    
    tmpfile = Tempfile.new node.hash
    cmd = "adb shell am instrument -w #{package}/#{testrunner} > #{tmpfile.path}"
    puts cmd
    run "running tests", <<-EOT
      cd #{path} && #{cmd}
    EOT
    
    test_result = tmpfile.read
  
    if test_result.include? "INSTRUMENTATION" then
      puts test_result
      return {:was_succsessful => false, :broken_instrumentation => true}
    elsif test_result.include? "OK (" then
      tests_run = test_result.scan(/OK \((\d*) tests\)/)[0][0].to_i
      return {:was_succsessful => true, :tests_run => tests_run}
    elsif test_result.include? "FAILURES!!!" then
        statistics = test_result.scan(/Tests run: (\d*),  Failures: (\d*),  Errors: (\d*)/)[0]
        puts test_result
        return {:was_succsessful => false, 
                :tests_run => statistics[0].to_i, 
                :tests_failed => statistics[1].to_i,
                :tests_with_exception => statistics[2].to_i}
    else
      puts test_result
      return {:was_succsessful => false}
    end

  end

  
end
