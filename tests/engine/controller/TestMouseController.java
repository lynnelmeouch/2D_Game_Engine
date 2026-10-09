package engine.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("MouseController - FSM des deux boutons")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestMouseController {

	static class CommandesMemoire implements RotationCommands {

		int leftStart;
		int leftStop;
		int rightStart;
		int rightStop;

		@Override
		public void startTurnLeft() {
			this.leftStart++;
		}

		@Override
		public void stopTurnLeft() {
			this.leftStop++;
		}

		@Override
		public void startTurnRight() {
			this.rightStart++;
		}

		@Override
		public void stopTurnRight() {
			this.rightStop++;
		}
	}

	@Test
	@Order(1)
	@DisplayName("01 - bouton gauche démarre puis arrête la rotation gauche")
	void rotationGaucheSimple() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);

		assertEquals(MouseController.State.LEFT_ROT, controller.state());
		assertEquals(1, commands.leftStart);

		controller.released(null, 0, 0, 1);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);
	}

	@Test
	@Order(2)
	@DisplayName("02 - le premier bouton reste prioritaire quand les deux sont pressés")
	void prioritePremierBouton() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		controller.pressed(null, 3, 0, 0);

		assertEquals(MouseController.State.LEFT_ROT, controller.state());
		assertEquals(1, commands.leftStart);
		assertEquals(0, commands.rightStart);

		controller.released(null, 0, 0, 3);

		assertEquals(MouseController.State.LEFT_ROT, controller.state());

		controller.released(null, 0, 0, 1);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);
	}

	@Test
	@Order(3)
	@DisplayName("03 - clic droit simple")
	void sourisDroiteSimple() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 3, 0, 0);

		assertEquals(MouseController.State.RIGHT_ROT, controller.state());
		assertEquals(1, commands.rightStart);
		assertEquals(0, commands.leftStart);

		controller.released(null, 0, 0, 3);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.rightStop);
	}

	@Test
	@Order(4)
	@DisplayName("04 - priorité droite puis gauche")
	void sourisPrioriteDroitePuisGauche() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 3, 0, 0);
		controller.pressed(null, 1, 0, 0);

		assertEquals(MouseController.State.RIGHT_ROT, controller.state());
		assertEquals(1, commands.rightStart);
		assertEquals(0, commands.leftStart);

		controller.released(null, 0, 0, 1);

		assertEquals(MouseController.State.RIGHT_ROT, controller.state());
		assertEquals(0, commands.rightStop);

		controller.released(null, 0, 0, 3);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.rightStop);
	}

	@Test
	@Order(5)
	@DisplayName("05 - priorité gauche puis droite, relâchement prioritaire d'abord")
	void sourisPrioriteGaucheRelacheeAvantDroite() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		controller.pressed(null, 3, 0, 0);

		assertEquals(MouseController.State.LEFT_ROT, controller.state());
		assertEquals(1, commands.leftStart);
		assertEquals(0, commands.rightStart);

		controller.released(null, 0, 0, 1);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);
		assertEquals(0, commands.rightStart);

		controller.released(null, 0, 0, 3);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);
		assertEquals(0, commands.rightStop);
	}

	@Test
	@Order(6)
	@DisplayName("06 - boutons inconnus et moved sont ignorés")
	void sourisIgnoreBoutonsInconnus() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.moved(null, 10, 20);
		controller.pressed(null, 2, 0, 0);
		controller.released(null, 0, 0, 2);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(0, commands.leftStart);
		assertEquals(0, commands.leftStop);
		assertEquals(0, commands.rightStart);
		assertEquals(0, commands.rightStop);
	}

	@Test
	@Order(7)
	@DisplayName("07 - relâchements non cohérents ignorés")
	void sourisRelachementsIncoherents() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		controller.released(null, 0, 0, 3);

		assertEquals(MouseController.State.LEFT_ROT, controller.state());
		assertEquals(0, commands.leftStop);

		controller.released(null, 0, 0, 1);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);

		controller.pressed(null, 3, 0, 0);
		controller.released(null, 0, 0, 1);

		assertEquals(MouseController.State.RIGHT_ROT, controller.state());
		assertEquals(0, commands.rightStop);

		controller.released(null, 0, 0, 3);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.rightStop);
	}

	@Test
	@Order(8)
	@DisplayName("08 - pressions ignorées quand les deux boutons sont déjà pressés")
	void sourisDeuxBoutonsDejaPresses() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		controller.pressed(null, 3, 0, 0);
		controller.pressed(null, 1, 0, 0);
		controller.pressed(null, 3, 0, 0);

		assertEquals(MouseController.State.LEFT_ROT, controller.state());
		assertEquals(1, commands.leftStart);
		assertEquals(0, commands.rightStart);

		controller.released(null, 0, 0, 3);
		controller.released(null, 0, 0, 1);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);
	}
}