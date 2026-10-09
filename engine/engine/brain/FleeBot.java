package engine.brain;

import java.util.ArrayList;
import java.util.List;

import engine.model.Entity;
import engine.model.ISU;
import engine.model.Stunt;

/*
 * Bot qui éloigne son entité de l'ennemi vivant
 * le plus proche.
 *
 * Le Bot choisit la direction.
 * Le Stunt exécute réellement le déplacement.
 */
public final class FleeBot extends Bot {

	private final List<Entity> enemies;

	/*
	 * Dernière direction choisie.
	 */
	private Integer lastDirection;

	/*
	 * Constructeur pratique pour un seul ennemi.
	 */
	public FleeBot(Brain brain, Stunt stunt, Entity enemy) {
		super(brain, stunt);

		this.enemies = new ArrayList<Entity>();

		addEnemy(enemy);
	}

	/*
	 * Constructeur pour pouvoir fuir l'ennemi le plus proche parmi
	 * plusieurs entités.
	 */
	public FleeBot(Brain brain, Stunt stunt, List<? extends Entity> enemies) {
		super(brain, stunt);

		this.enemies = new ArrayList<Entity>();

		for (Entity enemy : enemies) {
			addEnemy(enemy);
		}
	}

	public void addEnemy(Entity enemy) {
		assert enemy != this.stunt.entity();
		assert enemy.model() == this.stunt.entity().model();

		if (!this.enemies.contains(enemy)) {
			this.enemies.add(enemy);
		}
	}

	public void removeEnemy(Entity enemy) {

		this.enemies.remove(enemy);
	}

	public List<Entity> enemies() {
		return new ArrayList<Entity>(this.enemies);
	}

	public Entity closestEnemy() {
		Entity self = this.stunt.entity();

		Entity closest = null;

		double closestDistance = Double.POSITIVE_INFINITY;

		for (Entity enemy : this.enemies) {
			if (enemy == null || enemy == self || !enemy.alive() || !enemy.installed()) {
				continue;
			}

			double distance = self.distanceCenterToCenter(enemy);

			if (distance < closestDistance) {
				closestDistance = distance;
				closest = enemy;
			}
		}

		return closest;
	}

	@Override
	public void think() {
		if (!valid()) {
			return;
		}

		/*
		 * Une action est déjà en cours. On attend done() ou collision().
		 */
		if (this.stunt.entity().isMoving()) {
			return;
		}

		Entity closest = closestEnemy();

		if (closest == null) {
			return;
		}

		walkAwayFrom(closest);
	}

	@Override
	public void done() {
		think();
	}

	@Override
	public void collision(Entity entity) {
		think();
	}

	@Override
	public void collision(List<Entity> entities) {
		think();
	}

	private void walkAwayFrom(Entity enemy) {
		Entity self = this.stunt.entity();

		/*
		 * Vecteur torique le plus court allant de self vers enemy.
		 */
		ISU.Vector towardEnemy = self.center().mkVectorToward(enemy.center());

		double dx = towardEnemy.x();

		double dy = towardEnemy.y();

		/*
		 * Si l'ennemi est à droite, on essaie de partir à gauche.
		 */
		int horizontalAway = dx >= 0.0 ? 180 : 0;

		/*
		 * Si l'ennemi est en dessous, on essaie de partir vers le haut.
		 */
		int verticalAway = dy >= 0.0 ? 270 : 90;

		int[] directions;

		/*
		 * On privilégie l'axe sur lequel la distance est la plus importante.
		 */
		if (Math.abs(dx) >= Math.abs(dy)) {
			directions = new int[] { horizontalAway, verticalAway, opposite(verticalAway), opposite(horizontalAway) };
		} else {
			directions = new int[] { verticalAway, horizontalAway, opposite(horizontalAway), opposite(verticalAway) };
		}

		/*
		 * On évite d'abord un demi-tour inutile.
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
		 * Si toutes les autres directions sont bloquées, le demi-tour reste autorisé.
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