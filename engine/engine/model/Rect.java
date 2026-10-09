package engine.model;

// = Rect =

/*
 * Rectangle utilisé pour les collisions.
 *
 * Il est défini par son centre, ses demi-dimensions et son angle.
 * Le rectangle peut donc être tourné autour de son centre.
 */
public class Rect extends Shape {

	private final double halfWidth;
	private final double halfHeight;

	private int angle_degree;

	public Rect(ISU.Coord center, ISU.Dimension size, int angle_degree) {
		super(center);

		assert size.isu() == this.isu;

		/*
		 * Les dimensions complètes du rectangle doivent respecter la contrainte du
		 * tore.
		 */
		checkToricDimensions(size.x(), size.y());

		this.halfWidth = size.x() / 2.0;

		this.halfHeight = size.y() / 2.0;

		this.angle_degree = ((angle_degree % 360) + 360) % 360;
	}

	public double halfWidth() {
		return this.halfWidth;
	}

	public double halfHeight() {
		return this.halfHeight;
	}

	public int angle() {
		return this.angle_degree;
	}

	public void rotate(int angle_degree) {
		this.angle_degree += angle_degree;
		this.angle_degree = ((this.angle_degree % 360) + 360) % 360;
	}

	@Override
	public boolean intersects(IShape shape) {

		return shape.intersects(this);
	}

	@Override
	public boolean intersects(Circle circle) {

		// On ramène le problème dans le repère du rectangle.
		RectCircleIntersection rci = new RectCircleIntersection(this, circle);
		rci.remedy();

		return rci.intersects();
	}

	/*
	 * Test rectangle / cercle.
	 *
	 * L'idée est de se placer dans le repère local du rectangle : - le centre du
	 * rectangle devient l'origine ; - le rectangle est considéré comme aligné avec
	 * les axes.
	 *
	 * Une fois dans ce repère, le test devient simple : on cherche le point du
	 * rectangle le plus proche du cercle, puis on compare la distance avec le rayon
	 * du cercle.
	 */
	class RectCircleIntersection {

		private Rect outer;
		private Circle circle;

		// Coordonnées du cercle dans le repère local du rectangle.
		private double circleX;
		private double circleY;

		RectCircleIntersection(Rect outer, Circle circle) {

			this.outer = outer;
			this.circle = circle;
		}

		void remedy() {
			// Vecteur entre le centre du rectangle et le centre du cercle.
			ISU.Vector delta = outer.center.mkVectorToward(circle.center());

			double dx = delta.x();
			double dy = delta.y();

			// On annule la rotation du rectangle.
			double angle = Math.toRadians(-outer.angle_degree);

			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			this.circleX = dx * cos - dy * sin;
			this.circleY = dx * sin + dy * cos;
		}

		boolean intersects() {
			// Point du rectangle le plus proche du centre du cercle.
			double closestX = clamp(this.circleX, -outer.halfWidth, outer.halfWidth);
			double closestY = clamp(this.circleY, -outer.halfHeight, outer.halfHeight);

			double dx = this.circleX - closestX;
			double dy = this.circleY - closestY;

			double distanceSquared = dx * dx + dy * dy;
			double radius = circle.radius();

			// Collision si la distance est plus petite que le rayon.
			return distanceSquared <= radius * radius;
		}

		double clamp(double p, double l, double r) {
			if (p < l) {
				return l;
			}

			if (p > r) {
				return r;
			}

			return p;
		}
	}

	@Override
	public boolean intersects(Rect rect) {

		RectRectIntersection rri = new RectRectIntersection(this, rect);

		return rri.intersects();
	}

	/*
	 * Test rectangle / rectangle.
	 *
	 * On utilise le théorème des axes séparateurs. S'il existe un axe sur lequel
	 * les projections des deux rectangles ne se chevauchent pas, alors les
	 * rectangles ne se touchent pas.
	 */
	class RectRectIntersection {

		private Rect r1;
		private Rect r2;

		RectRectIntersection(Rect r1, Rect r2) {

			this.r1 = r1;
			this.r2 = r2;
		}

		/*
		 * Petit vecteur local utilisé uniquement pour les calculs SAT.
		 */
		class Vec2 {
			double x;
			double y;

