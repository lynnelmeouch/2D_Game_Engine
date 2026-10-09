package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.brain.Brain;
import engine.brain.Bot;
import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("Stunt - contrat entre Entity et Bot")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestStunt {

	static class StuntTest extends Stunt {
		StuntTest(Model model, Entity entity) {
			super(model, entity);
		}

		@Override
		public boolean walk(int degree) {
			return valid();
		}

		void fireDone() {
			notifyDone();
		}

		void fireCollision(Entity e) {
			notifyCollision(e);
		}
	}

	static class BotTest extends Bot {
		int done;
		int collisions;

		BotTest(Brain brain, Stunt stunt) {
			super(brain, stunt);
		}

		@Override
		public void think() {
		}

		@Override
		public void done() {
			this.done++;
		}

		@Override
		public void collision(Entity entity) {
			this.collisions++;
		}

		@Override
		public void collision(List<Entity> entities) {
			this.collisions++;
		}
	}

	@Test
	@Order(1)
	@DisplayName("01 - un Stunt est valide seulement avec une entité vivante et installée")
	void validiteDuStunt() {
		System.out.println("Stunt/01 : validité liée à Entity");
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 0, 0);
		StuntTest stunt = new StuntTest(model, e);
		e.setStunt(stunt);

		assertTrue(stunt.valid());
		e.death();
		assertFalse(stunt.valid());
	}

	@Test
	@Order(2)
	@DisplayName("02 - notifyDone et notifyCollision remontent au Bot courant")
	void notificationsVersBot() {
		System.out.println("Stunt/02 : notifications vers Bot");
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 0, 0);
		EntiteTest other = OutilsMoteur.ajoute(model, "other", 1, 0);
		StuntTest stunt = new StuntTest(model, e);
		e.setStunt(stunt);
		BotTest bot = new BotTest(new Brain(), stunt);

		stunt.fireDone();
		stunt.fireCollision(other);

		assertEquals(1, bot.done);
		assertEquals(1, bot.collisions);
	}

	@Test
	@Order(3)
	@DisplayName("03 - remplacer le Stunt invalide l'ancien")
	void remplacementStunt() {
		System.out.println("Stunt/03 : un seul Stunt actif");
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 0, 0);
		StuntTest oldStunt = new StuntTest(model, e);
		StuntTest newStunt = new StuntTest(model, e);

		e.setStunt(oldStunt);
		assertTrue(oldStunt.valid());
		e.setStunt(newStunt);

		assertFalse(oldStunt.valid());
		assertTrue(newStunt.valid());
	}
}
