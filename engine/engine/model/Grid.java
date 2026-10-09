// = GRID =
package engine.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import engine.view.Picture;

/*
 * Grid représente le monde en nombre de cellules.
 *
 * Elle sert surtout à organiser les entités par cases :
 * - savoir dans quelle cellule se trouve une entité ;
 * - récupérer une cellule à partir d'une position ;
 * - gérer les déplacements en unités "cellule".
 *
 * Les coordonnées précises en cm sont gérées par ISU.
 */
public class Grid {

	private ISU isu;
	private Axis xAxis, yAxis;

	private int width_ncell, height_ncell;

	private Cell[][] grid;

	private final Game game;

	public Grid(Game game) {

		this.game = game;

		this.isu = game.getIsu();

		this.width_ncell = game.getWidthCell();
		this.height_ncell = game.getHeightCell();

		/*
		 * La grille possède ses propres axes. Ici, les distances sont exprimées en
		 * nombre de cellules.
		 */
		this.xAxis = new Axis(game.getTorusOnXaxis(), this.width_ncell);
		this.yAxis = new Axis(game.getTorusOnYaxis(), this.height_ncell);

		this.grid = new Cell[this.width_ncell][this.height_ncell];

		init();
	}

	/*
	 * Crée toutes les cellules de la grille. Chaque cellule connaît sa propre
	 * position.
	 */
	void init() {
		for (int x = 0; x < this.width_ncell; x++) {
			for (int y = 0; y < this.height_ncell; y++) {
				this.grid[x][y] = new Cell(new Position(x, y));
			}
		}
	}

	public int width() {
		return this.width_ncell;
	}

	public int height() {
		return this.height_ncell;
	}

	public Axis xAxis() {
		return this.xAxis;
	}

	public Axis yAxis() {
		return this.yAxis;
	}

	public Grid.Cell cellAt(Grid.Position p) {

		Grid.Position normalized = p.copy();
		normalized.normalize();

		int x = normalized.x();
		int y = normalized.y();

		if (x < 0) {
			x = 0;
		} else if (x >= this.width_ncell) {
			x = this.width_ncell - 1;
		}

		if (y < 0) {
			y = 0;
		} else if (y >= this.height_ncell) {
			y = this.height_ncell - 1;
		}

		return this.grid[x][y];
	}

	public boolean contains(Grid.Position p) {

		return p.x() >= 0 && p.x() < this.width_ncell && p.y() >= 0 && p.y() < this.height_ncell;
	}

	public void show(PrintStream ps) {
		ps.println("Grid:");
		ps.println("width  = " + this.width_ncell + " ncell");
		ps.println("height = " + this.height_ncell + " ncell");
	}

	// == DIMENSION (nb cell) ==

	/*
	 * Dimension exprimée en nombre de cellules.
	 *
	 * Une dimension représente une taille, pas une position. Elle doit donc
	 * toujours rester strictement positive.
	 */
	public class Dimension {

		private int x_ncell, y_ncell;

		public Dimension(int x_ncell, int y_ncell) {
			assert x_ncell > 0 : x_ncell;
			assert y_ncell > 0 : y_ncell;

			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;

			normalize();

			assert this.x_ncell > 0;
			assert this.y_ncell > 0;
			assert this.x_ncell <= width();
			assert this.y_ncell <= height();
		}

		public int x() {
			return this.x_ncell;
		}

		public int y() {
			return this.y_ncell;
		}

		public void normalize() {
			this.x_ncell = xAxis.normalizeDimension(this.x_ncell);
			this.y_ncell = yAxis.normalizeDimension(this.y_ncell);
		}

		public Dimension copy() {
			return new Dimension(this.x_ncell, this.y_ncell);
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof Grid.Dimension)) {
				return false;
			}

			Dimension other = (Dimension) object;

