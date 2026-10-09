// = ISU =
package engine.model;

import java.io.PrintStream;

/*
 * ISU représente l'espace continu du jeu, en centimètres.
 *
 * La Grid travaille avec des positions de cellules.
 * ISU travaille avec des coordonnées plus précises dans le monde.
 *
 * C'est utile pour les déplacements progressifs, les collisions,
 * les distances et les rotations.
 */
public class ISU {

	private Axis xAxis, yAxis;
	private Grid grid;

	private double width_cm;
	private double height_cm;

	private double cmPerCell;

	public ISU(Game game) {

		this.width_cm = game.getWidthCm();
		this.height_cm = game.getHeightCm();

		this.cmPerCell = game.getCmPerCell();

		// Les axes ISU utilisent les dimensions du monde en cm.
		this.xAxis = new Axis(game.getTorusOnXaxis(), this.width_cm);
		this.yAxis = new Axis(game.getTorusOnYaxis(), this.height_cm);
	}

	public void set(Grid grid) {
		this.grid = grid;
	}

	public double width() {
		return this.width_cm;
	}

	public double height() {
		return this.height_cm;
	}

	public double cmPerCell() {
		return this.cmPerCell;
	}

	public Axis xAxis() {
		return this.xAxis;
	}

	public Axis yAxis() {
		return this.yAxis;
	}

	public Grid grid() {
		return this.grid;
	}

	// == DIMENSION (cm) ==

	/*
	 * Dimension en cm.
	 *
	 * Contrairement à une coordonnée, une dimension représente une taille. Elle
	 * doit donc toujours être strictement positive.
	 */
	public class Dimension {

		private double x_cm, y_cm;

		public Dimension(double x_cm, double y_cm) {
			assert x_cm > 0.0 : x_cm;
			assert y_cm > 0.0 : y_cm;

			this.x_cm = x_cm;
			this.y_cm = y_cm;

			normalize();

			assert this.x_cm > 0.0;
			assert this.y_cm > 0.0;
			assert this.x_cm <= width_cm;
			assert this.y_cm <= height_cm;
		}

		public void normalize() {
			this.x_cm = xAxis.normalizeDimension(this.x_cm);
			this.y_cm = yAxis.normalizeDimension(this.y_cm);
		}

		public void setxy(double x_cm, double y_cm) {
			assert x_cm > 0.0 : x_cm;
			assert y_cm > 0.0 : y_cm;

			this.x_cm = x_cm;
			this.y_cm = y_cm;

			normalize();

			assert this.x_cm > 0.0;
			assert this.y_cm > 0.0;
			assert this.x_cm <= width_cm;
			assert this.y_cm <= height_cm;
		}

		public Dimension copy() {
			return new Dimension(this.x_cm, this.y_cm);
		}

		public ISU isu() {
			return ISU.this;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof ISU.Dimension)) {
				return false;
			}

			Dimension other = (Dimension) object;

