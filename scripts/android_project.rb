#!/usr/bin/env ruby

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'project'

class AndroidProject < Project

  attr_reader :manifest_xml

  def initialize pj_name
    super pj_name
    manifest_file = "#{@path}/AndroidManifest.xml"
    LOGGER.debug "reading manifest for project '#{@name}' from '#{manifest_file}'"
    @manifest_xml = REXML::Document.new(File.new(manifest_file))
  end

  def create_build_env
    super
    merge_source_folders
    generate_properties
  end
  
  def merge_source_folders
    LOGGER.info " * Merging source folders for #{name}"
    File.makedirs "#{path}/merged_src"
    
    if File.exists?("#{path}/src")
      LOGGER.info " * Merging source folders", <<-EOT
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

    LOGGER.info " * Uninstalling '#{name}' on device id: '#{device_id}'"
    package = @manifest_xml.root.attributes["package"]
    LOGGER.info "   * Uninstalling package: #{package}"
    run "uninstalling with adb", <<-EOT
      adb #{s} uninstall #{package} 
    EOT
  end

  def install device_id="", additional_install_flags=""
    s = "-s #{device_id}" unless device_id.nil? || device_id.empty?

    apk_dir = "#{path}/bin"
    LOGGER.info " * Installing '#{name}' on device id: '#{device_id}', looking for apk in '#{apk_dir}'"
    Dir["#{apk_dir}/*.apk"].each do |apk|
      run "installing", <<-EOT
        adb #{s} push #{apk} /data/local/
        adb #{s} shell pm install #{additional_install_flags} /data/local/#{File.basename apk}
      EOT
    end
  end

  def reinstall device_id=""
    # adb can reinstall apps when using the -r flag 
    install device_id, "-r"
  end

  # run adroid instumentation tests; returns true if succsessfull
  def test
    LOGGER.info " * Testing project '#{@name}':"
    
    successful_tests        = 0
    broken_instrumentations = 0
    failed_tests            = 0
    tests_with_exception    = 0
    test_suite_success      = false
    
    LOGGER.info "    * Reinstalling packages (to determine testsuites present)..."
    system("rake removeartcom") # TODO handle error
    system("rake install") # TODO handle error
    LOGGER.info "    * Reinstalling packages... DONE"
    
    # check the manifest instrumentation test definiton
    node = REXML::XPath.first(@manifest_xml, "*/instrumentation")
    return {:was_successful => true} unless node
    
    package = node.attributes["targetPackage"]
    testrunner = node.attributes["name"]

    log_command = "adb shell am instrument -w -e log true #{package}/#{testrunner}"
    adb_test_suites = open "|#{log_command}"
    suite_list = []
    while (line = adb_test_suites.gets)
      if line.include? ":" then
        line_array = line.split(":")
        line_first_part = line_array[0]
        line_second_part = line_array[1]
        if (line_first_part.include? "#{package}" ) and ((line_second_part =~ /\A\.+\s*$/) == 0) then
          suite_list.push(line_first_part)
        end
      end
    end

    LOGGER.info "Found #{suite_list.size} Testsuites: #{suite_list.inspect}"
    
    suite_list.each_with_index { |suite, index|
        exception_next_line = false
        LOGGER.info " * Suite: #{suite} ... START"
        if index != 0
          LOGGER.info " * Preparing testrun for Testsuite: #{suite} in project #{@name}"
          LOGGER.info "    * Reinstalling packages..."
          system("rake removeartcom")
          system("rake install")
          LOGGER.info "    * Reinstalling packages... DONE"
        else
          LOGGER.info " * Preparation is not necessary since packages were installed freshly since the last test run"
        end
        LOGGER.info " * Executing testsuite #{suite} in project #{@name}"
        test_log_output = open "|adb shell am instrument -w -e class #{suite} #{package}/#{testrunner}"
        while (line=test_log_output.gets)
            LOGGER.info line
            if line.include? "INSTRUMENTATION" then
              broken_instrumentations += 1
              test_suite_success = false
            elsif line.include? "OK (" then
              successful_tests += line.scan(/OK \((\d*) \w+\)/)[0][0].to_i
              test_suite_success = true
            elsif line.include? "FAILURES!!!" then
              exception_next_line = true
            end
            if exception_next_line
              exception_next_line = false
              successful_tests += line.scan(/Tests run: (\d*)/)[0][0].to_i
              failed_tests += line.scan(/Failures: (\d*)/)[0][0].to_i
              tests_with_exception += line.scan(/Errors: (\d*)/)[0][0].to_i
              test_suite_success = false
              break
            end
        end
        LOGGER.info " * Suite: #{suite} ... END"
    }
    return {:was_successful => test_suite_success, 
            :tests_run => successful_tests, 
            :tests_failed => failed_tests,
            :tests_with_exception => tests_with_exception,
            :broken_instrumentation => broken_instrumentations}
  end

end