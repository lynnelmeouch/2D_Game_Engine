package game.view;

import java.util.HashMap;
import java.util.Map;

import engine.model.Orientation;
import engine.view.Avatar;
import engine.view.SpawnAnimation;
import engine.view.View;
import game.model.entity.Enemies;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;


public final class EnemiesAvatar extends Avatar {

	private final Enemies enemy;
	private final Map<String, BufferedImage> images;
	private final SpawnAnimation spawnAnimation;
	private long crushedElapsed_ms;
	private String lastVisibleStateName = "Normal";

	public EnemiesAvatar(View view, Enemies enemy) {
		super(view, enemy);
		this.enemy = enemy;
		this.images = new HashMap<String, BufferedImage>();
		this.spawnAnimation = new SpawnAnimation();
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;
		this.spawnAnimation.tick(elapsed);
		if (this.enemy.isCrushed()) {
			this.crushedElapsed_ms += elapsed;

			if (this.crushedElapsed_ms >= Enemies.CRUSH_DISPLAY_MS) {
				this.enemy.finishCrushDeath();
			}
		} else {
			this.crushedElapsed_ms = 0L;
		}
	}

	@Override
	public void paint(Graphics graphics) {
		BufferedImage image = imageForStateAndOrientation(graphics);
		double revealScale = this.view.revealScale(this.enemy);
		if (revealScale <= 0.0) {
			return;
		}
		if (this.enemy.isCrushed()) {
			paintCrushed(graphics, image, revealScale);
			return;
		}
		int centerX = this.view.toPixelX(this.enemy.center().x());
		int centerY = this.view.toPixelY(this.enemy.center().y());
		int width = this.view.toPixelWidth(this.enemy.size().x() * revealScale);
		int height = this.view.toPixelHeight(this.enemy.size().y() * revealScale);
		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	private void paintCrushed(Graphics graphics, BufferedImage image, double revealScale) {
		Enemies.CrushGeometry geometry = this.enemy.currentCrushGeometry();
		double centerX_cm;
		double centerY_cm;
		double width_cm;
		double height_cm;
		if (geometry != null) {
			centerX_cm = geometry.centerX();
			centerY_cm = geometry.centerY();
			width_cm = geometry.width();
			height_cm = geometry.height();
		} else {
			centerX_cm = this.enemy.center().x();
			centerY_cm = this.enemy.center().y();
			width_cm = this.enemy.crushedDisplayWidthCm();
			height_cm = this.enemy.crushedDisplayHeightCm();
		}
		int centerX = this.view.toPixelX(centerX_cm);
		int centerY = this.view.toPixelY(centerY_cm);
		int width = Math.max(1, this.view.toPixelWidth(width_cm * revealScale));
		int height = Math.max(1, this.view.toPixelHeight(height_cm * revealScale));
		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	private BufferedImage imageForStateAndOrientation(Graphics graphics) {
		if (this.enemy.isCrushed()) {
			String path = this.enemy.crushedHorizontal() ? "pengo_rsrc/ecrasee_horizontal.png"
					: "pengo_rsrc/ecrasee_vertical.png";
			return imageOrNull(graphics, path);
		}
		String direction = directionName(this.enemy.orientation());
		if (this.enemy.isFrozen()) {
			BufferedImage frozen = imageOrNull(graphics, "peur", direction);
			if (frozen != null) {
				return frozen;
			}
		}
		String visibleState = visibleStateName(this.enemy.galState());
		String state = normalizeStateName(visibleState);
		BufferedImage stateImage = imageOrNull(graphics, state, direction);
		if (stateImage != null) {
			return stateImage;
		}
		BufferedImage fallback = imageOrNull(graphics, "normal", direction);
		if (fallback != null) {
			return fallback;
		}
		throw new IllegalStateException("Missing enemy fallback image normal_" + direction + ".png");
	}

	private String visibleStateName(String stateName) {
		if (stateName == null || stateName.trim().length() == 0) {
			return this.lastVisibleStateName;
		}
		String cleanStateName = stateName.trim();
		if ("Brain".equalsIgnoreCase(cleanStateName)) {
			return this.lastVisibleStateName;
		}
		this.lastVisibleStateName = cleanStateName;
		return cleanStateName;
	}

	private BufferedImage imageOrNull(Graphics graphics, String state, String direction) {
		String path = "pengo_rsrc/" + state + "_" + direction + ".png";
		return imageOrNull(graphics, path);
	}

	private BufferedImage imageOrNull(Graphics graphics, String path) {
		if (this.images.containsKey(path)) {
			return this.images.get(path);
		}
		try {
			BufferedImage image = graphics.load(path);
			this.images.put(path, image);
			return image;
		} catch (RuntimeException exception) {
			this.images.put(path, null);
			return null;
		} catch (AssertionError error) {
			this.images.put(path, null);
			return null;
		}
	}

	private static String normalizeStateName(String state) {
		if (state == null || state.trim().length() == 0) {
			return "normal";
		}
		return state.trim().toLowerCase();
	}

	private static String directionName(int orientation) {
		int direction = Orientation.normalizeCardinal(orientation);
		switch (direction) {
		case 0:
			return "right";
		case 90:
			return "down";
		case 180:
			return "left";
		case 270:
			return "up";
		default:
			return "down";
		}
	}

}