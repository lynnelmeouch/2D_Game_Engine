package game.model.player;

import java.util.ArrayList;
import java.util.List;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Orientation;
import game.config.PengoRunState;
import game.model.entity.Enemies;
import game.model.entity.Glacon;
import game.model.entity.Mur;
import game.model.entity.Power;
import game.model.entity.PowerType;
import game.model.entity.Spawner;
import game.model.slide.Pushable;

// Classe parent de Pengo et Friend.
public abstract class Player extends Entity {

	private static final long HIT_COOLDOWN_MS = 900L;

	private static final long SHIELD_DURATION_MS = 10000L;
	private static final long SPEED_DURATION_MS = 8000L;
	private static final long FREEZE_DURATION_MS = 5000L;
	private static final long STAR_DURATION_MS = 5000L;
	private static final long RESPAWN_INVINCIBILITY_DURATION_MS = 3000L;

	private final Grid grid;
	private final int gridWidth;
	private final int gridHeight;

	private int lives;
	private long lastHitTime_ms;
	private boolean controlled;
	private PengoRunState runState;

	private long shieldEnd_ms;
	private long speedEnd_ms;
	private long freezeEnd_ms;
	private long starEnd_ms;
	private long respawnInvincibilityEnd_ms;

	private ISU.Dimension normalStep;
	private ISU.Dimension boostedStep;
	private boolean friendActivator;

	protected Player(Model model, String name, int initialLives) {
		super(model, name);

		this.grid = model.grid();
		this.gridWidth = this.grid.width();
		this.gridHeight = this.grid.height();

		this.lives = initialLives;

		this.normalStep = model.isu().new Dimension(model.isu().cmPerCell(), model.isu().cmPerCell());
		this.boostedStep = model.isu().new Dimension(model.isu().cmPerCell() * 2.0, model.isu().cmPerCell() * 2.0);

		setStep(this.normalStep);
	}

	public final void setRunState(PengoRunState runState) {
		this.runState = runState;
	}

	protected final long gameTimeMs() {
		if (this.runState == null) {
			return System.currentTimeMillis();
		}

		return this.runState.nowMs();
	}

	// LIFE

	public final int nLives() {
		return this.lives;
	}

	public final void gainLife() {
		this.lives++;
	}

	public final void loseLife() {
		if (this.lives > 0) {
			this.lives--;
		}
	}

	public final boolean isGameOver() {
		return this.lives == 0;
	}

	// CONTROLE

	public final boolean isControlled() {
		return this.controlled;
	}

	public final void setControlled(boolean controlled) {
		this.controlled = controlled;
	}

	// POWERS

	public void activateShield() {
		this.shieldEnd_ms = gameTimeMs() + SHIELD_DURATION_MS;
	}

	public void deactivateShield() {
		this.shieldEnd_ms = 0L;
	}

	public boolean hasShield() {
		return gameTimeMs() < this.shieldEnd_ms;
	}

	public void activateRespawnInvincibility() {
		this.respawnInvincibilityEnd_ms = gameTimeMs() + RESPAWN_INVINCIBILITY_DURATION_MS;
	}

	public boolean hasRespawnInvincibility() {
		return gameTimeMs() < this.respawnInvincibilityEnd_ms;
	}

	public long respawnInvincibilityRemainingMs() {
		return remainingMs(this.respawnInvincibilityEnd_ms);
	}

	public boolean visibleDuringInvincibilityBlink() {
		if (!hasRespawnInvincibility()) {
			return true;
		}

		long elapsed = RESPAWN_INVINCIBILITY_DURATION_MS - respawnInvincibilityRemainingMs();

		return (elapsed / 120L) % 2L == 0L;
	}

	public void activateSpeed() {
		this.speedEnd_ms = gameTimeMs() + SPEED_DURATION_MS;
		setStep(this.boostedStep);
		refreshCurrentMovementSpeed();
	}

	public void deactivateSpeed() {
		this.speedEnd_ms = 0L;
		setStep(this.normalStep);
		refreshCurrentMovementSpeed();
	}

	public boolean hasSpeedBoost() {
		return gameTimeMs() < this.speedEnd_ms;
	}

	private void refreshCurrentMovementSpeed() {
		if (!isMoving()) {
			return;
		}

		double walkSpeed = 5.0;

		switch (Orientation.normalize(orientation())) {
		case Orientation.EAST:
			setLinearSpeed(model().isu().new Vector(step().x() * walkSpeed, 0.0));
			break;

		case Orientation.SOUTH:
			setLinearSpeed(model().isu().new Vector(0.0, step().y() * walkSpeed));
			break;

		case Orientation.WEST:
			setLinearSpeed(model().isu().new Vector(-step().x() * walkSpeed, 0.0));
			break;

		case Orientation.NORTH:
			setLinearSpeed(model().isu().new Vector(0.0, -step().y() * walkSpeed));
			break;

		default:
			break;
		}
	}

	public void activateFreezePower() {
		activateFreezePower(FREEZE_DURATION_MS);
	}
	
