package game.view;

import engine.model.Orientation;
import engine.view.Avatar;
import engine.view.SpawnAnimation;
import engine.view.View;
import game.model.player.Pengo;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public final class PengoAvatar extends Avatar {

	private static final long FRAME_DURATION_MS = 120L;

	private final Pengo pengo;
	private final SpawnAnimation spawnAnimation;

	private BufferedImage down1;
	private BufferedImage down2;
	private BufferedImage up1;
	private BufferedImage up2;
	private BufferedImage right1;
	private BufferedImage right2;
	private BufferedImage left1;
	private BufferedImage left2;

	private long elapsedSinceFrame;
	private int frame;
	private int animatedOrientation;

	public PengoAvatar(View view, Pengo pengo) {
		super(view, pengo);
		this.pengo = pengo;
		this.spawnAnimation = new SpawnAnimation();
		this.animatedOrientation = Orientation.normalize(pengo.orientation());
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L : elapsed;
		this.spawnAnimation.tick(elapsed);

		if (!this.pengo.alive() || !this.pengo.installed()) {
			this.elapsedSinceFrame = 0L;
			this.frame = 0;
			this.animatedOrientation = Orientation.normalize(this.pengo.orientation());
			return;
		}

		int currentOrientation = Orientation.normalize(this.pengo.orientation());

		if (!this.pengo.isMoving()) {
			this.elapsedSinceFrame = 0L;
			this.frame = 0;
			this.animatedOrientation = currentOrientation;
			return;
		}

		if (this.animatedOrientation != currentOrientation) {
			this.animatedOrientation = currentOrientation;
			this.elapsedSinceFrame = 0L;
			this.frame = 0;
		}

		this.elapsedSinceFrame += elapsed;

		while (this.elapsedSinceFrame >= FRAME_DURATION_MS) {
			this.elapsedSinceFrame -= FRAME_DURATION_MS;
			this.frame = 1 - this.frame;
		}
	}

	@Override
	public void paint(Graphics graphics) {
		if (!this.pengo.alive() || !this.pengo.installed()) {
			return;
		}
		if (!this.pengo.visibleDuringInvincibilityBlink()) {
			return;
		}

		loadImages(graphics);

		int orientation = Orientation.normalize(this.pengo.orientation());
		BufferedImage image = imageForOrientation(orientation);

		double revealScale = this.view.revealScale(this.pengo);

		if (revealScale <= 0.0) {
			return;
		}

		double spawnScale = 1.0;

		if (!this.view.startupRevealActive()) {
			spawnScale = this.spawnAnimation.scale();
		}

		double scale = revealScale * spawnScale;

		if (scale <= 0.0) {
			return;
		}

		int centerX = this.view.toPixelX(this.pengo.center().x());
		int centerY = this.view.toPixelY(this.pengo.center().y());

		int width = this.view.toPixelWidth(this.pengo.size().x() * scale);
		int height = this.view.toPixelHeight(this.pengo.size().y() * scale);

		if (width <= 0 || height <= 0) {
			return;
		}

		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	private void loadImages(Graphics graphics) {
		if (this.down1 != null) {
			return;
		}
		this.down1 = graphics.load("pengo_rsrc/pengo_down1.png");
		this.down2 = graphics.load("pengo_rsrc/pengo_down2.png");

		this.up1 = graphics.load("pengo_rsrc/pengo_up1.png");
		this.up2 = graphics.load("pengo_rsrc/pengo_up2.png");

		this.right1 = graphics.load("pengo_rsrc/pengo_right1.png");
		this.right2 = graphics.load("pengo_rsrc/pengo_right2.png");

		this.left1 = graphics.load("pengo_rsrc/pengo_left1.png");
		this.left2 = graphics.load("pengo_rsrc/pengo_left2.png");
	}

	private BufferedImage imageForOrientation(int orientation) {
		int activeFrame = this.pengo.isMoving() ? this.frame : 0;
		switch (orientation) {
		case Orientation.EAST:
			return activeFrame == 0 ? this.right1 : this.right2;
		case Orientation.SOUTH:
			return activeFrame == 0 ? this.down1 : this.down2;
		case Orientation.WEST:
			return activeFrame == 0 ? this.left1 : this.left2;
		case Orientation.NORTH:
			return activeFrame == 0 ? this.up1 : this.up2;
		default:
			return this.down1;
		}
	}
}