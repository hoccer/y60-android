require 'rubygems'
require 'socket'
require 'osc'
require 'time'

ADDR           = ['<broadcast>', 6567] # broadcast address
STATION_ID     = 1
DIMENSIONALITY = 2
EVENTS   = [ "swipeDown", "swipeUp", "swipeLeft", "swipeRight", "zoomIn", "zoomOut",
             "cancel", "mouseDown", 'bigSwipe', "mouseMove", 'mouseUp', 'otherunknown' ]

UDPSock = UDPSocket.new
UDPSock.setsockopt(Socket::SOL_SOCKET, Socket::SO_BROADCAST, true)

puts "sending osc packages"

while true do
  #case ['cursor', 'event', 'event_and_cursor', 'invalid'].choice
  case ['cursor', 'event', 'event_and_cursor', 'invalid'].choice
    when 'cursor':
      puts 'cursor'
      my_message = OSC::Message.new("/cursor/#{STATION_ID}/1/#{DIMENSIONALITY}",
                                     'ff', 0.3, 0.3).encode
    when 'event':
      puts 'event'
      my_message = OSC::Message.new("/cursor/#{STATION_ID}/1/#{DIMENSIONALITY}",'s', EVENTS.choice).encode
    when 'event_and_cursor':
      puts 'event_and_cursor'
      my_message = OSC::Message.new("/cursor/#{STATION_ID}/1/#{DIMENSIONALITY}",
                                     'ffs', 0.3, 0.3, EVENTS.choice).encode
    when 'invalid';
      puts 'invalid'
      my_message = 'hallo'
  end

  puts my_message.inspect

  UDPSock.send(my_message, 0, ADDR[0], ADDR[1])
  sleep 1.0/2.0 # Hz
end

UDPSock.close

puts "good bye"

