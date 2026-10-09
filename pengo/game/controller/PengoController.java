package game.controller;

import java.util.HashSet;
import java.util.Set;

import engine.brain.PlayerBot;
import engine.controller.Controller;
import engine.model.Entity;
import engine.model.Orientation;
import engine.view.DebugOptions;
import engine.view.View;
import game.model.PengoGameState;
import game.model.player.Player;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

public final class PengoController extends Controller {

	private final Switch controlSwitcher;
	private final View view;
	private final boolean continuousWalk;

	private final Set<Integer> pressedKeys;

	private final DebugOptions debugOptions;

	private PengoGameState gameState;
	private Integer continuousDirection;

	private static final int KEY_TAB = 9;

	private static final int KEY_F1 = 112;
	private static final int KEY_F2 = 113;
	private static final int KEY_F3 = 114;
	private static final int KEY_F4 = 115;
	private static final int KEY_F5 = 116;

	public PengoController(Switch controlSwitcher, View view, boolean continuousWalk) {
		this(controlSwitcher, view, continuousWalk, null);
	}

	public PengoController(Switch controlSwitcher, View view, boolean continuousWalk, DebugOptions debugOptions) {
		this.controlSwitcher = controlSwitcher;
		this.view = view;
		this.continuousWalk = continuousWalk;

		if (debugOptions == null) {
			this.debugOptions = view.debugOptions();
		} else {
			this.debugOptions = debugOptions;
		}

		this.pressedKeys = new HashSet<Integer>();
	}

	public void setGameState(PengoGameState gameState) {
		this.gameState = gameState;
	}

	public void clearInputs() {
		this.pressedKeys.clear();
		this.continuousDirection = null;

		PlayerBot bot = this.controlSwitcher.playerBot();

		if (bot != null) {
			bot.stopWishing();
		}
	}

	public boolean continuousWalk() {
		return this.continuousWalk;
	}

	public boolean gameFinished() {
		return this.gameState != null && this.gameState.finished();
	}

	public boolean gameWon() {
		return this.gameState != null && this.gameState.won();
	}

	public boolean gameLost() {
		return this.gameState != null && this.gameState.lost();
	}

	// KEYBOARD

