package engine.view;

import engine.model.Entity;

public abstract class AnimatedImageAvatar extends ImageAvatar {

	private final long frameDuration;

	private long elapsedSinceFrame;
	private int animatedOrientation;

	public AnimatedImageAvatar(View view, Entity entity, String[] paths, long frameDuration) {
		super(view, entity, paths);

		assert frameDuration > 0L;

		this.frameDuration = frameDuration;
		this.animatedOrientation = entity.orientation();
	}

	public final long frameDuration() {
		return this.frameDuration;
	}

	protected boolean shouldAnimate() {
		return this.entity.isMoving();
	}

	protected boolean resetOnOrientationChange() {
		return true;
	}

	protected final void resetAnimation() {
		this.elapsedSinceFrame = 0L;
		resetFrame();
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		tickSpawnAnimation(elapsed);

		if (!this.entity.alive() || !this.entity.installed()) {
			resetAnimation();
			return;
		}

		int orientation = this.entity.orientation();

		if (resetOnOrientationChange() && orientation != this.animatedOrientation) {
			this.animatedOrientation = orientation;
			resetAnimation();
		}

		if (!shouldAnimate()) {
			resetAnimation();
			return;
		}

		this.elapsedSinceFrame += elapsed;

		while (this.elapsedSinceFrame >= this.frameDuration) {
			this.elapsedSinceFrame -= this.frameDuration;
			nextFrame();
		}
	}
}	