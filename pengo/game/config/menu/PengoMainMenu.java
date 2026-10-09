package game.config.menu;

import game.config.map.PengoMapChoice;
import game.config.map.PengoMaps;
import oop.graphics.BufferedImage;
import oop.graphics.Canvas;
import oop.graphics.Font;
import oop.graphics.Graphics;

public class PengoMainMenu {

	public enum Action {
		NONE, START
	}

	// pour adapter le menu aux differents ecrans
	private static final int BASE_WIDTH_PIXEL = 950;
	private static final int BASE_HEIGHT_PIXEL = 600;

	// dimension de base du logo
	private static final int LOGO_W = 360;
	private static final int LOGO_H = 150;

	// selecteur de la map
	private static final int MENU_W = 430;
	private static final int MENU_H = 40;
	private static final int ARROW_W = 48;

	// liste map
	private static final int LIST_LINE_H = 34;
	private static final int MAX_VISIBLE_MAPS = 6;

	// boutton start
	private static final int START_W = 230;
	private static final int START_H = 52;

	// FIELDS
	private final PengoMapChoice[] maps;

	private int selectedMapIndex;
	private int firstVisibleMapIndex;

	private boolean mapListOpen;

	private BufferedImage backgroundImage;
	private BufferedImage logoImage;

	// CONSTRUCTEUR
	public PengoMainMenu() {
		this.maps = PengoMaps.all();
	}

	public void paint(Canvas canvas, Graphics g) {
		// affichage de la page principale
		drawBackground(canvas, g);
		drawLogo(canvas, g);
		drawMapSelector(canvas, g);
		drawStartButton(canvas, g);
		drawHelp(canvas, g);
		if (mapListOpen) {
			drawMapList(canvas, g);
		}
	}

	public Action mousePressed(Canvas canvas, int x, int y) {
		if (inside(x, y, arrowX(canvas), arrowY(canvas), arrowW(canvas), menuH(canvas))
				|| inside(x, y, menuX(canvas), menuY(canvas), menuW(canvas), menuH(canvas))) {
			this.mapListOpen = !this.mapListOpen;
			return Action.NONE;
		}

		if (this.mapListOpen && clickOnMapList(canvas, x, y)) {
			this.mapListOpen = false;
			return Action.NONE;
		}

		if (inside(x, y, startX(canvas), startY(canvas), startW(canvas), startH(canvas))) {
			return Action.START;
		}

		this.mapListOpen = false;
		return Action.NONE;
	}

	public void moveSelection(int delta) {
		if (this.maps.length == 0) {
			return;
		}

		this.selectedMapIndex += delta;

		if (this.selectedMapIndex < 0) {
			this.selectedMapIndex = this.maps.length - 1;
		} else if (this.selectedMapIndex >= this.maps.length) {
			this.selectedMapIndex = 0;
		}

		updateVisibleWindow();
	}

	public boolean hasMaps() {
		return this.maps.length > 0;
	}

	public PengoMapChoice selectedMap() {
		assert this.maps.length > 0;
		assert this.selectedMapIndex >= 0;
		assert this.selectedMapIndex < this.maps.length;

		return this.maps[this.selectedMapIndex];
	}

	// DRAW

	// photo background page principale
	private void drawBackground(Canvas canvas, Graphics g) {
		if (backgroundImage == null) {
			//image generée pas l'IA
			backgroundImage = g.load("pengo_rsrc/menu_background.png");
		}
		g.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
	}

	// photo pengo logo
	private void drawLogo(Canvas canvas, Graphics g) {
		if (logoImage == null) {
			logoImage = g.load("pengo_rsrc/pengo_logo.png");
		}
		int logoW = scaled(canvas, LOGO_W);
		int logoH = scaled(canvas, LOGO_H);
		int x = (canvas.getWidth() - logoW) / 2;
		int y = logoY(canvas);

		g.drawImage(logoImage, x, y, logoW, logoH);
	}

