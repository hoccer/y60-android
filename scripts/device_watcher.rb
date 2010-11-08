#!/usr/bin/env ruby

require 'rubygems'
require 'sinatra'
require 'erb'

require 'device_executor'

class DeviceWatcher < DeviceExecutor
end

def main
  
  dw = DeviceWatcher.new
  get '/' do
  
  @dw = dw 
    erb :device_watcher
  end
  
rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main) #end.