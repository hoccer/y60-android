#!/usr/bin/env ruby

require 'rubygems'
require 'hpricot'
require 'open-uri'

$socket_number = 1

def fetch_build_state_from uri
  doc = Hpricot(open(uri))
  status_img = doc.at("//img[@src='buildStatus']")
  
  tg_buid_state = status_img.attributes['alt']
end

def fetch_build_states

  tg_buid_state = fetch_build_state_from "http://tg-svn.t-gallery.act/job/T-Gallery%20Android%20Projects/lastBuild/"

  y60_buid_state = fetch_build_state_from "http://tg-svn.t-gallery.act/job/Y60%20Android%20Projects/lastBuild/"

  [tg_build_state, y60_build_state]
end

def build_in_progress?
  states = fetch_build_states
  states[0] == "In progress" || states[0] == "In progress"
end

def build_succsessful?
  states = fetch_build_states
  states[0] == "Success" && states[0] == "Success"
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
