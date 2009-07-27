#!/usr/bin/env ruby

require 'rubygems'
require 'hpricot'
require 'open-uri'

$socket_number = 1


def fetch_build_state
  uri = "http://tg-svn.t-gallery.act/job/T-Gallery%20Android%20Projects/lastBuild/"

  doc = Hpricot(open(uri))
  status_img = doc.at("//img[@src='buildStatus']")
  
  status_img.attributes['alt']
end

def build_in_progress?
  fetch_build_state == "In progress"    
end

def build_succsessful?
  fetch_build_state == "Success"
end

def build_not_succsessful?
  ! build_succsessful?
end

def fountain_on?
  system "sudo sispmctl -g #{$socket_number} | grep on > /dev/null"
end

def fountain_off?
  ! fountain_on?
end

def switch new_state
end

def main 

  puts "Make shure you have added this to visudo:

  # enable everyone to use sispmctl
  ALL ALL=NOPASSWD: /usr/bin/sispmctl
  "
  
  sleep_time = 5

  puts "deamon started and will check the build status every #{sleep_time} seconds"
  while true
    
    begin
      sleep sleep_time

      next if build_in_progress? 
    
      if fountain_off? and build_succsessful? then
        puts "starting fountain"
        system "sudo sispmctl -q -o #{$socket_number}"
      elsif fountain_on? and build_not_succsessful? then
        puts "stopping fountain"
        system "sudo sispmctl -q -f #{$socket_number}"
      end
    rescue
      puts "There was an error. Trying again."
      retry
    end
  end

rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main) #end.
