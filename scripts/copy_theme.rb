#!/usr/bin/env ruby
require "rubygems"
require "rexml/document"

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'project'
require 'java_project'
require 'android_project'

def main action, pj_names
  projects = Project.load_in_dependency_order(pj_names)
  projects = projects.select { |p| p.is_t_gallery_project? and File.exist? p.path+"/res" } 
  
  sourceProject = Project.find_or_create "TgCommonLib"

  projects.each do |project| 
     Dir[sourceProject.path+"/theme/*/*"].each do |file| 
       fileParts = file.split("/")

       dir = fileParts[fileParts.size-2] 
       filename = fileParts[fileParts.size-1]
     
       target_path = project.path + "/res/" + dir + "/" + filename
       
       #puts file + " -> " + target_path

       FileUtils.rm(target_path)
       
     end
  end
rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main ARGV[0], nil)