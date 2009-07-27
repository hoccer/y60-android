$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')

require 'project'
require 'java_project'
require 'android_project'
require 'active_support'


projects = Project.load_in_dependency_order
tg_common_lib = Project.find_or_create "TgCommonLib"
path = tg_common_lib.path

system("adb root")
system("adb shell sync")
system("adb remount")
system("adb shell sync")

system("adb push #{path}/tg-framework-res.apk /system/framework-res.apk")
system("adb shell sync")

