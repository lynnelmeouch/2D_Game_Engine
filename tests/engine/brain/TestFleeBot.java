package engine.brain;

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
import java.util.ArrayList;
import java.util.List;
import engine.model.Model;
import engine.model.Entity;

@DisplayName("FleeBot - fuir l'ennemi le plus proche")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestFleeBot {

	@Test
	@Order(1)
	@DisplayName("01 - closestEnemy ignore les ennemis morts")
	void ennemiLePlusProche() {
		System.out.println("FleeBot/01 : recherche du plus proche ennemi vivant");
		Brain brain = new Brain();
		var model = new JeuTest().model();
		EntiteTest self = OutilsMoteur.ajoute(model, "self", 5, 5);
		EntiteTest deadNear = OutilsMoteur.ajoute(model, "dead", 5, 4);
		EntiteTest aliveFar = OutilsMoteur.ajoute(model, "alive", 8, 5);
		deadNear.death();
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, self);
		self.setStunt(stunt);
		FleeBot bot = new FleeBot(brain, stunt, Arrays.asList(deadNear, aliveFar));

		assertSame(aliveFar, bot.closestEnemy());
	}

	@Test
	@Order(2)
	@DisplayName("02 - si l'ennemi est à droite, le bot demande Ouest")
	void fuitHorizontalement() {
		System.out.println("FleeBot/02 : ennemi à droite => Ouest");
		Brain brain = new Brain();
		var model = new JeuTest().model();
		EntiteTest self = OutilsMoteur.ajoute(model, "self", 5, 5);
		EntiteTest enemy = OutilsMoteur.ajoute(model, "enemy", 7, 5);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, self);
		self.setStunt(stunt);

		FleeBot bot = new FleeBot(brain, stunt, enemy);
		bot.think();

		assertEquals(180, stunt.derniereDirection());
	}

	@Test
	@Order(3)
	@DisplayName("03 - addEnemy ne duplique pas la liste")
	void ajoutSansDoublon() {
		System.out.println("FleeBot/03 : liste d'ennemis encapsulée");
		Brain brain = new Brain();
		var model = new JeuTest().model();
		EntiteTest self = OutilsMoteur.ajoute(model, "self", 5, 5);
		EntiteTest enemy = OutilsMoteur.ajoute(model, "enemy", 7, 5);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, self);
		self.setStunt(stunt);

		FleeBot bot = new FleeBot(brain, stunt, enemy);
		bot.addEnemy(enemy);
		bot.enemies().clear();

		assertEquals(1, bot.enemies().size());
	}
	@Test
	@Order(7)
	@DisplayName("07 - FleeBot fuit horizontalement quand dx est dominant")
	void fleeHorizontal() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 5, 4);
		EntiteTest enemy = OutilsMoteur.ajoute(model, "enemy", 7, 4);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		FleeBot bot = new FleeBot(new Brain(), stunt, enemy);

		bot.think();

		assertEquals(1, stunt.nbCommandes());
		assertEquals(180, stunt.derniereDirection());
		assertSame(enemy, bot.closestEnemy());
	}

	@Test
	@Order(8)
	@DisplayName("08 - FleeBot fuit verticalement quand dy est dominant")
	void fleeVertical() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 5, 4);
		EntiteTest enemy = OutilsMoteur.ajoute(model, "enemy", 5, 6);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		FleeBot bot = new FleeBot(new Brain(), stunt, enemy);

		bot.think();

		assertEquals(1, stunt.nbCommandes());
		assertEquals(270, stunt.derniereDirection());
	}

	@Test
	@Order(9)
	@DisplayName("09 - FleeBot choisit l'ennemi vivant et installé le plus proche")
	void fleeClosestEnemy() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 5, 4);
		EntiteTest far = OutilsMoteur.ajoute(model, "far", 9, 4);
		EntiteTest close = OutilsMoteur.ajoute(model, "close", 6, 4);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		FleeBot bot = new FleeBot(new Brain(), stunt, List.of(far, close));

		assertSame(close, bot.closestEnemy());

		close.death();

		assertSame(far, bot.closestEnemy());

		model.remove(far);

		assertNull(bot.closestEnemy());
	}

	@Test
	@Order(10)
	@DisplayName("10 - FleeBot add/remove enemy et évite les doublons")
	void fleeAddRemoveEnemies() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 5, 4);
		EntiteTest enemy = OutilsMoteur.ajoute(model, "enemy", 6, 4);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		FleeBot bot = new FleeBot(new Brain(), stunt, new ArrayList<Entity>());

		assertTrue(bot.enemies().isEmpty());

		bot.addEnemy(enemy);
		bot.addEnemy(enemy);

		assertEquals(1, bot.enemies().size());

		bot.removeEnemy(enemy);

		assertTrue(bot.enemies().isEmpty());

		bot.think();

		assertEquals(0, stunt.nbCommandes());
	}

	@Test
	@Order(11)
	@DisplayName("11 - FleeBot évite le demi-tour si possible")
	void fleeEviteDemiTour() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 5, 4);
		EntiteTest enemy = OutilsMoteur.ajoute(model, "enemy", 7, 4);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		FleeBot bot = new FleeBot(new Brain(), stunt, enemy);

		bot.think();
		assertEquals(180, stunt.derniereDirection());

		enemy.setPosition(model.grid().new Position(3, 4));

		stunt.clear();
		bot.done();

		assertEquals(270, stunt.derniereDirection());
		assertFalse(stunt.directions().contains(Integer.valueOf(0)));
	}

	@Test
	@Order(12)
	@DisplayName("12 - FleeBot done/collision relancent think")
	void fleeDoneEtCollision() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 5, 4);
		EntiteTest enemy = OutilsMoteur.ajoute(model, "enemy", 7, 4);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		FleeBot bot = new FleeBot(new Brain(), stunt, enemy);

		bot.done();
		bot.collision(enemy);
		bot.collision(List.of(enemy));

		assertEquals(3, stunt.nbCommandes());
	}

}
