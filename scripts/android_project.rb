#!/usr/bin/env ruby

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'project'
require 'open3'
require 'lib/test_result_collector'

class AndroidProject < Project

  attr_reader :manifest_xml

  ADB_SLEEP_TIME = 3

  def initialize pj_name
    super pj_name
    manifest_file = "#{@path}/AndroidManifest.xml"
    LOGGER.debug "reading manifest for project '#{@name}' from '#{manifest_file}'"
    @manifest_xml = REXML::Document.new(File.new(manifest_file))
    
    @test_settings = YAML::load(File.open(File.join(@path, 'test_settings.yml'))) rescue {:suites => {}}
    #puts @test_settings.inspect
    #puts @test_settings['suites']['com.artcom.y60.dc.DeviceControllerHandlerTest']['testmode'].to_sym
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

    LOGGER.info " * Uninstalling '#{name}' on device id: '#{device_id}'"
    package = @manifest_xml.root.attributes["package"]
    LOGGER.info "   * Uninstalling package: #{package}"
    run "uninstalling with adb", <<-EOT
      sleep #{ADB_SLEEP_TIME}
      adb #{s} uninstall #{package} 
    EOT
  end

  def install device_id="", additional_install_flags=""
    s = "-s #{device_id}" unless device_id.nil? || device_id.empty?

    apk_dir = "#{path}/bin"
    LOGGER.info " * Installing '#{name}' on device id: '#{device_id}', looking for apk in '#{apk_dir}'"
    Dir["#{apk_dir}/*.apk"].each do |apk|
      run "installing", <<-EOT
        sleep #{ADB_SLEEP_TIME}
        adb #{s} push #{apk} /data/local/
        sleep #{ADB_SLEEP_TIME}
        adb #{s} shell pm install #{additional_install_flags} /data/local/#{File.basename apk}
      EOT
    end
  end

  def reinstall device_id=""
    # adb can reinstall apps when using the -r flag 
    install device_id, "-r"
  end

  # run android instumentation tests; returns true if succsessfull
  def test
    LOGGER.info " * Testing project '#{@name}':"
    myTestResultCollector = TestResultCollector.new
    
    # check the manifest instrumentation test definiton
    node = REXML::XPath.first(@manifest_xml, "*/instrumentation")
    # No Tests - early exit
    return myTestResultCollector unless node
    
    LOGGER.info "    * Determining testsuites present ..."
    AndroidProject::restore_from_snapshot
    
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
        LOGGER.info " * Suite: #{suite} ... START (#{index + 1} of #{suite_list.size})"
        suite_testsetting = @test_settings['suites'][suite]['testmode'] rescue 'normal'
        LOGGER.info "   * testsetting: '#{suite_testsetting}'"
        
        if suite_testsetting != 'skip'
          if suite_testsetting == 'normal'
            if index != 0
              LOGGER.info " * Preparing testrun for Testsuite: #{suite} in project #{@name}"
              AndroidProject::restore_from_snapshot
            else
              LOGGER.info " * Preparation is not necessary since packages were installed freshly since the last test run"
            end
          elsif suite_testsetting == 'no-reinstall'
            LOGGER.info "  * Not reinstalling according to test_setting for this suite."
          end
          LOGGER.info " * Executing testsuite #{suite} in project #{@name}"
          test_log_output = open "|adb shell am instrument -w -e class #{suite} #{package}/#{testrunner}"
          test_result = nil
          while (line=test_log_output.gets)
              LOGGER.info line
              if test_result.nil?
                tmp_result = AndroidProject::extract_test_status line
                test_result = tmp_result if tmp_result
              end
          end
          if test_result
            test_result.test_suite_name = suite
            LOGGER.info "\n#{test_result}"
            myTestResultCollector << test_result
          end
        else
          LOGGER.info "   * skipped suite '#{suite}' as indicated by 'test_setting.yml'"
        end
        LOGGER.info " * Suite: #{suite} ... END"
    }
    if suite_list.size == myTestResultCollector.test_results.size
      LOGGER.info " * Collected Testresults for #{suite_list.size} suites! OK"
    else
      LOGGER.info " * Collected Testresults for ONLY #{myTestResultCollector.test_results.size} - Should have been #{suite_list.size}"
      raise "Invalid number of testsuite test results collected!"
    end
    return myTestResultCollector
  end
  
  private
  
    def self.extract_test_status line
      # Format for ok:
      #  OK (1 test) or OK (x tests) where x is > 1
      result = line.match(/^OK \((\d*) \w+\)/)
      if result
        return TestResult.new result[1].to_i,0,0,0
      end
      
      # Format for failures:
      #  Tests run: %d, Failures: %d, Errors: %d
      result = line.match(/^Tests run: (\d*),  Failures: (\d*),  Errors: (\d*)/)
      if result
        return TestResult.new result[1].to_i,result[2].to_i,result[3].to_i,0
      end
      
      # Format for broken instrumentations:
      #  INSTRUMENTATION in the line
      #result = line.match(/INSTRUMENTATION_FAILED: ([a-zA-Z.0-9\/]*InstrumentationTestRunner)/)
      result = line.match(/^(INSTRUMENTATION).*$/)
      if result
        return TestResult.new 0,0,0,1
      end
      
      return nil
    end
    
    def self.get_device_list
      stdin, stdout, stderr = Open3.popen3("adb devices")
      stderr = stderr.read
      stdout = stdout.read
      raise stderr if stderr != ''
      devices = []
      stdout.split("\n").each { |line|
        devices << line.split("\t").first if line.to_s.end_with?("device")
      }
      return devices
    end
  
    def self.reboot_emulator trial=0
      LOGGER.info "Rebooting emulator : try: #{trial}"
      
      system("rake emulator:kill_all")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
      system("adb kill-server")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
      system("adb start-server")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
      system("rake emulator:boot")
      LOGGER.info "      * sleeping 120 secs..."
      sleep 120
      if trial == 0 and
        AndroidProject::get_device_list.size == 0
        LOGGER.info "cannot see devices - rebooting once more..."
        AndroidProject::reboot_emulator trial + 1
      elsif trial > 0 and
        AndroidProject::get_device_list.size == 0
        LOGGER.error "Cannot boot device - giving up"
        raise "Cannot reboot emulator - giving up tries: #{trial}"
      end
      LOGGER.info "Emulator seems to have booted fine..."
      system("rake emulator:port_forward")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
      LOGGER.info "verifying device_config.json presence"
      result = system("rake device_config:verify")
      raise "Device config is not present!" unless result
    end
  
    def self.reinstall_artcom_packages with_reboot=false
      rebooted = false
      if AndroidProject::get_device_list.size == 0
        LOGGER.info "First rebooting emulator since it seems not to be running...."
        AndroidProject::reboot_emulator
        rebooted = true
      end
      LOGGER.info "    * Reinstalling packages... START"
      system("rake removeartcom")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
      if with_reboot and !rebooted
        AndroidProject::reboot_emulator
      end
      system("rake install")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
      LOGGER.info "    * Reinstalling packages... DONE"
    end

    def self.restore_from_snapshot
      LOGGER.info "    * Restoring from snapshot..."
      LOGGER.info "    * Stopping all emulators."
      system("rake emulator:kill_all")
      sleep 5
      
      LOGGER.info "    * Restoring snapshot and booting device..."
      system("rake emulator:snapshot:restore")
      sleep 5
      AndroidProject::reboot_emulator
    end

end