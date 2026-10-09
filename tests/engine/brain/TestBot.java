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

@DisplayName("Bot - lien avec son Stunt")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestBot {

	@Test
	@Order(1)
	@DisplayName("01 - le constructeur lie le Bot au Stunt")
	void constructeurLieLeStunt() {
		System.out.println("Bot/01 : stunt.setBot(this)");
		Brain brain = new Brain();
		EntiteTest e = OutilsMoteur.ajoute(new JeuTest().model(), "e", 0, 0);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(e.model(), e);
		e.setStunt(stunt);
		OutilsBrain.BotMemoire bot = new OutilsBrain.BotMemoire(brain, stunt);

		assertSame(brain, bot.brain());
		assertSame(stunt, bot.stunt());
		assertSame(bot, stunt.bot());
		assertTrue(bot.valid());
	}

	@Test
	@Order(2)
	@DisplayName("02 - un Bot devient invalide si son Stunt est invalidé")
	void validiteLieeAuStunt() {
		System.out.println("Bot/02 : invalidation du stunt");
		Brain brain = new Brain();
		EntiteTest e = OutilsMoteur.ajoute(new JeuTest().model(), "e", 0, 0);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(e.model(), e);
		e.setStunt(stunt);
		OutilsBrain.BotMemoire bot = new OutilsBrain.BotMemoire(brain, stunt);

		stunt.invalidate();

		assertFalse(bot.valid());
	}
}
