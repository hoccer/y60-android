class TestResult
  
  attr_accessor :tests_run, :failures, :exceptions, :broken_instrumentations,
                :test_suite_name
  
  STATUS_NAMES = {
    'true' => "Succeeded",
    'false' => "Failed"
  }
  
  def initialize tests_run, failures, exceptions, broken_instrumentations,
                 test_suite_name=nil
    @tests_run = tests_run
    @failures = failures
    @exceptions = exceptions
    @broken_instrumentations = broken_instrumentations
    @test_suite_name = test_suite_name
  end

  def failed?
    !succeeded?
  end
  
  def succeeded?
    if @failures == 0 and
       @exceptions == 0 and
       @broken_instrumentations == 0
      return true
    else
      return false
    end
  end
  
  def to_s
    msg = ""
    if @test_suite_name
      msg += "Name                    : #{@test_suite_name}\n"
    else
      msg += "\n"
    end
    msg += <<-TXT
Tests run               : #{@tests_run}
Failures                : #{@failures}
Exceptions              : #{@exceptions}
Broken Instrumentations : #{@broken_instrumentations}
Total Status            : #{TestResult::STATUS_NAMES[succeeded?.to_s]}
TXT
    return msg
  end
  
end