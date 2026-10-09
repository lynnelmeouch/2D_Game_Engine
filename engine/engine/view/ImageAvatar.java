package engine.view;

import engine.model.Entity;
import engine.model.ISU;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public abstract class ImageAvatar extends Avatar {

	private final String[] paths;
	private final BufferedImage[] images;

	private int currentFrame;

	private final SpawnAnimation spawnAnimation;

	public ImageAvatar(View view, Entity entity, String[] paths) {
		super(view, entity);

		assert paths.length > 0;

		this.paths = paths.clone();
		this.images = new BufferedImage[paths.length];

		for (String path : this.paths) {
			assert !path.isEmpty();
		}

		this.spawnAnimation = new SpawnAnimation();
	}

	protected final int frameCount() {
		return this.paths.length;
	}

	protected final int currentFrame() {
		return this.currentFrame;
	}

	protected final void setCurrentFrame(int frameIndex) {
		assert frameIndex >= 0;
		assert frameIndex < frameCount();

		this.currentFrame = frameIndex;
	}

	protected final void nextFrame() {
		this.currentFrame = (this.currentFrame + 1) % frameCount();
	}

	protected final void resetFrame() {
		this.currentFrame = 0;
	}

	protected final String pathAt(int frameIndex) {
		assert frameIndex >= 0;
		assert frameIndex < frameCount();

		return this.paths[frameIndex];
	}

	protected BufferedImage image(Graphics graphics) {

		if (this.images[this.currentFrame] == null) {
			this.images[this.currentFrame] = graphics.load(this.paths[this.currentFrame]);
		}

		return this.images[this.currentFrame];
	}

	protected double visualScale() {
		return 1.0;
	}

	protected boolean rotateWithEntity() {
		return true;
	}

	protected final void tickSpawnAnimation(long elapsed) {
		this.spawnAnimation.tick(elapsed);
	}

	protected final double spawnAnimationScale() {
		return this.spawnAnimation.scale();
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		tickSpawnAnimation(elapsed);
	}

	@Override
	public void paint(Graphics graphics) {

		BufferedImage image = image(graphics);

		ISU.Dimension size = this.entity.size();
		ISU.Coord center = this.entity.center();

		double scale = visualScale() * this.view.revealScale(this.entity);

		if (scale <= 0.0) {
			return;
		}

		int width = this.view.toPixelWidth(size.x() * scale);
		int height = this.view.toPixelHeight(size.y() * scale);

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

		graphics.drawImage(image, x, y, width, height);

		graphics.setTransform(savedTransform);
	}
}