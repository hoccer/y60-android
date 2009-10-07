#!/usr/bin/env ruby

require 'rubygems'
require 'hpricot'
require 'open-uri'

$socket_number = 1

def fetch_build_state_from uri
  doc = Hpricot(open(uri))
  title = doc.at("//entry/title")
  title.to_s.gsub /.*\((.*)\).*/, '\1'
end

def fetch_build_states

  tg_build_state = fetch_build_state_from "http://tg-svn.t-gallery.act:8080/job/T-Gallery%20Android%20Projects/rssAll"
  y60_build_state = fetch_build_state_from "http://tg-svn.t-gallery.act/job/Y60%20Android%20Projects/rssAll/"

  puts y60_build_state
  [tg_build_state, y60_build_state]
end

def build_succsessful?
  states = fetch_build_states
  states[0] == "SUCCESS" && states[1] == "SUCCESS"
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
  
  sleep_time = 10

  puts "deamon started and will check the build status every #{sleep_time} seconds"
  while true
    
    begin
      sleep sleep_time

      if fountain_off? and build_succsessful? then
        puts "starting fountain"
        system "sudo sispmctl -q -o #{$socket_number}"
      elsif fountain_on? and build_not_succsessful? then
        puts "stopping fountain"
        system "sudo sispmctl -q -f #{$socket_number}"
      end
    rescue => e
      puts "There was an error:\n#{e.backtrace.join "\n"}\nTrying again."
      retry
    end
  end

rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main) #end.