	@Override
	protected void keyPressed(Canvas canvas, int keyCode, char keyChar) {
		if (handleDebugKey(keyCode)) {
			return;
		}

		if (gameFinished()) {
			return;
		}

		this.controlSwitcher.updateControlledEntity();

		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
		case VirtualKeyCodes.VK_D:
			directionPressed(keyCode, Orientation.EAST);
			break;

		case VirtualKeyCodes.VK_DOWN:
		case VirtualKeyCodes.VK_S:
			directionPressed(keyCode, Orientation.SOUTH);
			break;

		case VirtualKeyCodes.VK_LEFT:
		case VirtualKeyCodes.VK_Q:
			directionPressed(keyCode, Orientation.WEST);
			break;

		case VirtualKeyCodes.VK_UP:
		case VirtualKeyCodes.VK_Z:
			directionPressed(keyCode, Orientation.NORTH);
			break;

		case VirtualKeyCodes.VK_SPACE:
			if (this.pressedKeys.add(keyCode)) {
				useAction();
			}
			break;

		case KEY_TAB:
			if (this.pressedKeys.add(keyCode)) {
				this.controlSwitcher.switchControl();
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void keyReleased(Canvas canvas, int keyCode, char keyChar) {
		if (gameFinished()) {
			return;
		}

		this.pressedKeys.remove(keyCode);

		Integer releasedDirection = directionForKey(keyCode);

		if (releasedDirection == null) {
			return;
		}

		Integer stillPressedDirection = findStillPressedDirection();

		if (this.continuousWalk && releasedDirection.equals(this.continuousDirection)) {
			this.continuousDirection = stillPressedDirection;
		}

		if (stillPressedDirection == null) {
			PlayerBot bot = this.controlSwitcher.playerBot();

			if (bot != null) {
				bot.stopWishing();
			}
		}
	}

	@Override
	protected void keyTyped(Canvas canvas, char keyChar) {
	}

	public void updateContinuousWalk() {
		if (!enabled()) {
			return;
		}

		if (gameFinished()) {
			return;
		}

		if (!this.continuousWalk) {
			return;
		}

		if (this.continuousDirection == null) {
			return;
		}

		moveOnly(this.continuousDirection);
	}

	private void directionPressed(int keyCode, int direction) {
		boolean firstPress = this.pressedKeys.add(keyCode);

		int normalizedDirection = Orientation.normalize(direction);

		if (this.continuousWalk) {
			this.continuousDirection = normalizedDirection;
		}

		if (!firstPress && !this.continuousWalk) {
			return;
		}

		moveOnly(normalizedDirection);
	}

	private Integer findStillPressedDirection() {
		for (Integer key : this.pressedKeys) {
			Integer direction = directionForKey(key);

			if (direction != null) {
				return direction;
			}
		}

		return null;
	}

	private Integer directionForKey(int keyCode) {
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
		case VirtualKeyCodes.VK_D:
			return Orientation.EAST;

		case VirtualKeyCodes.VK_DOWN:
		case VirtualKeyCodes.VK_S:
			return Orientation.SOUTH;

		case VirtualKeyCodes.VK_LEFT:
		case VirtualKeyCodes.VK_Q:
			return Orientation.WEST;

		case VirtualKeyCodes.VK_UP:
		case VirtualKeyCodes.VK_Z:
			return Orientation.NORTH;

		default:
			return null;
		}
	}

	private void moveOnly(int direction) {
		this.controlSwitcher.updateControlledEntity();

		Entity controlled = this.controlSwitcher.controlledEntity();

		if (!(controlled instanceof Player)) {
			return;
		}

		Player player = (Player) controlled;

		if (player.faceIceAhead(direction)) {
			return;
		}

		PlayerBot bot = this.controlSwitcher.playerBot();

		if (bot != null) {
			bot.wish(direction);
		}
	}

	private void useAction() {
		this.controlSwitcher.updateControlledEntity();

		Entity controlled = this.controlSwitcher.controlledEntity();

		if (controlled instanceof Player) {
			Player player = (Player) controlled;
			player.useIceAhead();
		}
	}

	// MOUSE

	@Override
	protected void mouseMoved(Canvas canvas, int x, int y) {
	}

	@Override
	protected void mousePressed(Canvas canvas, int button, int x, int y) {
		if (gameFinished()) {
			return;
		}

		if (button == 1) {
			int direction = directionFromMouseClick(x, y);
			faceOnly(direction);
			return;
		}

		if (button == 3) {
			useAction();
		}
	}

	@Override
	protected void mouseReleased(Canvas canvas, int x, int y, int button) {
	}

	private int directionFromMouseClick(int mouseX, int mouseY) {
		this.controlSwitcher.updateControlledEntity();

		Entity controlled = this.controlSwitcher.controlledEntity();

		if (!(controlled instanceof Player)) {
			return Orientation.SOUTH;
		}

		if (mouseX > this.view.widthPixel()) {
			return Orientation.normalize(controlled.orientation());
		}

		int centerX = this.view.toPixelX(controlled.center().x());
		int centerY = this.view.toPixelY(controlled.center().y());

		int dx = mouseX - centerX;
		int dy = mouseY - centerY;

		if (Math.abs(dx) >= Math.abs(dy)) {
			if (dx >= 0) {
				return Orientation.EAST;
			}

			return Orientation.WEST;
		}

		if (dy >= 0) {
			return Orientation.SOUTH;
		}

		return Orientation.NORTH;
	}

	private void faceOnly(int direction) {
		this.controlSwitcher.updateControlledEntity();

		Entity controlled = this.controlSwitcher.controlledEntity();

		if (!(controlled instanceof Player)) {
			return;
		}

		int normalized = Orientation.normalize(direction);

		controlled.setOrientation(normalized);

		PlayerBot bot = this.controlSwitcher.playerBot();

		if (bot != null) {
			bot.stopWishing();
		}
	}

	private boolean handleDebugKey(int keyCode) {
		if (this.debugOptions == null) {
			return false;
		}

		switch (keyCode) {
		case KEY_F1:
			this.debugOptions.toggleEnabled();
			return true;

		case KEY_F2:
			this.debugOptions.toggleBoundingBoxes();
			return true;

		case KEY_F3:
			this.debugOptions.toggleActions();
			return true;

		case KEY_F4:
			this.debugOptions.togglePerformance();
			return true;

		case KEY_F5:
			this.debugOptions.toggleLoadTest();
			return true;

		default:
			return false;
		}
	}
}