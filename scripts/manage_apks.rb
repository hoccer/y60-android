#!/usr/bin/env ruby
require "rubygems"
require "rexml/document"

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'project'
require 'java_project'
require 'android_project'

Usage = <<-EOT

This script helps managing apks on an android device
usage: $ #{__FILE__} <action>

  actions:
     install     deploys apks; existing ones will be kept
     reinstall   replaces existing apks (to be done)
     uninstall   removes installed apks
EOT

def main action, device_id, pj_names
  
  projects = Project.load_in_dependency_order(pj_names)
  
  projects = projects.select { |p| p.respond_to? :reinstall }

  puts "#{action}ing #{projects.map {|p| p.name}.join(' ')}"
  
  # uninstall requires reverse projects order
  projects = projects.reverse if action == "uninstall"
  projects.each do |p|
    p.send action, device_id
  end
  
  # clean the mess up
  system "adb shell rm /data/local/*apk"
  
rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  puts Usage
  exit 1
end

(__FILE__ == $0) and (main ARGV[0], ARGV[1], nil)
