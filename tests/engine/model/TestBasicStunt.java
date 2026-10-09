package engine.model;

import static engine.tests.OutilsMoteur.EPS;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("BasicStunt - marche cellule par cellule")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestBasicStunt {

	@Test
	@Order(1)
	@DisplayName("01 - walk démarre une action et tick la termine au centre de la cellule suivante")
	void walkEstUneActionDansLeTemps() {
		System.out.println("BasicStunt/01 : walk puis tick virtuel");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, e);

		assertTrue(stunt.walk(0));
		assertTrue(e.isMoving());
		model.tick(1000L);

		assertEquals(2, e.position().x());
		assertEquals(1, e.position().y());
		assertFalse(e.isMoving());
		assertFalse(stunt.walking());
	}

	@Test
	@Order(2)
	@DisplayName("02 - une direction non cardinale est arrondie vers le cardinal le plus proche")
	void angleArrondi() {
		System.out.println("BasicStunt/02 : 80 degrés devient Sud");
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, e);

		assertTrue(stunt.walk(80));
		assertEquals(90, e.orientation());
	}

	@Test
	@Order(3)
	@DisplayName("03 - walk refuse une cellule occupée par une entité bloquante")
	void refusCelluleBloquante() {
		System.out.println("BasicStunt/03 : obstacle dans la cellule cible");
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		OutilsMoteur.ajouteRectangle(model, "wall", 2, 1, true);
		BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, e);

		assertFalse(stunt.walk(0));
		assertFalse(e.isMoving());
		assertEquals(1, e.position().x());
	}

	@Test
	@Order(4)
	@DisplayName("04 - un demi-tour pendant la marche est accepté immédiatement")
	void demiTourImmediat() {
		System.out.println("BasicStunt/04 : droite puis gauche");
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, e);

		assertTrue(stunt.walk(0));
		assertTrue(stunt.walk(180));

		assertEquals(180, e.orientation());
		assertTrue(e.linearSpeed().x() < 0.0);
	}

	@Test
	@Order(5)
	@DisplayName("05 - cancelCurrentAction arrête la marche et recentre l'entité")
	void annulationAction() {
		System.out.println("BasicStunt/05 : annulation d'une action en cours");
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, e);

		assertTrue(stunt.walk(0));
		stunt.cancelCurrentAction();

		assertFalse(e.isMoving());
		assertFalse(stunt.walking());
	}
	@Test
	@Order(6)
	@DisplayName("06 - BasicStunt couvre directions, demi-tour, virage proche/loin et annulation")
	void basicStuntPlusComplet() {
		int[] directions = { 0, 90, 180, 270 };
		for (int direction : directions) {
			Game game = new JeuTest(10, 8, true, true, 3.7, 2);
			Model model = game.model();
			EntiteTest e = OutilsMoteur.ajoute(model, "e" + direction, 4, 4);
			BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, e);

			assertTrue(stunt.walk(direction));
			assertTrue(stunt.walking());
			assertTrue(e.isMoving());

			stunt.cancelCurrentAction();
			assertFalse(stunt.walking());
			assertFalse(e.isMoving());
		}

		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 3, 3);
		BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, e);

		stunt.setWalkSpeed(-2.0);
		assertEquals(5.0, stunt.walkSpeed(), EPS);

		stunt.setWalkSpeed(2.0);
		assertEquals(2.0, stunt.walkSpeed(), EPS);

		assertTrue(stunt.walk(10));
		assertTrue(stunt.walk(20));

		assertTrue(stunt.walk(180));
		assertEquals(180, e.orientation());

		stunt.cancelCurrentAction();

		assertTrue(stunt.walk(0));
		assertTrue(stunt.walk(90));
		assertEquals(90, e.orientation());

		stunt.cancelCurrentAction();

		assertTrue(stunt.walk(0));
		model.move(e, 200L);
		assertFalse(stunt.walk(90));

		stunt.invalidate();
		stunt.invalidate();
		assertFalse(stunt.walk(0));
		stunt.cancelCurrentAction();
	}

}