			return other.grid() == Grid.this && this.x_ncell == other.x_ncell && this.y_ncell == other.y_ncell;
		}

		private Grid grid() {
			return Grid.this;
		}

		public boolean equiv(Dimension d) {
			if (d == this) {
				return true;
			}

			return d != null && this.x_ncell == d.x_ncell && this.y_ncell == d.y_ncell;
		}

		/*
		 * Conversion vers ISU. On passe d'une taille en cellules à une taille en cm.
		 */
		public ISU.Dimension toISUDimension() {

			double x_cm = this.x_ncell * isu.cmPerCell();
			double y_cm = this.y_ncell * isu.cmPerCell();

			return isu.new Dimension(x_cm, y_cm);
		}

		public void show(PrintStream ps) {
			ps.println("(" + this.x_ncell + ", " + this.y_ncell + ") ncell");
		}
	}

	// == VECTOR ==

	/*
	 * Vector représente un déplacement en nombre de cellules.
	 *
	 * Contrairement à une position, un vecteur n'est pas normalisé : il décrit un
	 * mouvement, pas un endroit du monde.
	 */
	public class Vector {

		private int x_deplacement, y_deplacement;

		Grid grid() {
			return Grid.this;
		}

		public Vector(int x_ncell, int y_ncell) {
			this.x_deplacement = x_ncell;
			this.y_deplacement = y_ncell;
		}

		public int x() {
			return this.x_deplacement;
		}

		public int y() {
			return this.y_deplacement;
		}

		public void add(Vector vector) {
			assert vector.grid() == Grid.this;

			this.x_deplacement += vector.x_deplacement;
			this.y_deplacement += vector.y_deplacement;
		}

		public Vector copy() {
			return new Vector(this.x_deplacement, this.y_deplacement);
		}

		public boolean equiv(Vector vector) {
			return vector != null && vector.grid() == Grid.this && this.x_deplacement == vector.x_deplacement
					&& this.y_deplacement == vector.y_deplacement;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof Grid.Vector)) {
				return false;
			}

			return equiv((Vector) object);
		}

		public void show(PrintStream ps) {
			ps.println("Vector(" + this.x_deplacement + ", " + this.y_deplacement + ") ncell");
		}
	}

	// == POSITION ==

	/*
	 * Position représente une case de la grille.
	 *
	 * Sur un tore, une position est automatiquement ramenée dans les limites de la
	 * grille.
	 */
	public class Position {

		private int x_point, y_point;

		public Position(int x_ncell, int y_ncell) {
			this.x_point = x_ncell;
			this.y_point = y_ncell;

			normalize();
		}

		public void normalize() {
			this.x_point = Grid.this.xAxis.normalize(this.x_point);

			this.y_point = Grid.this.yAxis.normalize(this.y_point);

			check();
		}

		public int x() {
			return this.x_point;
		}

		public int y() {
			return this.y_point;
		}

		public Grid.Position copy() {
			return new Position(this.x_point, this.y_point);
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof Grid.Position)) {
				return false;
			}

			Position other = (Position) object;

			return other.grid() == Grid.this && this.x_point == other.x_point && this.y_point == other.y_point;
		}

		private void check() {
			if (!Grid.this.xAxis.onTorus()) {
				assert this.x_point >= 0;
				assert this.x_point < Grid.this.width_ncell;
			}

			if (!Grid.this.yAxis.onTorus()) {
				assert this.y_point >= 0;
				assert this.y_point < Grid.this.height_ncell;
			}
		}

		public void translate(Vector vector) {
			assert vector.grid() == Grid.this;

			this.x_point += vector.x_deplacement;
			this.y_point += vector.y_deplacement;

			normalize();
		}

		public void moveNorth(int n_ncell) {
			this.y_point -= n_ncell;
			normalize();
		}

		public void moveSouth(int n_ncell) {
			this.y_point += n_ncell;
			normalize();
		}

		public void moveEast(int n_ncell) {
			this.x_point += n_ncell;
			normalize();
		}

		public void moveWest(int n_ncell) {
			this.x_point -= n_ncell;
			normalize();
		}

		/*
		 * Rotation discrète autour d'une autre position. On accepte seulement les
		 * angles compatibles avec une grille carrée.
		 */
		public void rotateAround(Grid.Position center, int angle_degree) {
			assert center.grid() == Grid.this;

			int dx = Grid.this.xAxis.delta(center.x_point, this.x_point);

			int dy = Grid.this.yAxis.delta(center.y_point, this.y_point);

			int newDx;
			int newDy;

			int angle = ((angle_degree % 360) + 360) % 360;

			switch (angle) {
			case 0:
				newDx = dx;
				newDy = dy;
				break;

			case 90:
				newDx = -dy;
				newDy = dx;
				break;

			case 180:
				newDx = -dx;
				newDy = -dy;
				break;

			case 270:
				newDx = dy;
				newDy = -dx;
				break;

			default:
				throw new IllegalArgumentException("Only 0, 90, 180, 270 degrees are supported on Grid");
			}

			this.x_point = center.x_point + newDx;
			this.y_point = center.y_point + newDy;

			normalize();
		}

		/*
		 * Permet de vérifier que deux positions appartiennent à la même grille.
		 */
		Grid grid() {
			return Grid.this;
		}

		public double distanceTo(Position position) {
			assert position.grid() == Grid.this;

			double dx = xAxis.distance(this.x_point, position.x_point);

			double dy = yAxis.distance(this.y_point, position.y_point);

			return Math.sqrt(dx * dx + dy * dy);
		}

		/*
		 * Convertit la position de grille vers ISU. Ici, on prend le coin supérieur
		 * gauche de la cellule.
		 */
		public ISU.Coord toISUCoord() {

			double x_cm = this.x_point * isu.cmPerCell();
			double y_cm = this.y_point * isu.cmPerCell();

			return isu.new Coord(x_cm, y_cm);
		}

		/*
		 * Convertit la position vers le centre de la cellule. C'est la conversion la
		 * plus utilisée pour placer les entités.
		 */
		public ISU.Coord toISUCoordCentered() {

			double x_cm = (this.x_point + 0.5) * isu.cmPerCell();
			double y_cm = (this.y_point + 0.5) * isu.cmPerCell();

			return isu.new Coord(x_cm, y_cm);
		}

		/*
		 * Convertit le coin supérieur gauche de la cellule en coordonnées graphiques.
		 */
		public Picture.Pixel toPicturePixel() {
			Picture picture = Grid.this.game.getPicture();

			return picture.from(this);
		}

		/*
		 * Convertit le centre de la cellule en pixels.
		 */
		public Picture.Pixel toPicturePixelCentered() {
			Picture picture = Grid.this.game.getPicture();

			return picture.from(toISUCoordCentered());
		}

		public void show(PrintStream ps) {
			ps.println("Position(" + this.x_point + ", " + this.y_point + ")");
		}
	}

	// == CELL ==

	/*
	 * Une cellule représente une case de la grille.
	 *
	 * Elle garde la liste des entités qui l'occupent. Cette information permet
	 * ensuite d'optimiser les recherches de voisins ou de collisions.
	 */
	public class Cell {

		private Grid.Dimension size;
		private Grid.Position position;
		private List<Entity> entities;

		public Cell(Position p) {
			this.size = new Dimension(1, 1);
			this.position = p;
			this.entities = new ArrayList<Entity>();
		}

		public Grid.Dimension size() {
			return this.size;
		}

		public Grid.Position position() {
			return this.position;
		}

		public List<Entity> entities() {
			return this.entities;
		}

		public void add(Entity e) {
			if (!this.entities.contains(e)) {
				this.entities.add(e);
			}
		}

		public void remove(Entity e) {

			this.entities.remove(e);
		}

		public boolean contains(Entity e) {
			return this.entities.contains(e);
		}

		public void show(PrintStream ps) {
			ps.println("Cell at (" + this.position.x() + ", " + this.position.y() + ")");
			ps.println("Number of entities: " + this.entities.size());
		}
	}

}