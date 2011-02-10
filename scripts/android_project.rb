#!/usr/bin/env ruby
$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'project'
require 'open3'
require 'lib/test_result_collector'
require 'lib/os-helper'

class AndroidProject < Project

  attr_reader :manifest_xml, :package

  ADB_SLEEP_TIME = 3
  EMULATOR_BOOT_SLEEP_TIME = 40
  SU_PACKAGE = "com.noshufou.android.su"

  def initialize pj_name
    super pj_name
    manifest_file = "#{@path}/AndroidManifest.xml"
    LOGGER.debug "reading manifest for project '#{@name}' from '#{manifest_file}'"
    @manifest_xml = REXML::Document.new(File.new(manifest_file))
    @package = @manifest_xml.root.attributes["package"]
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
    LOGGER.info "   * Uninstalling package: #{@package}"
    run "uninstalling with adb", <<-EOT
      sleep #{ADB_SLEEP_TIME}
      adb #{s} uninstall #{@package} 
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
    
    if File::exist? "#{path}/grant_root_permission" 
      LOGGER.info " * Trying to grant root permissions to #{@package} (#{@name})"
      grant_root_permission device_id
    end
  end
  
  def has_device_root_permissions device_id=""
    s = "-s #{device_id}" unless device_id.nil? || device_id.empty?        
     stdin, stdout, stderr = OS::executePopen3("adb #{s} shell id")    
     if stdout.to_s.include? "root"
       return true
     end
     return false
   end
  
  def grant_root_permission device_id=""

    if !has_device_root_permissions device_id    
      LOGGER.info " * Device with device id: '#{device_id}' has no root permissions"
      return
    end

    s = "-s #{device_id}" unless device_id.nil? || device_id.empty?
    uid = getPackageUid @package, device_id

    sqlPatch = <<MYSQLITE
