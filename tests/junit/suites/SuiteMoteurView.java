package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite moteur - View / Picture / Time")
@SelectClasses({
	engine.view.TestPicture.class,
	engine.view.TestView.class,
	engine.view.TestAvatar.class,
	engine.view.TestImageAvatar.class,
	engine.view.TestAnimatedImageAvatar.class,
	engine.view.TestDebugOptions.class,
	engine.view.TestSpawnAnimation.class,
	engine.view.TestStartupReveal.class,
	engine.view.TestTimeStats.class,
	engine.view.TestPerformanceMonitor.class,
	engine.view.TestTicker.class
})
public class SuiteMoteurView {
}
