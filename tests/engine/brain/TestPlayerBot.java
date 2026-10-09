package engine.brain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import engine.model.Model;
import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("PlayerBot - commandes du joueur")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestPlayerBot {

	@Test
	@Order(1)
	@DisplayName("01 - wish tente immédiatement de marcher")
	void wishImmediat() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, player);

		player.setStunt(stunt);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		bot.wish(5);

		assertNull(bot.wishedDirection());
		assertEquals(Integer.valueOf(0), bot.currentDirection());
		assertEquals(0, stunt.derniereDirection());
	}

	@Test
	@Order(2)
	@DisplayName("02 - une direction refusée reste en attente")
	void directionEnAttente() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, player);

		stunt.accepte(false);
		player.setStunt(stunt);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		bot.wish(90);

		assertEquals(Integer.valueOf(90), bot.wishedDirection());
		assertNull(bot.currentDirection());
	}

	@Test
	@Order(3)
	@DisplayName("03 - stopWishing remet les directions à zéro")
	void stopWishing() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, player);

		player.setStunt(stunt);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		bot.wish(0);
		bot.stopWishing();

		assertNull(bot.currentDirection());
		assertNull(bot.wishedDirection());
	}

	@Test
	@Order(4)
	@DisplayName("04 - PlayerBot normalise les directions quelconques")
	void playerNormalizeDirections() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		player.setStunt(stunt);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		bot.wish(10);
		assertEquals(Integer.valueOf(0), bot.currentDirection());

		bot.wish(80);
		assertEquals(Integer.valueOf(90), bot.currentDirection());

		bot.wish(170);
		assertEquals(Integer.valueOf(180), bot.currentDirection());

		bot.wish(260);
		assertEquals(Integer.valueOf(270), bot.currentDirection());

		bot.wish(-20);
		assertEquals(Integer.valueOf(0), bot.currentDirection());
	}

	@Test
	@Order(5)
	@DisplayName("05 - PlayerBot garde une direction souhaitée si elle est refusée")
	void playerDirectionEnAttente() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		player.setStunt(stunt);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		stunt.accepteSeulement(0);

		bot.wish(0);

		assertEquals(Integer.valueOf(0), bot.currentDirection());
		assertNull(bot.wishedDirection());

		stunt.refuseTout();

		bot.wish(90);

		assertEquals(Integer.valueOf(90), bot.wishedDirection());
		assertEquals(Integer.valueOf(0), bot.currentDirection());

		stunt.accepteSeulement(0);
		bot.think();

		assertEquals(Integer.valueOf(90), bot.wishedDirection());
		assertEquals(Integer.valueOf(0), bot.currentDirection());
	}

	@Test
	@Order(6)
	@DisplayName("06 - done et collision relancent la réflexion du PlayerBot")
	void playerDoneCollisionStop() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		player.setStunt(stunt);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		bot.wish(0);

		assertEquals(Integer.valueOf(0), bot.currentDirection());

		bot.done();
		bot.collision(player);
		bot.collision(List.of(player));

		assertTrue(stunt.nbCommandes() >= 3);

		bot.stopWishing();

		assertNull(bot.wishedDirection());
		assertNull(bot.currentDirection());
	}
}