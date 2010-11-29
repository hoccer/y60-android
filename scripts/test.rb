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
  
  last_project_sorting.each { |name|
    index = project_paths.index{|p| p.include? name}
    if index != nil then
      LOGGER.debug " - Adding #{name} to projects (was at index #{index})"
      projects << Project.find_or_create(name, Dir.getwd)
      project_paths.delete_at index
    end
  }
  
  project_paths.each { |dir|
    name = File.basename(File.dirname dir)
    projects << Project.find_or_create(name, Dir.getwd)
  }
  
  failing_projects = []
  #tests_run = tests_failed = tests_with_exception = broken_instrumentations = 0

  projects = projects.select { |p| p.respond_to? :test }
  LOGGER.info "Testing #{projects.size} projects: #{projects.map {|p| p.name}.join(' ')}"
  
  myTestResultCollector = TestResultCollector.new
  
  projects.each do |project|
    starttime = Time.new
    
    test_result = project.test
    myTestResultCollector << test_result
    #if !test_result[:was_successful] then 
    if !test_result.succeeded? then
      failing_projects.push project.name
    end
    
    #if test_result.has_key? :tests_run then 
    #  tests_run += test_result[:tests_run]
    #end
    #
    #if test_result.has_key? :broken_instrumentation then 
    #  #broken_instrumentations += 1
    #  broken_instrumentations += test_result[:broken_instrumentation]
    #end
    #
    #if test_result.has_key? :tests_failed then 
    #  tests_failed += test_result[:tests_failed]
    #end
    #
    #if test_result.has_key? :tests_with_exception then 
    #  tests_with_exception += test_result[:tests_with_exception]
    #end
    
    elapsedSeconds = Time.new - starttime
    LOGGER.info "Results for project: '#{project}':"
    LOGGER.info "duration: #{elapsedSeconds} seconds"
    LOGGER.info "result: #{TestResult::STATUS_NAMES[test_result.succeeded?.to_s]}"
    LOGGER.info "\n#{test_result}"
    #test_result[:was_successful] and yet
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
 
  #LOGGER.info "
  #tests run: #{tests_run},
  #tests failed: #{tests_failed}, 
  #tests with exception: #{tests_with_exception}, 
  #broken instrumentations: #{broken_instrumentations}
  #"
  
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

(__FILE__ == $0) and (main ARGV)
