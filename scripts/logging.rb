
require 'rubygems'
require 'activesupport'

require 'logger'
require 'singleton'

class Logger
  include Singleton
  
  class Formatter
    #original: Format = "%s, [%s#%d] %5s -- %s: %s\n"
    #Change the name of constant to avoid redefining the super which bugs us a bit. So let it be Formato then.
    Formato = "[%s] [%5s] : %s\n" 
    
    #keep the original signature but alter implementation to change formatting
    def call(severity, time, progname, msg)
      #add logging to stdout
      output_message = msg2str(msg)
      #STDOUT.flush
      #original: Format % [severity[0..0], format_datetime(time), $$, severity, progname, msg2str(msg)]
      Formato % [format_datetime(time),severity, output_message]
    end
  end

  @@old_initialize = Logger.instance_method :initialize

  def initialize
      @@old_initialize.bind(self).call(STDOUT)
  end  
end
LOGGER = Logger.instance
LOGGER.formatter = Logger::Formatter.new
LOGGER.datetime_format = "%H:%M:%S"