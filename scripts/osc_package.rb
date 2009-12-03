require 'osc'
require 'socket'
#require 'ccg_logger'

module CanControlsGateway
 
  class OscPackage
 
    ADDR    = ['<broadcast>', OSC_BROADCAST_PORT] # broadcast address
 
    def initialize event, station_id
      @event      = event
      @station_id = station_id
    end
 
    def broadcast
      my_message = prepare_message
      ##
      #CcgLogger::LOGGER.info "#{Time.now.iso8601} | broadcasting osc_package with vector '#{@event.vector}' and event_type '#{@event.event_type}'"
      ##
      my_encoded_message                = my_message.encode
      BasicSocket.do_not_reverse_lookup = true
      my_socket                         = UDPSocket.new
   
      my_socket.setsockopt(Socket::SOL_SOCKET, Socket::SO_BROADCAST, true)
      my_socket.send(my_encoded_message, 0, ADDR[0], ADDR[1])
      my_socket.close
   
      my_encoded_message.to_s
    end
 
    private
   
      def prepare_message
        if @event.vector.nil?
          dimensionality = 0
        else
          dimensionality = @event.vector.dimensions
        end

        if dimensionality == 0
          my_message = OSC::Message.new "/cursor/#{@station_id}/1/#{dimensionality}",
                                        's', @event.event_type
        elsif dimensionality == 2
          my_message = OSC::Message.new "/cursor/#{@station_id}/1/#{dimensionality}",
                                        'sff', @event.event_type, @event.vector.x, @event.vector.y
        else
          my_message = OSC::Message.new "/cursor/#{@station_id}/1/#{dimensionality}",
                                        's', @event.event_type
        end
        my_message
      end
 
  end

end


