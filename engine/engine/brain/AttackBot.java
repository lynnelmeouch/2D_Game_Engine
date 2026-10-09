package engine.brain;

import java.util.List;

import engine.model.Entity;
import engine.model.ISU;
import engine.model.Stunt;

/*
 * Bot simple qui poursuit une entité donnée.
 *
 * Le Bot choisit une direction.
 * Le Stunt exécute le déplacement.
 * Le Model applique physiquement le mouvement.
 */
public final class AttackBot extends Bot {

	private static final double EPSILON = 0.000000001;

	/*
	 * Cible poursuivie pendant toute la vie du Bot.
	 */
	private final Entity target;

	/*
	 * null signifie qu'aucun déplacement n'a encore été effectué.
	 */
	private Integer lastDirection;

	public AttackBot(Brain brain, Stunt stunt, Entity target) {
		super(brain, stunt);

		assert target != stunt.entity();

		/*
		 * L'attaquant et la cible doivent appartenir au même monde simulé.
		 */
		assert target.model() == stunt.entity().model();

		this.target = target;
	}

	public Entity target() {
		return this.target;
	}

	@Override
	public void think() {
		/*
		 * Le Bot a peut-être été remplacé ou son Stunt a été invalidé.
		 */
		if (!valid()) {
			return;
		}

		Entity attacker = this.stunt.entity();

		if (!attacker.alive() || !attacker.installed()) {
			return;
		}

		/*
		 * Une cible morte ou retirée du Model ne doit plus être poursuivie.
		 */
		if (!this.target.alive() || !this.target.installed()) {
			return;
		}

		/*
		 * Une marche est une action qui prend du temps. On attend sa fin avant d'en
		 * demander une autre.
		 */
		if (attacker.isMoving()) {
			return;
		}

		walkTowardTarget();
	}

	@Override
	public void done() {
		think();
	}

	@Override
	public void collision(Entity other) {
		think();
	}

	@Override
	public void collision(List<Entity> others) {
		think();
	}

	private void walkTowardTarget() {
		Entity self = this.stunt.entity();

		/*
		 * Vecteur torique le plus court allant de l'attaquant vers sa cible.
		 */
		ISU.Vector toward = self.center().mkVectorToward(this.target.center());

		double dx = toward.x();
		double dy = toward.y();

		/*
		 * Les deux centres sont confondus. Le contact sera traité par les collisions.
		 */
		if (Math.abs(dx) <= EPSILON && Math.abs(dy) <= EPSILON) {
			return;
		}

		int horizontal = dx >= 0.0 ? 0 : 180;

		int vertical = dy >= 0.0 ? 90 : 270;

		int[] directions;

		/*
		 * On privilégie l'axe sur lequel la cible est la plus éloignée.
		 */
		if (Math.abs(dx) >= Math.abs(dy)) {
			directions = new int[] { horizontal, vertical, opposite(vertical), opposite(horizontal) };
		} else {
			directions = new int[] { vertical, horizontal, opposite(horizontal), opposite(vertical) };
		}

		/*
		 * On évite d'abord le demi-tour, mais seulement si un déplacement précédent
		 * existe.
		 */
		if (this.lastDirection != null) {
			int reverse = opposite(this.lastDirection);

			for (int direction : directions) {
				if (direction == reverse) {
					continue;
				}

				if (this.stunt.walk(direction)) {
					this.lastDirection = direction;

					return;
				}
			}
		}

		/*
		 * Premier déplacement, ou aucune autre direction possible : toutes les
		 * directions sont autorisées.
		 */
		for (int direction : directions) {
			if (this.stunt.walk(direction)) {
				this.lastDirection = direction;

				return;
			}
		}
	}

	private int opposite(int direction) {
		switch (direction) {
		case 0:
			return 180;

		case 90:
			return 270;

		case 180:
			return 0;

		case 270:
			return 90;

		default:
			throw new AssertionError("Direction must be cardinal: " + direction);
		}
	}
}