require 'rake'
require 'tempfile'
require 'fileutils'

@y60_path = File.dirname(__FILE__)
@scripts_path = "#{@y60_path}/scripts"
@config_path = "#{@y60_path}/config"

Dir["#{@scripts_path}/tasks/*.rake"].sort.each { |ext| load ext }