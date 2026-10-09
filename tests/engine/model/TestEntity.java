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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

@DisplayName("Entity - état géométrique et occupation")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestEntity {

	private PrintStream sortieMuette() {
		return new PrintStream(new ByteArrayOutputStream());
	}

	private static final class StuntTemoin extends Stunt {
		int nbOrientation;
		int nbCellule;
		int nbCoord;
		int nbCommit;
		int nbCollisionSimple;
		int nbCollisionListe;

		StuntTemoin(Model model, Entity entity) {
			super(model, entity);
		}

		@Override
		public boolean walk(int degree) {
			return valid();
		}

		@Override
		public void set(int orientation) {
			super.set(orientation);
			nbOrientation++;
		}

		@Override
		public void set(Grid.Cell cell) {
			super.set(cell);
			nbCellule++;
		}

		@Override
		public void set(double x, double y) {
			super.set(x, y);
			nbCoord++;
		}

		@Override
		public void commit() {
			super.commit();
			nbCommit++;
		}

		@Override
		public void collision(Entity e) {
			super.collision(e);
			nbCollisionSimple++;
		}

		@Override
		public void collision(List<Entity> entities) {
			super.collision(entities);
			nbCollisionListe++;
		}

		void finirActionPourTest() {
			notifyDone();
		}
	}


	@Test
	@Order(1)
	@DisplayName("01 - une entité ajoutée est installée dans la bonne cellule")
	void installationDansLaGrille() {
		System.out.println("Entity/01 : installation dans Model + Cell");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "joueur", 2, 3);

		assertTrue(e.installed());
		assertEquals(2, e.position().x());
		assertEquals(3, e.position().y());
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(2, 3)).contains(e));
		assertEquals(1, e.occupied().size());
	}

	@Test
	@Order(2)
	@DisplayName("02 - déplacer une entité synchronise position, centre et cellule")
	void deplacementSynchroniseTout() {
		System.out.println("Entity/02 : place/translate via Model");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);

		e.translate(game.getGrid().new Vector(2, 3));

		assertEquals(3, e.position().x());
		assertEquals(4, e.position().y());
		assertEquals((3 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(3, 4)).contains(e));
		assertFalse(game.getGrid().cellAt(game.getGrid().new Position(1, 1)).contains(e));
	}

	@Test
	@Order(3)
	@DisplayName("03 - orientation est toujours normalisée")
	void orientationNormalisee() {
		System.out.println("Entity/03 : orientation normalisée");
		EntiteTest e = OutilsMoteur.ajoute(new JeuTest().model(), "e", 0, 0);

		e.turn(-90);
		assertEquals(270, e.orientation());
		e.setOrientation(450);
		assertEquals(90, e.orientation());
	}

	@Test
	@Order(4)
	@DisplayName("04 - une entité morte coupe sa vitesse et sera nettoyée au tick")
	void mortEtNettoyage() {
		System.out.println("Entity/04 : death + cleanupDeadEntities");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 0, 0);
		e.setLinearSpeed(model.isu().new Vector(10.0, 0.0));

		e.death();
		assertFalse(e.alive());
		assertFalse(e.isMoving());

		model.tick(1L);
		assertFalse(model.entities().contains(e));
		assertFalse(e.installed());
	}

	@Test
	@Order(5)
	@DisplayName("05 - intersects délègue au Bounding de chaque entité")
	void collisionEntreEntites() {
		System.out.println("Entity/05 : collision par Bounding");
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest a = OutilsMoteur.ajoute(model, "a", 2, 2);
		EntiteTest b = OutilsMoteur.ajoute(model, "b", 2, 2);
		EntiteTest c = OutilsMoteur.ajoute(model, "c", 8, 6);

		assertTrue(a.intersects(b));
		assertFalse(a.intersects(c));
	}
	@Test
	@Order(4)
	@DisplayName("04 - Entity couvre getters, vitesses, stunt, notifications, show et death")
	void entityPlusComplete() {
		Game game = new JeuTest();
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		assertSame(model, e.model());
		assertEquals("e", e.name());
		assertNotNull(e.bounding());
		assertTrue(e.alive());

		e.setZOrder(4);
		e.setSOrder(7);
		assertEquals(4, e.zOrder());
		assertEquals(7, e.sOrder());

		e.setLinearSpeed(model.isu().new Vector(2.0, 0.0));
		e.setAngularSpeed(90.0);
		assertTrue(e.isMoving());
		assertEquals(2.0, e.linearSpeed().x(), EPS);
		assertEquals(90.0, e.angularSpeed(), EPS);

		StuntTemoin stunt = new StuntTemoin(model, e);
		e.setStunt(stunt);
		e.setStunt(stunt);
		assertSame(stunt, e.stunt());

		e.setCoord(model.isu().new Coord(15.0, 15.0));
		e.setOrientation(90);
		e.translate(model.grid().new Vector(1, 0));

		assertTrue(stunt.nbCoord > 0);
		assertTrue(stunt.nbOrientation > 0);
		assertTrue(stunt.nbCellule > 0);

		e.collision(e);
		e.collision(List.of(e));
		assertEquals(1, stunt.nbCollisionSimple);
		assertEquals(1, stunt.nbCollisionListe);

		((Entity) e).commit();
		assertEquals(1, stunt.nbCommit);

		e.moveEast(1.0);
		e.moveWest(1.0);
		e.moveSouth(1);
		e.moveNorth(1);

		assertNotNull(e.size());
		assertNotNull(e.step());
		assertNotNull(e.center());
		assertNotNull(e.position());
		assertNotNull(e.occupied());
		assertFalse(e.blocks(e));

		assertTrue(e.debugLabel().contains("IDLE"));

		e.setLinearSpeed(model.isu().new Vector(1.0, 0.0));
		assertTrue(e.debugLabel().contains("MOVING"));

		e.show(sortieMuette());

		e.removeStunt();
		assertNull(e.stunt());
		assertFalse(e.isMoving());

		e.removeStunt();

		e.death();
		e.death();
		assertFalse(e.alive());
	}

}
