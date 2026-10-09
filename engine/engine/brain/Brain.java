package engine.brain;

import java.util.ArrayList;
import java.util.List;

import engine.model.Entity;

public class Brain {

	private final List<Bot> bots;

	public Brain() {
		this.bots = new ArrayList<Bot>();
	}

	public void add(Bot bot) {
		assert bot.brain() == this;

		if (!this.bots.contains(bot)) {
			this.bots.add(bot);
		}
	}

	public void remove(Bot bot) {

		this.bots.remove(bot);
	}

	public void think() {
		List<Bot> copy = new ArrayList<Bot>(this.bots);

		for (Bot bot : copy) {
			if (bot.valid()) {
				bot.think();
			}
		}

		List<Bot> toRemove = new ArrayList<Bot>();

		for (Bot bot : this.bots) {
			if (!bot.valid()) {
				toRemove.add(bot);
			}
		}

		for (Bot bot : toRemove) {
			this.bots.remove(bot);
		}
	}

	public void removeBotsOf(Entity entity) {
		List<Bot> toRemove = new ArrayList<Bot>();

		for (Bot bot : this.bots) {
			if (bot.stunt().entity() == entity) {
				toRemove.add(bot);
			}
		}

		for (Bot bot : toRemove) {
			this.bots.remove(bot);
		}
	}

	public List<Bot> bots() {
		return new ArrayList<Bot>(this.bots);
	}

}