			return other.isu() == ISU.this && Double.compare(this.x_cm, other.x_cm) == 0
					&& Double.compare(this.y_cm, other.y_cm) == 0;
		}

		public boolean equiv(Dimension d) {
			if (d == this) {
				return true;
			}

			return d != null && this.x_cm == d.x_cm && this.y_cm == d.y_cm;
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		/*
		 * Fabrique un vecteur proportionnel à cette dimension. Pratique pour construire
		 * des offsets dépendant de la taille d'une entité.
		 */
		public ISU.Vector mkScaledVector(double factor) {
			return new ISU.Vector(this.x_cm * factor, this.y_cm * factor);
		}

		public ISU.Vector mkScaledVector(double xFactor, double yFactor) {
			return new ISU.Vector(this.x_cm * xFactor, this.y_cm * yFactor);
		}

		public ISU.Vector mkVector() {
			return new ISU.Vector(this.x_cm, this.y_cm);
		}

		public void show(PrintStream ps) {
			ps.println("Dimension(" + this.x_cm + ", " + this.y_cm + ") cm");
		}
	}

	// == POINT ==

	/*
	 * Coord représente une position précise dans le monde.
	 *
	 * Sur un tore, la coordonnée est automatiquement normalisée pour rester dans
	 * les limites du monde.
	 */
	public class Coord {

		private double x_cm, y_cm;

		public Coord(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;

			normalize();
		}

		private void normalize() {
			this.x_cm = xAxis.normalize(this.x_cm);
			this.y_cm = yAxis.normalize(this.y_cm);

			check();
		}

		private void check() {
			if (!ISU.this.xAxis.onTorus()) {
				assert this.x_cm >= 0.0;
				assert this.x_cm < ISU.this.width_cm;
			}

			if (!ISU.this.yAxis.onTorus()) {
				assert this.y_cm >= 0.0;
				assert this.y_cm < ISU.this.height_cm;
			}
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		public ISU isu() {
			return ISU.this;
		}

		public void show(PrintStream ps) {
			ps.println("Coord(" + this.x_cm + ", " + this.y_cm + ") cm");
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof ISU.Coord)) {
				return false;
			}

			Coord other = (Coord) object;

			return other.isu() == ISU.this && Double.compare(this.x_cm, other.x_cm) == 0
					&& Double.compare(this.y_cm, other.y_cm) == 0;
		}

		/*
		 * Construit le plus court vecteur vers une autre coordonnée.
		 *
		 * Sur un tore, il faut parfois passer par le bord du monde. 
		 */
		public ISU.Vector mkVectorToward(Coord target) {
			assert target.isu() == ISU.this;

			/*
			 * Le choix du plus court chemin dépend de chaque axe. 
			 */
			double dx = xAxis.delta(this.x_cm, target.x_cm);
			double dy = yAxis.delta(this.y_cm, target.y_cm);

			return new ISU.Vector(dx, dy);
		}

		/*
		 * Convertit une coordonnée ISU vers une position de grille.
		 *
		 * On utilise floor pour retrouver la cellule dans laquelle se trouve la
		 * coordonnée.
		 */
		public Grid.Position toGridPosition() {
			int x_ncell = (int) Math.floor(this.x_cm / cmPerCell);
			int y_ncell = (int) Math.floor(this.y_cm / cmPerCell);

			if (x_ncell < 0) {
				x_ncell = 0;
			} else if (x_ncell >= grid.width()) {
				x_ncell = grid.width() - 1;
			}

			if (y_ncell < 0) {
				y_ncell = 0;
			} else if (y_ncell >= grid.height()) {
				y_ncell = grid.height() - 1;
			}

			return grid.new Position(x_ncell, y_ncell);
		}

		public void translate(ISU.Vector vector) {
			assert vector.isu() == ISU.this;

			this.x_cm += vector.x();
			this.y_cm += vector.y();

			normalize();
		}

		public ISU.Coord mkTranslated(ISU.Vector v) {
			assert v.isu() == ISU.this;

			return new ISU.Coord(this.x_cm + v.x_cm, this.y_cm + v.y_cm);
		}

		public ISU.Coord mkCopy() {
			return new ISU.Coord(this.x_cm, this.y_cm);
		}

		/*
		 * Rotation autour de l'origine du repère.
		 */
		public void rotation(int angle_degree) {
			double angle = Math.toRadians(angle_degree);

			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			double newX = this.x_cm * cos - this.y_cm * sin;
			double newY = this.x_cm * sin + this.y_cm * cos;

			this.x_cm = newX;
			this.y_cm = newY;

			normalize();
		}

		public void rotateAround(Coord center, int angle_degree) {
			assert center.isu() == ISU.this;

			/*
			 * Vecteur local du centre de rotation vers le point. 
			 */
			double dx = ISU.this.xAxis.delta(center.x_cm, this.x_cm);

			double dy = ISU.this.yAxis.delta(center.y_cm, this.y_cm);

			double angle = Math.toRadians(angle_degree);

			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			double rotatedX = dx * cos - dy * sin;
			double rotatedY = dx * sin + dy * cos;

			this.x_cm = center.x_cm + rotatedX;
			this.y_cm = center.y_cm + rotatedY;
			
			normalize();
		}

		public double distanceTo(Coord pt) {

			double dx = xAxis.distance(this.x_cm, pt.x_cm);
			double dy = yAxis.distance(this.y_cm, pt.y_cm);

			return Math.sqrt(dx * dx + dy * dy);
		}

		public boolean equiv(Coord coord) {
			if (coord == this) {
				return true;
			}

			return coord != null && Double.compare(this.x_cm, coord.x_cm) == 0
					&& Double.compare(this.y_cm, coord.y_cm) == 0;
		}
	}

	// == VECTOR ==

	/*
	 * Vector représente un déplacement en cm.
	 *
	 * Contrairement à Coord, un vecteur n'est pas une position. On ne le normalise
	 * donc pas automatiquement sur le tore.
	 */
	public class Vector {

		private double x_cm;
		private double y_cm;

		public Vector(double targetX_cm, double targetY_cm) {
			this.x_cm = targetX_cm;
			this.y_cm = targetY_cm;
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		public void add(Vector v) {

			this.x_cm += v.x_cm;
			this.y_cm += v.y_cm;
		}

		public void scale(double factor) {
			this.x_cm *= factor;
			this.y_cm *= factor;
		}

		public void scale(double xFactor, double yFactor) {
			this.x_cm *= xFactor;
			this.y_cm *= yFactor;
		}

		public double dot(ISU.Vector v) {
			return this.x_cm * v.x_cm + this.y_cm * v.y_cm;
		}

		public ISU isu() {
			return ISU.this;
		}

		public double norm() {
			return Math.sqrt(this.x_cm * this.x_cm + this.y_cm * this.y_cm);
		}

		/*
		 * Transforme le vecteur en vecteur unitaire.
		 */
		public void unity() {
			double n = norm();

			assert n != 0.0;

			this.x_cm /= n;
			this.y_cm /= n;
		}

		/*
		 * Tourne le vecteur autour de son origine. Utile pour appliquer l'orientation
		 * d'une entité à un offset.
		 */
		public void turn(int angle_degree) {
			double angle = Math.toRadians(angle_degree);

			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			double newX = this.x_cm * cos - this.y_cm * sin;
			double newY = this.x_cm * sin + this.y_cm * cos;

			this.x_cm = newX;
			this.y_cm = newY;
		}

		public Vector copy() {
			return new Vector(this.x_cm, this.y_cm);
		}

		public void show(PrintStream ps) {
			ps.println("Vector(" + this.x_cm + ", " + this.y_cm + ") cm");
		}
	}
}