package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite moteur - Brain / Bots")
@SelectClasses({
	engine.brain.TestBot.class,
	engine.brain.TestBrain.class,
	engine.brain.TestAttackBot.class,
	engine.brain.TestFleeBot.class,
	engine.brain.TestPlayerBot.class
})
public class SuiteMoteurBrain {
}
