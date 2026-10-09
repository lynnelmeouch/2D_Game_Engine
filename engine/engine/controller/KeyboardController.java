package engine.controller;

import engine.brain.PlayerBot;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

public class KeyboardController extends Controller {

	private final PlayerBot playerBot;

	public KeyboardController(PlayerBot playerBot) {
		this.playerBot = playerBot;
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
		case VirtualKeyCodes.VK_D:
			this.playerBot.wish(0);
			break;

		case VirtualKeyCodes.VK_DOWN:
		case VirtualKeyCodes.VK_S:
			this.playerBot.wish(90);
			break;

		case VirtualKeyCodes.VK_LEFT:
		case VirtualKeyCodes.VK_Q:
			this.playerBot.wish(180);
			break;

		case VirtualKeyCodes.VK_UP:
		case VirtualKeyCodes.VK_Z:
			this.playerBot.wish(270);
			break;

		default:
			break;
		}
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
	}
}