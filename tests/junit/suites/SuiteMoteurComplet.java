package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite complète - Tests moteur")
@SelectClasses({
	SuiteMoteurModel.class,
	SuiteMoteurBrain.class,
	SuiteMoteurController.class,
	SuiteMoteurView.class
})
public class SuiteMoteurComplet {
}
