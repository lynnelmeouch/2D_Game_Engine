package engine.model;

/*
 * Classe abstraite commune aux formes géométriques
 * utilisées par les Bounding.
 */
public abstract class Shape implements IShape {

	protected final ISU isu;
	protected ISU.Coord center;

	public Shape(ISU.Coord center) {

		this.isu = center.isu();
		this.center = center;
	}

	public ISU.Coord center() {
		return this.center;
	}

	@Override
	public ISU isu() {
		return this.isu;
	}

	public void translate(ISU.Vector vector) {
		assert vector.isu() == this.isu;

		this.center.translate(vector);
	}

	/*
	 * Chaque dimension d'une Shape doit être strictement plus petite que le plus
	 * petit demi-périmètre des axes toriques.
	 *
	 * Sur un monde non torique, cette contrainte n'est pas nécessaire.
	 */
	protected final void checkToricDimensions(double width, double height) {
		assert width > 0.0 : width;
		assert height > 0.0 : height;

		double smallestHalfPerimeter = Double.POSITIVE_INFINITY;

		if (this.isu.xAxis().onTorus()) {
			smallestHalfPerimeter = Math.min(smallestHalfPerimeter, this.isu.xAxis().halfPerimeter());
		}

		if (this.isu.yAxis().onTorus()) {
			smallestHalfPerimeter = Math.min(smallestHalfPerimeter, this.isu.yAxis().halfPerimeter());
		}

		/*
		 * Aucun axe torique : pas de contrainte liée au tore.
		 */
		if (Double.isInfinite(smallestHalfPerimeter)) {
			return;
		}

		assert width < smallestHalfPerimeter : "shape width must be smaller than the smallest toric half-perimeter"
				+ " (width=" + width + ", limit=" + smallestHalfPerimeter + ")";

		assert height < smallestHalfPerimeter : "shape height must be smaller than the smallest toric half-perimeter"
				+ " (height=" + height + ", limit=" + smallestHalfPerimeter + ")";
	}
}