package engine.model;

/*
 * Hitbox représente une boîte rectangulaire alignée sur les axes X et Y.
 *
 * Elle sert de test rapide avant les collisions précises.
 * Si deux hitbox ne se touchent pas, les formes qu'elles contiennent
 * ne peuvent pas se toucher non plus.
 */
public class Hitbox {

	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;

	public Hitbox(double xmin, double xmax, double ymin, double ymax) {
		assert xmin <= xmax;
		assert ymin <= ymax;

		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	public double xmin() {
		return this.xmin;
	}

	public double xmax() {
		return this.xmax;
	}

	public double ymin() {
		return this.ymin;
	}

	public double ymax() {
		return this.ymax;
	}

	/*
	 * Test classique d'intersection entre deux rectangles alignés.
	 *
	 * Deux hitbox ne se touchent pas si l'une est complètement à gauche, à droite,
	 * au-dessus ou en dessous de l'autre.
	 */
	public boolean intersects(Hitbox h) {

		return !(this.xmax < h.xmin || h.xmax < this.xmin || this.ymax < h.ymin || h.ymax < this.ymin);
	}

	/*
	 * Variante utilisée quand le monde est un tore.
	 *
	 * Une forme proche d'un bord peut toucher une autre forme située de l'autre
	 * côté du monde. Pour gérer ce cas simplement, on teste aussi des copies de la
	 * deuxième hitbox décalées d'un tour de monde.
	 */
	public boolean intersectsOnTorus(Hitbox h, double worldWidth, double worldHeight) {
		return intersects(h, true, true, worldWidth, worldHeight);
	}

	/*
	 * Même principe que intersectsOnTorus, mais axe par axe. 
	 */
	public boolean intersects(Hitbox h, boolean torusX, boolean torusY, double worldWidth, double worldHeight) {
		assert worldWidth > 0.0;
		assert worldHeight > 0.0;

		double[] dxs = torusX ? new double[] { 0.0, -worldWidth, worldWidth } : new double[] { 0.0 };
		double[] dys = torusY ? new double[] { 0.0, -worldHeight, worldHeight } : new double[] { 0.0 };

		for (double dx : dxs) {
			for (double dy : dys) {
				Hitbox shifted = new Hitbox(h.xmin() + dx, h.xmax() + dx, h.ymin() + dy, h.ymax() + dy);

				if (this.intersects(shifted)) {
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * Construit une hitbox qui englobe deux hitbox.
	 *
	 * Cette méthode est utile pour créer la hitbox globale d'un Bounding composé de
	 * plusieurs formes.
	 */
	public Hitbox union(Hitbox h) {

		return new Hitbox(Math.min(this.xmin, h.xmin), Math.max(this.xmax, h.xmax), Math.min(this.ymin, h.ymin),
				Math.max(this.ymax, h.ymax));
	}
}