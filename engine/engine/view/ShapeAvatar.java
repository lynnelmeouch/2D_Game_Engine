package engine.view;

import engine.model.Entity;
import engine.model.ISU;
import oop.graphics.Graphics;

/*
 * Avatar générique pour les entités dessinées avec une forme simple.
 *
 * Il factorise :
 * - le calcul du centre en pixels ;
 * - le calcul de la taille en pixels ;
 * - la rotation graphique autour du centre ;
 * - la sauvegarde/restauration de la transformation graphique.
 *
 * Les sous-classes choisissent seulement la forme à dessiner :
 * cercle, rectangle, etc.
 */
public abstract class ShapeAvatar extends Avatar {

	public ShapeAvatar(View view, Entity entity) {
		super(view, entity);
	}

	protected boolean rotateWithEntity() {
		return false;
	}

	@Override
	public void paint(Graphics graphics) {

		ISU.Dimension size = this.entity.size();
		ISU.Coord center = this.entity.center();

		int width = this.view.toPixelWidth(size.x());
		int height = this.view.toPixelHeight(size.y());

		int centerX = this.view.toPixelX(center.x());
		int centerY = this.view.toPixelY(center.y());

		int x = centerX - width / 2;
		int y = centerY - height / 2;

		Object savedTransform = graphics.getTransform();

		if (rotateWithEntity()) {
			graphics.translate(centerX, centerY);
			graphics.rotate(Math.toRadians(this.entity.orientation()));
			graphics.translate(-centerX, -centerY);
		}

		paintShape(graphics, x, y, width, height);

		graphics.setTransform(savedTransform);
	}

	/*
	 * Chaque sous-classe dessine sa forme concrète.
	 */
	protected abstract void paintShape(Graphics graphics, int x, int y, int width, int height);
}