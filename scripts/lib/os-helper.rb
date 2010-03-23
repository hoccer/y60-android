module OS
  
  def self.execute cmd, err_str
    putsf "executing '#{cmd}'"
    successful = system cmd
    if !successful
      raise "error while "+err_str
    end
  end
  
end