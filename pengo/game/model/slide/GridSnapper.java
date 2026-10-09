package game.model.slide;

import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Orientation;
import game.model.entity.Glacon;
import game.model.entity.Mur;
import game.model.entity.Obstacle;
import game.model.entity.Power;
import game.model.entity.Spawner;

final class GridSnapper {

	// bloc glissant auquel ce snapper appartient
	private final Glissade owner;

	// dernière cellule considérée comme stable pendant la glissade.
	private Grid.Position lastStablePosition;

	GridSnapper(Glissade owner) {
		this.owner = owner;
	}

	// sauvegarde une position stable.
	void setLastStablePosition(Grid.Position position) {
		this.lastStablePosition = position == null ? null : position.copy();
	}

	// memorise la cellule actuelle si le bloc est proche du centre de la case.
	void rememberStableCellIfUseful() {
		if (!this.owner.alive() || !this.owner.installed()) {
			return;
		}

		Grid.Position current = this.owner.position();
		ISU.Coord cellCenter = current.toISUCoordCentered();

		if (this.owner.center().distanceTo(cellCenter) <= this.owner.model().isu().cmPerCell() * 0.20) {
			this.lastStablePosition = current.copy();
		}
	}

	// choisit la meilleure cellule ou replacer le bloc quand il sarrete
	Grid.Position safeSnapPosition(int slideDirection) {
		Grid.Position current = this.owner.position();

		// Priorite 1 : rester sur la cellule actuelle si elle est libre
		if (cellCanReceiveStoppedBlock(current)) {
			return current.copy();
		}

		// Priorite 2 : revenir à la derniere cellule stable connue
		if (this.lastStablePosition != null && cellCanReceiveStoppedBlock(this.lastStablePosition)) {
			return this.lastStablePosition.copy();
		}

		// Priorite 3 : reculer d'une case dans la direction opposee
		Grid.Position behind = this.owner.geometry().neighbor(current, Orientation.backOf(slideDirection));

		if (behind != null && cellCanReceiveStoppedBlock(behind)) {
			return behind;
		}

		// garder la position actuelle
		return current;
	}

	// verifie si une cellule peut recevoir le bloc a larret
	private boolean cellCanReceiveStoppedBlock(Grid.Position position) {
		if (position == null) {
			return false;
		}

		Grid.Cell cell = this.owner.model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other == this.owner || !other.alive()) {
				continue;
			}

			if (other instanceof Power) {
				continue;
			}

			// ces entités empechent le bloc de snapper sur cette cellule
			if (other instanceof Obstacle || other instanceof Mur || other instanceof Glacon
					|| other instanceof Spawner) {
				return false;
			}
		}

		return true;
	}
}