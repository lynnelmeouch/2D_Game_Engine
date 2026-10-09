package game;

import java.awt.Dimension;

import game.config.menu.PengoConfigMenu;
import oop.graphics.Canvas;
import oop.tasks.Runtime;
import oop.tasks.Runnable;
import oop.tasks.Task;

public final class MainPengo {

	private static final int WINDOW_WIDTH_PIXEL = 1280;
	private static final int WINDOW_HEIGHT_PIXEL = 720;

	/*
	 * Important pour le resize. La plateforme donnée recrée/agrandit le
	 * double-buffer en gris quand la fenêtre change de taille. Elle ne redemande
	 * pas forcément un repaint applicatif juste après. Donc on garde une demande de
	 * repaint régulière.
	 */
	private static final int REPAINT_DELAY_MS = 33;

	private MainPengo() {
	}

	public static void main(String[] args) {
		Runtime.boot(new Dimension(WINDOW_WIDTH_PIXEL, WINDOW_HEIGHT_PIXEL), new Runnable() {
			@Override
			public void run() {
				bootMenu();
			}
		});
	}

	private static void bootMenu() {
		Task task = Runtime.task();
		Canvas canvas = (Canvas) task.find("canvas");
		assert canvas != null;

		PengoConfigMenu menu = new PengoConfigMenu(canvas);

		canvas.set((Canvas.PaintListener) menu);
		canvas.set((Canvas.KeyListener) menu);
		canvas.set((Canvas.MouseListener) menu);

		startRepaintLoop(menu);
		canvas.repaint();
	}

	private static void startRepaintLoop(final PengoConfigMenu menu) {
		assert menu != null;

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				boolean keepGoing = menu.repaintIfPossible();

				if (keepGoing) {
					Runtime.post(this, REPAINT_DELAY_MS);
				}
			}
		}, REPAINT_DELAY_MS);
	}
}