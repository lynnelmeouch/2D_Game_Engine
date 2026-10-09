package engine.view;

import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class SpriteSheet {

	private String path;
	private BufferedImage sheet;

	public SpriteSheet(String path) {
		this.path = path;
	}

	public void load(Graphics g) {

		if (this.sheet == null) {
			this.sheet = g.load(this.path);
		}
	}

	public BufferedImage frame(Graphics g, int x, int y, int w, int h) {

		load(g);

		return this.sheet.getSubimage(x, y, w, h);
	}
}