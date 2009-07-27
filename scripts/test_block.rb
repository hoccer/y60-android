def bla
  [1].each { return "block" }
  return "method"
end

puts bla
