package engine.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;
import engine.model.ViewPort;
import oop.graphics.Graphics;

public class View {

	private final Model model;

	private int widthPixel;
	private int heightPixel;
	private StartupReveal startupReveal;

	private DebugOptions debugOptions;
	private PerformanceMonitor performanceMonitor;

	private Overlay overlay;

	public View(Model model, int widthPixel, int heightPixel) {
		this(model, widthPixel, heightPixel, null);
	}

	public View(Model model, int widthPixel, int heightPixel, Overlay overlay) {
		assert widthPixel > 0;
		assert heightPixel > 0;

		this.model = model;
		this.widthPixel = widthPixel;
		this.heightPixel = heightPixel;
		this.overlay = overlay;
	}

	public Model model() {
		return this.model;
	}

	public ViewPort viewPort() {
		return this.model.viewPort();
	}

	public int widthPixel() {
		return this.widthPixel;
	}

	public int heightPixel() {
		return this.heightPixel;
	}

	public int overlayWidthPixel() {
		if (this.overlay == null) {
			return 0;
		}

		return this.overlay.widthPixel();
	}

	public int overlayHeightPixel() {
		if (this.overlay == null) {
			return 0;
		}

		return this.overlay.heightPixel();
	}

	public int totalWidthPixel() {
		return this.widthPixel + overlayWidthPixel();
	}

	public int totalHeightPixel() {
		return this.heightPixel + overlayHeightPixel();
	}

	public Overlay overlay() {
		return this.overlay;
	}

	public void setOverlay(Overlay overlay) {
		this.overlay = overlay;
	}

	public void removeOverlay() {
		this.overlay = null;
	}

	public void startStartupReveal() {
		this.startupReveal = new StartupReveal(this.model.entities());
	}

	public boolean startupRevealActive() {
		return this.startupReveal != null && this.startupReveal.active();
	}

	public double revealScale(Entity entity) {

		if (this.startupReveal == null) {
			return 1.0;
		}

		return this.startupReveal.scale(entity);
	}

	public void resize(int widthPixel, int heightPixel) {
		assert widthPixel > 0;
		assert heightPixel > 0;

		this.widthPixel = widthPixel;
		this.heightPixel = heightPixel;
	}

	/*
	 * On utilise une seule échelle pour X et Y. Comme ça, la map garde ses
	 * proportions.
	 */
	public double worldScale() {
		double scaleX = this.widthPixel / this.viewPort().width();
		double scaleY = this.heightPixel / this.viewPort().height();

		return Math.min(scaleX, scaleY);
	}

	public double scaleX() {
		return worldScale();
	}

	public double scaleY() {
		return worldScale();
	}

	public int renderedWorldWidthPixel() {
		return (int) Math.round(this.viewPort().width() * worldScale());
	}

	public int renderedWorldHeightPixel() {
		return (int) Math.round(this.viewPort().height() * worldScale());
	}

	/*
	 * Centre la map horizontalement dans l'espace disponible.
	 */
	public int worldOffsetXPixel() {
		return Math.max(0, (this.widthPixel - renderedWorldWidthPixel()) / 2);
	}

	/*
	 * Centre la map verticalement sous le HUD.
	 */
	public int worldOffsetYPixel() {
		return overlayHeightPixel() + Math.max(0, (this.heightPixel - renderedWorldHeightPixel()) / 2);
	}

	public int toPixelX(double xIsu) {
		double translated = translatedX(xIsu);

		return worldOffsetXPixel() + (int) Math.round(translated * worldScale());
	}

	public int toPixelY(double yIsu) {
		double translated = translatedY(yIsu);

		return worldOffsetYPixel() + (int) Math.round(translated * worldScale());
	}

	private double translatedX(double xIsu) {
		double translated = xIsu - this.viewPort().x();

		if (this.model.isu().xAxis().onTorus()) {
			translated = this.model.isu().xAxis().modp(translated, this.model.isu().width());
		}

		return translated;
	}

	private double translatedY(double yIsu) {
		double translated = yIsu - this.viewPort().y();

		if (this.model.isu().yAxis().onTorus()) {
			translated = this.model.isu().yAxis().modp(translated, this.model.isu().height());
		}

		return translated;
	}

