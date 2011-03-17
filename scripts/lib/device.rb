require 'os-helper'
require 'tempfile'
require 'open3'
require 'json'

class Device
  include Comparable
  
  def initialize adb_id
    @id ||= adb_id
    @owner = "not_set"
    @name = "not_found"
    @odp_enabled = "not_found" 
    @is_online_device = true
  end
  attr_accessor :owner, :name, :odp_enabled, :id, :is_online_device
  
  def wait
    OS::execute "adb -s #{@id} wait-for-device", "wait for device", 20
  end
  
  def execute cmd
    OS::execute "adb -s #{@id} shell #{cmd}", "executing on device"
  end

  def adb params
    OS::execute "adb -s #{@id} #{params}", "adb #{params}"
  end

  def start activity
    execute "am start -a #{activity}"
  end

  def broadcast bc_name
    execute "am broadcast -a #{bc_name}"
  end

  def screenshot path
    OS::execute "#{File.dirname(__FILE__)}/../external/screenshot/screenshot2 -s #{@id} #{path}", "taking screenshot" rescue puts "error while takting screenshot"
  end
  
  def isAndroidHomescreenEnabled?
    
    if isAndroidHomescreenRunning?
      return true
    end
    
    stdin, stdout, stderr = OS::executePopen3("adb -s #{@id} shell am start -a android.intent.action.MAIN -c android.intent.category.HOME -c android.intent.category.MONKEY")    
    if stdout.include? "unable to resolve Intent"
      return false    
    else      
      stdin, stdout, stderr =  OS::executePopen3("adb -s #{@id} shell pm list packages")      
      if stdout.include? "com.artcom.tgallery.homescreen"
        start "tgallery.intent.HOME_SCREEN"
      else
        start "y60.intent.SUPER_COW_POWER"
      end       
      return true
    end
    
  end
  
  def isAndroidHomescreenRunning?
    launcher_package = get_device_config['launcher_apk']

    puts "EXECUTING: adb -s #{@id} shell busybox ps aux | grep #{launcher_package}"
    stdin, stdout, stderr = OS::executePopen3("adb -s #{@id} shell busybox ps aux | grep #{launcher_package}")
    if stdout.to_s.include? launcher_package
      return true
    end        
    return false
  end
  
  def setHomescreenEnabled enabledFlag
    launcher_package = get_device_config['launcher_apk']    
    myEnabledLookup = { true => 'enable',
                        false => 'disable'}
    execute "pm #{myEnabledLookup[enabledFlag]} #{launcher_package}"
  end
  
  def get_device_config_launcher_apk 
    launcher_package = get_device_config['launcher_apk']    
    
  end
  
  def get_device_config
    stdin, stdout, stderr = OS::executePopen3("adb -s #{@id} shell cat /sdcard/device_config.json")  
    
    puts "get dev_config: #{stdout}"
    
    if stdout.include? "No such file or directory"
      return nil
    end  
    
    return JSON.parse(stdout)
    
  end
  
  def <=>(other)
    @id <=> other.id
  end
  
  def hash
    @id.hash
  end
  
  def eql?(other)
    hash.eql? other.hash
  end
  
  def to_s
    "#{@id}"
  end
end