#!/usr/bin/env ruby

require 'rubygems'
require 'fileutils'
require 'ftools'
require 'rexml/document'
require 'erb'


def main arguments

#system "rm -rf t-gallery"
#system "rm -rf y60-android"

system "cd t-gallery && ./scripts/build.rb"

rescue => e
  puts "oops: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

(__FILE__ == $0) and (main ARGV) #end.
