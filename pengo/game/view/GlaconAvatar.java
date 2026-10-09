package game.view;

import engine.view.ImageAvatar;
import engine.view.View;
import game.model.entity.Glacon;
import oop.graphics.Graphics;

/** Avatar d'un bloc de glace. */
public final class GlaconAvatar extends ImageAvatar {

	private final Glacon glacon;

	public GlaconAvatar(View view, Glacon glacon) {
		super(view, glacon, new String[] { "pengo_rsrc/glacon.png", "pengo_rsrc/broken_glacon2.png" });
		this.glacon = glacon;
	}

	@Override
	public void paint(Graphics graphics) {
		if (this.glacon.isBreaking()) {
			setCurrentFrame(1);
		} else {
			setCurrentFrame(0);
		}

		super.paint(graphics);
	}

	@Override
	protected double visualScale() {
		return 1.4 / Glacon.collisionSizeFactor();
	}

	@Override
	protected boolean rotateWithEntity() {
		return false;
	}
}