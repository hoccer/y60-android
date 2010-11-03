require 'rake'
require 'tempfile'
require 'fileutils'

@y60_path = File.dirname(__FILE__)
@scripts_path = "#{@y60_path}/scripts"
@config_path = "#{@y60_path}/config"
@config_file = "#{@config_path}/app_settings.yml"


@config = nil # Not yet initialized

Dir["#{@scripts_path}/tasks/*.rake"].sort.each { |ext| load ext }

task :default => ["config:load", "config:dump"] do 
  puts "nothing to do... list tasks via 'rake -T'"
end