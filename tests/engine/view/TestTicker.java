package engine.view;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.brain.Brain;
import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("Ticker - pause et reprise")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestTicker {

	@Test
	@Order(1)
	@DisplayName("01 - pause/resume modifie seulement l'état du ticker")
	void pauseResume() {
		System.out.println("Ticker/01 : pause/reprise sans lancer la task");
		var model = new JeuTest().model();
		View view = new View(model, 100, 100);
		Ticker ticker = new Ticker(model, new Brain(), view, 16, null);

		assertFalse(ticker.paused());
		ticker.pause();
		assertTrue(ticker.paused());
		ticker.resume();
		assertFalse(ticker.paused());
		ticker.stop();
	}
}
