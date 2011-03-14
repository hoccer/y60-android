namespace :device_config do
  
  desc "Generates device_config.json file from settings in config (app_settings.yml)"
  task :generate, [:force] => ['config:verify'] do |t, args|
    puts "Generating device config file at '#{@device_config_file}':"
    args.with_defaults(:force => false)
    puts " * INFO : Forcing generation of device_config" if args[:force]
    if !args[:force] && File.exists?("#{@device_config_file}")
      puts " * WARNING::\n    The device config already exists at '#{@device_config_file}'\n    - use force parameter to overwrite"
      Rake::Task["device_config:dump"].execute
    else
      puts " * Generating device config at '#{@device_config_file}':"
      my_device_config_json = <<-JSON
{
  "gom_url" : "#{@config['gom_url']}",
  "device_path" : "#{@config['device_path']}",
  "launcher_apk" : "#{@config['launcher_apk']}"
}
JSON
      f = File.open @device_config_file, "w"
      f << my_device_config_json
      f.close
      Rake::Task["device_config:dump"].execute
    end
  end
  
  desc "Dumps the current device config to console"
  task :dump do
    if !File.exists?("#{@device_config_file}")
      puts "The device config does not exist at '#{@device_config_file}' - generate it first"
    else
      device_config_file = File.open @device_config_file
      puts "Content of '#{@device_config_file}':"
      puts "------------------------------------"
      device_config_file.each {|line| puts line }
      puts "------------------------------------\n"
    end
  end
  
  desc "Uploads Device configuration into device's sdcard"
  task :upload => ['emulator:is_running'] do
    if !File.exists?("#{@device_config_file}")
      puts "The device config does not exist at '#{@device_config_file}' - generate it first"
    else
      puts " * uploading '#{@device_config_file}' to device's sdcard"
      result = system "adb push #{@device_config_file} /sdcard/device_config.json"
      if result
        puts " * done. The device should now be reinitialized"
      else
        puts " * ERROR occurred :#{$?}"
        fail
      end
    end
  end
  
  desc "Verifies that the device_config is present"
  task :verify => ['config:verify','emulator:is_running'] do
    log "Verifying device_config.json on device"
    my_avd_name = avd_name
    log " * avd-name: #{my_avd_name}"
    
    fh = open "|adb shell ls /sdcard/device_config.json"
    device_config_json = fh.read
    
    if device_config_json.include? "No such file or directory"
      puts "Device config json is not present!"
      fail
    end
    puts "Device config json is present"
  end
  
end