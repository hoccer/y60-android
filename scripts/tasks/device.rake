require 'open3'

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

desc "Removes all packages in namespaces #{@package_namespaces.inspect}"
task :removeartcom do
  puts "Preparing removing all packages from the following namespaces: #{@package_namespaces.inspect}"
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
  puts "removing the following packages: #{list.map {|line| line.strip[/:(.*)/,1]}.inspect}"
  list.each { |line|
      pkg = line.strip[/:(.*)/,1] # cutting "package:" string at beginning
      remove pkg
  }
end

def get_packages namespaces=[]
  stdin, pkgs_io, stderr = Open3::popen3 "adb #{@device_flag} shell pm list packages"
  stderr = stderr.read
  if stderr != ""
    raise <<-ERROR
  Error occured:
    #{stderr.split("\n").join("\n    ")}
  --aborting--
ERROR
  end
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