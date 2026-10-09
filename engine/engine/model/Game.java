// == GAME ==
package engine.model;

import java.io.PrintStream;

import engine.view.Picture;

/*
 * Game contient la configuration générale d'un jeu.
 *
 * Cette classe fait partie du moteur : elle ne décrit pas PacMan directement,
 * mais les paramètres nécessaires pour construire le monde :
 * taille, unités, géométrie du tore, grille, espace ISU et modèle.
 */
public abstract class Game {

	private static final double EPS = 0.000001;

	// Géométrie du monde : chaque axe peut être torique ou non.
	private boolean torusOnXaxis;
	private boolean torusOnYaxis;

	// Facteurs de conversion entre les différentes unités.
	private double cmPerCell;
	private int pixelPerCm;

	// Dimensions du monde en cellules.
	private int width_ncell;
	private int height_ncell;

	// Dimensions du monde en coordonnées ISU.
	private double width_cm;
	private double height_cm;

	// Objets principaux construits à partir de la configuration du jeu.
	private Grid grid;
	private ISU isu;
	private Picture pict;
	private Model model;

	public Game(int w_ncell, int h_ncell, boolean torusOnXaxis, boolean torusOnYaxis, double cmPerCell,
			int pixelPerCm) {
		assert w_ncell > 0;
		assert h_ncell > 0;
		assert cmPerCell > 0.0;
		assert pixelPerCm > 0;

		this.torusOnXaxis = torusOnXaxis;
		this.torusOnYaxis = torusOnYaxis;

		this.cmPerCell = cmPerCell;
		this.pixelPerCm = pixelPerCm;

		this.width_ncell = w_ncell;
		this.height_ncell = h_ncell;

		// Les dimensions en cm sont déduites des dimensions en cellules.
		this.width_cm = this.width_ncell * this.cmPerCell;
		this.height_cm = this.height_ncell * this.cmPerCell;

		buildEngineObjects();

		assert Math.abs(this.width_ncell * this.cmPerCell - this.width_cm) < EPS;
		assert Math.abs(this.height_ncell * this.cmPerCell - this.height_cm) < EPS;
	}

	public Game(double w_cm, double h_cm, boolean torusOnXaxis, boolean torusOnYaxis, double cmPerCell,
			int pixelPerCm) {
		assert w_cm > 0.0;
		assert h_cm > 0.0;
		assert cmPerCell > 0.0;
		assert pixelPerCm > 0;

		/*
		 * Ici, on construit le jeu à partir des dimensions en cm. Elles doivent donc
		 * tomber exactement sur un nombre entier de cellules.
		 */
		assert Math.abs(w_cm / cmPerCell - Math.round(w_cm / cmPerCell)) < EPS;
		assert Math.abs(h_cm / cmPerCell - Math.round(h_cm / cmPerCell)) < EPS;

		this.torusOnXaxis = torusOnXaxis;
		this.torusOnYaxis = torusOnYaxis;

		this.cmPerCell = cmPerCell;
		this.pixelPerCm = pixelPerCm;

		this.width_cm = w_cm;
		this.height_cm = h_cm;

		// Les dimensions en cellules sont déduites des dimensions en cm.
		this.width_ncell = (int) Math.round(this.width_cm / this.cmPerCell);
		this.height_ncell = (int) Math.round(this.height_cm / this.cmPerCell);

		buildEngineObjects();

		assert Math.abs(this.width_ncell * this.cmPerCell - this.width_cm) < EPS;
		assert Math.abs(this.height_ncell * this.cmPerCell - this.height_cm) < EPS;
	}

	/*
	 * Construit les objets du moteur qui dépendent de la configuration.
	 *
	 * L'ordre est important : - ISU utilise les dimensions du jeu ; - Grid utilise
	 * aussi le jeu ; - ISU reçoit ensuite la Grid pour faire les conversions ; -
	 * Model regroupe les entités du monde.
	 */
	private void buildEngineObjects() {
		this.isu = new ISU(this);
		this.grid = new Grid(this);
		this.isu.set(this.grid);

		this.pict = new Picture(this);
		this.model = new Model(this);
	}

	public Game game() {
		return this;
	}

	public double getWidthCm() {
		return this.width_cm;
	}

	public double getHeightCm() {
		return this.height_cm;
	}

	public int getWidthCell() {
		return this.width_ncell;
	}

	public int getHeightCell() {
		return this.height_ncell;
	}

	public double getWidthPixel() {
		return this.width_cm * this.pixelPerCm;
	}

	public double getHeightPixel() {
		return this.height_cm * this.pixelPerCm;
	}

	public boolean isTorusOnXaxis() {
		return this.torusOnXaxis;
	}

	public boolean isTorusOnYaxis() {
		return this.torusOnYaxis;
	}

	public boolean getTorusOnXaxis() {
		return this.torusOnXaxis;
	}

	public boolean getTorusOnYaxis() {
		return this.torusOnYaxis;
	}

	public double getCmPerCell() {
		return this.cmPerCell;
	}

	public int getPixelPerCm() {
		return this.pixelPerCm;
	}

	public ISU getIsu() {
		return this.isu;
	}

	public Grid getGrid() {
		return this.grid;
	}

	public Picture getPicture() {
		return this.pict;
	}

	public Model model() {
		return this.model;
	}

	public void show(PrintStream ps) {
		ps.println("Game:");
		ps.println("width_ncell = " + this.width_ncell);
		ps.println("height_ncell = " + this.height_ncell);
		ps.println("width_cm = " + this.width_cm);
		ps.println("height_cm = " + this.height_cm);
		ps.println("cmPerCell = " + this.cmPerCell);
		ps.println("pixelPerCm = " + this.pixelPerCm);
		ps.println("torusOnXaxis = " + this.torusOnXaxis);
		ps.println("torusOnYaxis = " + this.torusOnYaxis);
	}
}