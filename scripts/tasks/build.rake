desc 'Builds the android projects (clobbers the checked out version so do this in a separate clone!!)'
task :build do 
  fail unless system "#{@y60_path}/scripts/build.rb"
end