require 'yaml'
require 'set'

namespace :config do

  desc "Loads the y60 configuration from '#{@config_file}'"
  task :load do
    if @config.nil?
      if File.exists? "#{@config_file}"
        puts "Loading config from '#{@config_file}'"
        @config = YAML::load(File.open(@config_file))
      else
        puts "Your environment is not configured\n\tyou need to create\n\t'#{@config_file}'.\n\tSee\n\t'#{@config_path}' for a template!"
        exit
      end
    end
  end
  
  desc "Verifies the application configuration"
  task :verify => ['config:load'] do
      valid = true
      required_keys = Set.new(YAML::load(File.open("#{@config_path}/app_settings.yml_template")).keys)
      puts " * y60 requires the following keys to be present:\n    #{required_keys.to_a.inspect}"
      missing_keys = required_keys.difference Set.new(@config.keys) 
      missing_keys.each {|key_name|
        puts "    * Missing key: '#{key_name}'"
        valid = false
      }
      if valid
        puts "  -> The y60 configuration is valid! All OK!"
      else
        puts "  -> The y60 configuration is not valid!"
        fail
      end
  end
  
  desc "Dumps the currently present configuration"
  task :dump => ["config:load"] do
    puts "Your current application configuration:"
    indent = @config.keys.sort{|a,b| b.size <=> a.size}.first.size
    @config.each_pair { |k,v|
      puts "  #{k}#{' ' * (indent - k.size)} : #{v}"
    }
    
  end
  
end