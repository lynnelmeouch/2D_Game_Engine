package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite moteur - Controller")
@SelectClasses({
	engine.controller.TestController.class,
	engine.controller.TestKeyboardController.class,
	engine.controller.TestMouseController.class
})
public class SuiteMoteurController {
}
