package engine.model;

import static engine.tests.OutilsMoteur.EPS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("Game - configuration du moteur")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestGame {

	@Test
	@Order(1)
	@DisplayName("01 - le constructeur en cellules construit Grid, ISU, Picture et Model")
	void construitLesObjetsDuMoteur() {
		System.out.println("Game/01 : construction complète des services du moteur");
		Game game = new JeuTest(10, 8, true, false, 3.7, 2);

		assertEquals(10, game.getWidthCell());
		assertEquals(8, game.getHeightCell());
		assertEquals(37.0, game.getWidthCm(), EPS);
		assertEquals(29.6, game.getHeightCm(), EPS);
		assertNotNull(game.getGrid());
		assertNotNull(game.getIsu());
		assertNotNull(game.getPicture());
		assertNotNull(game.model());
		assertSame(game, game.game());
	}

	@Test
	@Order(2)
	@DisplayName("02 - le constructeur en centimètres retrouve le nombre de cellules")
	void construitDepuisLesCentimetres() {
		System.out.println("Game/02 : cohérence entre cm et cellules");
		Game game = new JeuTest(10.0, 6.0, false, false, 2.0, 3);

		assertEquals(5, game.getWidthCell());
		assertEquals(3, game.getHeightCell());
		assertEquals(30.0, game.getWidthPixel(), EPS);
		assertEquals(18.0, game.getHeightPixel(), EPS);
	}

	@Test
	@Order(3)
	@DisplayName("03 - accesseurs de configuration et affichage")
	void accesseursConfigurationEtAffichage() {
		System.out.println("Game/03 : accesseurs et show");

		Game game = new JeuTest(12, 9, true, false, 3.5, 4);

		assertEquals(12, game.getWidthCell());
		assertEquals(9, game.getHeightCell());

		assertEquals(42.0, game.getWidthCm(), EPS);
		assertEquals(31.5, game.getHeightCm(), EPS);

		assertEquals(168.0, game.getWidthPixel(), EPS);
		assertEquals(126.0, game.getHeightPixel(), EPS);

		assertEquals(3.5, game.getCmPerCell(), EPS);
		assertEquals(4, game.getPixelPerCm());

		assertTrue(game.isTorusOnXaxis());
		assertFalse(game.isTorusOnYaxis());

		assertTrue(game.getTorusOnXaxis());
		assertFalse(game.getTorusOnYaxis());

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(buffer);

		game.show(ps);

		String text = buffer.toString();

		assertTrue(text.contains("Game:"));
		assertTrue(text.contains("width_ncell = 12"));
		assertTrue(text.contains("height_ncell = 9"));
		assertTrue(text.contains("width_cm = 42.0"));
		assertTrue(text.contains("height_cm = 31.5"));
		assertTrue(text.contains("cmPerCell = 3.5"));
		assertTrue(text.contains("pixelPerCm = 4"));
		assertTrue(text.contains("torusOnXaxis = true"));
		assertTrue(text.contains("torusOnYaxis = false"));
	}
}
