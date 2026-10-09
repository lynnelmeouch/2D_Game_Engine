package engine.controller;

/*
 * Commandes génériques de rotation produites
 * par un contrôleur souris.
 *
 * Le MouseController ne connaît ni le Bot,
 * ni le Stunt, ni le jeu concret.
 */
public interface RotationCommands {

	void startTurnLeft();

	void stopTurnLeft();

	void startTurnRight();

	void stopTurnRight();
}