			Vec2(double x, double y) {
				this.x = x;
				this.y = y;
			}

			double dot(Vec2 v) {
				return this.x * v.x + this.y * v.y;
			}
		}

		boolean intersects() {
			/*
			 * On travaille dans un repère euclidien local. Le déplacement entre les centres
			 * est calculé avec mkVectorToward, donc le cas du tore est déjà pris en compte.
			 */
			ISU.Vector centerDelta = r1.center.mkVectorToward(r2.center);

			Vec2[] corners1 = corners(r1, 0.0, 0.0);
			Vec2[] corners2 = corners(r2, centerDelta.x(), centerDelta.y());

			Vec2[] axes1 = axes(r1);
			Vec2[] axes2 = axes(r2);

			Vec2[] axes = new Vec2[] { axes1[0], axes1[1], axes2[0], axes2[1] };

			for (Vec2 axis : axes) {
				double[] p1 = project(corners1, axis);
				double[] p2 = project(corners2, axis);

				if (!overlaps(p1, p2)) {
					return false;
				}
			}

			return true;
		}

		/*
		 * Renvoie les deux axes locaux du rectangle.
		 */
		Vec2[] axes(Rect rect) {
			double angle = Math.toRadians(rect.angle_degree);

			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			Vec2 localX = new Vec2(cos, sin);
			Vec2 localY = new Vec2(-sin, cos);

			return new Vec2[] { localX, localY };
		}

		/*
		 * Calcule les quatre coins du rectangle dans le repère local utilisé.
		 */
		Vec2[] corners(Rect rect, double cx, double cy) {
			Vec2[] axes = axes(rect);

			Vec2 ax = axes[0];
			Vec2 ay = axes[1];

			double hw = rect.halfWidth;
			double hh = rect.halfHeight;

			return new Vec2[] { new Vec2(cx + ax.x * hw + ay.x * hh, cy + ax.y * hw + ay.y * hh),
					new Vec2(cx - ax.x * hw + ay.x * hh, cy - ax.y * hw + ay.y * hh),
					new Vec2(cx - ax.x * hw - ay.x * hh, cy - ax.y * hw - ay.y * hh),
					new Vec2(cx + ax.x * hw - ay.x * hh, cy + ax.y * hw - ay.y * hh) };
		}

		/*
		 * Projette tous les coins d'un rectangle sur un axe. Le résultat est un
		 * intervalle [min, max].
		 */
		double[] project(Vec2[] corners, Vec2 axis) {
			double min = corners[0].dot(axis);
			double max = min;

			for (int i = 1; i < corners.length; i++) {
				double p = corners[i].dot(axis);

				if (p < min) {
					min = p;
				}

				if (p > max) {
					max = p;
				}
			}

			return new double[] { min, max };
		}

		/*
		 * Deux projections se chevauchent si leurs intervalles ont une partie commune.
		 */
		boolean overlaps(double[] a, double[] b) {
			return a[1] >= b[0] && b[1] >= a[0];
		}
	}

	@Override
	public Hitbox hitbox() {
		double angle = Math.toRadians(this.angle_degree);

		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		double[][] localCorners = { { this.halfWidth, this.halfHeight }, { -this.halfWidth, this.halfHeight },
				{ -this.halfWidth, -this.halfHeight }, { this.halfWidth, -this.halfHeight } };

		double xmin = Double.POSITIVE_INFINITY;
		double xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY;
		double ymax = Double.NEGATIVE_INFINITY;

		/*
		 * On transforme les coins du rectangle dans le repère du monde, puis on garde
		 * les bornes minimales et maximales.
		 */
		for (double[] corner : localCorners) {
			double localX = corner[0];
			double localY = corner[1];

			double worldX = localX * cos - localY * sin + this.center.x();
			double worldY = localX * sin + localY * cos + this.center.y();

			xmin = Math.min(xmin, worldX);
			xmax = Math.max(xmax, worldX);
			ymin = Math.min(ymin, worldY);
			ymax = Math.max(ymax, worldY);
		}

		return new Hitbox(xmin, xmax, ymin, ymax);
	}
}