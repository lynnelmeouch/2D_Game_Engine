package game.view;

import java.util.EnumMap;
import java.util.Map;

import engine.view.Avatar;
import engine.view.SpawnAnimation;
import engine.view.View;
import game.model.entity.Power;
import game.model.entity.PowerType;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public final class PowerAvatar extends Avatar {

	private final Power power;
	private final Map<PowerType, BufferedImage> images;
	private final SpawnAnimation spawnAnimation;

	private boolean imagesLoaded;

	public PowerAvatar(View view, Power power) {
		super(view, power);
		this.power = power;
		this.images = new EnumMap<PowerType, BufferedImage>(PowerType.class);
		this.spawnAnimation = new SpawnAnimation();
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		this.spawnAnimation.tick(elapsed);
	}

	@Override
	public void paint(Graphics graphics) {
		loadImages(graphics);

		BufferedImage image = this.images.get(this.power.getType());

		if (image == null) {
			return;
		}

		int centerX = this.view.toPixelX(this.power.center().x());
		int centerY = this.view.toPixelY(this.power.center().y());

		double revealScale = this.view.revealScale(this.power);

		if (revealScale <= 0.0) {
			return;
		}

		int width = this.view.toPixelWidth(this.power.size().x() * revealScale);
		int height = this.view.toPixelHeight(this.power.size().y() * revealScale);

		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	private void loadImages(Graphics graphics) {
		if (this.imagesLoaded) {
			return;
		}

		this.images.put(PowerType.SHIELD, graphics.load("pengo_rsrc/shield.png"));
		this.images.put(PowerType.SPEED, graphics.load("pengo_rsrc/piment.png"));
		this.images.put(PowerType.FREEZE, graphics.load("pengo_rsrc/bonbon.png"));
		this.images.put(PowerType.STAR, graphics.load("pengo_rsrc/etoile.png"));
		this.images.put(PowerType.EXTRA_LIFE, graphics.load("pengo_rsrc/crevette.png"));
		this.images.put(PowerType.BOOMFISH, graphics.load("pengo_rsrc/boom_fish.png"));
		this.images.put(PowerType.FRIEND_ACTIVATOR, graphics.load("pengo_rsrc/friend_activator.png"));

		this.imagesLoaded = true;
	}
}