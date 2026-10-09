package engine.brain;

import java.util.List;

import engine.model.Entity;
import engine.model.Stunt;

public abstract class Bot {

	protected final Brain brain;
	protected final Stunt stunt;

	protected Bot(Brain brain, Stunt stunt) {
		this.brain = brain;
		this.stunt = stunt;

		this.stunt.setBot(this);
	}

	public final Brain brain() {
		return this.brain;
	}

	public final Stunt stunt() {
		return this.stunt;
	}

	public abstract void think();

	public void done() {
		// Réaction optionnelle dans les sous-classes.
	}

	public void collision(Entity entity) {
		// Réaction optionnelle dans les sous-classes.
	}

	public void collision(List<Entity> entities) {
		// Réaction optionnelle dans les sous-classes.
	}

	/*
	 * Un Bot est valide seulement si : - son Stunt est encore valide ; - il est
	 * toujours le Bot actuellement associé à ce Stunt.
	 */
	public final boolean valid() {
		return this.stunt.valid() && this.stunt.bot() == this;
	}
}