require 'test/unit'
require "#{File.dirname(__FILE__)}/../../lib/test_result"

class TestResultTest < Test::Unit::TestCase
  # def setup
  # end

  # def teardown
  # end

  def test_construction_and_accessors
    myTestResult = TestResult.new 4,0,0,0
    assert_equal nil, myTestResult.test_suite_name
    assert_equal 4, myTestResult.tests_run
    assert_equal 0, myTestResult.failures
    assert_equal 0, myTestResult.exceptions
    assert_equal 0, myTestResult.broken_instrumentations
    assert_equal true, myTestResult.succeeded?
    assert_equal false, myTestResult.failed?
    
    myTestResult = TestResult.new 9,1,0,0, "OneFailure"
    assert_equal "OneFailure", myTestResult.test_suite_name
    assert_equal 9, myTestResult.tests_run
    assert_equal 1, myTestResult.failures
    assert_equal 0, myTestResult.exceptions
    assert_equal 0, myTestResult.broken_instrumentations
    assert_equal false, myTestResult.succeeded?
    assert_equal true, myTestResult.failed?
    
    myTestResult = TestResult.new 9,0,1,0, "OneException"
    assert_equal "OneException", myTestResult.test_suite_name
    assert_equal 9, myTestResult.tests_run
    assert_equal 0, myTestResult.failures
    assert_equal 1, myTestResult.exceptions
    assert_equal 0, myTestResult.broken_instrumentations
    assert_equal false, myTestResult.succeeded?
    assert_equal true, myTestResult.failed?
    
    myTestResult = TestResult.new 9,0,0,1, "OneBrokenInstrumentation"
    assert_equal "OneBrokenInstrumentation", myTestResult.test_suite_name
    assert_equal 9, myTestResult.tests_run
    assert_equal 0, myTestResult.failures
    assert_equal 0, myTestResult.exceptions
    assert_equal 1, myTestResult.broken_instrumentations
    assert_equal false, myTestResult.succeeded?
    assert_equal true, myTestResult.failed?
  end
  
  def test_serialization
    myTestResult = TestResult.new 9,0,0,1, "OneBrokenInstrumentation"
    myResult = <<-RESULT
Name                    : OneBrokenInstrumentation
Tests run               : 9
Failures                : 0
Exceptions              : 0
Broken Instrumentations : 1
Total Status            : Failed
RESULT
    
    assert_equal myResult, myTestResult.to_s
    
    myTestResult = TestResult.new 9,0,0,1
    myResult = <<-RESULT

Tests run               : 9
Failures                : 0
Exceptions              : 0
Broken Instrumentations : 1
Total Status            : Failed
RESULT
    assert_equal myResult, myTestResult.to_s
  end
  
end