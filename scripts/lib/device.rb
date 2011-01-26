require 'os-helper'
require 'tempfile'
require 'open3'

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
    stdin, stdout, stderr = Open3.popen3("adb -s #{@id} shell am start -a android.intent.action.MAIN -c android.intent.category.HOME -c android.intent.category.MONKEY")
    stderr = stderr.read
    stdout = stdout.read
    raise stderr if stderr != ''
    
    if stdout.include? "unable to resolve Intent"
      return false
    else
      start "tgallery.intent.HOME_SCREEN"
      return true
    end
  end
  
  def setHomescreenEnabled enabledFlag
    myEnabledLookup = { true => 'enable',
                        false => 'disable'}
    execute "pm #{myEnabledLookup[enabledFlag]} com.android.launcher"
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