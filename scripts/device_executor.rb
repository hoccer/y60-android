#!/usr/bin/env ruby

require 'rubygems'
require 'activesupport'
    
$:.unshift File.join(File.dirname(__FILE__), 'lib')
require 'hash_from_argv'
require 'os-helper'
require 'device'

def putsf s
  puts s
  $stdout.flush
end

class DeviceExecutor
  
  def initialize
    @connected_devices ||= []
  end
  
  def devices
    refresh_device_list
    return @connected_devices
  end  
  
  #like this naming more, dont want to break usage of old function, TODO: clean usage
  def get_refreshed_devices
    refresh_device_list
    return @connected_devices
  end
  
  def execution_loop
    while (true)
      process_devices
      sleep 3
    end
  end
  
  def process_devices
     get_refreshed_devices.each {|device|
        MyTimer.timeout(600) do
          execute_cmds_for device
        end
    }
  end
  
  def execute_cmds_for device
    putsf "executing tasks for #{device}"
    device.wait
  end

  def new_device_connected device
    putsf "new Device connected: #{device}"
  end
  
  def refresh_device_list
    known_device_ids = @connected_devices.map {|d| d.id }
    adb_in = open "|adb devices"
    while (line = adb_in.gets)
      next unless line.strip.end_with? 'device' 
      device_id = extract_device_id line
      if known_device_ids.include? device_id
        known_device_ids.delete device_id
      else
        new_device_connected device_id
        @connected_devices << Device.new(device_id)
      end
    end
    adb_in.close
    
    known_device_ids.each do |id|
      putsf "device #{id} has been disconnected!"
      @connected_devices = @connected_devices.reject {|device| device.id == id}
    end
    
    return @connected_devices
  end
  
  
  def get_offline_devices
    offline_devices = Array.new  
    adb_in = open "|adb devices"
    while (line = adb_in.gets)
      next unless line.strip.end_with? 'offline' 
      device_id = extract_device_id line
      off_device = Device.new(device_id)
      off_device.is_online_device = false
      offline_devices << off_device
    end
    adb_in.close  
    puts "offline devices are: #{offline_devices.inspect}"    
    return offline_devices
  end
  
  private
  
    def extract_device_id line
      line.strip.split[0]
    end
    
    def is_connected? device
      refresh_device_list.include? device
    end
  
end

def main args
  
  params = Hash.from_argv(args)
  args = (params.delete :arguments) || []
   (DeviceExecutor.new).send "execution_loop"
  
rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  puts "type '#{__FILE__} --help' for help"
  exit 1
end

 (__FILE__ == $0) and (main ARGV) #end.
