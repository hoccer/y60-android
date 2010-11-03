require 'yaml'

namespace :emulator do
  
  desc "starts the default emulator or a specific one"
  task :boot do
    if avd_name
      fail unless boot_emulator(avd_name)
    else
      puts "FAILURE: Cannot determine emulator avd_name to boot!"
      fail
    end
  end
  
  desc "Displays log tail"
  task :logcat do
    if ENV['device_id']
      cmd = "adb -s #{ENV['device_id']} logcat -v time"
    else
      cmd = "adb logcat -v time"
    end
    fail unless system cmd
  end
  
  namespace :y60 do
  
    desc "Initializes Y60 on a running emulator (this will take some time although this command exits immediately)"
    task :start do
      if ENV['device_id']
        cmd = "adb -s #{ENV['device_id']} shell am broadcast -a tgallery.intent.INIT_SERVICE_STARTER_BC"
      else
        cmd = "adb shell am broadcast -a tgallery.intent.INIT_SERVICE_STARTER_BC"
      end
      puts " * Initializing Y60 via command '#{cmd}'"
      fail unless system cmd
    end
    
    desc "Stops Y60"
    task :stop do
      if ENV['device_id']
        cmd = "adb -s #{ENV['device_id']} shell am broadcast -a tgallery.intent.INIT_SERVICE_STARTER_BC"
      else
        cmd = "adb shell am broadcast -a tgallery.intent.INIT_SERVICE_STARTER_BC_KILL"
      end
      puts " * Initializing Y60 via command '#{cmd}'"
      fail unless system cmd
    end
    
  end
  
end

def avd_name
  config = YAML::load(File.open("#{@config_path}/app_settings.yml"))
  if config["avd_name"]
    puts "Determined avd_name '#{config["avd_name"]}' determined from app_settings.yml"
    return config['avd_name']
  end
  puts "Could not determine avd_name - check the file #{@config_path}/app_settings.yml"
  nil
end

def boot_emulator avd_name
  cmd = "emulator -avd #{avd_name} -no-boot-anim &"
  puts " * Starting emulator via command:\n\t#{cmd}"
  system cmd
end