require 'test/unit'
require "#{File.dirname(__FILE__)}/../../android_project"

class AndroidProjectTest < Test::Unit::TestCase
  # def setup
  # end

  # def teardown
  # end

  def test_parse_success_from_log_output
    result = AndroidProject::extract_test_status "OK (1 test)"
    assert_equal 1, result.tests_run
    assert_equal 0, result.failures
    assert_equal 0, result.exceptions
    assert_equal 0, result.broken_instrumentations

    result = AndroidProject::extract_test_status "OK (2 tests)"
    assert_equal 2, result.tests_run
    assert_equal 0, result.failures
    assert_equal 0, result.exceptions
    assert_equal 0, result.broken_instrumentations
  end
  
  def test_parse_failure_from_log_output
    result = AndroidProject::extract_test_status "Tests run: 7,  Failures: 1,  Errors: 0"
    assert_equal 7, result.tests_run
    assert_equal 1, result.failures
    assert_equal 0, result.exceptions
    assert_equal 0, result.broken_instrumentations


    result = AndroidProject::extract_test_status "Tests run: 7,  Failures: 0,  Errors: 3"
    assert_equal 7, result.tests_run
    assert_equal 0, result.failures
    assert_equal 3, result.exceptions
    assert_equal 0, result.broken_instrumentations
  end
  
  def test_parse_broken_instrumentation_from_log_output
    result = AndroidProject::extract_test_status "INSTRUMENTATION_FAILED: com.artcom.y60/android.test.InstrumentationTestRunner"
    assert_equal 0, result.tests_run
    assert_equal 0, result.failures
    assert_equal 0, result.exceptions
    assert_equal 1, result.broken_instrumentations
    
    result = AndroidProject::extract_test_status "Blahfasel com.artcom.y60/android.test.InstrumentationTestRunner"
    assert_equal nil, result
  end
  
  def test_non_matching_lines_produce_nil
    result = AndroidProject::extract_test_status "This is a test of instrumentation failure:"
    assert_equal nil, result
    result = AndroidProject::extract_test_status "OK"
    assert_equal nil, result
    result = AndroidProject::extract_test_status "INSTRUMENTATION:"
    assert_equal nil, result
    result = AndroidProject::extract_test_status "Tests"
    assert_equal nil, result
  end
  
end