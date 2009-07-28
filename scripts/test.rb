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
  
  failing_projects = []
  tests_run = tests_failed = tests_with_exception = broken_instrumentations = 0

 
  projects = projects.select { |p| p.respond_to? :test }
  puts "testing #{projects.map {|p| p.name}.join(' ')}"
  puts "testing #{projects.size}"
  
  success = true

  
  system "adb pull /sdcard/error_log.txt /tmp/error_log.txt"
  if File.exists? "/tmp/error_log.txt" then
    success = false
    puts "\n\nnoticed an error on sdcard:"
    puts File.new("/tmp/error_log.txt").read
    system "adb shell rm /sdcard/error_log.txt"
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
