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
  "gom-url" : "#{@config['gom']}",
  "device-path" : "#{@config['device_path']}",
  "log-level" : "verbose",
  "color-code" : "orange"
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
      system "adb push #{@device_config_file} /sdcard/device_config.json"
      puts " * done. The device should now be reinitialized"
    end
  end
  
end