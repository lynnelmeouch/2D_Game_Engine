package engine.view;

import java.util.List;

import engine.model.Entity;
import engine.model.Hitbox;
import oop.graphics.Font;
import oop.graphics.Graphics;

final class DebugRenderer {

	private DebugRenderer() {
	}

	static void paintWorldDebug(Graphics graphics, View view, List<Entity> entities) {
		DebugOptions options = view.debugOptions();

		if (options == null || !options.enabled()) {
			return;
		}

		Font oldFont = graphics.getFont();
		graphics.setFont(graphics.getFont("Monospaced", Font.BOLD, 10));

		for (Entity entity : entities) {
			if (entity == null || !entity.alive() || !entity.installed()) {
				continue;
			}

			if (options.showBoundingBoxes()) {
				paintBoundingBox(graphics, view, entity);
			}

			if (options.showActions()) {
				paintDebugLabel(graphics, view, entity);
			}
		}

		graphics.setFont(oldFont);
	}

	private static void paintBoundingBox(Graphics graphics, View view, Entity entity) {
		if (entity.bounding() == null || entity.bounding().isEmpty()) {
			return;
		}

		Hitbox hitbox = entity.bounding().hitbox();

		int x = view.toPixelX(hitbox.xmin());
		int y = view.toPixelY(hitbox.ymin());
		int w = view.toPixelWidth(hitbox.xmax() - hitbox.xmin());
		int h = view.toPixelHeight(hitbox.ymax() - hitbox.ymin());

		if (w < 1) {
			w = 1;
		}

		if (h < 1) {
			h = 1;
		}

		graphics.setColor(Graphics.Colors.green);
		graphics.drawRect(x, y, w, h);
	}

	private static void paintDebugLabel(Graphics graphics, View view, Entity entity) {
		String label = entity.debugLabel();

		if (label == null || label.length() == 0) {
			return;
		}

		int x = view.toPixelX(entity.center().x()) - 28;
		int y = view.toPixelY(entity.center().y()) - view.toPixelHeight(entity.size().y() / 2.0) - 6;

		if (y < view.overlayHeightPixel() + 10) {
			y = view.overlayHeightPixel() + 10;
		}

		graphics.setColor(Graphics.Colors.black);
		graphics.drawString(label, x + 1, y + 1);

		graphics.setColor(Graphics.Colors.yellow);
		graphics.drawString(label, x, y);
	}

	static void paintPerformance(Graphics graphics, View view) {
		DebugOptions options = view.debugOptions();
		PerformanceMonitor monitor = view.performanceMonitor();

		if (options == null || monitor == null || !options.showPerformance()) {
			return;
		}

		int x = 12;
		int y = view.overlayHeightPixel() + 12;
		int w = 360;
		int h = options.loadTest() ? 116 : 98;

		graphics.setColor(graphics.getColor(210, 0, 0, 0));
		graphics.fillRect(x, y, w, h);

		graphics.setColor(Graphics.Colors.cyan);
		graphics.drawRect(x, y, w, h);

		Font oldFont = graphics.getFont();
		graphics.setFont(graphics.getFont("Monospaced", Font.BOLD, 12));

		graphics.setColor(Graphics.Colors.white);
		graphics.drawString("DEBUG  F1:on/off F2:box F3:state F4:perf F5:load", x + 8, y + 18);

		graphics.setColor(Graphics.Colors.lightGray);
		graphics.drawString("TICK  " + formatMs(monitor.tickStats()), x + 8, y + 40);
		graphics.drawString("PAINT " + formatMs(monitor.paintStats()), x + 8, y + 58);
		graphics.drawString("FPS   " + formatFps(monitor.fpsStats()), x + 8, y + 76);
		graphics.drawString("ENTITIES " + view.model().entities().size(), x + 8, y + 94);

		if (options.loadTest()) {
			graphics.setColor(Graphics.Colors.red);
			graphics.drawString("LOAD TEST ACTIVE", x + 150, y + 94);
		}

		graphics.setFont(oldFont);
	}

	private static String formatMs(TimeStats stats) {
		return "min=" + round(stats.min()) + " avg=" + round(stats.average()) + " max=" + round(stats.max()) + " ms";
	}

	private static String formatFps(TimeStats stats) {
		return "min=" + round(stats.min()) + " avg=" + round(stats.average()) + " max=" + round(stats.max());
	}

	private static int round(double value) {
		return (int) Math.round(value);
	}
}