package engine.view;

import engine.model.Entity;
import oop.graphics.Graphics;

/*
 * Avatar générique représentant une entité par un cercle.
 */
public class CircleAvatar extends ShapeAvatar {

	public CircleAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	protected void paintShape(Graphics graphics, int x, int y, int width, int height) {
		graphics.fillOval(x, y, width, height);
	}
}