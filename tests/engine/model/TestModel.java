package engine.model;

import static engine.tests.OutilsMoteur.EPS;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
import engine.tests.OutilsMoteur.FormeTest;

@DisplayName("Model - monde, mouvements et collisions")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestModel {

	@Test
	@Order(1)
	@DisplayName("01 - add/remove gère la liste et les cellules")
	void addRemove() {
		System.out.println("Model/01 : ajout puis retrait d'une entité");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 3, 3);

		assertTrue(model.entities().contains(e));
		assertTrue(e.installed());

		model.remove(e);

		assertFalse(model.entities().contains(e));
		assertFalse(e.installed());
		assertTrue(e.occupied().isEmpty());
	}

	@Test
	@Order(2)
	@DisplayName("02 - collisions retourne seulement les entités vivantes en contact")
	void collisionsCourantes() {
		System.out.println("Model/02 : collisions à la position actuelle");
		Model model = new JeuTest().model();
		EntiteTest a = OutilsMoteur.ajoute(model, "a", 1, 1);
		EntiteTest b = OutilsMoteur.ajoute(model, "b", 1, 1);
		EntiteTest c = OutilsMoteur.ajoute(model, "c", 5, 5);
		c.death();

		List<Entity> collisions = model.collisions(a);

		assertTrue(collisions.contains(b));
		assertFalse(collisions.contains(c));
	}

	@Test
	@Order(3)
	@DisplayName("03 - tick applique une vitesse avec temps virtuel")
	void tickDeplaceSelonLeTemps() {
		System.out.println("Model/03 : mouvement = vitesse * elapsed");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);

		e.setLinearSpeed(model.isu().new Vector(3.7, 0.0));
		model.tick(1000L);

		assertEquals(2, e.position().x());
		assertEquals((2 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
	}

	@Test
	@Order(4)
	@DisplayName("04 - un obstacle bloquant annule le mouvement et notifie la collision")
	void collisionBloquanteAnnule() {
		System.out.println("Model/04 : mouvement bloqué par une entité bloquante");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest mover = OutilsMoteur.ajoute(model, "mover", 1, 1);
		EntiteTest wall = OutilsMoteur.ajouteRectangle(model, "wall", 2, 1, true);

		mover.setLinearSpeed(model.isu().new Vector(3.7, 0.0));
		boolean moved = model.move(mover, 1000L);

		assertFalse(moved);
		assertEquals(1, mover.position().x());
		assertFalse(mover.isMoving());
		assertEquals(1, mover.collisionsRecues());
		assertTrue(mover.dernieresCollisions().contains(wall));
	}

	@Test
	@Order(5)
	@DisplayName("05 - les candidats de collision couvrent tout le chemin")
	void collisionsSurLeChemin() {
		System.out.println("Model/05 : détection sur le chemin, pas seulement à l'arrivée");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest mover = OutilsMoteur.ajoute(model, "mover", 1, 1);
		EntiteTest obstacle = OutilsMoteur.ajoute(model, "obstacle", 4, 1);

		ISU.Coord from = mover.center();
		ISU.Coord to = game.getGrid().new Position(5, 1).toISUCoordCentered();

		List<Entity> candidates = model.potentialCollisionsByMotionHitbox(mover, from, to);

		assertTrue(candidates.contains(obstacle));
	}
	@Test
	@Order(5)
	@DisplayName("05 - Model couvre remove absent, contacts non bloquants, viewport visible et wrapping")
	void modelPlusComplet() {
		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		Model model = game.model();

		EntiteTest pasAjoutee = new EntiteTest(model, "absente");
		model.remove(pasAjoutee);

		EntiteTest a = new EntiteTest(model, "a", FormeTest.RECTANGLE, false);
		model.add(a);

		assertTrue(model.entities().contains(a));
		assertSame(game, model.game());
		assertSame(game.getGrid(), model.grid());
		assertSame(game.getIsu(), model.isu());

		EntiteTest cible = OutilsMoteur.ajoute(model, "cible", 2, 0);
		a.setLinearSpeed(model.isu().new Vector(game.getCmPerCell() * 2.0, 0.0));

		boolean ok = model.move(a, 1000L);
		assertTrue(ok);
		assertTrue(a.collisionsRecues() > 0);
		assertTrue(a.dernieresCollisions().contains(cible));

		EntiteTest bloqueurMobile = new EntiteTest(model, "bloqueurMobile", FormeTest.CERCLE, true);
		model.add(bloqueurMobile, model.grid().new Position(4, 0));

		EntiteTest normal = OutilsMoteur.ajoute(model, "normal", 5, 0);
		bloqueurMobile.setLinearSpeed(model.isu().new Vector(game.getCmPerCell(), 0.0));

		boolean avance = model.move(bloqueurMobile, 1000L);
		assertFalse(avance);
		assertFalse(bloqueurMobile.isMoving());

		EntiteTest bordDroit = OutilsMoteur.ajoute(model, "droite", 9, 1);
		EntiteTest bordGauche = OutilsMoteur.ajoute(model, "gauche", 0, 1);

		ViewPort vp = new ViewPort(model, model.isu().new Coord(9 * game.getCmPerCell(), 0.0),
				model.isu().new Dimension(2 * game.getCmPerCell(), 3 * game.getCmPerCell()));

		model.setViewPort(vp);

		List<Entity> visibles = model.visibleEntities();
		assertTrue(visibles.contains(bordDroit));
		assertTrue(visibles.contains(bordGauche));

		List<Entity> surChemin = model.collisionsAlongMotion(bordGauche, bordGauche.center(),
				model.isu().new Vector(game.getCmPerCell() * 9.0, 0.0));

		assertFalse(surChemin.contains(bordGauche));
	}

}
