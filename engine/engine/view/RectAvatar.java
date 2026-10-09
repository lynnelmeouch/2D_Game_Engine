package engine.view;

import engine.model.Entity;
import oop.graphics.Graphics;

/*
 * Avatar générique représentant une entité par un rectangle orienté.
 */
public class RectAvatar extends ShapeAvatar {

	public RectAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	protected boolean rotateWithEntity() {
		return true;
	}

	@Override
	protected void paintShape(Graphics graphics, int x, int y, int width, int height) {
		graphics.fillRect(x, y, width, height);
	}
}