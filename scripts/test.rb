#!/usr/bin/env ruby

require "yaml"

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'project'
require 'java_project'
require 'android_project'

def main pj_names

  last_project_sorting_file = "/tmp/project_sorting.yaml"
  if File.exists? last_project_sorting_file then
    last_project_sorting = YAML::load_file(last_project_sorting_file);
  else
    last_project_sorting = []
  end
  
  project_paths = Dir["#{Dir.getwd}/*/.project"]
  puts "project paths #{project_paths}"
  projects = []
  
  
  last_project_sorting.each { |name|
    
    index = project_paths.index{|p| p.include? name}
    puts "#################### #{name} #{index} ----"
    if index != nil then
      puts "adding #{name} to projects (was at index #{index})"
      projects.push(Project.find_or_create name, Dir.getwd)
      project_paths.delete_at index
    else
      puts "#{name} is not included in list of project_path's"
    end
  }
  
  
  project_paths.each { |dir|
    name = File.basename(File.dirname dir)
    projects.push(Project.find_or_create name, Dir.getwd)
  }

  puts "testing #{projects.map {|p| p.name}.join(' ')}"
  
  
  failing_projects = []
  tests_run = tests_failed = tests_with_exception = broken_instrumentations = 0

 
  projects = projects.select { |p| p.respond_to? :test }

  success = projects.inject do |yet, project|
    starttime = Time.new

    puts "#{Time.now.to_s}--- running test for project #{project.name}"
    test_result = project.test
    
    if !test_result[:was_succsessful] then 
      failing_projects.push project.name
    end
    
    if test_result.has_key? :tests_run then 
      tests_run += test_result[:tests_run]
    end
    
    if test_result.has_key? :broken_instrumentation then 
      broken_instrumentations += 1
    end
    
    if test_result.has_key? :tests_failed then 
      tests_failed += test_result[:tests_failed]
    end
    
    if test_result.has_key? :tests_with_exception then 
      tests_with_exception += test_result[:tests_with_exception]
    end
    
    elapsedSeconds = Time.new - starttime
    puts "test duration: #{elapsedSeconds} seconds"
    test_result[:was_succsessful] and yet    
  end
  
  
  
  puts "
  tests run: #{tests_run},
  tests failed: #{tests_failed}, 
  tests with exception: #{tests_with_exception}, 
  broken instrumentations: #{broken_instrumentations}
  "
  
  if success
    puts "all tests run succsesfull"
    exit 0
  else
    puts "There where test failures in #{failing_projects.join ', '}"
    last_project_sorting = failing_projects + (last_project_sorting - failing_projects)
    
    File.open(last_project_sorting_file, 'w') {|f| f.write(last_project_sorting.to_yaml) }
    exit 1
  end
  
rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main ARGV) #end.
