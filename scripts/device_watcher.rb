require 'rubygems'
require 'sinatra'

require 'device_executor'

class DeviceWatcher < DeviceExecutor
  
  
end


def main
  dw = DeviceWatcher.new
  
  get '/' do
    dw.process_devices
    dw.get_connected_devices.map{|d| d.to_s}.join ", "
  end
  
rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main) #end.