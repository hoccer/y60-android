require 'yaml'
require 'set'

namespace :config do

  desc "Creates app_settings.yml from template in y60-android folder"
   task :copy_from_template, [:force] do |t, args|
     puts "Creating app settings sample from the yml template '#{@config_file_template}':"
     args.with_defaults(:force => false)
     puts " * INFO : Forcing generation of settings sample" if args[:force]
     
     if !args[:force] && File.exists?("#{@config_file}")
       puts " * WARNING::\n    The config sample already exists at '#{@device_config}'\n    - use force parameter to overwrite"
     else
       puts " * Generating config sample at '#{@config_file}':"
       puts "cp #{@config_file_template} #{@config_file}"
       system "cp #{@config_file_template} #{@config_file}"
     end
   end


  desc "Loads the y60 configuration from '#{@config_file}'"
  task :load do
    if @config.nil?
      if File.exists? "#{@config_file}"
        log "Loading config from '#{@config_file}'"
        @config = YAML::load(File.open(@config_file))
      else
        log "Your environment is not configured\n\tyou need to create\n\t'#{@config_file}'.\n\tSee\n\t'#{@config_path}' for a template!"
        exit
      end
    end
  end
  
  desc "Verifies the application configuration"
  task :verify => ['config:load'] do
      valid = true
      required_keys = Set.new(YAML::load(File.open("#{@config_path}/app_settings.yml_template")).keys)
      log " * y60 requires the following keys to be present:\n    #{required_keys.to_a.inspect}"
      missing_keys = required_keys.difference Set.new(@config.keys) 
      missing_keys.each {|key_name|
        log "    * Missing key: '#{key_name}'"
        valid = false
      }
      if valid
        log "  -> The y60 configuration is valid! All OK!"
      else
        log "  -> The y60 configuration is not valid!"
        fail
      end
  end
  
  desc "Dumps the currently present configuration"
  task :dump => ["config:load"] do
    log "Your current application configuration:"
    indent = @config.keys.sort{|a,b| b.size <=> a.size}.first.size
    @config.each_pair { |k,v|
      log "  #{k}#{' ' * (indent - k.size)} : #{v}"
    }
    
  end
  
end