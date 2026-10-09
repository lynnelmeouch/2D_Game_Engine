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
import java.util.List;
import engine.model.Model;

@DisplayName("AttackBot - poursuivre une cible")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestAttackBot {

	@Test
	@Order(1)
	@DisplayName("01 - si la cible est à droite, le bot demande Est")
	void cibleADroite() {
		System.out.println("AttackBot/01 : cible à droite => Est");
		Brain brain = new Brain();
		var game = new JeuTest();
		var model = game.model();
		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 1, 1);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 4, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, attacker);
		attacker.setStunt(stunt);

		AttackBot bot = new AttackBot(brain, stunt, target);
		bot.think();

		assertEquals(0, stunt.derniereDirection());
	}

	@Test
	@Order(2)
	@DisplayName("02 - sur un tore, le bot peut choisir le bord comme plus court chemin")
	void cibleParLeBordTorique() {
		System.out.println("AttackBot/02 : de x=1 vers x=9 => Ouest sur tore");
		Brain brain = new Brain();
		var game = new JeuTest();
		var model = game.model();
		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 1, 1);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 9, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, attacker);
		attacker.setStunt(stunt);

		AttackBot bot = new AttackBot(brain, stunt, target);
		bot.think();

		assertEquals(180, stunt.derniereDirection());
	}

	@Test
	@Order(3)
	@DisplayName("03 - le bot ne pense pas si l'attaquant bouge déjà")
	void neCommandePasPendantMouvement() {
		System.out.println("AttackBot/03 : action déjà en cours");
		Brain brain = new Brain();
		var game = new JeuTest();
		var model = game.model();
		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 1, 1);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 4, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, attacker);
		attacker.setStunt(stunt);
		attacker.setLinearSpeed(model.isu().new Vector(1.0, 0.0));

		AttackBot bot = new AttackBot(brain, stunt, target);
		bot.think();

		assertTrue(stunt.directions().isEmpty());
	}
	@Test
	@Order(1)
	@DisplayName("01 - AttackBot choisit l'axe horizontal quand dx est dominant")
	void attackHorizontal() {
		Model model = new JeuTest().model();

		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 1, 1);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 4, 2);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, attacker);

		Brain brain = new Brain();
		AttackBot bot = new AttackBot(brain, stunt, target);

		brain.add(bot);
		brain.think();

		assertEquals(1, stunt.nbCommandes());
		assertEquals(0, stunt.derniereDirection());
		assertSame(target, bot.target());
	}

	@Test
	@Order(2)
	@DisplayName("02 - AttackBot choisit l'axe vertical quand dy est dominant")
	void attackVertical() {
		Model model = new JeuTest().model();

		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 1, 1);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 2, 5);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, attacker);

		Brain brain = new Brain();
		AttackBot bot = new AttackBot(brain, stunt, target);

		brain.add(bot);
		brain.think();

		assertEquals(1, stunt.nbCommandes());
		assertEquals(90, stunt.derniereDirection());
	}

	@Test
	@Order(3)
	@DisplayName("03 - AttackBot ne bouge pas si les centres sont confondus")
	void attackMemeCentre() {
		Model model = new JeuTest().model();

		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 2, 2);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 2, 2);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, attacker);

		AttackBot bot = new AttackBot(new Brain(), stunt, target);

		bot.think();

		assertEquals(0, stunt.nbCommandes());
	}

	@Test
	@Order(4)
	@DisplayName("04 - AttackBot ignore cible morte, cible retirée, attaquant en mouvement et stunt invalide")
	void attackCasInvalides() {
		Model model = new JeuTest().model();

		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 1, 1);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 4, 1);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, attacker);
		AttackBot bot = new AttackBot(new Brain(), stunt, target);

		attacker.setLinearSpeed(model.isu().new Vector(1.0, 0.0));
		bot.think();
		assertEquals(0, stunt.nbCommandes());

		attacker.setLinearSpeed(model.isu().new Vector(0.0, 0.0));
		target.death();
		bot.think();
		assertEquals(0, stunt.nbCommandes());

		EntiteTest target2 = OutilsMoteur.ajoute(model, "target2", 4, 1);
		AttackBot bot2 = new AttackBot(new Brain(), stunt, target2);

		model.remove(target2);
		bot2.think();
		assertEquals(0, stunt.nbCommandes());

		stunt.invalidate();
		bot2.think();
		assertEquals(0, stunt.nbCommandes());
	}

	@Test
	@Order(5)
	@DisplayName("05 - AttackBot évite le demi-tour si une autre direction est possible")
	void attackEviteDemiTour() {
		Model model = new JeuTest().model();

		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 5, 4);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 7, 4);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, attacker);

		AttackBot bot = new AttackBot(new Brain(), stunt, target);

		bot.think();
		assertEquals(0, stunt.derniereDirection());

		target.setPosition(model.grid().new Position(3, 4));

		stunt.clear();
		bot.done();

		assertEquals(90, stunt.derniereDirection());
		assertFalse(stunt.directions().contains(Integer.valueOf(180)));
	}

	@Test
	@Order(6)
	@DisplayName("06 - AttackBot done/collision relancent think")
	void attackDoneEtCollision() {
		Model model = new JeuTest().model();

		EntiteTest attacker = OutilsMoteur.ajoute(model, "attacker", 1, 1);
		EntiteTest target = OutilsMoteur.ajoute(model, "target", 3, 1);

		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, attacker);

		AttackBot bot = new AttackBot(new Brain(), stunt, target);

		bot.done();
		bot.collision(target);
		bot.collision(List.of(target));

		assertEquals(3, stunt.nbCommandes());
	}

}
