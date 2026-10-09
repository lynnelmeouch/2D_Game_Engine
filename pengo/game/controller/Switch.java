package game.controller;

import engine.brain.Brain;
import engine.brain.PlayerBot;
import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Model;
import engine.view.Ticker;
import gal.ast.Automaton;
import game.gal.CurrentPlayer;
import game.gal.GalBot;
import game.model.player.Friend;
import game.model.player.Pengo;
import game.model.player.Player;

public final class Switch {

	private final Brain brain;
	private final Model model;
	private final Ticker ticker;
	private final Pengo pengo;
	private final Friend friend;
	private final Automaton randomAutomaton;
	private final CurrentPlayer currentPlayer;
	private Entity controlledEntity;
	private PlayerBot playerBot;

	public Switch(Brain brain, Model model, Ticker ticker, Pengo pengo, Friend friend, Automaton randomAutomaton,
			CurrentPlayer currentPlayer) {
		this.brain = brain;
		this.model = model;
		this.ticker = ticker;
		this.pengo = pengo;
		this.friend = friend;
		this.randomAutomaton = randomAutomaton;
		this.currentPlayer = currentPlayer;
		control(this.pengo);
	}

	public Entity controlledEntity() {
		return this.controlledEntity;
	}

	public PlayerBot playerBot() {
		return this.playerBot;
	}

	// switch entre friend et pengo
	public void switchControl() {
		updateControlledEntity();
		if (!canSwitch()) {
			return;
		}
		Entity nextEntity = nextEntityToControl();
		if (nextEntity == null || nextEntity == this.controlledEntity) {
			return;
		}
		makeAutomatic(this.controlledEntity);
		control(nextEntity);
	}

	public void updateControlledEntity() {
		if (isUsable(this.controlledEntity)) {
			return;
		}
		Entity previousControlledEntity = this.controlledEntity;
		// on update letat de controlled
		if (this.controlledEntity instanceof Player) {
			Player player = (Player) this.controlledEntity;
			player.setControlled(false);
		}
		if (isUsable(this.pengo) && this.pengo.nLives() > 0) {
			control(this.pengo);
			return;
		}
		this.controlledEntity = null;
		this.playerBot = null;
	}

	// switch power
	private boolean canSwitch() {
		if (!this.pengo.hasFriendActivator()) {
			return false;
		}
		if (!isUsable(this.pengo) || this.pengo.isGameOver()) {
			return false;
		}
		if (!friendCanBeControlled()) {
			return false;
		}
		return true;
	}

	
	private Entity nextEntityToControl() {
		if (this.controlledEntity == this.pengo) {
			return this.friend;
		}
		return this.pengo;
	}

	private boolean isUsable(Entity entity) {
		return entity != null && entity.alive() && entity.installed();
	}

	private boolean friendCanBeControlled() {
		return isUsable(this.friend) && this.friend.nLives() > 0;
	}

	private void makeAutomatic(Entity entity) {
		if (!isUsable(entity)) {
			return;
		}
		this.brain.removeBotsOf(entity);
		if (!(entity instanceof Player)) {
			return;
		}

		Player player = (Player) entity;
		player.setControlled(false);

		BasicStunt autoStunt = new BasicStunt(this.model, entity);
		entity.setStunt(autoStunt);

		GalBot autoBot = new GalBot(this.brain, autoStunt, entity, this.randomAutomaton, this.currentPlayer);
		this.brain.add(autoBot);
	}

	private void control(Entity entity) {
		if (!isUsable(entity)) {
			return;
		}
		this.brain.removeBotsOf(entity);

		BasicStunt playerStunt = new BasicStunt(this.model, entity);
		entity.setStunt(playerStunt);

		if (entity instanceof Player) {
			Player player = (Player) entity;
			player.setControlled(true);
		}

		this.playerBot = new PlayerBot(this.brain, playerStunt);
		this.brain.add(this.playerBot);

		this.controlledEntity = entity;
		this.currentPlayer.setCurrentPlayer(entity);

		this.model.viewPort().centerOn(entity);
		this.ticker.setFollowedEntity(entity);
	}
}