package engine.model;

/*
 * Partie du monde actuellement visible.
 *
 * Le ViewPort appartient au Model et est exprimé
 * entièrement dans le repère ISU.
 */
public final class ViewPort {

	private final Model model;
	private final ISU isu;

	/*
	 * Coin supérieur gauche du ViewPort.
	 */
	private ISU.Coord origin;

	/*
	 * Largeur et hauteur visibles en ISU.
	 */
	private ISU.Dimension dimension;

	public ViewPort(Model model, ISU.Coord origin, ISU.Dimension dimension) {

		this.model = model;
		this.isu = model.isu();

		assert origin.isu() == this.isu;
		assert dimension.isu() == this.isu;

		ISU.Dimension dimensionCopy = dimension.copy();

		checkDimension(dimensionCopy);

		this.dimension = dimensionCopy;
		this.origin = origin.mkCopy();

		normalizeOrigin();
	}

	public Model model() {
		return this.model;
	}

	public ISU isu() {
		return this.isu;
	}

	/*
	 * On retourne des copies pour empêcher une modification du ViewPort sans passer
	 * par ses méthodes.
	 */
	public ISU.Coord origin() {
		return this.origin.mkCopy();
	}

	public ISU.Dimension dimension() {
		return this.dimension.copy();
	}

	public double x() {
		return this.origin.x();
	}

	public double y() {
		return this.origin.y();
	}

	public double width() {
		return this.dimension.x();
	}

	public double height() {
		return this.dimension.y();
	}

	private void checkDimension(ISU.Dimension candidate) {
		assert candidate.isu() == this.isu;

		double width = candidate.x();

		double height = candidate.y();

		assert width > 0.0;
		assert height > 0.0;

		if (this.isu.xAxis().onTorus()) {
			assert width < this.isu.width() : "viewport width must be smaller than toric map width";
		} else {
			assert width <= this.isu.width() : "viewport width must be <= map width";
		}

		if (this.isu.yAxis().onTorus()) {
			assert height < this.isu.height() : "viewport height must be smaller than toric map height";
		} else {
			assert height <= this.isu.height() : "viewport height must be <= map height";
		}
	}

	public void setOrigin(ISU.Coord newOrigin) {
		assert newOrigin.isu() == this.isu;

		this.origin = newOrigin.mkCopy();

		normalizeOrigin();
	}

	public void setDimension(ISU.Dimension newDimension) {
		assert newDimension.isu() == this.isu;

		ISU.Dimension copy = newDimension.copy();

		checkDimension(copy);

		this.dimension = copy;

		/*
		 * Une nouvelle dimension peut rendre l'ancienne origine invalide sur un axe non
		 * torique.
		 */
		normalizeOrigin();
	}

	/*
	 * Place immédiatement l'entité au centre du ViewPort.
	 */
	public void centerOn(Entity entity) {
		assert entity.model() == this.model;
		assert entity.installed();

		ISU.Coord center = entity.center();

		double targetX = center.x() - width() / 2.0;

		double targetY = center.y() - height() / 2.0;

		targetX = legalOriginX(targetX);

		targetY = legalOriginY(targetY);

		setOrigin(this.isu.new Coord(targetX, targetY));
	}

	public void follow(Entity entity) {
		centerOn(entity);
	}

	public void follow(Entity entity, double elasticity) {
		assert entity != null;
		assert entity.model() == this.model;
		assert entity.installed();

		assert elasticity >= 0.0;
		assert elasticity <= 1.0;

		ISU.Coord center = entity.center();

		double targetX = legalOriginX(center.x() - width() / 2.0);

		double targetY = legalOriginY(center.y() - height() / 2.0);

		double dx;

		if (this.isu.xAxis().onTorus()) {
			dx = this.isu.xAxis().delta(this.origin.x(), targetX);
		} else {
			dx = targetX - this.origin.x();
		}

		double dy;

		if (this.isu.yAxis().onTorus()) {
			dy = this.isu.yAxis().delta(this.origin.y(), targetY);
		} else {
			dy = targetY - this.origin.y();
		}

		setOrigin(this.isu.new Coord(this.origin.x() + dx * elasticity,

				this.origin.y() + dy * elasticity));
	}

	private void normalizeOrigin() {
		double normalizedX = legalOriginX(this.origin.x());

		double normalizedY = legalOriginY(this.origin.y());

		this.origin = this.isu.new Coord(normalizedX, normalizedY);
	}

	private double legalOriginX(double requestedX) {
		if (this.isu.xAxis().onTorus()) {
			return this.isu.xAxis().normalize(requestedX);
		}

		double maximum = this.isu.width() - width();

		return clamp(requestedX, 0.0, maximum);
	}

	private double legalOriginY(double requestedY) {
		if (this.isu.yAxis().onTorus()) {
			return this.isu.yAxis().normalize(requestedY);
		}

		double maximum = this.isu.height() - height();

		return clamp(requestedY, 0.0, maximum);
	}

	private double clamp(double value, double minimum, double maximum) {
		if (value < minimum) {
			return minimum;
		}

		if (value > maximum) {
			return maximum;
		}

		return value;
	}
}