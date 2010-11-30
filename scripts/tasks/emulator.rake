namespace :emulator do
  
  desc "Creates the emulatator specified in settings"
  task :create => ['config:verify'] do
    puts "Creating emulator:"
    my_avd_name = avd_name
    myAvdDirectoryPath = File::expand_path(".android/avd/#{my_avd_name}.avd", "~")

    puts " * avd-name: #{my_avd_name}"
    puts " * avd-path: #{myAvdDirectoryPath}"
    
    if File::exists?(myAvdDirectoryPath)
      puts "The emulator '#{my_avd_name}' seems to already exist at '#{myAvdDirectoryPath}'! - ABORTING"
      fail
    end
    
    cmd = "echo no | android create avd --name #{my_avd_name} --target 2 --sdcard 256M"
    fail unless execute_command cmd, "creating emulator"
    puts "    ... done creating emulator."
  end
  
  desc "Deletes the emulator instance as specified in settings"
  task :delete => ['config:verify'] do
    puts "Deleting emulator:"
    if emulators_running? > 0
      puts "There is at least one emulator running - stop them first"
      fail
    end
    my_avd_name = avd_name
    myAvdDirectoryPath = File::expand_path(".android/avd/#{my_avd_name}.avd", "~")
    
    puts " * avd-name: #{my_avd_name}"
    puts " * avd-path: #{myAvdDirectoryPath}"
    
    if !File::exists?(myAvdDirectoryPath)
      puts "The emulator '#{my_avd_name}' does not seem to exist at '#{myAvdDirectoryPath}'! - ABORTING"
      fail
    end
    
    cmd = "android delete avd --name #{my_avd_name}"
    fail unless execute_command cmd, "deleting emulator"
    puts "    ... done deleting emulator."
  end
  
  desc "starts the default emulator or a specific one"
  task :boot => ['config:load'] do
    # TODO make this defensive -> do not start if already started/starting...
    my_avd_name = avd_name
    if my_avd_name
      fail unless boot_emulator(my_avd_name)
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
  
  desc "Determines if an emulator is running"
  task :is_running, [:fail] do
    found_emulators = emulators_running?
    if found_emulators > 1
      puts "FAILURE: Found more than one emulator instances running - don't know what to do!"
      fail
    end
    if found_emulators == 1
      puts "Found one running emulator instance"
    else
      puts "Currently no emulator is running"
    end
  end
  
  desc "Deactivate Screen Lock permanently"
  task :deactivate_screen_lock do
    fail if emulators_running? != 1
    
    SQL_PATCH = 'UPDATE "secure" SET VALUE="0" WHERE NAME="device_provisioned";'
    mySqlPatchFile = Tempfile.new 'disable_screen_lock'
    mySqlPatchFile << SQL_PATCH
    mySqlPatchFile.flush
    
    puts mySqlPatchFile.path
    
    system "adb pull /data/data/com.android.providers.settings/databases/settings.db /tmp/settings.db"
    system "sqlite3 /tmp/settings.db < #{mySqlPatchFile.path}"
    system "adb push /tmp/settings.db /data/data/com.android.providers.settings/databases/settings.db"
    mySqlPatchFile.close
    system "rm /tmp/settings.db"
  end
  
  desc "forwards the host machines DeviceController Port to the Emulator (which needs to run)"
  task :port_forward do
    fail if emulators_running? != 1
    puts " ... you may also need to use rinetd (linux) or ipfw (osx)"
    fail unless system "adb forward tcp:4042 tcp:4042"
  end
  
  desc "Kills all currently running emulator instances"
  task :kill_all do
    system "killall emulator || no emulators running"
  end
  
  namespace :y60 do
  
    desc "Initializes Y60 on a running emulator (this will take some time although this command exits immediately)"
    task :start do
      if ENV['device_id']
        cmd = "adb -s #{ENV['device_id']} shell am broadcast -a tgallery.intent.INIT_SERVICE_STARTER_BC"
      else
        fail if emulators_running? != 1
        cmd = "adb -e shell am broadcast -a tgallery.intent.INIT_SERVICE_STARTER_BC"
      end
      puts " * Initializing Y60 via command '#{cmd}'"
      fail unless system cmd
    end
    
    desc "Stops Y60"
    task :stop do
      if ENV['device_id']
        cmd = "adb -s #{ENV['device_id']} shell am broadcast -a tgallery.intent.INIT_SERVICE_STARTER_BC"
      else
        fail if emulators_running? != 1
        cmd = "adb -e shell am broadcast -a tgallery.intent.INIT_SERVICE_STARTER_BC_KILL"
      end
      puts " * Initializing Y60 via command '#{cmd}'"
      fail unless system cmd
    end
    
  end
  
  namespace :snapshot do
  
    desc "Takes a snapshot of the emulator"
    task :take => ['config:verify'] do
      ensure_emulator_is_stopped
      my_avd_name = avd_name
      myAvdDirectoryPath = File::expand_path(".android/avd/#{my_avd_name}.avd", "~")
      mySnapshotDirectoryPath = "#{@y60_path}/snapshots/#{my_avd_name}"

      if !File.exists?(myAvdDirectoryPath)
        puts " * The provided avd_name: '#{my_avd_name}' seems to be invalid - cannot find images in '#{myAvdDirectoryPath}'  - ABORTING"
        fail
      end
      puts " Source Directory:\n    #{myAvdDirectoryPath}"
      puts " Target Directory:\n    #{mySnapshotDirectoryPath}"
      puts "Taking a snapshot ..."
      `mkdir -p #{mySnapshotDirectoryPath}`
      `rm -f #{mySnapshotDirectoryPath}/*`
      `cp -R #{myAvdDirectoryPath}/* #{mySnapshotDirectoryPath}`
      puts "... done"
    end
    
    desc "Restores a snapshot (if present)"
    task :restore => ['config:verify'] do
      ensure_emulator_is_stopped
      my_avd_name = avd_name
      myAvdDirectoryPath = File::expand_path(".android/avd/#{my_avd_name}.avd", "~")
      mySnapshotDirectoryPath = "#{@y60_path}/snapshots/#{my_avd_name}"
      
      if !File.exists?(mySnapshotDirectoryPath)
        puts " * Cannot find snapshot for avd: '#{my_avd_name}' - ABORTING"
        fail
      end
      if !File.exists?(myAvdDirectoryPath)
        puts " * The provided avd_name: '#{my_avd_name}' seems to be invalid - cannot restore to '#{myAvdDirectoryPath}'  - ABORTING"
        fail
      end
      puts " Source Directory:\n    #{mySnapshotDirectoryPath}"
      puts " Target Directory:\n    #{myAvdDirectoryPath}"
      
      `rm -f #{myAvdDirectoryPath}/*`
      `cp -R #{mySnapshotDirectoryPath}/* #{myAvdDirectoryPath}`
      puts "... done"
    end
  end
  
end

def execute_command cmd, message="command"
  puts " * executing\n   '#{cmd}':"
  puts "------------"
  result = system cmd
  puts "------------"
  if !result
    puts "An error occured while #{msg}:"
    puts $?
  end
  result
end

def avd_name
  if @config["avd_name"]
    puts "Determined avd_name '#{@config["avd_name"]}' from app_settings.yml"
    return @config['avd_name']
  end
  puts "Could not determine avd_name - check the file #{@config_path}/app_settings.yml"
  nil
end

def boot_emulator avd_name
  cmd = "emulator -avd #{avd_name} -no-boot-anim &"
  puts " * Starting emulator via command:\n\t#{cmd}"
  system cmd
end

def ensure_emulator_is_stopped
  raise "Emulator must not be running" if emulators_running? > 0
end

def emulators_running?
  found_emulators = `adb devices | grep emulator | grep device`
  found_emulators.lines.count
end