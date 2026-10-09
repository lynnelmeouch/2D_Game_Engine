package game.model;

import engine.model.Entity;
import engine.model.Model;
import game.model.entity.Enemies;
import game.model.entity.PowerType;
import game.model.entity.Spawner;
import game.model.player.Friend;
import game.model.player.Pengo;
import game.model.player.Player;

public class PengoGameState {

	private final Model model;
	private final Pengo pengo;

	private final int targetTotalEnemies;

	private int spawnedEnemies;

	private static final int ENEMY_KILL_SCORE = 100;

	private boolean won;
	private boolean lost;

	public PengoGameState(Model model, Pengo pengo, int initialEnemies, int spawnerCount, int maxTotalEnemies) {
		assert initialEnemies >= 0;
		assert spawnerCount >= 0;
		assert maxTotalEnemies >= 0;

		this.model = model;
		this.pengo = pengo;

		this.spawnedEnemies = initialEnemies;

		if (spawnerCount > 0) {
			this.targetTotalEnemies = Math.max(initialEnemies, maxTotalEnemies);
		} else {
			this.targetTotalEnemies = initialEnemies;
		}

		this.won = false;
		this.lost = false;

		update();
	}

	public Pengo pengo() {
		return this.pengo;
	}
	
	public Player controlledPlayerForHud() {
	    for (Entity entity : this.model.entities()) {
	        if (entity instanceof Player && entity.alive()) {
	            Player player = (Player) entity;

	            if (player.isControlled()) {
	                return player;
	            }
	        }
	    }

	    return this.pengo;
	}

	public boolean friendControlledForHud() {
	    return controlledPlayerForHud() instanceof Friend;
	}

	public String controlledPlayerNameForHud() {
	    if (friendControlledForHud()) {
	        return "FRIEND";
	    }

	    return "PENGO";
	}

	public PowerType activePowerType() {
		return controlledPlayerForHud().activePowerTypeForHud();
	}

	public long activePowerRemainingMs() {
		return controlledPlayerForHud().activePowerRemainingMsForHud();
	}

	public void enemySpawned() {
		this.spawnedEnemies++;
	}

	public void update() {
	    if (this.won || this.lost) {
	        return;
	    }

	    if (!friendCanStillBeUsed()) {
	        this.pengo.deactivateFriendActivator();
	    }

	    if (!this.pengo.alive() || this.pengo.isGameOver()) {
	        this.lost = true;
	        return;
	    }

	    if (this.targetTotalEnemies > 0 && this.spawnedEnemies >= this.targetTotalEnemies && livingEnemies() == 0) {
	        this.won = true;
	    }
	}

	public boolean finished() {
		return this.won || this.lost;
	}

	public boolean playing() {
		return !this.won && !this.lost;
	}

	public boolean won() {
		return this.won;
	}

	public boolean lost() {
		return this.lost;
	}

	public int lives() {
		return controlledPlayerForHud().nLives();
	}

	public int score() {
		return killedEnemies() * ENEMY_KILL_SCORE;
	}

	public int killedEnemies() {
		int killed = this.spawnedEnemies - livingEnemies();

		if (killed < 0) {
			return 0;
		}

		return killed;
	}

	public int spawnedEnemies() {
		return this.spawnedEnemies;
	}

	public int targetTotalEnemies() {
		return this.targetTotalEnemies;
	}

	public int livingEnemies() {
		int count = 0;

		for (Entity entity : this.model.entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				count++;
			}
		}

		return count;
	}

	public int livingSpawners() {
		int count = 0;

		for (Entity entity : this.model.entities()) {
			if (entity instanceof Spawner && entity.alive()) {
				count++;
			}
		}

		return count;
	}

	public String status() {
		if (this.won) {
			return "YOU WIN";
		}

		if (this.lost) {
			return "GAME OVER";
		}

		return "PLAYING";
	}

	public void stopAllEntities() {
		for (Entity entity : this.model.entities()) {
			if (entity.alive()) {
				entity.setLinearSpeed(this.model.isu().new Vector(0.0, 0.0));
				entity.setAngularSpeed(0.0);
			}
		}
	}

	public boolean powerActive(PowerType type) {
		return controlledPlayerForHud().powerActiveForHud(type);
	}

	public long powerRemainingMs(PowerType type) {
		return controlledPlayerForHud().powerRemainingMsForHud(type);
	}
	
	public boolean respawnInvincible() {
		return controlledPlayerForHud().hasRespawnInvincibility();
	}

	public long respawnInvincibleRemainingMs() {
		return controlledPlayerForHud().respawnInvincibilityRemainingMs();
	}
	
	private boolean friendCanStillBeUsed() {
	    for (Entity entity : this.model.entities()) {
	        if (entity instanceof Friend) {
	            Friend friend = (Friend) entity;

	            return friend.alive()
	                    && friend.installed()
	                    && friend.nLives() > 0;
	        }
	    }

	    return false;
	}
}