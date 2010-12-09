# http://ph7spot.com/articles/system_timer
#begin
#  require 'system_timer'
#  MyTimer = SystemTimer
#rescue LoadError
#  puts "Using green threads for timeout because SystemTimer wasn't installed. For native threads:"
#  puts "sudo gem install SystemTimer"
  require 'timeout'
  MyTimer = Timeout
#end

module OS
  
  def self.execute cmd, err_str, timeout = nil
    putsf "executing '#{cmd}'"
    MyTimer::timeout(timeout) do
      raise "error while #{err_str}" if !system(cmd)
    end
  rescue Timeout::Error
    raise "Timeout while executing cmd: '#{cmd}'"
  end
  
end
