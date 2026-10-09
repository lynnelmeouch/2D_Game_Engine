package engine.view;

import java.io.PrintStream;
import java.util.Objects;

import engine.model.Game;
import engine.model.Grid;
import engine.model.ISU;

/*
 * Picture représente le repère graphique exprimé en pixels.
 *
 * Il permet les conversions entre :
 * - les coordonnées ISU, exprimées en centimètres ;
 * - les coordonnées graphiques, exprimées en pixels.
 *
 * Le facteur de conversion est Game.pixelPerCm.
 */
public final class Picture {

	private final Game game;
	private final ISU isu;

	private final int pixelPerCm;

	private final int widthPixel;
	private final int heightPixel;

	public Picture(Game game) {
		assert game.getPixelPerCm() > 0;

		this.game = game;
		this.isu = game.getIsu();

		this.pixelPerCm = game.getPixelPerCm();

		/*
		 * il garantit que toute la carte ISU peut être représentée dans l'image.
		 */
		this.widthPixel = (int) Math.ceil(game.getWidthCm() * this.pixelPerCm);

		this.heightPixel = (int) Math.ceil(game.getHeightCm() * this.pixelPerCm);

		assert this.widthPixel > 0;
		assert this.heightPixel > 0;
	}

	public Game game() {
		return this.game;
	}

	public ISU isu() {
		return this.isu;
	}

	public int pixelPerCm() {
		return this.pixelPerCm;
	}

	public int width() {
		return this.widthPixel;
	}

	public int height() {
		return this.heightPixel;
	}

	/*
	 * Conversion d'une longueur ou coordonnée exprimée en cm vers les pixels.
	 *
	 * floor est adapté à une coordonnée : une coordonnée située dans la carte
	 * produit toujours un pixel situé dans l'image.
	 */
	public int toPixel(double valueCm) {
		assert valueCm >= 0.0;

		return (int) Math.floor(valueCm * this.pixelPerCm);
	}

	/*
	 * Conversion inverse.
	 */
	public double toCm(int valuePixel) {
		assert valuePixel >= 0;

		return (double) valuePixel / this.pixelPerCm;
	}

	/*
	 * Convertit une coordonnée ISU en Pixel.
	 */
	public Pixel from(ISU.Coord coord) {
		assert coord.isu() == this.isu;

		return new Pixel(toPixel(coord.x()), toPixel(coord.y()));
	}

	/*
	 * Convertit le coin supérieur gauche de la cellule représentée par une
	 * Grid.Position.
	 */
	public Pixel from(Grid.Position position) {

		return from(position.toISUCoord());
	}

	public void show(PrintStream ps) {

		ps.println("Picture:");
		ps.println("width = " + this.widthPixel + " pixels");

		ps.println("height = " + this.heightPixel + " pixels");

		ps.println("pixelPerCm = " + this.pixelPerCm);
	}

	/*
	 * Point exprimé dans le repère graphique.
	 */
	public final class Pixel {

		private final int xPixel;
		private final int yPixel;

		public Pixel(int xPixel, int yPixel) {
			assert xPixel >= 0;
			assert yPixel >= 0;

			assert xPixel < Picture.this.widthPixel : "x pixel outside picture";

			assert yPixel < Picture.this.heightPixel : "y pixel outside picture";

			this.xPixel = xPixel;
			this.yPixel = yPixel;
		}

		public Picture picture() {
			return Picture.this;
		}

		public int x() {
			return this.xPixel;
		}

		public int y() {
			return this.yPixel;
		}

		/*
		 * Conversion inverse vers le monde simulé.
		 */
		public ISU.Coord toISUCoord() {
			return Picture.this.isu.new Coord(Picture.this.toCm(this.xPixel), Picture.this.toCm(this.yPixel));
		}

		public Pixel copy() {
			return new Pixel(this.xPixel, this.yPixel);
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof Picture.Pixel)) {
				return false;
			}

			Picture.Pixel other = (Picture.Pixel) object;

			return other.picture() == Picture.this && this.xPixel == other.x() && this.yPixel == other.y();
		}

		@Override
		public int hashCode() {
			return Objects.hash(System.identityHashCode(Picture.this), this.xPixel, this.yPixel);
		}

		public void show(PrintStream ps) {

			ps.println("Pixel(" + this.xPixel + ", " + this.yPixel + ")");
		}
	}
}