	private void drawMapSelector(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font labelFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 18));
		Font valueFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 16));

		int menuX = menuX(canvas);
		int menuY = menuY(canvas);
		int menuW = menuW(canvas);
		int menuH = menuH(canvas);

		g.setFont(labelFont);
		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, "CHOISIS TA MAP", menuY - scaled(canvas, 18));

		g.setColor(g.getColor(185, 0, 0, 0));
		g.fillRect(menuX, menuY, menuW, menuH);

		g.setColor(Graphics.Colors.white);
		g.drawRect(menuX, menuY, menuW, menuH);

		if (maps.length == 0) {
			g.setColor(Graphics.Colors.yellow);
			drawCenteredString(canvas, g, "Aucune map trouvee", menuY + scaled(canvas, 27));
			g.setFont(oldFont);
			return;
		}

		PengoMapChoice selected = selectedMap();

		g.setFont(valueFont);

		String nameText = selected.label();
		String infoText = selected.width() + "x" + selected.height() + " | " + selected.geometryLabel();

		int textY = menuY + scaled(canvas, 27);
		int nameX = menuX + scaled(canvas, 20);

		int arrowX = arrowX(canvas);
		int infoRight = arrowX - scaled(canvas, 12);
		int infoX = infoRight - g.getFont().getWidth(infoText);

		g.setColor(Graphics.Colors.white);
		g.drawString(nameText, nameX, textY);

		g.setColor(Graphics.Colors.lightGray);
		g.drawString(infoText, infoX, textY);

		drawArrow(canvas, g);

		g.setFont(oldFont);
	}

	private void drawArrow(Canvas canvas, Graphics g) {
		int arrowX = arrowX(canvas);
		int arrowY = arrowY(canvas);
		int arrowW = arrowW(canvas);
		int menuH = menuH(canvas);

		g.setColor(g.getColor(220, 0, 0, 0));
		g.fillRect(arrowX, arrowY, arrowW, menuH);

		g.setColor(Graphics.Colors.white);
		g.drawRect(arrowX, arrowY, arrowW, menuH);

		g.setColor(Graphics.Colors.cyan);
		g.drawString(mapListOpen ? "^" : "v", arrowX + scaled(canvas, 19), arrowY + scaled(canvas, 27));
	}

	private void drawMapList(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font listFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 15));
		g.setFont(listFont);

		int visibleCount = Math.min(maps.length, MAX_VISIBLE_MAPS);
		int lineH = listLineH(canvas);
		int listHeight = visibleCount * lineH;

		int listX = listX(canvas);
		int listY = listY(canvas);
		int menuW = menuW(canvas);

		g.setColor(g.getColor(235, 0, 0, 0));
		g.fillRect(listX, listY, menuW, listHeight);

		g.setColor(Graphics.Colors.white);
		g.drawRect(listX, listY, menuW, listHeight);

		int end = Math.min(firstVisibleMapIndex + visibleCount, maps.length);

		for (int i = firstVisibleMapIndex; i < end; i++) {
			PengoMapChoice map = maps[i];

			int line = i - firstVisibleMapIndex;
			int y = listY + line * lineH;

			if (i == selectedMapIndex) {
				g.setColor(Graphics.Colors.darkGray);
				g.fillRect(listX + 1, y + 1, menuW - 2, lineH - 2);

				g.setColor(Graphics.Colors.yellow);
				g.drawString(">", listX + scaled(canvas, 8), y + scaled(canvas, 23));
			}

			g.setColor(Graphics.Colors.white);
			g.drawString(map.label(), listX + scaled(canvas, 30), y + scaled(canvas, 23));

			g.setColor(Graphics.Colors.lightGray);
			g.drawString(map.width() + "x" + map.height() + " | " + map.geometryLabel(), listX + scaled(canvas, 220),
					y + scaled(canvas, 23));
		}

		if (firstVisibleMapIndex > 0) {
			g.setColor(Graphics.Colors.yellow);
			g.drawString("^", listX + menuW - scaled(canvas, 22), listY + scaled(canvas, 22));
		}

		if (end < maps.length) {
			g.setColor(Graphics.Colors.yellow);
			g.drawString("v", listX + menuW - scaled(canvas, 22), listY + listHeight - scaled(canvas, 8));
		}

		g.setFont(oldFont);
	}

	private void drawStartButton(Canvas canvas, Graphics g) {
		if (maps.length == 0) {
			return;
		}

		Font oldFont = g.getFont();
		Font bold = g.getFont("Arial", Font.BOLD, scaled(canvas, 17));
		g.setFont(bold);

		int startX = startX(canvas);
		int startY = startY(canvas);
		int startW = startW(canvas);
		int startH = startH(canvas);

		g.setColor(g.getColor(185, 0, 0, 0));
		g.fillRect(startX, startY, startW, startH);

		g.setColor(Graphics.Colors.white);
		g.drawRect(startX, startY, startW, startH);

		g.setColor(Graphics.Colors.cyan);
		drawCenteredString(canvas, g, "START GAME", startY + scaled(canvas, 33));

		g.setFont(oldFont);
	}

	private void drawHelp(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font bold = g.getFont("Arial", Font.BOLD, scaled(canvas, 14));
		g.setFont(bold);

		int bottom = canvas.getHeight();

		int firstLineY = Math.max(startY(canvas) + startH(canvas) + scaled(canvas, 45), bottom - scaled(canvas, 70));

		int secondLineY = firstLineY + scaled(canvas, 25);

		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, "SOURIS : clique sur la fleche pour ouvrir / fermer les maps", firstLineY);
		drawCenteredString(canvas, g, "CLAVIER : UP / DOWN choisir map  |  ENTER lancer  |  ESC/Q quitter",
				secondLineY);

		g.setFont(oldFont);
	}

	// HELPERS

	private boolean clickOnMapList(Canvas canvas, int x, int y) {
		int visibleCount = Math.min(maps.length, MAX_VISIBLE_MAPS);
		int lineH = listLineH(canvas);

		if (!inside(x, y, listX(canvas), listY(canvas), menuW(canvas), visibleCount * lineH)) {
			return false;
		}

		int line = (y - listY(canvas)) / lineH;
		int mapIndex = firstVisibleMapIndex + line;

		if (mapIndex < 0 || mapIndex >= maps.length) {
			return false;
		}

		selectedMapIndex = mapIndex;
		updateVisibleWindow();

		return true;
	}

	private void updateVisibleWindow() {
		if (selectedMapIndex < firstVisibleMapIndex) {
			firstVisibleMapIndex = selectedMapIndex;
		}

		if (selectedMapIndex >= firstVisibleMapIndex + MAX_VISIBLE_MAPS) {
			firstVisibleMapIndex = selectedMapIndex - MAX_VISIBLE_MAPS + 1;
		}

		if (firstVisibleMapIndex < 0) {
			firstVisibleMapIndex = 0;
		}
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

	private int menuW(Canvas canvas) {
		return scaled(canvas, MENU_W);
	}

	private int menuH(Canvas canvas) {
		return scaled(canvas, MENU_H);
	}

	private int menuX(Canvas canvas) {
		return (canvas.getWidth() - menuW(canvas)) / 2;
	}

	private int menuY(Canvas canvas) {
		return canvas.getHeight() / 2 - scaled(canvas, 60);
	}

	private int arrowW(Canvas canvas) {
		return scaled(canvas, ARROW_W);
	}

	private int arrowX(Canvas canvas) {
		return menuX(canvas) + menuW(canvas) - arrowW(canvas);
	}

	private int arrowY(Canvas canvas) {
		return menuY(canvas);
	}

	private int listX(Canvas canvas) {
		return menuX(canvas);
	}

	private int listY(Canvas canvas) {
		return menuY(canvas) + menuH(canvas);
	}

	private int listLineH(Canvas canvas) {
		return scaled(canvas, LIST_LINE_H);
	}

	private int startW(Canvas canvas) {
		return scaled(canvas, START_W);
	}

	private int startH(Canvas canvas) {
		return scaled(canvas, START_H);
	}

	private int startX(Canvas canvas) {
		return (canvas.getWidth() - startW(canvas)) / 2;
	}

	private int startY(Canvas canvas) {
		return menuY(canvas) + scaled(canvas, 100);
	}

	private int logoY(Canvas canvas) {
		return Math.max(scaled(canvas, 20), menuY(canvas) - scaled(canvas, 190));
	}

	private boolean inside(int px, int py, int x, int y, int w, int h) {
		return px >= x && px <= x + w && py >= y && py <= y + h;
	}

}
