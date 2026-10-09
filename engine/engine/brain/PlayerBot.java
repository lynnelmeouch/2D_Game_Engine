package engine.brain;

import java.util.List;

import engine.controller.RotationCommands;
import engine.model.Entity;
import engine.model.RotationStunt;
import engine.model.Stunt;

/*
 * Bot contrôlé par le joueur.
 *
 * Il mémorise :
 * - la direction actuellement suivie ;
 * - la dernière direction demandée par le joueur.
 *
 * Si la direction souhaitée n'est pas immédiatement possible,
 * elle reste en attente et sera réessayée lors des prochains think().
 */
public final class PlayerBot extends Bot implements RotationCommands {

	public enum State {
		IDLE, WALKING, WAITING_FOR_DIRECTION
	}

	/*
	 * Dernière direction demandée, mais pas encore acceptée. 
	 */
	private Integer wishedDirection;

	/*
	 * Direction actuellement suivie.
	 */
	private Integer currentDirection;

	private State state;

	/*
	 * Stunt permettant de contrôler une rotation continue avec la
	 * souris.
	 */
	private final RotationStunt rotationStunt;

	/*
	 * Constructeur sans gestion de rotation.
	 */
	public PlayerBot(Brain brain, Stunt walkingStunt) {
		this(brain, walkingStunt, null);
	}

	/*
	 * Constructeur avec gestion de la rotation.
	 */
	public PlayerBot(Brain brain, Stunt walkingStunt, RotationStunt rotationStunt) {
		super(brain, walkingStunt);

		if (rotationStunt != null) {
			assert rotationStunt.model() == walkingStunt.model();

			assert rotationStunt.entity() == walkingStunt.entity();

		}

		this.rotationStunt = rotationStunt;

		this.state = State.IDLE;
	}

	public State state() {
		return this.state;
	}

	public Integer wishedDirection() {
		return this.wishedDirection;
	}

	public Integer currentDirection() {
		return this.currentDirection;
	}

	/*
	 * Reçoit une commande du contrôleur du jeu.
	 *
	 * La nouvelle commande remplace toujours la précédente commande en attente.
	 */
	public void wish(int direction) {
		this.wishedDirection = normalizeCardinal(direction);

		/*
		 * Essai immédiat pour éviter un tick de retard entre l'appui clavier et la
		 * réaction du joueur.
		 */
		tryToMove();
	}

	public void stopWishing() {
		this.wishedDirection = null;
		this.currentDirection = null;
		this.state = State.IDLE;
	}

	@Override
	public void think() {
		tryToMove();
	}

	/*
	 * Le Stunt vient de terminer son déplacement vers une cellule.
	 */
	@Override
	public void done() {
		this.state = State.IDLE;

		tryToMove();
	}

	@Override
	public void collision(Entity entity) {
		tryToMove();
	}

	@Override
	public void collision(List<Entity> entities) {
		tryToMove();
	}

	/*
	 * Essaie d'abord la dernière direction demandée.
	 *
	 * Si elle n'est pas encore possible, le Bot continue dans sa direction
	 * actuelle.
	 */
	private void tryToMove() {
		/*
		 * La nouvelle commande du joueur est prioritaire.
		 */
		if (this.wishedDirection != null) {
			boolean accepted = this.stunt.walk(this.wishedDirection);

			if (accepted) {
				this.currentDirection = this.wishedDirection;

				this.wishedDirection = null;
				this.state = State.WALKING;

				return;
			}

			/*
			 * La commande impossible reste mémorisée. Elle sera réessayée au prochain
			 * think(), done() ou collision().
			 */
			this.state = State.WAITING_FOR_DIRECTION;
		}

		/*
		 * Le virage souhaité n'est pas encore possible : on essaie de poursuivre la
		 * direction actuelle.
		 */
		if (this.currentDirection != null) {
			boolean continued = this.stunt.walk(this.currentDirection);

			if (continued) {
				/*
				 * Si une direction souhaitée existe toujours, l'état reste
				 * WAITING_FOR_DIRECTION.
				 */
				if (this.wishedDirection == null) {
					this.state = State.WALKING;
				}

				return;
			}
		}

		/*
		 * Aucun mouvement n'a pu commencer.
		 */
		if (this.wishedDirection == null) {
			this.state = State.IDLE;
		}
	}

	private int normalizeCardinal(int direction) {
		int normalized = ((direction % 360) + 360) % 360;

		if (normalized < 45 || normalized >= 315) {
			return 0;
		}

		if (normalized < 135) {
			return 90;
		}

		if (normalized < 225) {
			return 180;
		}

		return 270;
	}

	public boolean canRotate() {
		return this.rotationStunt != null;
	}

	private RotationStunt requireRotationStunt() {
		if (this.rotationStunt == null) {
			throw new IllegalStateException("This PlayerBot has no RotationStunt");
		}

		return this.rotationStunt;
	}

	@Override
	public void startTurnLeft() {
		requireRotationStunt().startTurnLeft();
	}

	@Override
	public void stopTurnLeft() {
		requireRotationStunt().stopTurnLeft();
	}

	@Override
	public void startTurnRight() {
		requireRotationStunt().startTurnRight();
	}

	@Override
	public void stopTurnRight() {
		requireRotationStunt().stopTurnRight();
	}
}