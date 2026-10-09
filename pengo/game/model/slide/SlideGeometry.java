package game.model.slide;

import engine.model.Grid;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Orientation;

final class SlideGeometry {

	// modele utilise pour acceder a la grille et aux mesures isu
	private final Model model;

	SlideGeometry(Model model) {
		this.model = model;
	}

	// calcule la distance dans l axe de la direction
	double forwardProjection(ISU.Vector vector, int direction) {
		switch (direction) {
		case Orientation.EAST:
			return vector.x();

		case Orientation.SOUTH:
			return vector.y();

		case Orientation.WEST:
			return -vector.x();

		case Orientation.NORTH:
			return -vector.y();

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	// calcule la distance perpendiculaire a la direction
	double lateralProjection(ISU.Vector vector, int direction) {
		switch (direction) {
		case Orientation.EAST:
		case Orientation.WEST:
			return vector.y();

		case Orientation.SOUTH:
		case Orientation.NORTH:
			return vector.x();

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	// retourne la longueur de laxe selon la direction
	double axisLength(int direction) {
		if (direction == Orientation.EAST || direction == Orientation.WEST) {
			return this.model.isu().width();
		}

		return this.model.isu().height();
	}

	// cree un decalage dans la direction donnee
	ISU.Vector forwardOffset(int direction, double distance_cm) {
		switch (direction) {
		case Orientation.EAST:
			return this.model.isu().new Vector(distance_cm, 0.0);

		case Orientation.SOUTH:
			return this.model.isu().new Vector(0.0, distance_cm);

		case Orientation.WEST:
			return this.model.isu().new Vector(-distance_cm, 0.0);

		case Orientation.NORTH:
			return this.model.isu().new Vector(0.0, -distance_cm);

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	// cree un vecteur de vitesse dans la direction donnee
	ISU.Vector speedVector(int direction, double speed) {
		switch (direction) {
		case Orientation.EAST:
			return this.model.isu().new Vector(speed, 0.0);

		case Orientation.SOUTH:
			return this.model.isu().new Vector(0.0, speed);

		case Orientation.WEST:
			return this.model.isu().new Vector(-speed, 0.0);

		case Orientation.NORTH:
			return this.model.isu().new Vector(0.0, -speed);

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	// retourne la cellule voisine dans la direction donnee
	Grid.Position neighbor(Grid.Position from, int direction) {
		if (from == null) {
			return null;
		}
		return Orientation.neighbor(this.model.grid(), from, direction);
	}
}