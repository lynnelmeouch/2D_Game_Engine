package game.config.menu;

import oop.graphics.Canvas;
import oop.graphics.Font;
import oop.graphics.Graphics;

public final class PengoPauseMenu {

	public enum Action {
		NONE, RESUME, MENU, QUIT
	}

	// pour adapter le menu aux differents ecrans
	private static final int BASE_WIDTH_PIXEL = 950;
	private static final int BASE_HEIGHT_PIXEL = 600;

	// menu de pause
	private static final int PANEL_W = 430;
	private static final int PANEL_H = 310;
	private static final int BUTTON_W = 260;
	private static final int BUTTON_H = 46;
	private static final int BUTTON_GAP = 18;

	public void paint(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();

		g.setColor(g.getColor(175, 0, 0, 0));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		int panelX = panelX(canvas);
		int panelY = panelY(canvas);
		int panelW = panelW(canvas);
		int panelH = panelH(canvas);

		g.setColor(g.getColor(225, 5, 8, 22));
		g.fillRect(panelX, panelY, panelW, panelH);

		g.setColor(Graphics.Colors.white);
		g.drawRect(panelX, panelY, panelW, panelH);

		Font titleFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 34));
		Font textFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 15));

		g.setFont(titleFont);
		g.setColor(Graphics.Colors.yellow);
		drawCenteredString(canvas, g, "PAUSE", panelY + scaled(canvas, 58));

		drawButton(canvas, g, 0, "REPRENDRE");
		drawButton(canvas, g, 1, "MENU INITIAL");
		drawButton(canvas, g, 2, "QUITTER");

		g.setFont(textFont);
		g.setColor(Graphics.Colors.lightGray);
		drawCenteredString(canvas, g, "ESC/ENTER reprendre  |  M menu  |  Q quitter",
				panelY + panelH - scaled(canvas, 28));

		g.setFont(oldFont);
	}

	public Action mousePressed(Canvas canvas, int x, int y) {
		if (inside(x, y, buttonX(canvas), buttonY(canvas, 0), buttonW(canvas), buttonH(canvas))) {
			return Action.RESUME;
		}

		if (inside(x, y, buttonX(canvas), buttonY(canvas, 1), buttonW(canvas), buttonH(canvas))) {
			return Action.MENU;
		}

		if (inside(x, y, buttonX(canvas), buttonY(canvas, 2), buttonW(canvas), buttonH(canvas))) {
			return Action.QUIT;
		}

		return Action.NONE;
	}

	private void drawButton(Canvas canvas, Graphics g, int index, String text) {
		int x = buttonX(canvas);
		int y = buttonY(canvas, index);
		int w = buttonW(canvas);
		int h = buttonH(canvas);

		g.setColor(g.getColor(210, 0, 0, 0));
		g.fillRect(x, y, w, h);

		g.setColor(Graphics.Colors.cyan);
		g.drawRect(x, y, w, h);

		Font oldFont = g.getFont();

		g.setFont(g.getFont("Arial", Font.BOLD, scaled(canvas, 17)));
		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, text, y + scaled(canvas, 30));

		g.setFont(oldFont);
	}

	private void drawCenteredString(Canvas canvas, Graphics g, String text, int y) {
		int x = (canvas.getWidth() - g.getFont().getWidth(text)) / 2;
		g.drawString(text, x, y);
	}

	private double uiScale(Canvas canvas) {
		double scaleX = canvas.getWidth() / (double) BASE_WIDTH_PIXEL;
		double scaleY = canvas.getHeight() / (double) BASE_HEIGHT_PIXEL;

		double scale = Math.min(scaleX, scaleY);

		if (scale < 0.75) {
			return 0.75;
		}

		if (scale > 1.8) {
			return 1.8;
		}

		return scale;
	}

	private int scaled(Canvas canvas, int value) {
		return (int) Math.round(value * uiScale(canvas));
	}

	private int panelW(Canvas canvas) {
		return scaled(canvas, PANEL_W);
	}

	private int panelH(Canvas canvas) {
		return scaled(canvas, PANEL_H);
	}

	private int panelX(Canvas canvas) {
		return (canvas.getWidth() - panelW(canvas)) / 2;
	}

	private int panelY(Canvas canvas) {
		return (canvas.getHeight() - panelH(canvas)) / 2;
	}

	private int buttonW(Canvas canvas) {
		return scaled(canvas, BUTTON_W);
	}

	private int buttonH(Canvas canvas) {
		return scaled(canvas, BUTTON_H);
	}

	private int buttonX(Canvas canvas) {
		return (canvas.getWidth() - buttonW(canvas)) / 2;
	}

	private int buttonY(Canvas canvas, int index) {
		int start = panelY(canvas) + scaled(canvas, 92);
		return start + index * (buttonH(canvas) + scaled(canvas, BUTTON_GAP));
	}

	private boolean inside(int px, int py, int x, int y, int w, int h) {
		return px >= x && px <= x + w && py >= y && py <= y + h;
	}
}