	public void activateFreezePower(long durationMs) {
		assert durationMs > 0L;
		this.freezeEnd_ms = gameTimeMs() + durationMs;
		freezeEnemies();
	}

	public void deactivateFreezePower() {
		this.freezeEnd_ms = 0L;
		unfreezeEnemies();
	}

	public boolean hasFreezePower() {
		return gameTimeMs() < this.freezeEnd_ms;
	}

	public void freezeEnemies() {
		for (Entity entity : model().entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				Enemies enemy = (Enemies) entity;

				enemy.freeze();

				if (enemy.stunt() instanceof BasicStunt) {
					BasicStunt stunt = (BasicStunt) enemy.stunt();
					stunt.cancelCurrentAction();
				} else {
					enemy.setLinearSpeed(model().isu().new Vector(0.0, 0.0));
					enemy.setAngularSpeed(0.0);
				}
			}
		}
	}

	private void unfreezeEnemies() {
		for (Entity entity : model().entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				Enemies enemy = (Enemies) entity;
				enemy.unfreeze();
			}
		}
	}

	public void activateStar() {
		activateStar(STAR_DURATION_MS);
	}

	public void activateStar(long durationMs) {
		assert durationMs > 0L;
		this.starEnd_ms = gameTimeMs() + durationMs;
	}

	public void deactivateStar() {
		this.starEnd_ms = 0L;
	}

	public boolean hasStar() {
		return gameTimeMs() < this.starEnd_ms;
	}

	public void activateFriendActivator() {
		this.friendActivator = true;
	}

	public void deactivateFriendActivator() {
		this.friendActivator = false;
	}

	public boolean hasFriendActivator() {
		return this.friendActivator;
	}

	private long remainingMs(long endMs) {
		long remaining = endMs - gameTimeMs();

		if (remaining < 0L) {
			return 0L;
		}

		return remaining;
	}

	public long shieldRemainingMs() {
		return remainingMs(this.shieldEnd_ms);
	}

	public long speedRemainingMs() {
		return remainingMs(this.speedEnd_ms);
	}

	public long freezeRemainingMs() {
		return remainingMs(this.freezeEnd_ms);
	}

	public long starRemainingMs() {
		return remainingMs(this.starEnd_ms);
	}

	public long activePowerRemainingMs(PowerType type) {
		switch (type) {
		case SHIELD:
			return shieldRemainingMs();

		case SPEED:
			return speedRemainingMs();

		case FREEZE:
			return freezeRemainingMs();

		case STAR:
			return starRemainingMs();

		default:
			return 0L;
		}
	}

	// HUD

	public PowerType activePowerTypeForHud() {
		PowerType bestType = null;
		long bestRemaining = 0L;

		PowerType[] timedTypes = new PowerType[] { PowerType.STAR, PowerType.SHIELD, PowerType.FREEZE,
				PowerType.SPEED };

		for (PowerType type : timedTypes) {
			long remaining = activePowerRemainingMs(type);

			if (remaining > bestRemaining) {
				bestRemaining = remaining;
				bestType = type;
			}
		}

		if (bestType != null) {
			return bestType;
		}

		if (this.friendActivator) {
			return PowerType.FRIEND_ACTIVATOR;
		}

		return null;
	}

	public long activePowerRemainingMsForHud() {
		PowerType type = activePowerTypeForHud();

		if (type == null) {
			return 0L;
		}

		return activePowerRemainingMs(type);
	}

	public boolean powerActiveForHud(PowerType type) {
		switch (type) {
		case SHIELD:
			return hasShield();

		case SPEED:
			return hasSpeedBoost();

		case FREEZE:
			return hasFreezePower();

		case STAR:
			return hasStar();

		case FRIEND_ACTIVATOR:
			return hasFriendActivator();

		default:
			return false;
		}
	}

	public long powerRemainingMsForHud(PowerType type) {
		switch (type) {
		case SHIELD:
			return shieldRemainingMs();

		case SPEED:
			return speedRemainingMs();

		case FREEZE:
			return freezeRemainingMs();

		case STAR:
			return starRemainingMs();

		default:
			return 0L;
		}
	}

	public void updateTimedPowers() {
		long now = gameTimeMs();

		if (this.freezeEnd_ms > 0L) {
			if (now < this.freezeEnd_ms) {
				freezeEnemies();
			} else {
				deactivateFreezePower();
			}
		}

		if (this.respawnInvincibilityEnd_ms > 0L && now >= this.respawnInvincibilityEnd_ms) {
			this.respawnInvincibilityEnd_ms = 0L;
		}

		if (this.shieldEnd_ms > 0L && now >= this.shieldEnd_ms) {
			deactivateShield();
		}

		if (this.speedEnd_ms > 0L && now >= this.speedEnd_ms) {
			deactivateSpeed();
		}

		if (this.starEnd_ms > 0L && now >= this.starEnd_ms) {
			deactivateStar();
		}
	}

	protected boolean hasEnemyProtection() {
		return hasShield() || hasRespawnInvincibility();
	}

	protected boolean hasStarPower() {
		return hasStar();
	}

	public void explode() {
		forceLoseLife();
	}

	// PLAYER BEHAVIOR

	public final boolean faceIceAhead(int directionDegree) {
		if (!alive() || !installed() || isMoving()) {
			return false;
		}

		int direction = Orientation.normalize(directionDegree);

		Pushable pushable = pushableBlockAhead(direction);

		if (pushable == null) {
			return false;
		}

		setOrientation(direction);

		return true;
	}

	public final boolean useIceAhead() {
		if (!alive() || !installed() || isMoving()) {
			return false;
		}

		int direction = Orientation.normalize(orientation());

		Pushable pushable = pushableBlockAhead(direction);

		if (pushable == null) {
			return false;
		}

		return pushable.push(direction);
	}

	private Pushable pushableBlockAhead(int direction) {
		Grid.Position target = Orientation.neighbor(this.grid, position(), direction);

		if (target == null) {
			return null;
		}

		Grid.Cell targetCell = this.grid.cellAt(target);

		for (Entity entity : targetCell.entities()) {
			if (entity.alive() && entity instanceof Pushable) {
				return (Pushable) entity;
			}
		}

		return null;
	}

	@Override
	public void collision(Entity other) {
		if (other instanceof Power && other.alive()) {
			if (!isControlled()) {
				return;
			}

			Power power = (Power) other;
			power.apply(this);
			power.death();

			return;
		}

		if (other instanceof Enemies && other.alive()) {
			Enemies enemy = (Enemies) other;

			if (enemy.isCrushed()) {
				return;
			}

			if (hasStarPower()) {
				enemy.kill();
				return;
			}

			hitByEnemy();

			return;
		}

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		List<Entity> physicalContacts = new ArrayList<Entity>();

		boolean touchedEnemy = false;

		for (Entity entity : entities) {
			if (entity instanceof Power && entity.alive()) {
				if (isControlled()) {
					Power power = (Power) entity;
					power.apply(this);
					power.death();
				}

				continue;
			}

			if (entity instanceof Enemies && entity.alive()) {
				Enemies enemy = (Enemies) entity;

				if (enemy.isCrushed()) {
					continue;
				}

				if (hasStarPower()) {
					enemy.kill();
					continue;
				}

				touchedEnemy = true;
			} else {
				physicalContacts.add(entity);
			}
		}

		if (touchedEnemy) {
			hitByEnemy();
		}

		if (!physicalContacts.isEmpty()) {
			super.collision(physicalContacts);
		}
	}

	public final void forceLoseLife() {
		if (!alive() || !installed()) {
			return;
		}

		loseLife();

		if (isGameOver()) {
			stopPlayer();
			death();
			return;
		}

		respawnAtStart();
		afterRespawn();
	}

	private void respawnAtStart() {
		Grid.Position respawn = centerRespawnPosition();

		cancelCurrentMovement();
		stopPlayer();

		model().place(this, respawn);
		setOrientation(Orientation.EAST);
	}

	protected void afterRespawn() {
	}

	private void cancelCurrentMovement() {
		if (stunt() instanceof BasicStunt) {
			BasicStunt basicStunt = (BasicStunt) stunt();
			basicStunt.cancelCurrentAction();
		}
	}

	private void stopPlayer() {
		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);
	}

	public final void hitByEnemy() {
		if (!alive() || !installed()) {
			return;
		}

		long now = gameTimeMs();

		if (now - this.lastHitTime_ms < HIT_COOLDOWN_MS) {
			return;
		}

		if (!isControlled() || hasEnemyProtection()) {
			return;
		}

		this.lastHitTime_ms = now;

		loseLife();

		if (isGameOver()) {
			stopPlayer();
			death();
			return;
		}

		respawnAtStart();
		afterRespawn();
	}

	private Grid.Position centerRespawnPosition() {
		int centerX = this.gridWidth / 2;
		int centerY = this.gridHeight / 2;

		Grid.Position center = this.grid.new Position(centerX, centerY);

		if (canRespawnOn(center)) {
			return center;
		}

		int maxRadius = Math.max(this.gridWidth, this.gridHeight);

		for (int radius = 1; radius <= maxRadius; radius++) {
			for (int y = centerY - radius; y <= centerY + radius; y++) {
				for (int x = centerX - radius; x <= centerX + radius; x++) {
					if (x < 0 || x >= this.gridWidth || y < 0 || y >= this.gridHeight) {
						continue;
					}

					Grid.Position position = this.grid.new Position(x, y);

					if (canRespawnOn(position)) {
						return position;
					}
				}
			}
		}

		return center;
	}

	private boolean canRespawnOn(Grid.Position position) {
		Grid.Cell cell = this.grid.cellAt(position);

		for (Entity entity : cell.entities()) {
			if (entity == this || !entity.alive()) {
				continue;
			}

			if (entity instanceof Mur || entity instanceof Glacon || entity instanceof Spawner) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String debugLabel() {
		String action = isMoving() ? "WALK" : "REST";

		if (isControlled()) {
			action = "CONTROL " + action;
		}

		return name() + " " + action + " dir=" + orientation();
	}
}