INSERT OR FAIL INTO apps (uid, package, name, exec_uid, exec_cmd, allow)
VALUES(#{uid}, "#{@package}", "#{@name}", 0, "/system/bin/sh", 1);
MYSQLITE
    
    mySqlPatchFile = Tempfile.new "grant_root_for_#{@package}.sql"
    mySqlPatchFile << sqlPatch
    mySqlPatchFile.flush
    system "adb #{s} pull /data/data/#{SU_PACKAGE}/databases/permissions.sqlite /tmp/permissions.sqlite.db"
    system "sqlite3 /tmp/permissions.sqlite.db < #{mySqlPatchFile.path}"
    system "adb #{s} push /tmp/permissions.sqlite.db /data/data/#{SU_PACKAGE}/databases/permissions.sqlite"
    mySqlPatchFile.close
    system "rm /tmp/permissions.sqlite.db"
    
    su_package_uid = self.getPackageUid SU_PACKAGE, device_id
    system "adb #{s} shell chmod 660 /data/data/#{SU_PACKAGE}/databases/permissions.sqlite"
    system "adb #{s} shell chown #{su_package_uid}:#{su_package_uid} /data/data/#{SU_PACKAGE}/databases/permissions.sqlite"
    LOGGER.info "   * Successfully granted root permissions"    
  end
  
  def getPackageUid package, device_id=""
    s = "-s #{device_id}" unless device_id.nil? || device_id.empty?
    myResult = `adb #{s} shell busybox ls -la /data/data/#{package}`
    myResult.to_s.split("\n").to_a[1].split[2]
  end

  def reinstall device_id=""
    # adb can reinstall apps when using the -r flag 
    install device_id, "-r"
  end

  # run android instumentation tests; returns TestResultCollector
  def test
    LOGGER.info " * Testing project '#{@name}':"
    myTestResultCollector = TestResultCollector.new
    
    # check the manifest instrumentation test definiton
    node = REXML::XPath.first(@manifest_xml, "*/instrumentation")
    # No Tests - early exit
    return myTestResultCollector unless node
    
    AndroidProject::restore_from_snapshot
    
    package = node.attributes["targetPackage"]
    testrunner = node.attributes["name"]
    
    LOGGER.info "    * Determining testsuites present for project '#{@name}' ..."
    log_command = "adb shell am instrument -w -e log true #{package}/#{testrunner}"
    LOGGER.info "      * Command used: '#{log_command}'"
    adb_test_suites = open "|#{log_command}"
    
    puts "#{adb_test_suites}.inspect"
    
    suite_list = []
    while (line = adb_test_suites.gets)
      LOGGER.debug line
      if line.include? ":" then
        line_array = line.split(":")
        line_first_part = line_array[0]
        line_second_part = line_array[1]
        if (line_first_part.include? "#{package}" ) and ((line_second_part =~ /\A\.+\s*$/) == 0) then
          LOGGER.info "    * Adding Suite: '#{line_first_part}'"
          suite_list.push(line_first_part)
        end
      end
    end
    adb_test_suites.close

    LOGGER.info "Found #{suite_list.size} Testsuites: #{suite_list.inspect}"
    
    suite_list.each_with_index { |suite, index|
        LOGGER.info " * Suite: #{suite} ... START (#{index + 1} of #{suite_list.size})"
        
        if index != 0
          LOGGER.info " * Preparing testrun for Testsuite: '#{suite}' in project '#{@name}'"
          AndroidProject::restore_from_snapshot
        end
        
        LOGGER.info " * Executing testsuite '#{suite}' in project '#{@name}'"
        testing_cmd = "adb shell am instrument -w -e class #{suite} #{package}/#{testrunner}"
        LOGGER.info "    via command: '#{testing_cmd}'"
        LOGGER.info " ------------------- OUTPUT START"

        test_result = nil
        lineno = 0
        test_log_output = open "|#{testing_cmd}"
        while (line=test_log_output.gets)
            lineno += 1
            LOGGER.info "#{lineno}\t#{line.chomp}"
            if test_result.nil?
              tmp_result = AndroidProject::extract_test_status line
              test_result = tmp_result if tmp_result
            end
        end
        test_log_output.close
        LOGGER.info " ------------------- OUTPUT END"
        if test_result
          test_result.test_suite_name = suite
          LOGGER.info "\n Result for suite '#{suite}' (#{index + 1} of #{suite_list.size}) in project '#{@name}':\n#{test_result}"
          myTestResultCollector << test_result
          
          if index + 1 != suite_list.size
            LOGGER.info "Total results for project '#{@name}' so far ...:"
            LOGGER.info "\n#{myTestResultCollector}\n"
          end
        else
          LOGGER.info "    * Test result is still nil - something went wrong while parsing output? -ABORTING!"
          raise "Error while executing testsuite #{suite} in project #{@name} - no test_result received"
        end
        LOGGER.info " * Suite: #{suite} ... END"
    }
    
    if suite_list.size == myTestResultCollector.test_results.size
      LOGGER.info " * Collected Testresults for #{suite_list.size} suites in project '#{@name}'! OK"
    else
      LOGGER.info " * Collected Testresults for ONLY #{myTestResultCollector.test_results.size} test suites in project '#{@name}' - Should have been #{suite_list.size}"
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
      if trial > 3 and
        raise "giving up tries: #{trial}"
      end
      
      system("rake emulator:kill_all")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
      
      system("rake emulator:boot")
      LOGGER.info "      * sleeping #{EMULATOR_BOOT_SLEEP_TIME} secs..."
      sleep EMULATOR_BOOT_SLEEP_TIME

      system("adb kill-server")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
      system("adb start-server")
      LOGGER.info "      * sleeping 15 secs..."
      sleep 15

      emulator_running_result = false
      10.times {  
          emulator_running_result= system "rake emulator:is_running --silent"
          if emulator_running_result
            LOGGER.info "    * could verify that the emulator is running"
            break
          end
          LOGGER.info "    * could not verify that the emulator is running, sleeping 10 seconds ..."
          sleep 10
      }
      unless emulator_running_result
          LOGGER.info "cannot see devices - rebooting once more..."
          AndroidProject::reboot_emulator trial + 1
      end
      LOGGER.info "Emulator seems to have booted fine..."

      result = false
      20.times {  
          LOGGER.info "verifying device_config.json presence"
          result = system("rake device_config:verify --silent")
          if result 
            break
          elsif
              LOGGER.info "device_config.json is not present, will try again"
          end
          LOGGER.info "      * sleeping 10 secs..."
          sleep 10
      }
      unless result
          LOGGER.info "Device config is not present!"
          AndroidProject::reboot_emulator trial + 1
      end
      LOGGER.info "Setting port forward"
      system("rake emulator:port_forward")
      LOGGER.info "      * sleeping 5 secs..."
      sleep 5
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
