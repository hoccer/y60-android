#require 'rubygems'

$:.unshift File.join(File.dirname(__FILE__), 'lib')
require 'hash_from_argv'

class DeviceExecutor

  
  def execution_loop *args
  puts args
    while (true)
      process_devices
      sleep 1
    end
  end

  def process_devices
    
    puts "processing hard"
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