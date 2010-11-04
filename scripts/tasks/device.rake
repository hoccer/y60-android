
#namespace :device do

desc "Installs the built project on either the only connected device or the one specified in ENV via device_id"
task :install do 
  fail unless system "#{@y60_scripts_path}/manage_apks.rb install #{ENV['device_id']}"
end

desc "Removes all packages specified by this project from the device (can be specified in ENV via 'device_id')"
task :uninstall do 
  fail unless system "#{@y60_scripts_path}/manage_apks.rb uninstall #{ENV['device_id']}"
end

desc "Reinstalls all packages specified by this project from the device (can be specified in ENV via 'device_id')"
task :reinstall do 
  fail unless system "#{@y60_scripts_path}/manage_apks.rb reinstall #{ENV['device_id']}"
end

desc "execute all tests"
task :test do 
  fail unless system "#{@y60_scripts_path}/test.rb" 
end

#desc "Removes all packages in namespace com.artcom"
#task :removeartcom do
#  if ENV.include?('device_id') then
#  s = "-s #{ENV['device_id']}"
#  end
#
#  list_pkgs_io = open "|adb #{s} shell pm list packages"
#  while (line = list_pkgs_io.gets) do
#    if (line.include?("com.artcom") && !line.include?("deployment"))
#      pkg = line.strip[/:(.*)/,1] # removing "package:" string at beginning
#      puts "removing #{pkg} from #{ENV['device_id']}"
#      uninst_io = open "|adb #{s} shell pm uninstall #{pkg}"
#      while (uninst_line = uninst_io.gets)
#        raise "uninstalling of #{pkg} failed!" if uninst_line.include? "Failure"
#      end
#      uninst_io.close
#      sleep 1
#    end
#  end
#  list_pkgs_io.close
#end

desc "Removes all packages in namespaces #{@package_namespaces.inspect}"
task :removeartcom do
  remove_packages(get_packages(@package_namespaces))
end

def remove pkg
  puts "removing #{pkg} from #{ENV['device_id']}"
  uninst_io = open "|adb #{@device_flag} shell pm uninstall #{pkg}"
  while (uninst_line = uninst_io.gets)
    raise "uninstalling of #{pkg} failed!" if uninst_line.include? "Failure"
  end
  uninst_io.close
end

def remove_packages list
  puts "removing the following packages: #{list.inspect}"
  list.each {|line|
      pkg = line.strip[/:(.*)/,1] # cutting "package:" string at beginning
      remove pkg
  }
end

def get_packages namespaces=[]
  pkgs_io = open "|adb #{@device_flag} shell pm list packages"
  list = pkgs_io.to_a
  pkgs_io.close
  
  list.select { |line|
    select_package = false
    namespaces.each { |namespace|
      if line.include? namespace
        select_package = true
      end  
    }
    select_package
  }
end



#end