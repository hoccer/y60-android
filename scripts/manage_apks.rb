#!/usr/bin/env ruby
require "rubygems"
require "rexml/document"

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'logging'

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

def get_git_version
  log = open "|git log --summary HEAD"
  version = log.gets[7..-1][0..6]
  log.close
  LOGGER.debug "got git version '#{version}'"
  return version
end

def remember_version device_flag
  version = get_git_version
  LOGGER.debug "remembering version '#{version}'"
  system %(adb #{device_flag} shell "echo '#{version}' > /sdcard/deployed_version.txt")
end

def main action, device_id, pj_names
  projects = Project.load_in_dependency_order(pj_names)
  projects = projects.select { |p| p.respond_to? :reinstall }

  LOGGER.info "#{action.capitalize}ing #{projects.map {|p| p.name}.join(' ')}"
  
  # uninstall requires reverse projects order
  projects = projects.reverse if action == "uninstall"
  projects.each do |p|
    p.send action, device_id
  end
  
  device = "-s #{device_id}" unless device_id.nil? || device_id.empty?

  # clean the mess up
  system "adb #{device} shell rm /data/local/*apk"

  remember_version device unless action == "uninstall"
rescue => e
  LOGGER.error "oops: #{e}\n#{e.backtrace.join "\n"}"
  LOGGER.error Usage
  exit 1
end

if __FILE__ == $0
  main ARGV[0], ARGV[1], nil
end