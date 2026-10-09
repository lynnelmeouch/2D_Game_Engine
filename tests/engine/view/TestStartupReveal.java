package engine.view;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

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

@DisplayName("StartupReveal - apparition ligne par ligne")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestStartupReveal {

	@Test
	@Order(1)
	@DisplayName("01 - les entités apparaissent dans l'ordre y puis x")
	void ordreLigneParLigne() {
		System.out.println("StartupReveal/01 : haut vers bas, gauche vers droite");
		var model = new JeuTest().model();
		EntiteTest bas = OutilsMoteur.ajoute(model, "bas", 0, 3);
		EntiteTest hautDroite = OutilsMoteur.ajoute(model, "hautDroite", 5, 1);
		EntiteTest hautGauche = OutilsMoteur.ajoute(model, "hautGauche", 2, 1);

		StartupReveal reveal = new StartupReveal(Arrays.asList(bas, hautDroite, hautGauche), 10L, 100L);

		assertEquals(0.0, reveal.scale(hautGauche));
		assertEquals(0.0, reveal.scale(hautDroite));
		reveal.tick(1L);
		assertTrue(reveal.scale(hautGauche) > 0.0);
		assertEquals(0.0, reveal.scale(hautDroite));
		reveal.tick(10L);
		assertTrue(reveal.scale(hautDroite) > 0.0);
		assertEquals(0.0, reveal.scale(bas));
	}

	@Test
	@Order(2)
	@DisplayName("02 - une entité absente est considérée déjà visible")
	void entiteAbsenteVisible() {
		System.out.println("StartupReveal/02 : entité non listée");
		var model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 0, 0);
		EntiteTest autre = OutilsMoteur.ajoute(model, "autre", 1, 0);
		StartupReveal reveal = new StartupReveal(Arrays.asList(e), 10L, 100L);

		assertEquals(1.0, reveal.scale(autre));
		assertTrue(reveal.visible(autre));
	}
}
