require 'rubygems'
require 'socket'
require 'osc'
require 'time'

ADDR           = ['127.0.0.1', 6567] # broadcast address
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
      my_message = OSC::Message.new("/a/b/c/das ist eine message von cursor",
                                     'ff', 0.3, 0.3,"ff hello world, cursor").encode
    when 'event':
      puts 'event'
      my_message = OSC::Message.new("/curxxsor/#{STATION_ID}/0/#{DIMENSIONALITY}",'s', "s event").encode
    when 'event_and_cursor':
      puts 'event_and_cursor'
      my_message = OSC::Message.new("/curxxsor/#{STATION_ID}/0/#{DIMENSIONALITY}",
                                     'ffs', 0.3, 0.3, "ffs event and cursor").encode
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

