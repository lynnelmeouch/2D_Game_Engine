package engine.view;

import oop.graphics.Canvas;
import oop.graphics.Graphics;

public class Painter implements Canvas.PaintListener {

	private View view;
	private Canvas canvas;
	private PerformanceMonitor performanceMonitor;

	public Painter(View view) {
		this(view, null);
	}

	public Painter(View view, PerformanceMonitor performanceMonitor) {
		this.view = view;
		this.performanceMonitor = performanceMonitor;
	}

	public Canvas canvas() {
		return this.canvas;
	}

	@Override
	public void visible(Canvas canvas) {
		this.canvas = canvas;
		canvas.repaint();
	}

	@Override
	public void paint(Canvas canvas, Graphics g) {

		if (this.performanceMonitor != null) {
			this.performanceMonitor.recordPaintEvent();
		}

		g.setColor(Graphics.Colors.black);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		this.view.paint(g);
	}

	@Override
	public void revoked(Canvas canvas) {
		this.canvas = null;
	}
}