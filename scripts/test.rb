#!/usr/bin/env ruby

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'logging'

require "yaml"
require 'project'
require 'java_project'
require 'android_project'
require 'lib/test_result_collector'

$boot_sleep_time = 60

def main pj_names
  
  if pj_names.first == 'all' || pj_names == [] || pj_names.nil?
    LOGGER.info "Executing tests for all projects."
    myListOfProjectsToTest = []
  else
    LOGGER.info "Executing tests for projects: #{pj_names.inspect}"
    myListOfProjectsToTest = pj_names
  end
  
  last_project_sorting_file = "/tmp/project_sorting.yaml"
  if File.exists? last_project_sorting_file then
    last_project_sorting = YAML::load_file(last_project_sorting_file);
  else
    last_project_sorting = []
  end
  
  project_paths = Dir["#{Dir.getwd}/*/.project"]
  LOGGER.info "Project paths: \n\t#{project_paths.inspect}"
  projects = []
  failing_projects = []
  
  last_project_sorting.each { |name|
    index = project_paths.index{|p| p.include? name}
    if index != nil then
      LOGGER.debug " - Adding #{name} to projects (was at index #{index})"
      projects << Project.find_or_create(name, Dir.getwd) #if name != "Y60App" ################### 
      project_paths.delete_at index
    end
  }
  
  project_paths.each { |dir|
    name = File.basename(File.dirname dir)
    projects << Project.find_or_create(name, Dir.getwd) #if name != "Y60App" #####################
  }
  
  projects = projects.select { |p| p.respond_to? :test }
  
  if myListOfProjectsToTest != [] # We got a specific list of project names to test
    known_project_names = projects.map { |p| p.name}
    found_unknown_project_names = false
    myListOfProjectsToTest.each { |project_name|
      if !known_project_names.include? project_name
        found_unknown_project_names = true
        LOGGER.info " * The project named '#{project_name}' is unknown."
      end
    }
    if found_unknown_project_names
      LOGGER.info "ABORTED - found project names to be tested that cannot be found in the testable projects!"
      LOGGER.info "Known testable project names: \n\t#{known_project_names.join("\n\t")}"
      exit 1
    end
    
    projects = projects.select{ |p| myListOfProjectsToTest.include? p.name}
  end
  
  LOGGER.info "Testing #{projects.size} projects: #{projects.map {|p| p.name}.join(' ')}"
  if projects.size == 0
    LOGGER.info "No projects to test - please verify input '#{pj_names}'"
    exit 1
  end
  
  prepare_emulator
  myTestResultCollector = TestResultCollector.new
  projects.each do |project|
    starttime = Time.new
    
    test_result = project.test
    myTestResultCollector << test_result
    if test_result.failed? then
      failing_projects.push project.name
    end
    
    elapsedSeconds = Time.new - starttime
    LOGGER.info "Results for project: '#{project.name}':"
    LOGGER.info "duration: #{elapsedSeconds} seconds"
    LOGGER.info "result: #{TestResult::STATUS_NAMES[test_result.succeeded?.to_s]}"
    LOGGER.info "\n#{test_result}"
  end
  
  success = myTestResultCollector.succeeded?
  
  system "adb pull /sdcard/error_log.txt /tmp/error_log.txt"
  if File.exists? "/tmp/error_log.txt" then
    success = false
    LOGGER.info "\n\nnoticed an error on sdcard:"
    LOGGER.info File.new("/tmp/error_log.txt").read
    system "adb shell rm /sdcard/error_log.txt"
    FileUtils.rm "/tmp/error_log.txt"
  end
  LOGGER.info "\n#{myTestResultCollector}"
  
  if success
    LOGGER.info "all tests succeeded for #{projects.size} projects tested:"
    known_project_names = projects.map { |p| p.name}
    LOGGER.info "\n\t#{known_project_names.join("\n\t")}"
    exit 0
  else
    puts "There were test failures in #{failing_projects.join ', '}"
    last_project_sorting = failing_projects + (last_project_sorting - failing_projects)
    
    File.open(last_project_sorting_file, 'w') {|f| f.write(last_project_sorting.to_yaml) }
    exit 1
  end
  
  LOGGER.info " * Shutting down all emulators"
  system("rake emulator:kill_all")
  
rescue => e
  LOGGER.error "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

def prepare_emulator trial = 0
  LOGGER.info " * Preparing emulator (avd and a snapshot of it)"
  
  LOGGER.info "    * Shutting down all emulators"
  system("rake emulator:kill_all")
  sleep 5
  
  LOGGER.info "    * Deleting emulator according to app_settings..."
  system "rake emulator:delete"
  sleep 5
  
  LOGGER.info "    * Creating emulator according to app_settings..."
  system "rake emulator:create"
  sleep 5
  
  LOGGER.info "    * Booting emulator... (#{$boot_sleep_time} secs startup...)"
  system("rake emulator:boot")
  sleep $boot_sleep_time
  LOGGER.info "     ... done"
  
  LOGGER.info "    * Restarting adb server"
  system("adb kill-server")
  sleep 5
  system("adb start-server")
  sleep 15
  
  result = false
  20.times {  
      LOGGER.info "    * Creating and uploading device_config.json onto sdcard"
      system "rake device_config:generate[true]"
      system "rake device_config:upload"
      result = system "rake device_config:verify"
      if result 
        break
      elsif
          LOGGER.info "device_config.json is not present, will try again"
      end
      LOGGER.info "      * sleeping 10 secs..."
      sleep 10
  }
  raise "Device config is not present!" unless result

  LOGGER.info "    * Disabling screen lock"
  system "rake emulator:deactivate_screen_lock"
  sleep 5
  
  LOGGER.info "    * Installing packages ..."
  system("rake install")
  sleep 5
  
  sleep 15
  LOGGER.info "    * Shutting down all emulators"
  system("rake emulator:kill_all")
  sleep 5
  
  LOGGER.info "    * Taking snapshot"
  system("rake emulator:snapshot:take")
  sleep 5
  
  verifying_success = verify_emulator  
  if trial == 0 and !verifying_success
    LOGGER.error "Cannot find device config on prepared emulator - preparing a new one."
    prepare_emulator trial + 1
  elsif trial > 2 and !verifying_success
    LOGGER.error "Cannot find device config on prepared emulator - tried hard for #{trial} times."
    raise "Cannot find device config on prepared emulator - tried hard for #{trial} times."
  end

end

def verify_emulator
  LOGGER.info " * Verifying emulator - checking if device config is present"

  LOGGER.info "    * Shutting down all emulators"
  system("rake emulator:kill_all")
  sleep 5
  
  LOGGER.info "    * Booting emulator... (#{$boot_sleep_time} secs startup...)"
  system("rake emulator:boot")
  sleep $boot_sleep_time
  LOGGER.info "     ... done"
  
  LOGGER.info "    * Restarting adb server"
  system("adb kill-server")
  sleep 5
  system("adb start-server")
  sleep 15
  
  result = false
  20.times {  
      LOGGER.info "    * Verifying device_config.json is stored persistently on sdcard"
      result = system("rake device_config:verify")  
      if result 
        break
      elsif
          LOGGER.info "device_config.json is not present, will try again"
      end
      LOGGER.info "      * sleeping 10 secs..."
      sleep 10
  }
  
  if result
    LOGGER.info " * Verifying emulator - SUCCESS"    
    return true
  end
  
  LOGGER.info " * Verifying emulator - FAILURE"  
  return false
  
end

(__FILE__ == $0) and (main ARGV)
