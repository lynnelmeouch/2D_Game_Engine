package engine.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@DisplayName("Controller - activation générique")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestController {


	static class ControllerVide extends Controller {
	}

	@Test
	@Order(1)
	@DisplayName("01 - un contrôleur peut être activé ou désactivé")
	void enableDisable() {
		System.out.println("Controller/01 : enable/disable");
		ControllerVide controller = new ControllerVide();

		assertTrue(controller.enabled());
		controller.disable();
		assertFalse(controller.enabled());
		controller.enable();
		assertTrue(controller.enabled());
	}
	@Test
	@Order(1)
	@DisplayName("01 - Controller de base couvre enable, disable et événements vides")
	void controllerDeBase() {
		ControllerVide controller = new ControllerVide();

		assertTrue(controller.enabled());

		controller.disable();
		assertFalse(controller.enabled());

		controller.enable();
		assertTrue(controller.enabled());

		assertDoesNotThrow(() -> controller.pressed(null, 123, 'x'));
		assertDoesNotThrow(() -> controller.released(null, 123, 'x'));
		assertDoesNotThrow(() -> controller.typed(null, 'x'));

		assertDoesNotThrow(() -> controller.moved(null, 10, 20));
		assertDoesNotThrow(() -> controller.pressed(null, 1, 10, 20));
		assertDoesNotThrow(() -> controller.released(null, 1, 10, 20));
	}

}
