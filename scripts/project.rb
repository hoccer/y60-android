#!/usr/bin/env ruby

$:.unshift File.join(File.dirname(__FILE__), '..', 'scripts')
require 'logging'

require 'rubygems'
require 'activesupport'
require 'fileutils'
require 'ftools'
require 'tempfile'
require 'rexml/document'
require 'rexml/xpath'
require 'erb'

class Project
  
  @@project_paths = {}
  @@projects      = {}
  
  attr_reader :dependencies, :name, :path, :parent_dir
  
  def initialize pj_name
    #LOGGER.info "creating #{pj_name}"
    @dependencies = []
    @name = pj_name
    @path = @@project_paths[pj_name]
    
    path_list = @path.split('/')
    path_list.pop
    @parent_dir = path_list.join('/') 
    @parent_dir += "/" if @parent_dir != ""
    #puts "created #{to_s} --- #{name}"
  end
  
  def self.load_in_dependency_order pj_names = nil
    load_all = (pj_names.nil? or pj_names.empty?)
    if load_all
      LOGGER.debug "No projects given - proceeding to load all projects"
      find_project_paths :all
    else
      LOGGER.debug "Loading: #{pj_names.inspect}"
      find_project_paths pj_names
    end
    
    unsorted_pjs = []
    @@project_paths.each_key do |name|
      unsorted_pjs << self.find_or_create(name) if load_all or pj_names.member? name
    end
    
    sorted_pjs = []
    while !unsorted_pjs.empty? 
      project = unsorted_pjs.delete_at 0
      LOGGER.debug " * Loading dependencies for project '#{project}':"
      project.resolve_dependencies
      LOGGER.debug " * Loaded dependencies for #{project.name}"

      unless sorted_pjs.member? project 
        copy_to_front = (project.dependencies - sorted_pjs)
        if !copy_to_front.empty?
          # load dependencies first 
          unsorted_pjs.unshift project
          copy_to_front.each { |p| unsorted_pjs.unshift p }
        else
          sorted_pjs << project
        end
      end
    end
    LOGGER.debug " * Loaded projects in the following order: #{sorted_pjs.map{ |proj| proj.name}.inspect}"
    sorted_pjs
  end
  
  def self.find_or_create name, base_path = nil
    #puts "#{base_path}"
    @@project_paths[name] = "#{base_path}/#{name}" if base_path != nil
    
    pj = @@projects[name]
    if pj.nil?
      pj_xml = REXML::Document.new(File.new(@@project_paths[name] + "/.project"))
      nature = nil
      pj_xml.elements.each("*//nature") do |e| 
        nature = :android if e.text.include? "com.android.ide.eclipse.adt.AndroidNature"
        nature ||= :java if e.text.include? "org.eclipse.jdt.core.javanature" 
      end
      LOGGER.debug " * Loading #{nature} project #{name}"
      #pj = (nature.to_s+"Project").camelize.constantize.new name
      if nature == :android
        pj = AndroidProject.new name
      elsif nature == :java
        pj = JavaProject.new name
      elsif nature.nil? # RubyNature
        pj = Project.new name
      else
        raise "Unknown project-nature '#{nature}'! Cannot create project instance!"
      end
      @@projects[name] = pj
    end
    pj
  end

  def depends_on project
    @dependencies.member? project
  end

  # load's the list of project names this project depends on from the eclipse classpath
  def resolve_dependencies
    classpath = File.new "#{path}/.classpath" rescue puts "no dependencies for this projects"
    cp_xml = REXML::Document.new classpath
    cp_xml.elements.each("*/classpathentry | */*[local-name()='hidden-build-dependency']") do |path_entry| 
      if path_entry.attributes["kind"] == "src" then
        dep_name = path_entry.attributes["path"]
        LOGGER.debug "    * #{dep_name}  #{dep_name[0]}"
        # it's a project only if it's a path starting with a slash
        if dep_name[0] == "/"[0] then
          n = dep_name[1..-1]
          LOGGER.debug name + " depends on " + n
          dep_pj = Project.find_or_create(n)
          @dependencies << dep_pj
        end
      end
    end
  end
  
  # remove all generated files to start a fresh build
  def cleanup
    LOGGER.info "cleaning up #{name}"
    run "cleaning up", <<-EOT
      rm -f #{name}/build.xml && \
      rm -rf #{name}/bin/*
    EOT
  end

  # generates the build.xml and configs for ant
  def create_build_env
    LOGGER.info "creating build environment for #{name}"
    build_template = self.class.name.underscore.split("_")[0] + "_build.xml.erb"
    generate_from_template "build.xml", build_template
    # creating libs dir for ant
    File.makedirs "#{path}/libs"
  end
  
  # copies the content of projects this project depends on in this project's
  # source folder
  def merge_dependencies
    LOGGER.info "merging dependencies into #{name}"
    dependencies.each do |dep_pj|
      if !dep_pj.respond_to? :jar_path
        LOGGER.warn "Cannot depend on project #{dep_pj.name}, because it doesn't have a jar!"
      else
        run "copying #{dep_pj.name}s jar", <<-EOT
          cp #{dep_pj.jar_path} #{path}/libs/
        EOT
      end
    end
  end

  # run ant to build the project
  def build
    LOGGER.info "starting ant build for #{name}"
    run "building", <<-EOT
      cd #{path} && ant debug -q
    EOT
