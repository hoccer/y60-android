#!/usr/bin/env ruby

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'project'
require 'java_project'
require 'android_project'

def main pj_names
  projects = Project.load_in_dependency_order(pj_names)
  puts "building #{projects.map {|p| p.name}.join(' ')}"
 
#  return
 
  # build all projects
  projects.each do |project| 
    puts "--- doing project #{project.name}"
    project.cleanup
    project.create_build_env
    project.merge_dependencies
    project.build
  end
rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main ARGV) #end.
