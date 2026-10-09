package game.model.slide;

import java.util.List;

import engine.model.Bounding;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Orientation;
import engine.model.Rect;
import game.config.PengoRunState;
import game.model.entity.Enemies;
import game.model.entity.Glacon;
import game.model.entity.Mur;
import game.model.entity.Obstacle;
import game.model.entity.Power;
import game.model.entity.Spawner;
import game.model.player.Friend;
import game.model.player.Pengo;
import game.model.player.Player;

public abstract class Glissade extends Entity implements Pushable {

	// Paramètres principaux de la glissade.
	protected static final double INITIAL_SPEED_NCELL_PER_SECOND = 4.2;
	protected static final double COLLISION_SIZE_FACTOR = 0.86;
	protected static final double MAX_TRAVEL_FACTOR = 1.5;
	protected static final double EPS = 0.000001;

	// Composants qui gèrent chacun une partie du comportement.
	private final SlideMotion motion;
	private final SlideGeometry geometry;
	private final GridSnapper gridSnapper;
	private final EnemyTrain enemyTrain;
	private final PowerStopController powerStop;
	private final SlideCollisionHandler collisionHandler;

	// État global du jeu, utile pour le temps/pause/stop.
	private PengoRunState runState;

	protected Glissade(Model model, String name, int zOrder, int sOrder) {
		super(model, name);

		double cell = model.isu().cmPerCell();
		double collisionSize = cell * COLLISION_SIZE_FACTOR;

		// Le bloc est légèrement plus petit qu'une case pour les collisions.
		setSize(model.isu().new Dimension(collisionSize, collisionSize));
		setStep(model.isu().new Dimension(cell, cell));

		// Initialisation des gestionnaires internes.
		this.motion = new SlideMotion();
		this.geometry = new SlideGeometry(model);
		this.gridSnapper = new GridSnapper(this);
		this.enemyTrain = new EnemyTrain(this);
		this.powerStop = new PowerStopController(this, this.enemyTrain);
		this.collisionHandler = new SlideCollisionHandler(this, this.enemyTrain, this.powerStop);

		setZOrder(zOrder);
		setSOrder(sOrder);

		updateBounding();
	}

	public void setRunState(PengoRunState runState) {
		this.runState = runState;
	}

	protected final PengoRunState runState() {
		return this.runState;
	}

	protected final long gameTimeMs() {
		if (this.runState == null) {
			return System.currentTimeMillis();
		}

		return this.runState.nowMs();
	}

	protected final boolean pausedByRunState() {
		return this.runState != null && this.runState.paused();
	}

