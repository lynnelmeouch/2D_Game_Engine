package engine.controller;

import oop.graphics.Canvas;

/*
 * Contrôleur souris générique.
 *
 * Il traduit les événements bruts de la souris en commandes :
 * - commencer une rotation vers la gauche ;
 * - arrêter la rotation vers la gauche ;
 * - commencer une rotation vers la droite ;
 * - arrêter la rotation vers la droite.
 *
 */
public final class MouseController extends Controller {

	/*
	 * Lorsque les deux boutons sont pressés, seul celui qui a été pressé en premier
	 * agit.
	 */
	public enum State {
		NO_ROT, LEFT_ROT, RIGHT_ROT, LEFT_ROT_RP, RIGHT_ROT_LP
	}

	private final RotationCommands commands;

	private State state;

	public MouseController(RotationCommands commands) {

		this.commands = commands;
		this.state = State.NO_ROT;
	}

	public State state() {
		if (this.state == State.LEFT_ROT_RP) {
			return State.LEFT_ROT;
		}

		if (this.state == State.RIGHT_ROT_LP) {
			return State.RIGHT_ROT;
		}

		return this.state;
	}

	@Override
	protected void mouseMoved(Canvas canvas, int x, int y) {
	}

	@Override
	protected void mousePressed(Canvas canvas, int buttonNumber, int x, int y) {
		switch (this.state) {
		case NO_ROT:
			handlePressedFromNoRotation(buttonNumber);
			break;

		case LEFT_ROT:
			if (buttonNumber == 3) {
				/*
				 * Le bouton droit est pressé en second. La rotation gauche reste prioritaire.
				 */
				this.state = State.LEFT_ROT_RP;
			}
			break;

		case RIGHT_ROT:
			if (buttonNumber == 1) {
				/*
				 * Le bouton gauche est pressé en second. La rotation droite reste prioritaire.
				 */
				this.state = State.RIGHT_ROT_LP;
			}
			break;

		case LEFT_ROT_RP:
		case RIGHT_ROT_LP:
			/*
			 * Les deux boutons sont déjà considérés comme pressés.
			 */
			break;

		default:
			throw new AssertionError("Unknown mouse-controller state");
		}
	}

	private void handlePressedFromNoRotation(int buttonNumber) {
		if (buttonNumber == 1) {
			this.state = State.LEFT_ROT;

			this.commands.startTurnLeft();
			return;
		}

		if (buttonNumber == 3) {
			this.state = State.RIGHT_ROT;

			this.commands.startTurnRight();
		}
	}

	@Override
	protected void mouseReleased(Canvas canvas, int x, int y, int buttonNumber) {
		switch (this.state) {
		case NO_ROT:
			/*
			 * Événement released sans pressed connu.
			 */
			break;

		case LEFT_ROT:
			if (buttonNumber == 1) {
				this.commands.stopTurnLeft();
				this.state = State.NO_ROT;
			}
			break;

		case RIGHT_ROT:
			if (buttonNumber == 3) {
				this.commands.stopTurnRight();
				this.state = State.NO_ROT;
			}
			break;

		case LEFT_ROT_RP:
			releasedFromLeftPriority(buttonNumber);
			break;

		case RIGHT_ROT_LP:
			releasedFromRightPriority(buttonNumber);
			break;

		default:
			throw new AssertionError("Unknown mouse-controller state");
		}
	}

	/*
	 * Le bouton gauche avait été pressé en premier.
	 */
	private void releasedFromLeftPriority(int buttonNumber) {
		if (buttonNumber == 3) {
			/*
			 * Le bouton secondaire est relâché. La rotation gauche continue.
			 */
			this.state = State.LEFT_ROT;
			return;
		}

		if (buttonNumber == 1) {
			/*
			 * Le bouton prioritaire est relâché. Le bouton droit avait été ignoré : aucune
			 * nouvelle rotation ne démarre.
			 */
			this.commands.stopTurnLeft();
			this.state = State.NO_ROT;
		}
	}

	/*
	 * Le bouton droit avait été pressé en premier.
	 */
	private void releasedFromRightPriority(int buttonNumber) {
		if (buttonNumber == 1) {
			/*
			 * Le bouton secondaire est relâché. La rotation droite continue.
			 */
			this.state = State.RIGHT_ROT;
			return;
		}

		if (buttonNumber == 3) {
			this.commands.stopTurnRight();
			this.state = State.NO_ROT;
		}
	}
}