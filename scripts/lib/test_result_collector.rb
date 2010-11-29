require "#{File.dirname(__FILE__)}/test_result"

class TestResultCollector
  
  attr_accessor :test_results
  
  def initialize
    @test_results = []
  end
  
  def << theTestResult
    if theTestResult.kind_of?(TestResult)
      @test_results << theTestResult
    elsif theTestResult.kind_of?(TestResultCollector)
      #puts "adding another collector"
      theTestResult.test_results.each { |sub_test_result|
        #puts "adding #{sub_test_result}"
        @test_results << sub_test_result
      }
    else
      raise "Argument needs to be another TestResult"
    end
    @test_results
  end
  
  def succeeded?
    return @test_results.inject(true) { |memo, test_result|
      memo and test_result.succeeded?
    }
  end
  
  def dump_failed_suites
    msg = ""
    @test_results.each { |suite|
      if suite.failed?
        msg += "Failed TestSuite:\n"
        msg += "-----------------\n"
        msg += suite.to_s
      end
    }
    msg
  end
  
  def to_s
    total = TestResult.new 0,0,0,0
    @test_results.each { |suite|
      total.tests_run  += suite.tests_run
      total.failures   += suite.failures
      total.exceptions += suite.exceptions
      total.broken_instrumentations += suite.broken_instrumentations
    }
    msg =  "Total Results (#{@test_results.size} Testsuites):\n"
    msg += "--------------\n"
    msg += total.to_s
    msg += "\n--------------\n"
    msg
  end
  
end