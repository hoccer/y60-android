require 'test/unit'
require "#{File.dirname(__FILE__)}/../../lib/test_result_collector"

class TestResultCollectorTest < Test::Unit::TestCase
  # def setup
  # end

  # def teardown
  # end

  def test_construction_and_accessors
    myCollector = TestResultCollector.new
    assert_equal 0, myCollector.test_results.size
    assert_equal true, myCollector.succeeded?
    myResult = <<-RESULT
Total Results (0 Testsuites):
--------------

Tests run               : 0
Failures                : 0
Exceptions              : 0
Broken Instrumentations : 0
Total Status            : Succeeded

--------------
RESULT
    assert_equal myResult, myCollector.to_s
  end
  
  def test_append_test_results
    myCollector = TestResultCollector.new
    myCollector << TestResult.new(4,0,0,0,"Suite1")
    assert_equal 1, myCollector.test_results.size
    assert_equal true, myCollector.succeeded?
    myCollector << TestResult.new(3,1,0,0,"Suite2Failed")
    assert_equal 2, myCollector.test_results.size
    assert_equal false, myCollector.succeeded?
  end
  
  def test_serialization
    myCollector = TestResultCollector.new
    myCollector << TestResult.new(4,0,0,0,"Suite1")
    assert_equal 1, myCollector.test_results.size
    assert_equal true, myCollector.succeeded?
    myCollector << TestResult.new(3,1,0,0,"Suite2Failed")
    assert_equal 2, myCollector.test_results.size
    assert_equal false, myCollector.succeeded?
    
    myResult = <<-RESULT
Total Results (2 Testsuites):
--------------

Tests run               : 7
Failures                : 1
Exceptions              : 0
Broken Instrumentations : 0
Total Status            : Failed

--------------
RESULT
    assert_equal myResult, myCollector.to_s
    
    myResult = <<-RESULT
Failed TestSuite:
-----------------
Name                    : Suite2Failed
Tests run               : 3
Failures                : 1
Exceptions              : 0
Broken Instrumentations : 0
Total Status            : Failed
RESULT
    assert_equal myResult, myCollector.dump_failed_suites
  end
  
  def test_append_collector_to_collector
    myFirstCollector = TestResultCollector.new
    myFirstCollector << TestResult.new(4,0,0,0,"Suite1")
    myFirstCollector << TestResult.new(3,0,0,0,"Suite2")
    mySecondCollector = TestResultCollector.new
    mySecondCollector << TestResult.new(4,0,0,0,"Suite3")
    mySecondCollector << TestResult.new(5,1,0,0,"Suite4Failed")
    
    assert_equal true, myFirstCollector.succeeded?
    myFirstCollector << mySecondCollector
    
    assert_equal 4, myFirstCollector.test_results.size
    assert_equal false, myFirstCollector.succeeded?
    
    myResult = <<-RESULT
Total Results (4 Testsuites):
--------------

Tests run               : 16
Failures                : 1
Exceptions              : 0
Broken Instrumentations : 0
Total Status            : Failed

--------------
RESULT
    assert_equal myResult, myFirstCollector.to_s
  end
  
end