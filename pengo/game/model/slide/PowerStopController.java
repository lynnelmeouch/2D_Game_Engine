package game.model.slide;

import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Orientation;
import game.model.entity.Enemies;
import game.model.entity.Power;

final class PowerStopController {

	// bloc glissant controle par ce gestionnaire
	private final Glissade owner;

	// train dennemis pousse par le bloc
	private final EnemyTrain enemyTrain;

	// ennemi utilise comme reference pour larret devant power
	private Enemies smoothPowerStopEnemy;

	// position finale du bloc avant le power
	private Grid.Position smoothPowerStopBlockPosition;

	// position du power cible devant le train
	private Grid.Position smoothPowerStopEnemyPosition;

	PowerStopController(Glissade owner, EnemyTrain enemyTrain) {
		this.owner = owner;
		this.enemyTrain = enemyTrain;
	}

	// indique si un arret special devant power est en cours
	boolean active() {
		return this.smoothPowerStopEnemy != null;
	}

	// remet a zero letat darret special
	void clear() {
		this.smoothPowerStopEnemy = null;
		this.smoothPowerStopBlockPosition = null;
		this.smoothPowerStopEnemyPosition = null;
	}

	// demarre arret special devant un power
	void start(Enemies enemy, Grid.Position blockStopPosition, Grid.Position enemyStopPosition) {
		this.smoothPowerStopEnemy = enemy;
		this.smoothPowerStopBlockPosition = blockStopPosition.copy();
		this.smoothPowerStopEnemyPosition = enemyStopPosition.copy();

		this.enemyTrain.prepareCarriedEnemy(enemy);

		this.enemyTrain.removeInvalidCarriedEnemies();
	}

	// met a jour l arret special jusqu a la position finale
	void update() {
		if (this.smoothPowerStopEnemy == null || this.smoothPowerStopBlockPosition == null) {
			clear();
			this.owner.stopSliding();
			return;
		}
		this.enemyTrain.removeInvalidCarriedEnemies();
		if (this.enemyTrain.isEmpty()) {
			clear();
			this.owner.stopSliding();
			return;
		}
		this.enemyTrain.placeCarriedEnemiesAsTrain();
		if (!hasReachedSmoothPowerStopPosition()) {
			return;
		}
		Grid.Position blockStop = this.smoothPowerStopBlockPosition.copy();
		this.enemyTrain.releaseCarriedEnemiesAfterSmoothPowerStop(blockStop);
		clear();
		this.owner.stopSlidingAt(blockStop);
	}

	// stoppe le bloc si un power est directement devant lui
	boolean stopBeforePowerIfNeeded() {
		Grid.Position next = this.owner.positionInFront(1);
		if (next == null) {
			return false;
		}
		Entity obstacle = powerAt(next);
		if (obstacle == null) {
			return false;
		}
		this.owner.stopSlidingAt(this.owner.position().copy());
		return true;
	}

	// verifie si le power est juste apres le train d ennemis
	boolean isPowerAfterTrain(Entity other) {
		if (!(other instanceof Power) || this.enemyTrain.isEmpty()) {
			return false;
		}
		Grid.Position expected = this.owner.positionInFront(this.enemyTrain.frontSlot() + 1);
		return expected != null && expected.equals(other.position());
	}

	// verifie si cette entite est le power cible de l arret special
	boolean isSmoothPowerTarget(Entity other) {
		return other instanceof Power && this.smoothPowerStopEnemyPosition != null
				&& this.smoothPowerStopEnemyPosition.equals(other.position());
	}

	// calcule la position ou le bloc doit s arreter avant le power
	Grid.Position blockStopBeforePower(Grid.Position powerPosition) {
		if (powerPosition == null) {
			return this.owner.positionInFront(1);
		}
		Grid.Position result = powerPosition.copy();
		int backward = Orientation.backOf(this.owner.slideDirection());
		int distance = Math.max(1, this.enemyTrain.frontSlot());
		for (int i = 0; i < distance; i++) {
			result = this.owner.geometry().neighbor(result, backward);

			if (result == null) {
				return this.owner.positionInFront(1);
			}
		}
		return result;
	}

	// cherche un power sur une cellule donnee
	private Entity powerAt(Grid.Position position) {
		if (position == null) {
			return null;
		}
		Grid.Cell cell = this.owner.model().grid().cellAt(position);
		for (Entity other : cell.entities()) {
			if (other instanceof Power && other.alive()) {
				return other;
			}
		}
		return null;
	}

	// verifie si le bloc a atteint sa position finale d arret
	private boolean hasReachedSmoothPowerStopPosition() {
		if (this.smoothPowerStopBlockPosition == null) {
			return true;
		}
		ISU.Coord targetCenter = this.smoothPowerStopBlockPosition.toISUCoordCentered();
		ISU.Vector towardTarget = this.owner.center().mkVectorToward(targetCenter);

		double remaining = this.owner.geometry().forwardProjection(towardTarget, this.owner.slideDirection());
		double tolerance = this.owner.model().isu().cmPerCell() * 0.06;
		return remaining <= tolerance;
	}
}