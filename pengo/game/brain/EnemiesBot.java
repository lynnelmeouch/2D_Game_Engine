package game.brain;

import java.util.List;

import engine.brain.Bot;
import engine.brain.Brain;
import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.ISU;
import game.model.entity.Enemies;
import game.model.player.Pengo;

// Bot d'un ennemi : il tente de réduire la distance avec Pengo.
//Inutile quand comportement gal
public final class EnemiesBot extends Bot {

	private final Enemies enemy;
	private final Pengo target;
	private Integer lastDirection;

	public EnemiesBot(Brain brain, BasicStunt stunt, Enemies enemy, Pengo target) {
		super(brain, stunt);

		assert enemy != null;
		assert target != null;
		assert stunt.entity() == enemy;
		assert enemy.model() == target.model();

		this.enemy = enemy;
		this.target = target;
		this.lastDirection = null;
	}

	@Override
	public void think() {
		if (!valid() || this.enemy.isFrozen() || this.enemy.isCrushed() || this.enemy.isMoving()) {
			return;
		}

		ISU.Vector toward = this.enemy.center().mkVectorToward(this.target.center());

		int horizontal = toward.x() >= 0.0 ? 0 : 180;
		int vertical = toward.y() >= 0.0 ? 90 : 270;

		int[] directions;
		if (Math.abs(toward.x()) >= Math.abs(toward.y())) {
			directions = new int[] { horizontal, vertical, opposite(vertical), opposite(horizontal) };
		} else {
			directions = new int[] { vertical, horizontal, opposite(horizontal), opposite(vertical) };
		}

		tryDirections(directions, true);
	}

	private void tryDirections(int[] directions, boolean avoidImmediateReverse) {
		Integer reverse = this.lastDirection == null ? null : opposite(this.lastDirection);

		for (int direction : directions) {
			if (avoidImmediateReverse && reverse != null && direction == reverse) {
				continue;
			}

			if (this.stunt.walk(direction)) {
				this.lastDirection = direction;
				return;
			}
		}

		if (avoidImmediateReverse) {
			tryDirections(directions, false);
		}
	}

	private int opposite(int direction) {
		return (direction + 180) % 360;
	}

	@Override
	public void done() {
		think();
	}

	@Override
	public void collision(Entity entity) {
		assert entity != null;
		think();
	}

	@Override
	public void collision(List<Entity> entities) {
		assert entities != null;
		think();
	}
}