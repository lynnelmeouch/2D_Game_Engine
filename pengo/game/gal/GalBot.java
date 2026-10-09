package game.gal;

import java.util.List;

import engine.brain.Bot;
import engine.brain.Brain;
import engine.model.BasicStunt;
import engine.model.Entity;
import gal.ast.Automaton;
import game.model.entity.Enemies;
import game.model.player.Friend;

public final class GalBot extends Bot {

	private static final double NORMAL_ENEMY_SPEED = 5.0;
	private static final double ANGRY_ENEMY_SPEED = 7;
	private final Entity entity;
	private final GalRuntime runtime;

	public GalBot(Brain brain, BasicStunt stunt, Entity entity, Automaton automate, CurrentPlayer currentPlayer) {
		this(brain, stunt, entity, automate, currentPlayer, null);
	}

	public GalBot(Brain brain, BasicStunt stunt, Entity entity, Automaton automate, CurrentPlayer currentPlayer,
			java.lang.Runnable eggAction) {
		super(brain, stunt);
		this.entity = entity;
		this.runtime = new GalRuntime(automate, new GalGameContext(entity, stunt, currentPlayer, eggAction));
		syncGalStateToEntity();
	}

	public String currentGalStateName() {
		return runtime.currentState();
	}

	@Override
	public void think() {
		if (!valid() || entity.isMoving()) {
			return;
		}
		if (entity instanceof Enemies e && e.isFrozen()) {
			return;
		}
		if (entity instanceof Friend f && f.isControlled()) {
			return;
		}
		updateEnemySpeed();
		this.runtime.step();
		syncGalStateToEntity();
	}

	private void syncGalStateToEntity() {
		if (this.entity instanceof Enemies e) {
			e.setGalState(this.runtime.currentState());
		}
	}

	private void updateEnemySpeed() {
		if (!(this.entity instanceof Enemies)) {
			return;
		}
		if (!(stunt() instanceof BasicStunt)) {
			return;
		}
		BasicStunt basicStunt = (BasicStunt) stunt();
		if ("Angry".equalsIgnoreCase(this.runtime.currentState())) {
			basicStunt.setWalkSpeed(ANGRY_ENEMY_SPEED);
		} else {
			basicStunt.setWalkSpeed(NORMAL_ENEMY_SPEED);
		}
	}

	@Override
	public void done() {
		think();
	}

	@Override
	public void collision(Entity other) {

	}

	@Override
	public void collision(List<Entity> others) {

	}
}