package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite moteur - Model / Geometry / Collisions")
@SelectClasses({
	engine.model.TestAxis.class,
	engine.model.TestGame.class,
	engine.model.TestGrid.class,
	engine.model.TestISU.class,
	engine.model.TestHitbox.class,
	engine.model.TestCircle.class,
	engine.model.TestRect.class,
	engine.model.TestBounding.class,
	engine.model.TestOrientation.class,
	engine.model.TestEntity.class,
	engine.model.TestModel.class,
	engine.model.TestViewPort.class,
	engine.model.TestBasicStunt.class,
	engine.model.TestRotationStunt.class,
	engine.model.TestShape.class,
	engine.model.TestStunt.class,	
})
public class SuiteMoteurModel {
	// vide : les annotations font le travail	
}
