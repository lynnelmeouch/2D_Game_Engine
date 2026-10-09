package engine.model;

import java.util.List;

import engine.brain.Bot;

/*
 * Stunt représente la partie "action" d'une entité.
 *
 * Le Bot décide quoi faire.
 * Le Stunt sait comment exécuter l'action sur l'entité.
 *
 */
public abstract class Stunt {

	protected final Model model;
	protected final Entity entity;
	protected Bot bot;

	private boolean enabled;

	protected Stunt(Model model, Entity entity) {
		assert entity.model() == model;

		this.model = model;
		this.entity = entity;
		this.enabled = true;
	}

	public final Bot bot() {
		return this.bot;
	}

	/*
	 * Un Stunt est utilisable seulement si : - il n'a pas été invalidé ; - son
	 * entité est vivante ; - son entité est encore installée dans le Model.
	 */
	public final boolean valid() {
		return this.enabled && this.entity.alive() && this.entity.installed();
	}

	public final void invalidate() {
		if (!this.enabled) {
			return;
		}

		this.enabled = false;

		onInvalidate();

		this.bot = null;
	}

	protected void onInvalidate() {
		// Rien par défaut.
	}

	public Model model() {
		return this.model;
	}

	public Entity entity() {
		return this.entity;
	}

	public final void setBot(Bot bot) {
		assert this.enabled;
		assert bot.stunt() == this;

		if (this.bot == bot) {
			return;
		}

		/*
		 * Le nouveau Bot devient l'unique Bot associé à ce Stunt.
		 *
		 * L'ancien Bot deviendra automatiquement invalide grâce à Bot.valid().
		 */
		this.bot = bot;
	}

	/*
	 * Prévient le Bot que l'action courante est terminée.
	 *
	 * Cela permet au Bot de choisir une nouvelle action.
	 */
	protected void notifyDone() {
		if (valid() && this.bot != null) {
			this.bot.done();
		}
	}

	/*
	 * Prévient le Bot qu'une collision a eu lieu avec une entité.
	 */
	protected void notifyCollision(Entity entity) {
		if (valid() && this.bot != null) {
			this.bot.collision(entity);
		}
	}

	/*
	 * Prévient le Bot qu'une collision a eu lieu avec plusieurs entités.
	 */
	protected void notifyCollision(List<Entity> entities) {
		if (valid() && this.bot != null) {
			this.bot.collision(entities);
		}
	}

	/*
	 * Action de base demandée par les Bots.
	 */
	public abstract boolean walk(int degree);

	/*
	 * Notification envoyée quand l'orientation de l'entité change.
	 */
	public void set(int orientation) {
	}

	/*
	 * Notification envoyée quand l'entité change de cellule.
	 */
	public void set(Grid.Cell cell) {
	}

	/*
	 * Notification envoyée quand la position précise de l'entité change.
	 */
	public void set(double x, double y) {
	}

	/*
	 * Appelé à la fin d'une mise à jour du Model.
	 *
	 * Cela permet à un Stunt de différer certaines notifications, par exemple
	 * prévenir le Bot seulement après validation du mouvement.
	 */
	public void commit() {
	}

	/*
	 * Réaction par défaut à une collision.
	 *
	 * Ici, on se contente de transmettre l'information au Bot.
	 */
	public void collision(Entity e) {
		notifyCollision(e);
	}

	public void collision(List<Entity> entities) {
		notifyCollision(entities);
	}
}