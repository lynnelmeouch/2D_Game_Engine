package game.config.menu;

import game.config.PengoConfig;
import game.config.map.PengoMapChoice;
import game.config.menu.PengoPauseMenu.Action;
import game.config.session.PengoGameRunner;
import game.config.session.PengoGameSession;
import oop.graphics.Canvas;
import oop.graphics.Graphics;
import oop.graphics.VirtualKeyCodes;

public final class PengoConfigMenu implements Canvas.PaintListener, Canvas.KeyListener, Canvas.MouseListener {

	// FIELDS
	private Canvas canvas;
	private Canvas gameCanvas;

	private boolean started;

	private PengoGameSession gameSession;
	private final PengoMainMenu mainMenu;
	private final PengoPauseMenu pauseMenu;
	private final PengoEndMenu endMenu;

	public PengoConfigMenu(Canvas canvas) {
		this.canvas = canvas;
		mainMenu = new PengoMainMenu();
		pauseMenu = new PengoPauseMenu();
		endMenu = new PengoEndMenu();
	}

	@Override
	public void visible(Canvas canvas) {
		this.canvas = canvas;
		// si une session est deja lancée on rattavhe la session au canvas
		if (gameIsRunning()) {
			gameCanvas = canvas;
			gameSession.attach(canvas);
			return;
		}
		canvas.repaint();
	}

	@Override
	public void revoked(Canvas canvas) {
		quitApplication();
	}

	@Override
	public void paint(Canvas canvas, Graphics g) {
		this.canvas = canvas;
		if (gameIsRunning()) {
			paintGame(canvas, g);
			return;
		}
		this.mainMenu.paint(canvas, g);
	}

	private void paintGame(Canvas canvas, Graphics g) {
		if (this.gameCanvas != canvas) {
			this.gameCanvas = canvas;
			this.gameSession.attach(canvas);
		}

		this.gameSession.resizeTo(canvas);
		this.gameSession.painter().paint(canvas, g);

		if (this.gameSession.finished()) {
			this.endMenu.paint(canvas, g, gameSession.won());
		} else if (this.gameSession.paused()) {
			this.pauseMenu.paint(canvas, g);
		}
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		this.canvas = canvas;

		if (gameIsRunning()) {
			handleGameKey(canvas, keyCode, keyChar);
			return;
		}

		handleMenuKey(canvas, keyCode);
	}

	private void handleGameKey(Canvas canvas, int keyCode, char keyChar) {
		if (gameSession.finished()) {
			handleEndKey(canvas, keyCode, keyChar);
			return;
		}
		// esc ouvre ou ferme le menu pause
		if (keyCode == VirtualKeyCodes.VK_ESCAPE) {
			gameSession.togglePause();
			canvas.repaint();
			return;
		}
		if (this.gameSession.paused()) {
			handlePauseKey(canvas, keyCode);
			return;
		}
		gameSession.controller().pressed(canvas, keyCode, keyChar);
	}

	private void handlePauseKey(Canvas canvas, int keyCode) {
		switch (keyCode) {
		case VirtualKeyCodes.VK_ENTER:
			this.gameSession.resume();
			canvas.repaint();
			break;

		case VirtualKeyCodes.VK_M:
			returnToMainMenu();
			break;

		case VirtualKeyCodes.VK_Q:
			quitApplication();
			break;

		default:
			break;
		}
	}

	private void handleMenuKey(Canvas canvas, int keyCode) {
		switch (keyCode) {
		case VirtualKeyCodes.VK_UP:
			this.mainMenu.moveSelection(-1);
			canvas.repaint();
			break;

		case VirtualKeyCodes.VK_DOWN:
			this.mainMenu.moveSelection(1);
			canvas.repaint();
			break;

		case VirtualKeyCodes.VK_ENTER:
			startGame();
			break;

		case VirtualKeyCodes.VK_ESCAPE:
		case VirtualKeyCodes.VK_Q:
			quitApplication();
			break;

		default:
			break;
		}
	}

