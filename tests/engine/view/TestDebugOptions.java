package engine.view;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@DisplayName("DebugOptions - interrupteurs de debug")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestDebugOptions {

	@Test
	@Order(1)
	@DisplayName("01 - les options changent d'état avec toggle")
	void toggles() {
		System.out.println("DebugOptions/01 : toggles F1/debug");
		DebugOptions options = new DebugOptions();

		assertFalse(options.enabled());
		assertFalse(options.showActions());
		options.toggleEnabled();
		assertTrue(options.enabled());
		assertTrue(options.showActions());
		assertTrue(options.showBoundingBoxes());
		assertTrue(options.showPerformance());

		options.toggleActions();
		options.toggleBoundingBoxes();
		options.togglePerformance();
		options.toggleLoadTest();

		assertFalse(options.showActions());
		assertFalse(options.showBoundingBoxes());
		assertFalse(options.showPerformance());
		assertTrue(options.loadTest());
	}
	@Test
	@Order(7)
	@DisplayName("07 - DebugOptions couvre toggles quand debug est désactivé")
	void debugOptionsPlusComplet() {
		DebugOptions options = new DebugOptions();

		assertFalse(options.enabled());
		assertFalse(options.showActions());
		assertFalse(options.showBoundingBoxes());
		assertFalse(options.showPerformance());
		assertFalse(options.loadTest());

		options.toggleActions();
		options.toggleBoundingBoxes();
		options.togglePerformance();
		options.toggleLoadTest();

		assertFalse(options.showActions());
		assertFalse(options.showBoundingBoxes());
		assertFalse(options.showPerformance());
		assertFalse(options.loadTest());

		options.toggleEnabled();

		assertTrue(options.enabled());
		assertFalse(options.showActions());
		assertFalse(options.showBoundingBoxes());
		assertFalse(options.showPerformance());
		assertTrue(options.loadTest());
	}

}
