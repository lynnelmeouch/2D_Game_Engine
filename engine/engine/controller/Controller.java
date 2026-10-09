package engine.controller;

import oop.graphics.Canvas;

/*
 * Contrôleur générique du moteur.
 *
 * Il reçoit les événements clavier et souris de la plateforme.
 * Par défaut, il ne fait rien.
 *
 * Les jeux spécifiques héritent de cette classe et redéfinissent seulement
 * les hooks protégés dont ils ont besoin.
 */
public abstract class Controller implements Canvas.KeyListener, Canvas.MouseListener {

	private boolean enabled;

	protected Controller() {
		this.enabled = true;
	}

	public boolean enabled() {
		return this.enabled;
	}

	public void enable() {
		this.enabled = true;
	}

	public void disable() {
		this.enabled = false;
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		if (!this.enabled) {
			return;
		}

		keyPressed(canvas, keyCode, keyChar);
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		if (!this.enabled) {
			return;
		}

		keyReleased(canvas, keyCode, keyChar);
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
		if (!this.enabled) {
			return;
		}

		keyTyped(canvas, keyChar);
	}

	@Override
	public void moved(Canvas canvas, int x, int y) {
		if (!this.enabled) {
			return;
		}

		mouseMoved(canvas, x, y);
	}

	@Override
	public void pressed(Canvas canvas, int button, int x, int y) {
		if (!this.enabled) {
			return;
		}

		mousePressed(canvas, button, x, y);
	}

	@Override
	public void released(Canvas canvas, int x, int y, int button) {
		if (!this.enabled) {
			return;
		}

		mouseReleased(canvas, x, y, button);
	}

	protected void keyPressed(Canvas canvas, int keyCode, char keyChar) {
	}

	protected void keyReleased(Canvas canvas, int keyCode, char keyChar) {
	}

	protected void keyTyped(Canvas canvas, char keyChar) {
	}

	protected void mouseMoved(Canvas canvas, int x, int y) {
	}

	protected void mousePressed(Canvas canvas, int button, int x, int y) {
	}

	protected void mouseReleased(Canvas canvas, int x, int y, int button) {
	}
}
