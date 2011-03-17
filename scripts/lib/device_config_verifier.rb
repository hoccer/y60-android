#require 'os-helper'
require 'tempfile'
require 'open3'
require 'json'

class DeviceConfigVerifier
  
  def initialize
    
    @device_config_sample = nil
    stdin, stdout, stderr = OS::executePopen3("rake config:copy_from_template[true]") 
    stdin, stdout, stderr = OS::executePopen3("rake device_config:generate[true]") 
    dc_start = stdout.index('--START')
    dc_end = stdout.index('END--') - dc_start
    if dc_start && dc_end
      @device_config_sample = JSON.parse(stdout.slice(dc_start + 8, dc_end - 9))
    end
  
  rescue => e
    @device_config_sample = nil 
    puts "error : #{e}"
    puts "#{stdin}"         
    puts "#{stdout}"     
    puts "#{stderr}"
      
  end
 
  attr_reader :device_config_sample
  
  def is_device_config_valid device  
       
    if @device_config_sample == nil
      return nil
    end

    device_config = device.get_device_config    
    if device_config == nil
      return false
    end
    
    device_config.values.each do |value|
      if value.eql? ""
        return false  
      end
    end
    
    return device_config.keys.eql? @device_config_sample.keys
  
  end


  

end