	protected final boolean stoppedByRunState() {
		return this.runState != null && this.runState.stopped();
	}

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();
		result.add(new Rect(center, size, orientationDegree));
		return result;
	}

	@Override
	public final boolean push(int directionDegree) {
		// Impossible de pousser si le bloc est invalide ou déjà en mouvement.
		if (!alive() || !installed() || this.motion.isSliding()) {
			return false;
		}

		int direction = Orientation.normalize(directionDegree);
		Grid.Position target = this.geometry.neighbor(position(), direction);

		// Pas de cellule dans cette direction.
		if (target == null) {
			return false;
		}

		// La cellule devant est bloquée.
		if (isBlockedAt(target)) {
			return onPushBlocked(direction, target);
		}

		double initialSpeed = model().isu().cmPerCell() * INITIAL_SPEED_NCELL_PER_SECOND;
		double maximumDistance = this.geometry.axisLength(direction) * MAX_TRAVEL_FACTOR;

		// Démarre la glissade.
		this.motion.start(this, direction, initialSpeed, maximumDistance, gameTimeMs());

		// Réinitialise les états liés à la glissade précédente.
		this.powerStop.clear();
		this.enemyTrain.clear();
		this.gridSnapper.setLastStablePosition(position().copy());

		setOrientation(direction);
		setLinearSpeed(this.geometry.speedVector(direction, initialSpeed));

		return true;
	}

	// Hook pour les classes filles si un push est bloqué.
	protected boolean onPushBlocked(int direction, Grid.Position target) {
		return false;
	}

	public final void updateSliding() {
		// Rien à mettre à jour si le bloc ne glisse pas.
		if (!this.motion.isSliding() || !alive() || !installed()) {
			return;
		}

		// Si la vitesse est nulle, on arrête proprement.
		if (linearSpeed().norm() <= EPS) {
			stopSlidingAndSnapToCell();
			return;
		}

		// Met à jour la distance parcourue.
		this.motion.updateDistance(this);
		this.gridSnapper.rememberStableCellIfUseful();

		// Stop si la distance max est atteinte.
		if (this.motion.reachedMaximumDistance(EPS)) {
			stopSliding();
			return;
		}

		// Applique la décélération.
		this.motion.decelerate(gameTimeMs());

		// Stop si la vitesse devient trop faible.
		if (this.motion.isTooSlow(EPS)) {
			stopSliding();
			return;
		}

		// Réapplique la vitesse dans la bonne direction.
		restoreSlideSpeed();

		// Gère les ennemis qui croisent ou sont poussés.
		this.enemyTrain.againstCrossingEnemies();

		if (this.powerStop.active()) {
			this.powerStop.update();
		} else {
			this.enemyTrain.carryEnemies();
		}
	}

	public final void stopSliding() {
		stopSlidingAndSnapToCell();
	}

	private void stopSlidingAndSnapToCell() {
		stopSlidingAt(this.gridSnapper.safeSnapPosition(this.motion.direction()));
	}

	final void stopSlidingAt(Grid.Position snapPosition) {
		boolean wasSliding = this.motion.isSliding();

		// Nettoie l'état de glissade.
		this.powerStop.clear();
		this.motion.stop();

		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);

		// Relâche les ennemis transportés.
		this.enemyTrain.releaseCarriedEnemies();

		// Replace le bloc sur une cellule propre.
		if (wasSliding && alive() && installed() && snapPosition != null) {
			model().place(this, snapPosition);
			this.gridSnapper.setLastStablePosition(snapPosition.copy());
		}
	}

	@Override
	public final void collision(Entity other) {
		// Les collisions spéciales de glissade sont déléguées.
		boolean handled = this.collisionHandler.handleCollision(other);

		if (!handled) {
			super.collision(other);
		}
	}

	@Override
	public final void collision(List<Entity> entities) {
		// Même principe, mais pour plusieurs collisions en même temps.
		boolean handled = this.collisionHandler.handleCollision(entities);

		if (!handled) {
			super.collision(entities);
		}
	}

	@Override
	public final boolean blocks(Entity other) {
		return this.collisionHandler.blocks(other);
	}

	public final boolean isSliding() {
		return this.motion.isSliding();
	}

	public final int slideDirection() {
		return this.motion.direction();
	}

	public final double currentSpeedCmPerSecond() {
		return this.motion.currentSpeedCmPerSecond();
	}

	public final double traveledDistanceCm() {
		return this.motion.traveledDistanceCm();
	}

	public final double maximumDistanceCm() {
		return this.motion.maximumDistanceCm();
	}

	public static double collisionSizeFactor() {
		return COLLISION_SIZE_FACTOR;
	}

	// Remet la vitesse de glissade après une collision.
	final void restoreSlideSpeed() {
		setLinearSpeed(this.geometry.speedVector(this.motion.direction(), this.motion.currentSpeedCmPerSecond()));
		setAngularSpeed(0.0);
	}

	private boolean isBlockedAt(Grid.Position target) {
		Grid.Cell targetCell = model().grid().cellAt(target);

		for (Entity other : targetCell.entities()) {
			if (other == this || !other.alive()) {
				continue;
			}

			if (isHardBlockForThisBlock(other)) {
				return true;
			}
		}

		return false;
	}

	// Position située devant le bloc, selon sa direction de glissade.
	final Grid.Position positionInFront(int distanceInCells) {
		Grid.Position result = position().copy();

		for (int i = 0; i < distanceInCells; i++) {
			result = this.geometry.neighbor(result, this.motion.direction());

			if (result == null) {
				return null;
			}
		}

		return result;
	}

	// Position située devant une position donnée.
	final Grid.Position positionInFrontFrom(Grid.Position start, int distanceInCells) {
		if (start == null) {
			return null;
		}

		Grid.Position result = start.copy();

		for (int i = 0; i < distanceInCells; i++) {
			result = this.geometry.neighbor(result, this.motion.direction());

			if (result == null) {
				return null;
			}
		}

		return result;
	}

	// Ramasse un power touche par un glacon/spawner qui glisse.
	// Le Power ne bloque plus la glissade : il est donne au joueur controle puis
	// supprime.
	final boolean collectPowerForControlledPlayer(Power power) {
		if (power == null || !power.alive()) {
			return false;
		}

		Player receiver = controlledPlayer();

		if (receiver == null) {
			return false;
		}

		power.apply(receiver);
		power.death();

		return true;
	}

	private Player controlledPlayer() {
		Player fallback = null;

		for (Entity entity : model().entities()) {
			if (entity instanceof Player && entity.alive()) {
				Player player = (Player) entity;

				if (player.isControlled()) {
					return player;
				}

				if (fallback == null && player instanceof Pengo) {
					fallback = player;
				}
			}
		}

		return fallback;
	}

	// Obstacles considérés comme fixes pour la glissade.
	final boolean isStaticObstacle(Entity other) {
		if (other instanceof Power) {
			return false;
		}

		return other instanceof Obstacle || other instanceof Mur || other instanceof Glacon || other instanceof Spawner;
	}

	// Obstacles capables d'écraser les ennemis transportés.
	final boolean isCrushingObstacle(Entity other) {
		return other instanceof Mur || other instanceof Glacon || other instanceof Spawner;
	}

	// Détermine si une entité bloque ce bloc.
	final boolean isHardBlockForThisBlock(Entity other) {
		if (other instanceof Enemies) {
			return false;
		}

		if (other instanceof Power) {
			return false;
		}

		if (isSliding() && this.powerStop.active() && this.powerStop.isSmoothPowerTarget(other)) {
			return false;
		}

		if (other instanceof Pengo || other instanceof Friend) {
			if (!isSliding()) {
				return !sameGridCell(other);
			}

			return true;
		}

		boolean hardType = other instanceof Obstacle || other instanceof Mur || other instanceof Glacon
				|| other instanceof Spawner;

		if (!hardType) {
			return false;
		}

		if (!isSliding()) {
			return true;
		}

		return isInFrontOfSlidingBlock(other);
	}

	final boolean sameGridCell(Entity other) {
		return other != null && position().equals(other.position());
	}

	// Vérifie si l'entité est devant le bloc dans l'axe de glissade.
	final boolean isInFrontOfSlidingBlock(Entity other) {
		if (other == null || !isSliding()) {
			return false;
		}

		ISU.Vector towardOther = center().mkVectorToward(other.center());
		double forward = this.geometry.forwardProjection(towardOther, this.motion.direction());
		double lateral = Math.abs(this.geometry.lateralProjection(towardOther, this.motion.direction()));
		double cell = model().isu().cmPerCell();

		return forward > cell * 0.10 && lateral <= cell * 0.55;
	}

	final SlideGeometry geometry() {
		return this.geometry;
	}

	final PowerStopController powerStop() {
		return this.powerStop;
	}

	protected final String slideDebugLabel(String name) {
		if (isSliding()) {
			return name + " SLIDE d=" + this.motion.direction() + " v="
					+ Math.round(this.motion.currentSpeedCmPerSecond());
		}

		return name + " REST";
	}
}