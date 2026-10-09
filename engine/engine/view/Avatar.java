package engine.view;

import engine.model.Entity;
import oop.graphics.Graphics;

/*
 * Avatar est la représentation graphique d'une entité.
 *
 * Le modèle contient les entités et leur état.
 * L'avatar sait comment dessiner une entité dans la vue.
 */
public abstract class Avatar {

	protected final View view;
	protected final Entity entity;

	public Avatar(View view, Entity entity) {
		assert entity.model() == view.model();

		this.view = view;
		this.entity = entity;
	}

	public View view() {
		return this.view;
	}

	public Entity entity() {
		return this.entity;
	}

	/*
	 * Chaque type d'avatar définit sa propre façon de dessiner l'entité.
	 */
	public abstract void paint(Graphics g);

	/*
	 * Par défaut, un avatar n'a rien à mettre à jour. Les avatars animés
	 * redéfinissent cette méthode.
	 */
	public void tick(long elapsed) {
	}
}