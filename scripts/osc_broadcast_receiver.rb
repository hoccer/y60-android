require 'rubygems'
require 'socket'
require 'osc'
require 'time'

ADDR = ['127.0.0.1', 6567]  # host, port
BasicSocket.do_not_reverse_lookup = true

UDPSock = UDPSocket.new
UDPSock.bind(ADDR[0], ADDR[1])

lastUpdate  = 0
numPackages = 0
frequency   = 0

while true do
  currentUpdate = Time.now
  my_data, my_addr = UDPSock.recvfrom(32768) # if this number is too low it will drop the larger packets and never give them to you
 
  decoded = OSC::Packet.decode my_data rescue decoded = nil
 
  if decoded.nil?
    my_event = nil
  else
    my_msg = decoded[0][1]
    if my_msg[0].kind_of?(OSC::OSCString) #s
      my_event = my_msg[0]
    elsif my_msg[2].kind_of?(OSC::OSCString) # ffs
      my_event = my_msg[2]
    else
      my_event = nil # something else, e.g. ff
    end
  end
  puts "#{Time.now.iso8601} | From addr: '%s', msg: '%s'" % [my_addr.inspect, my_event]
  puts ""
  frequency   = 1 / (currentUpdate - lastUpdate) rescue nil
  lastUpdate  = currentUpdate
  numPackages = 0
  #puts "#{frequency} Hz"
end
UDPSock.close