	public int toPixelWidth(double widthIsu) {
		return (int) Math.round(widthIsu * worldScale());
	}

	public int toPixelHeight(double heightIsu) {
		return (int) Math.round(heightIsu * worldScale());
	}

	public ISU.Coord toISUCoord(int xPixel, int yPixel) {
		int left = worldOffsetXPixel();
		int top = worldOffsetYPixel();
		int right = left + renderedWorldWidthPixel() - 1;
		int bottom = top + renderedWorldHeightPixel() - 1;

		int safeX = clampInt(xPixel, left, right);
		int safeY = clampInt(yPixel, top, bottom);

		double xIsu = this.viewPort().x() + (safeX - left) / worldScale();
		double yIsu = this.viewPort().y() + (safeY - top) / worldScale();

		return this.model.isu().new Coord(xIsu, yIsu);
	}

	private static int clampInt(int value, int min, int max) {
		if (value < min) {
			return min;
		}

		if (value > max) {
			return max;
		}

		return value;
	}

	public void paint(Graphics graphics) {

		Object savedTransform = graphics.getTransform();

		int worldX = worldOffsetXPixel();
		int worldY = worldOffsetYPixel();
		int worldW = renderedWorldWidthPixel();
		int worldH = renderedWorldHeightPixel();

		/*
		 * Fond noir global sous le HUD.
		 */
		graphics.setColor(Graphics.Colors.black);
		graphics.fillRect(0, overlayHeightPixel(), this.widthPixel, this.heightPixel);

		/*
		 * On clippe seulement la zone réelle où la map est affichée.
		 */
		graphics.setClip(worldX, worldY, worldW, worldH);

		List<Entity> visible = new ArrayList<Entity>(this.model.visibleEntities());

		visible.sort(new Comparator<Entity>() {
			@Override
			public int compare(Entity first, Entity second) {
				if (first.zOrder() != second.zOrder()) {
					return Integer.compare(first.zOrder(), second.zOrder());
				}

				return Integer.compare(first.sOrder(), second.sOrder());
			}
		});

		for (Entity entity : visible) {
			if (!entity.alive() || !entity.installed() || entity.avatar() == null) {
				continue;
			}

			if (this.startupReveal != null && !this.startupReveal.visible(entity)) {
				continue;
			}

			entity.avatar().paint(graphics);
		}

		graphics.setTransform(savedTransform);
		graphics.setClip(worldX, worldY, worldW, worldH);

		DebugRenderer.paintWorldDebug(graphics, this, visible);

		/*
		 * HUD / overlay.
		 */
		if (this.overlay != null) {
			graphics.setClip(0, 0, totalWidthPixel(), totalHeightPixel());

			if (this.overlay.heightPixel() > 0) {
				this.overlay.paint(graphics, this, 0, 0);
			}

			if (this.overlay.widthPixel() > 0) {
				this.overlay.paint(graphics, this, this.widthPixel, overlayHeightPixel());
			}
		}

		/*
		 * Debug performance par-dessus tout.
		 */
		graphics.setClip(0, 0, totalWidthPixel(), totalHeightPixel());
		DebugRenderer.paintPerformance(graphics, this);
	}

	public void tick(long elapsed) {
		assert elapsed >= 0;

		if (this.startupReveal != null) {
			this.startupReveal.tick(elapsed);
		}

		for (Entity entity : this.model.visibleEntities()) {
			if (!entity.alive() || !entity.installed() || entity.avatar() == null) {
				continue;
			}

			entity.avatar().tick(elapsed);
		}

		if (this.overlay != null) {
			this.overlay.tick(elapsed);
		}
	}

	// debug methodes
	public DebugOptions debugOptions() {
		return this.debugOptions;
	}

	public void setDebugOptions(DebugOptions debugOptions) {
		this.debugOptions = debugOptions;
	}

	public PerformanceMonitor performanceMonitor() {
		return this.performanceMonitor;
	}

	public void setPerformanceMonitor(PerformanceMonitor performanceMonitor) {
		this.performanceMonitor = performanceMonitor;
	}

}