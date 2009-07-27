#!/usr/bin/env ruby

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'project'
require 'java_project'
require 'android_project'

def main pj_names
  projects = Project.load_in_dependency_order(pj_names)

  # non-y60 runs should only test theireself, not the y60 projects
  wd = Dir.new(Dir.getwd)
  if !wd.include? "Y60Lib"
    puts "non y60 run -- y60 apps will be excluded from testing"
    projects = projects.select { |p| wd.include? p.name }
  end

  puts "testing #{projects.map {|p| p.name}.join(' ')}"
  
  failing_projects = []
  tests_run = tests_failed = tests_with_exception = broken_instrumentations = 0

 
  projects = projects.select { |p| p.respond_to? :test }

    
    success = projects.inject do |yet, project|
    starttime = Time.new

    puts "#{Time.now.to_s}--- running test for project #{project.name}"
    test_result = project.test
    
    if !test_result[:was_succsessful] then failing_projects.push project.name end
    
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
    exit 1
  end
  
rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main ARGV) #end.
