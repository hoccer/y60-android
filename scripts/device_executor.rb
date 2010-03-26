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

# http://ph7spot.com/articles/system_timer
begin
  require 'system_timer'
  MyTimer = SystemTimer
rescue LoadError
  puts "Using green threads for timeout because SystemTimer wasn't installed. For native threads:"
  puts "sudo gem install SystemTimer"
  require 'timeout'
  MyTimer = Timeout
end

class DeviceExecutor
  
  def initialize
    @in_processing ||= []
    @connected_devices ||= []
  end
  
  def devices
    refresh_device_list
    @connected_devices
  end
  
  def execution_loop
    while (true)
      process_devices
      sleep 3
    end
  end
  
  def process_devices
    connected = refresh_device_list
    connected.each do |device|
      unless @in_processing.include? device
        putsf "device #{device} connected!"
        connected2 = refresh_device_list
        if connected2.include? device
          MyTimer.timeout(600.seconds) do
            execute_cmds_for device
          end
        end
      end
    end
    
    disconnected = @in_processing - connected
    
  end
  
  def execute_cmds_for device
    @in_processing << device
    putsf "executing tasks for #{device}"
    device.wait
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