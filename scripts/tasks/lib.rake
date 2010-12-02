# Helper Methods

def log msg
  unless Rake.application.options.silent
    puts msg
  end
end

def execute_command cmd, message="command"
  log " * executing\n   '#{cmd}':"
  log "------------"
  result = system cmd
  log "------------"
  if !result
    puts "An error occured while #{message}:"
    puts $?
  end
  result
end