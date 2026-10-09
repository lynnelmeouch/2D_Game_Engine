package engine.brain;

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
import engine.model.Model;

@DisplayName("Brain - liste et cycle des bots")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestBrain {

	@Test
	@Order(1)
	@DisplayName("01 - add évite les doublons")
	void addSansDoublon() {
		System.out.println("Brain/01 : ajout de bot sans doublon");
		Brain brain = new Brain();
		EntiteTest e = OutilsMoteur.ajoute(new JeuTest().model(), "e", 0, 0);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(e.model(), e);
		e.setStunt(stunt);
		OutilsBrain.BotMemoire bot = new OutilsBrain.BotMemoire(brain, stunt);

		brain.add(bot);
		brain.add(bot);

		assertEquals(1, brain.bots().size());
	}

	@Test
	@Order(2)
	@DisplayName("02 - think appelle seulement les bots valides")
	void thinkNettoieLesBotsInvalides() {
		System.out.println("Brain/02 : suppression des bots invalides");
		Brain brain = new Brain();
		EntiteTest e = OutilsMoteur.ajoute(new JeuTest().model(), "e", 0, 0);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(e.model(), e);
		e.setStunt(stunt);
		OutilsBrain.BotMemoire bot = new OutilsBrain.BotMemoire(brain, stunt);
		brain.add(bot);

		stunt.invalidate();
		brain.think();

		assertEquals(0, bot.think);
		assertTrue(brain.bots().isEmpty());
	}

	@Test
	@Order(3)
	@DisplayName("03 - removeBotsOf retire tous les bots d'une entité")
	void removeBotsOf() {
		System.out.println("Brain/03 : retrait par entité");
		Brain brain = new Brain();
		EntiteTest e = OutilsMoteur.ajoute(new JeuTest().model(), "e", 0, 0);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(e.model(), e);
		e.setStunt(stunt);
		OutilsBrain.BotMemoire bot = new OutilsBrain.BotMemoire(brain, stunt);
		brain.add(bot);

		brain.removeBotsOf(e);

		assertTrue(brain.bots().isEmpty());
	}
	@Test
	@Order(18)
	@DisplayName("18 - Brain supprime les bots invalides")
	void brainSupprimeBotsInvalides() {
		Model model = new JeuTest().model();

		EntiteTest entity = OutilsMoteur.ajoute(model, "entity", 1, 1);
		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, entity);

		Brain brain = new Brain();
		PlayerBot bot = new PlayerBot(brain, stunt);

		brain.add(bot);
		brain.add(bot);

		assertEquals(1, brain.bots().size());

		stunt.invalidate();

		brain.think();

		assertTrue(brain.bots().isEmpty());
	}

}
