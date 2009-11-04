require 'rake'
require 'tempfile'
require 'fileutils'

@y60_path = File.join(File.dirname(__FILE__))

task :build do 
  fail unless system "#{@y60_path}/scripts/build.rb"
end

task :install do 
  fail unless system "#{@y60_path}/scripts/manage_apks.rb install #{ENV['device_id']}"
end

task :uninstall do 
  fail unless system "#{@y60_path}/scripts/manage_apks.rb uninstall #{ENV['device_id']}"
end

task :reinstall do 
  fail unless system "#{@y60_path}/scripts/manage_apks.rb reinstall #{ENV['device_id']}"
end

task :test do 
  fail unless system "#{@y60_path}/scripts/test.rb" 
end


task :removeartcom do
  if ENV.include?('device_id') then
	s = "-s #{ENV['device_id']}"
  end

  list_pkgs_io = open "|adb #{s} shell pm list packages"
  while (line = list_pkgs_io.gets) do
    if (line.include?("com.artcom") && !line.include?("deployment"))
      pkg = line.strip[/:(.*)/,1] # removing "package:" string at beginning
      puts "removing #{pkg} from #{ENV['device_id']}"
      uninst_io = open "|adb #{s} shell pm uninstall #{pkg}"
      while (uninst_line = uninst_io.gets)
        raise "uninstalling of #{pkg} failed!" if uninst_line.include? "Failure"
      end
      uninst_io.close
      sleep 1
    end
  end
  list_pkgs_io.close
end
