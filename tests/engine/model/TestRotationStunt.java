package engine.model;

import static engine.tests.OutilsMoteur.EPS;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("RotationStunt - rotation continue")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestRotationStunt {

	@Test
	@Order(1)
	@DisplayName("01 - constructeur par défaut")
	void constructeurParDefaut() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity);
		entity.setStunt(stunt);

		assertFalse(stunt.rotating());
		assertFalse(stunt.turningLeft());
		assertFalse(stunt.turningRight());
		assertFalse(stunt.walk(0));

		stunt.startTurnRight();

		assertTrue(stunt.rotating());
		assertFalse(stunt.turningLeft());
		assertTrue(stunt.turningRight());
		assertEquals(180.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(2)
	@DisplayName("02 - startTurnRight applique une vitesse angulaire positive")
	void rotationDroite() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		stunt.startTurnRight();

		assertTrue(stunt.turningRight());
		assertFalse(stunt.turningLeft());
		assertTrue(stunt.rotating());
		assertEquals(90.0, entity.angularSpeed(), EPS);

		model.tick(1000L);

		assertEquals(90, entity.orientation());
	}

	@Test
	@Order(3)
	@DisplayName("03 - startTurnLeft applique une vitesse angulaire négative")
	void rotationGauche() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		stunt.startTurnLeft();

		assertTrue(stunt.turningLeft());
		assertFalse(stunt.turningRight());
		assertTrue(stunt.rotating());
		assertEquals(-90.0, entity.angularSpeed(), EPS);

		model.tick(1000L);

		assertEquals(270, entity.orientation());
	}

	@Test
	@Order(4)
	@DisplayName("04 - stopTurnRight coupe la rotation droite")
	void stopRotationDroite() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		stunt.startTurnRight();

		assertEquals(90.0, entity.angularSpeed(), EPS);

		stunt.stopTurnRight();

		assertFalse(stunt.turningRight());
		assertFalse(stunt.turningLeft());
		assertFalse(stunt.rotating());
		assertEquals(0.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(5)
	@DisplayName("05 - stopTurnLeft coupe la rotation gauche")
	void stopRotationGauche() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		stunt.startTurnLeft();

		assertEquals(-90.0, entity.angularSpeed(), EPS);

		stunt.stopTurnLeft();

		assertFalse(stunt.turningLeft());
		assertFalse(stunt.turningRight());
		assertFalse(stunt.rotating());
		assertEquals(0.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(6)
	@DisplayName("06 - stopTurnLeft et stopTurnRight ne font rien si rien ne tourne")
	void stopQuandDejaArrete() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		assertFalse(stunt.rotating());
		assertEquals(0.0, entity.angularSpeed(), EPS);

		stunt.stopTurnLeft();
		stunt.stopTurnRight();

		assertFalse(stunt.rotating());
		assertEquals(0.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(7)
	@DisplayName("07 - changer de direction remplace l'ancienne rotation")
	void changementDeDirection() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 60.0);
		entity.setStunt(stunt);

		stunt.startTurnLeft();

		assertTrue(stunt.turningLeft());
		assertFalse(stunt.turningRight());
		assertEquals(-60.0, entity.angularSpeed(), EPS);

		stunt.startTurnRight();

		assertFalse(stunt.turningLeft());
		assertTrue(stunt.turningRight());
		assertEquals(60.0, entity.angularSpeed(), EPS);

		stunt.startTurnLeft();

		assertTrue(stunt.turningLeft());
		assertFalse(stunt.turningRight());
		assertEquals(-60.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(8)
	@DisplayName("08 - un stunt invalidé refuse de démarrer")
	void stuntInvalide() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		stunt.invalidate();

		assertFalse(stunt.rotating());

		stunt.startTurnRight();
		stunt.startTurnLeft();

		assertFalse(stunt.rotating());
		assertFalse(stunt.turningRight());
		assertFalse(stunt.turningLeft());
		assertEquals(0.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(9)
	@DisplayName("09 - invalidate remet les états internes à false")
	void invalidateNettoieEtatInterne() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		stunt.startTurnRight();

		assertTrue(stunt.rotating());

		stunt.invalidate();

		assertFalse(stunt.rotating());
		assertFalse(stunt.turningRight());
		assertFalse(stunt.turningLeft());
		assertEquals(0.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(10)
	@DisplayName("10 - une entité non installée ne peut pas tourner")
	void entiteNonInstallee() {
		Model model = new JeuTest().model();
		EntiteTest entity = new EntiteTest(model, "nonInstallee");

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		assertFalse(entity.installed());

		stunt.startTurnRight();
		stunt.startTurnLeft();

		assertFalse(stunt.rotating());
		assertFalse(stunt.turningRight());
		assertFalse(stunt.turningLeft());
		assertEquals(0.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(11)
	@DisplayName("11 - une entité morte ne peut pas tourner")
	void entiteMorte() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		entity.death();

		assertFalse(entity.alive());

		stunt.startTurnRight();
		stunt.startTurnLeft();

		assertFalse(stunt.rotating());
		assertFalse(stunt.turningRight());
		assertFalse(stunt.turningLeft());
		assertEquals(0.0, entity.angularSpeed(), EPS);
	}

	@Test
	@Order(12)
	@DisplayName("12 - walk est toujours refusé par RotationStunt")
	void walkRefuse() {
		Model model = new JeuTest().model();
		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 2, 2);

		RotationStunt stunt = new RotationStunt(model, entity, 90.0);
		entity.setStunt(stunt);

		assertFalse(stunt.walk(Orientation.EAST));
		assertFalse(stunt.walk(Orientation.SOUTH));
		assertFalse(stunt.walk(Orientation.WEST));
		assertFalse(stunt.walk(Orientation.NORTH));
	}
}