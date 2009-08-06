#!/usr/bin/env ruby
require 'tempfile'
require 'fileutils'

require 'rubygems'
require 'activesupport'

class VersionController

  attr_reader :hashtag, :name

  def initialize name, project_dir
    @name = name
    @project_dir = project_dir
    @@base_path = File.dirname(__FILE__) + "/../.."

    @hashtag = nil

    @tagname = "stable_staging"
  end

  def update_version
    puts "update version for #{name}"
    is_new = false

    FileUtils.cd(@@base_path + @project_dir) do
      if new_version? then
        fetch_new_version
        is_new = true
      end
    end

    is_new
  end

  def new_version?
    `git fetch --tags`
    tags = `git tag`
    puts "found tags for #{name}: #{tags.split.join(', ')}"
    is_new = false

    if tags.include?(@tagname) then
      new_hashtag = `git rev-parse #{@tagname}`.strip

      is_new = !(new_hashtag == @hashtag)
      @hashtag = new_hashtag
    end

    is_new
  end

  def fetch_new_version
    puts "fetching new version for #{name}"
    `git checkout master`
    `git pull`
    `git checkout #{@tagname}`
  end

  def self.build_version
    puts "Start Eclipse and recompile the mango stuff. Then hit <ENTER>"
    gets
    #FileUtils.cd(@@base_path + "/artcom-tg-android") do
    #    `rake build`
    #end
  end
end


class AdbPortier

  attr_reader :mode

  def initialize switch
    @in_processing = []
    @repositories  = []
    puts "adb portier switch: '#{switch}'"
    if switch.nil?
      puts "adb portier is running in full mode"
      @repositories << VersionController.new("Y60", "/y60-android")
      @repositories << VersionController.new("T-Gallery", "/artcom-tg-android")
      @mode = :full
    elsif switch == "-local"
      puts "adb portier is running in local mode"
      @mode = :local
    else
      raise "Unrecognized adb portier switch: #{switch}"
    end
  end

  def portier_loop
    while (true)
      if @mode != :local
        process_versions
      end
      process_devices
      sleep 1 # second
    end
  end

  def process_versions
    is_new = @repositories.inject(false) { |yet,repo| repo.update_version or yet }

    if is_new then
      puts "building new version"
      VersionController.build_version
      reset_devices
      puts "ready building"
    end
  end

  def process_devices
    connected = get_connected_device_ids
    connected.each do |device_id|
      if !@in_processing.member? device_id
        puts "device #{device_id} connected!"
        process_device device_id
      end
    end

    disconnected = @in_processing - connected
    disconnected.each do |device_id|
      puts "device #{device_id} disconnected!"
      @in_processing.delete(device_id)
    end
  end

  def reset_devices
    @in_processing.clear
  end

  private
  def process_device device_id
    #processor = Thread.new do
    begin
      device_id_flag = "device_id=#{device_id}"
      puts "processing device #{device_id}"
      @in_processing << device_id

      deployment_apk = File.dirname(__FILE__) + "/../TgDeployment/bin/TgDeployment.apk"
      execute "adb -s #{device_id} uninstall com.artcom.tgallery.deployment", "uninstalling wait application"
      execute "adb -s #{device_id} install #{deployment_apk}", "installing wait application"

      execute "adb -s #{device_id} shell am start -a tgallery.intent.SHOW_DEPLOYMENT", "showing wait message on device"

      puts "in directory #{FileUtils.pwd}"
      
      # removed "still connected?" checks, since we don't have a separate thread which checks if
      # the devices are still connected. use method 'is_connected?' if this behavior is usable again
      
      puts "removing all ART+COM stuff from #{device_id}" # -------------------------------------------------------
      execute "rake removeartcom #{device_id_flag}", "removing apps from device #{device_id}"

      puts "done removing ART+COM stuff from #{device_id}, installing everything from scratch"
      execute "rake install #{device_id_flag}", "installing apps to device #{device_id}"

      puts "done installing to #{device_id}, now preparing data on the SDCARD" # ----------------------------------
      
      if device_config_exists_on_device?
        puts "configuration file already exists on device, not pushing the one from the repository"
      else
        puts "pushing device config to SDCARD"
        default_config_path = File.join(File.dirname(__FILE__), 'device_config.json.EXAMPLE')
        execute "adb -s #{device_id} push #{default_config_path} /sdcard/device_config.json", "pushing device config to SDCARD"
      end
      
      @repositories.each do |repo|
        repo_path = "/sdcard/#{repo.name}"
        execute "adb -s #{device_id} shell rmdir #{repo_path}/*", "removing old directory on sd card for #{repo.name}"
        execute "adb -s #{device_id} shell mkdir #{repo_path}", "creating directory"
        execute "adb -s #{device_id} shell mkdir #{repo_path}/#{repo.hashtag}", "creating version directory for #{repo.name}"
      end

      puts "done preparing SDCARD, now starting the Y60 activity" 
      execute "adb -s #{device_id} shell am start -a y60.intent.SHOW_Y60", "starting the Y60 activity"
      puts "device #{device_id} is finished"
    rescue => e
      puts "device #{device_id} aborted with error: #{e}\n#{e.backtrace.join "\n"}"
    end
    # end

  end

  def extract_device_id line
    line.strip.split[0]
  end
  
  def device_config_exists_on_device?
    !`adb shell 'ls /sdcard/device_config.json'`.include? "No such file"
  end

  def get_connected_device_ids
    connected = []

    adb_in = open "|adb devices"
    while (line = adb_in.gets)
      connected << extract_device_id(line) if (line.strip.end_with? 'device')
    end
    adb_in.close

    return connected
  end

  def execute cmd, err_str
    puts "executing '#{cmd}'"
    successful = system cmd
    if !successful
      raise "error while "+err_str
    end
  end

  def is_connected? device_id
    get_connected_device_ids.include? device_id
  end

end

def main switch_arg
  puts "adb portier is starting up"
  portier = AdbPortier.new switch_arg
  portier.portier_loop
rescue => e
  puts "error: #{e}\n#{e.backtrace.join "\n"}"
  exit 1
end

puts "adb portier command line arguments: #{ARGV.inspect}"
(__FILE__ == $0) and main ARGV[0]
