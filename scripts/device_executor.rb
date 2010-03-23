require 'rubygems'
require 'activesupport'

$:.unshift File.join(File.dirname(__FILE__), 'lib')
require 'hash_from_argv'

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
    @in_processing = []
    puts "initing device executor"
  end
  
  def reset_devices
    @in_processing.clear
  end


  def execution_loop *args
  puts args
    while (true)
      process_devices
      sleep 1
    end
  end

  def process_devices
    connected = get_connected_device_ids
    connected.each do |device_id|
      if !@in_processing.member? device_id
        putsf "device #{device_id} connected!"
        connected2 = get_connected_device_ids
        if connected2.member? device_id
          MyTimer.timeout(600.seconds) do
            process_device device_id
          end
        end
      end
    end

    disconnected = @in_processing - connected
    disconnected.each do |device_id|
      putsf "device #{device_id} disconnected!"
      @in_processing.delete(device_id)
    end
  end

  def process_device device_id
    @in_processing << device_id
    putsf "executing tasks for #{device_id}"
    execute "adb -s #{device_id} wait-for-device", "wait for device"
  end

  private

  def get_connected_device_ids
    connected = []
#    putsf "adb portier is in get_connected_device_id's."
    adb_in = open "|adb devices"
    while (line = adb_in.gets)
      connected << extract_device_id(line) if (line.strip.end_with? 'device')
    end
    adb_in.close

    return connected
  end
  
  def extract_device_id line
    line.strip.split[0]
  end

  def execute cmd, err_str
    putsf "executing '#{cmd}'"
    successful = system cmd
    if !successful
      raise "error while "+err_str
    end
  end

  def is_connected? device_id
    get_connected_device_ids.include? device_id
  end



end


def main args

  params = Hash.from_argv(args)
  args = (params.delete :arguments) || []
  (DeviceExecutor.new).send "execution_loop", *(args << params)

rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  puts "type '#{__FILE__} --help' for help"
  exit 1
end

(__FILE__ == $0) and (main ARGV) #end.