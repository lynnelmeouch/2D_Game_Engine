package engine.view;

import oop.graphics.Graphics;

/*
 * Élément graphique affiché en dehors
 * de la zone principale du monde.
 *
 * Un Overlay peut représenter :
 * - un HUD ;
 * - un inventaire ;
 * - une minimap ;
 * - un panneau de score.
 *
 * Le moteur ne connaît pas son contenu.
 */
public interface Overlay {

	/*
	 * Largeur supplémentaire occupée à droite de la zone de jeu.
	 */
	int widthPixel();

	/*
	 * Hauteur supplémentaire occupée au-dessus de la zone de jeu.
	 */
	default int heightPixel() {
		return 0;
	}

	/*
	 * Dessine l'overlay à partir de la position (x,y).
	 */
	void paint(Graphics graphics, View view, int x, int y);

	/*
	 * Mise à jour des animations de l'overlay.
	 */
	default void tick(long elapsed) {
	}
}