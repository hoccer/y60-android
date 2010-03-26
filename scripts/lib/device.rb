require 'os-helper'

class Device
  include Comparable
  
  def initialize adb_id
    @id ||= adb_id  
  end
  
  def id
    @id
  end
  
  def wait
    OS::execute "adb -s #{@id} wait-for-device", "wait for device"
  end
  
  def execute
    OS::execute "adb -s #{@id} shell "
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