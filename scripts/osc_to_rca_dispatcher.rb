require 'osc'
require 'rca_command'

module CanControlsGateway

  class OscToRcaDispatcher
   
    attr_reader :event, :raw_data, :osc_packet, :osc_message
   
    def initialize data
      @raw_data    = data
      @osc_packet  = OSC::Packet.decode @raw_data rescue @osc_packet = nil
      @osc_message = @osc_packet[0][1] rescue @osc_message = nil
      @event       = extract_event
    end
   
    def dispatch_rca
      return true if @event.nil?
      my_args = { 'action' => @event }
      CcgLogger::LOGGER.info "#{Time.now.iso8601} |  >> OscToRcaDispatcher: sending command : rci_uri: #{STATION['rci_uri']}, target: #{STATION['target']}, args: #{my_args.inspect}"
      my_command = CanControlsGateway::RcaCommand.new STATION['rci_uri'],
                                                      STATION['target'],
                                                      my_args
      CcgLogger::LOGGER.info "#{Time.now.iso8601} #{my_command.send_command}"
    end
   
    private
   
    def extract_event
      return nil if @osc_message.nil?
      if @osc_message[0].kind_of?(OSC::OSCString) #s
        return EVENTS[@osc_message[0].to_s]
      elsif @osc_message[2].kind_of?(OSC::OSCString) # ffs
        return EVENTS[@osc_message[2].to_s]
      else
        return nil # something else, e.g. ff
      end
    end
   
  end
 
end

