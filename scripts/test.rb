#!/usr/bin/env ruby

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'logging'

require "yaml"
require 'project'
require 'java_project'
require 'android_project'
require 'lib/test_result_collector'

def main pj_names
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
  LOGGER.info "Testing #{projects.size} projects: #{projects.map {|p| p.name}.join(' ')}"
  
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
    LOGGER.info "all tests succeeded"
    exit 0
  else
    puts "There were test failures in #{failing_projects.join ', '}"
    last_project_sorting = failing_projects + (last_project_sorting - failing_projects)
    
    File.open(last_project_sorting_file, 'w') {|f| f.write(last_project_sorting.to_yaml) }
    exit 1
  end
  
rescue => e
  LOGGER.error "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

def prepare_emulator
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
  
  LOGGER.info "    * Booting emulator... (120 secs startup...)"
  system("rake emulator:boot")
  sleep 120
  LOGGER.info "     ... done"
  
  LOGGER.info "    * Restarting adb server"
  system("adb kill-server")
  sleep 5
  system("adb start-server")
  sleep 15
  
  LOGGER.info "    * Creating and uploading device_config.json onto sdcard"
  system "rake device_config:generate[true]"
  system "rake device_config:upload"
  system "rake device_config:verify --silent"
  sleep 5
  
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
end

(__FILE__ == $0) and (main ARGV)