	private void handleEndKey(Canvas canvas, int keyCode, char keyChar) {
		PengoEndMenu.Action action = this.endMenu.keyPressed(this.gameSession.won(), this.gameSession.lost(), keyCode,
				keyChar);

		handleEndAction(canvas, action);
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		if (gameIsRunning() && !gameSession.paused() && !gameSession.finished()) {
			gameSession.controller().released(canvas, keyCode, keyChar);
		}
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
		if (started && gameSession != null && !gameSession.paused() && !gameSession.finished()) {
			gameSession.controller().typed(canvas, keyChar);
		}
	}

	@Override
	public void moved(Canvas canvas, int x, int y) {
		// Pas besoin.
	}

	@Override
	public void pressed(Canvas canvas, int button, int x, int y) {
		this.canvas = canvas;

		if (gameIsRunning()) {
			handleGameMousePressed(canvas, button, x, y);
			return;
		}

		handleMenuMousePressed(canvas, x, y);
	}

	private void handleGameMousePressed(Canvas canvas, int button, int x, int y) {
		if (this.gameSession.finished()) {
			handleEndMousePressed(canvas, x, y);
			return;
		}

		if (this.gameSession.paused()) {
			handlePauseMousePressed(canvas, x, y);
			return;
		}

		this.gameSession.controller().pressed(canvas, button, x, y);
	}

	private void handleMenuMousePressed(Canvas canvas, int x, int y) {
		PengoMainMenu.Action action = this.mainMenu.mousePressed(canvas, x, y);

		if (action == PengoMainMenu.Action.START) {
			startGame();
			return;
		}

		canvas.repaint();
	}

	private void handleEndMousePressed(Canvas canvas, int x, int y) {
		PengoEndMenu.Action action = this.endMenu.mousePressed(canvas, x, y, this.gameSession.won());

		handleEndAction(canvas, action);
	}

	private void handleEndAction(Canvas canvas, PengoEndMenu.Action action) {
		switch (action) {
		case RETRY:
			retryGame();
			break;

		case MAIN_MENU:
			returnToMainMenu();
			break;

		default:
			break;
		}
	}

	@Override
	public void released(Canvas canvas, int x, int y, int button) {
		// Rien.
	}

	private void handlePauseMousePressed(Canvas canvas, int x, int y) {
		PengoPauseMenu.Action action = pauseMenu.mousePressed(canvas, x, y);

		switch (action) {
		case RESUME:
			gameSession.resume();
			canvas.repaint();
			break;

		case MENU:
			returnToMainMenu();
			break;

		case QUIT:
			quitApplication();
			break;

		default:
			break;
		}
	}

	private void returnToMainMenu() {
		if (gameSession != null) {
			gameSession.stop();
		}
		gameSession = null;
		gameCanvas = null;
		started = false;
		if (canvas != null) {
			canvas.repaint();
		}
	}

	private void quitApplication() {
		if (gameSession != null) {
			gameSession.stop();
			gameSession = null;
		}
		oop.tasks.Runtime.shutdown();
		System.exit(0);
	}

	private void startGame() {
		if (started || !mainMenu.hasMaps()) {
			return;
		}

		PengoMapChoice selectedMap = mainMenu.selectedMap();
		PengoConfig defaultConfig = new PengoConfig(selectedMap, true);

		int windowWidth = canvas.getWidth();
		int windowHeight = canvas.getHeight();

		PengoConfig config = new PengoConfig(selectedMap, true, defaultConfig.cmPerCell(), defaultConfig.pixelPerCm(),
				windowWidth, windowHeight, defaultConfig.viewportWidthCells(), defaultConfig.viewportHeightCells(),
				defaultConfig.tickDelayMs(), defaultConfig.paintDelayMs(), defaultConfig.spawnDelayMs(),
				defaultConfig.maxLivingEnemies(), defaultConfig.maxTotalSpawnedEnemies());

		gameSession = PengoGameRunner.start(canvas, config);
		gameSession.attach(canvas);
		gameCanvas = canvas;
		started = true;

		canvas.repaint();
	}

	private void retryGame() {
		if (this.gameSession != null) {
			this.gameSession.stop();
		}
		this.gameSession = null;
		this.gameCanvas = null;
		this.started = false;
		startGame();
	}

	private boolean gameIsRunning() {
		return this.started && this.gameSession != null;
	}

	public boolean repaintIfPossible() {
		if (canvas == null) {
			return false;
		}
		if (started) {
			return false;
		}
		canvas.repaint();
		return true;
	}
}