require 'yaml'

namespace :config do

  desc "Loads the configuration from '#{@config_file}'"
  task :load do
    if @config.nil?
      if File.exists? "#{@config_file}"
        puts "Loading config from #{@config_file}"
        @config = YAML::load(File.open(@config_file))
      else
        puts "Your environment is not configured\n\tyou need to create\n\t'#{@config_file}'.\n\tSee\n\t'#{@config_path}' for a template!"
        exit
      end
    end
  end
  
  desc "Dumps the currently present configuration"
  task :dump => ["config:load"] do
    puts "Your current configuration:\n#{@config.inspect}\n"
  end
  
  #desc "Generates device_config.json file from settings in config (app_settings.yml)"
  #task :generate_device_config do
  #  
  #end
  
  #desc "uploads config into devices sdcard"
  #task :upload_device_config do
  #  #
  #end
  
end