# unnecessary since 1.6:
#    if File.exists?("#{path}/bin/#{name}-debug.apk")
#      run "aligning", <<-EOT
#        cd #{path} && mv bin/#{name}-debug.apk bin/#{name}-unaligned.apk && zipalign 4 bin/#{name}-unaligned.apk bin/#{name}-debug.apk && rm bin/#{name}-unaligned.apk
#      EOT
#    end
    apk_unaligned="#{path}/bin/#{name}-debug-unaligned.apk"
    if File.exists?(apk_unaligned)
      run "removing unaligned SDK 1.6 leftover", <<-EOT
        rm #{apk_unaligned}
      EOT
    end
  end
  
  def to_s
    "project #{name} at #{path}, depending on #{dependencies.map{|p| p.name}.inspect}"
  end
  
  def is_t_gallery_project?
    File.open(File.join(path, "../.git/config")) do |io|
      while (line = io.gets)
        return true if line.include? "ssh://gitosis@tg-scm.t-gallery.act/artcom-tg-android.git"
      end
      io.close
    end
    return false;
  end
  
  protected
  
    def generate_from_template target_file_name, template_file_name = nil
      if template_file_name.nil?
        template_file_name = target_file_name+".erb"
      end
      
      template = ERB.new IO.read(File.join(File.dirname(__FILE__), template_file_name))
      txt = template.result binding
      File.open("#{path}/#{target_file_name}", "w") { |fd| fd.write txt; fd.close }
    end
  
  private
  
    def self.y60_path
      script_path = File.join(File.dirname(__FILE__))
      File.expand_path("#{script_path}/../")
    end
    
    # two ways to call this:
    # - 'find_project_paths :all' finds all project paths
    # - 'find_project_paths <array-of-names>' finds only project paths for the given project names
    def self.find_project_paths all_or_names
      raise "no y60 path defined" unless self.y60_path
      LOGGER.debug " * adding '#{y60_path}' to project search path"
      dirs = Dir["#{y60_path}/*/.classpath"]
      
      my_path = File.expand_path Dir.getwd
      if my_path != y60_path
        LOGGER.debug " * adding '#{my_path}' to project search path"
        dirs.concat Dir["#{my_path}/*/.classpath"]
      end
      
      dirs.each do |project_path|
        pj_path_list = project_path.split("/")
        pj_path_list.pop # remove file from path
        pj_path = pj_path_list.join("/")
        pj_name = pj_path_list.pop
        
        LOGGER.debug " * Found #{pj_name}:\n\t  at path: '#{pj_path}'"
        
        # add to paths only if all project paths are to be loaded
        @@project_paths[pj_name] = pj_path if (all_or_names == :all) or all_or_names.member? pj_name
      end
    end
    
    # TODO better error reporting via Open3::popen3
    def run message, cmdline
      if $project_test
        puts "executing #{cmdline.split.join(" ")}"
      else
        successful = system cmdline
        raise "error while #{message}" unless successful
      end
    end
  
end

# main method: some very basic testing for the integrity of this class
def main args
  pjs = Project.load_in_dependency_order
  @loaded = []
  pjs.each do |pj|
    puts pj.to_s
    pj.dependencies.each do |dep|
      if !@loaded.member? dep
        throw "Dependency (#{dep}) for project (#{pj}) missing!"
      end
    end
    @loaded << pj
  end
  $project_test = true # prohibits the actual execution of any shell command
  pjs.each do |pj|
    pj.uninstall
    pj.install
    pj.cleanup
    pj.merge_dependencies
    pj.deploy
    pj.test
  end
end

(__FILE__ == $0) and (main ARGV) 