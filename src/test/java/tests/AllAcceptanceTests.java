package tests;

import org.junit.platform.suite.api.IncludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("acceptance_tests")
@IncludePackages("acceptance_tests")
public class AllAcceptanceTests {
    // This class will automatically run all tests in acceptance_tests package
}
