package game.view;

import engine.view.Overlay;
import engine.view.View;
import game.model.PengoGameState;
import oop.graphics.Graphics;

public class PengoOverlay implements Overlay {

	private static final int WIDTH_PIXEL = 190;

	private final PengoGameState state;

	public PengoOverlay(PengoGameState state) {
		this.state = state;
	}

	@Override
	public int widthPixel() {
		return WIDTH_PIXEL;
	}

	@Override
	public void paint(Graphics graphics, View view, int x, int y) {
		this.state.update();

		graphics.setColor(graphics.getColor(230, 0, 0, 0));
		graphics.fillRect(x, y, WIDTH_PIXEL, view.heightPixel());

		graphics.setColor(Graphics.Colors.white);
		graphics.drawString("PENGO", x + 20, y + 35);

		graphics.setColor(Graphics.Colors.lightGray);
		graphics.drawLine(x, y + 50, x + WIDTH_PIXEL, y + 50);

		graphics.setColor(Graphics.Colors.white);
		graphics.drawString("Lives: " + this.state.lives(), x + 20, y + 85);
		graphics.drawString("Score: " + this.state.score(), x + 20, y + 115);

		graphics.drawString("Enemies: " + this.state.livingEnemies() + " / " + this.state.targetTotalEnemies(), x + 20,
				y + 145);

		graphics.drawString("Spawned: " + this.state.spawnedEnemies(), x + 20, y + 175);
		graphics.drawString("Spawners: " + this.state.livingSpawners(), x + 20, y + 205);

		graphics.setColor(Graphics.Colors.lightGray);
		graphics.drawLine(x, y + 230, x + WIDTH_PIXEL, y + 230);

		if (this.state.won()) {
			graphics.setColor(Graphics.Colors.green);
		} else if (this.state.lost()) {
			graphics.setColor(Graphics.Colors.red);
		} else {
			graphics.setColor(Graphics.Colors.cyan);
		}

		graphics.drawString(this.state.status(), x + 20, y + 265);
	}
}