#!/usr/bin/ruby

class JavaProject < Project

  def initialize pj_name
    super pj_name

  end

  def jar_path
    return "#{path}/bin/#{name}.jar"
  end
  
end
