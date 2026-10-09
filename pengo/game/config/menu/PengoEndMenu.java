package game.config.menu;

import oop.graphics.Canvas;
import oop.graphics.Font;
import oop.graphics.Graphics;
import oop.graphics.VirtualKeyCodes;

public final class PengoEndMenu {

	public enum Action {
		NONE, RETRY, MAIN_MENU
	}

	// Pour adapter le menu aux differents ecrans.
	private static final int BASE_WIDTH_PIXEL = 950;
	private static final int BASE_HEIGHT_PIXEL = 600;

	// Dimensions du panneau de fin
	private static final int PANEL_W = 470;
	private static final int PANEL_H = 285;

	// Dimensions des boutons
	private static final int BUTTON_W = 250;
	private static final int BUTTON_H = 48;
	private static final int BUTTON_GAP = 18;

	
	public void paint(Canvas canvas, Graphics g, boolean win) {
		Font oldFont = g.getFont();
		
		// Fond sombre au-dessus du jeu
		g.setColor(g.getColor(185, 0, 0, 0));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		int panelX = panelX(canvas);
		int panelY = panelY(canvas);
		int panelW = panelW(canvas);
		int panelH = panelH(canvas);
		
		// Panneau central
		g.setColor(g.getColor(230, 5, 8, 22));
		g.fillRect(panelX, panelY, panelW, panelH);

		// Bordure jaune si victoire, rouge si defaite
		g.setColor(win ? Graphics.Colors.yellow : Graphics.Colors.red);
		
		g.drawRect(panelX, panelY, panelW, panelH);
		g.drawRect(panelX + scaled(canvas, 4), panelY + scaled(canvas, 4), panelW - scaled(canvas, 8),
				panelH - scaled(canvas, 8));

		Font titleFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 38));
		Font helpFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 14));

		g.setFont(titleFont);
		g.setColor(win ? Graphics.Colors.yellow : Graphics.Colors.red);
		drawCenteredString(canvas, g, win ? "YOU WIN" : "YOU LOSE", panelY + scaled(canvas, 62));

		// En cas de victoire : seulement retour menu
		if (win) {
			drawButton(canvas, g, 0, "MAIN MENU");
		} else {
			// En cas de defaite : retry + menu
			drawButton(canvas, g, 0, "TRY AGAIN");
			drawButton(canvas, g, 1, "MAIN MENU");
		}

		g.setFont(helpFont);
		g.setColor(Graphics.Colors.lightGray);

		if (win) {
			drawCenteredString(canvas, g, "ENTER / M : menu principal", panelY + panelH - scaled(canvas, 28));
		} else {
			drawCenteredString(canvas, g, "ENTER / R : rejouer  |  M : menu principal",
					panelY + panelH - scaled(canvas, 28));
		}

		g.setFont(oldFont);
	}

	public Action keyPressed(boolean win, boolean lost, int keyCode, char keyChar) {
		char lower = Character.toLowerCase(keyChar);

		if (lost && (keyCode == VirtualKeyCodes.VK_ENTER || lower == 'r')) {
			return Action.RETRY;
		}

		if (win && keyCode == VirtualKeyCodes.VK_ENTER) {
			return Action.MAIN_MENU;
		}

		if (lower == 'm' || keyCode == VirtualKeyCodes.VK_ESCAPE) {
			return Action.MAIN_MENU;
		}

		return Action.NONE;
	}

	public Action mousePressed(Canvas canvas, int x, int y, boolean win) {
		if (!win && inside(x, y, buttonX(canvas), buttonY(canvas, 0), buttonW(canvas), buttonH(canvas))) {
			return Action.RETRY;
		}

		int mainMenuIndex = win ? 0 : 1;

		if (inside(x, y, buttonX(canvas), buttonY(canvas, mainMenuIndex), buttonW(canvas), buttonH(canvas))) {
			return Action.MAIN_MENU;
		}

		return Action.NONE;
	}

	private void drawButton(Canvas canvas, Graphics g, int index, String text) {
		int x = buttonX(canvas);
		int y = buttonY(canvas, index);
		int w = buttonW(canvas);
		int h = buttonH(canvas);

		g.setColor(g.getColor(220, 0, 0, 0));
		g.fillRect(x, y, w, h);

		g.setColor(Graphics.Colors.cyan);
		g.drawRect(x, y, w, h);

		Font oldFont = g.getFont();

		g.setFont(g.getFont("Arial", Font.BOLD, scaled(canvas, 17)));
		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, text, y + scaled(canvas, 31));

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
		int start = panelY(canvas) + scaled(canvas, 105);
		return start + index * (buttonH(canvas) + scaled(canvas, BUTTON_GAP));
	}

	private boolean inside(int px, int py, int x, int y, int w, int h) {
		return px >= x && px <= x + w && py >= y && py <= y + h;
	}
}