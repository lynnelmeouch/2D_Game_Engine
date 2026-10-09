package engine.model;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
 * Représente un axe du monde.
 *
 * Un axe peut être normal ou torique.
 * Sur un axe torique, les positions reviennent au début quand elles dépassent
 * le périmètre.
 */
public class Axis {
	private static final Logger logger = Logger.getLogger(Axis.class.getName());
	private final boolean onTorus;
	private final double perimeter;

	public Axis(boolean onTorus, double perimeter) {
		assert perimeter > 0.0 : perimeter;

		this.onTorus = onTorus;
		this.perimeter = perimeter;

		logger.log(Level.FINE,
				"Axis created: torus={0}, perimeter={1}",
				new Object[] { Boolean.valueOf(onTorus), Double.valueOf(perimeter) });
	}

	public boolean onTorus() {
		return this.onTorus;
	}

	public double perimeter() {
		return this.perimeter;
	}

	public double halfPerimeter() {
		return this.perimeter / 2.0;
	}

	public int normalize(int value) {
		if (!this.onTorus) {
			return value;
		}

		return modp(value, integerPerimeter());
	}

	public int modp(int value, int perimeter) {
		assert perimeter > 0 : perimeter;

		int result = value % perimeter;

		if (result < 0) {
			result += perimeter;
		}

		return result;
	}

	public double normalize(double value) {
		if (!this.onTorus) {
			return value;
		}

		return modp(value, this.perimeter);
	}

	public double modp(double value, double perimeter) {
		assert perimeter > 0.0 : perimeter;

		double result = value % perimeter;

		if (result < 0.0) {
			result += perimeter;
		}

		return result;
	}

	public int normalizeDimension(int length) {
		assert length > 0 : length;

		int perimeter = integerPerimeter();

		if (!this.onTorus) {
			assert length <= perimeter : "dimension larger than a non-toric axis";

			return length;
		}

		int normalized = modp(length, perimeter);

		/*
		 * Une dimension égale à un ou plusieurs périmètres représente tout l'axe, pas
		 * une taille nulle.
		 */
		if (normalized == 0) {
			return perimeter;
		}

		return normalized;
	}

	public double normalizeDimension(double length) {
		assert length > 0.0 : length;

		if (!this.onTorus) {
			assert length <= this.perimeter : "dimension larger than a non-toric axis";

			return length;
		}

		double normalized = modp(length, this.perimeter);

		if (Math.abs(normalized) < 1.0e-9) {
			return this.perimeter;
		}

		return normalized;
	}

	/*
	 * Retourne le déplacement signé le plus court de from vers to. Sur un axe non
	 * torique, on garde simplement to - from. Sur un axe torique, on choisit le
	 * sens le plus court autour du tore.
	 */
	public double delta(double from, double to) {
		double d = to - from;

		if (!this.onTorus) {
			return d;
		}

		d = modp(d, this.perimeter);

		if (d > this.perimeter / 2.0) {
			d -= this.perimeter;
		}

		return d;
	}

	public double distance(double position1, double position2) {
		double delta = Math.abs(position2 - position1);
		double result;

		if (!this.onTorus) {
			result = delta;
		} else {
			delta = modp(delta, this.perimeter);

			// Sur un tore, on garde le plus court chemin entre les deux sens.
			result = Math.min(delta, this.perimeter - delta);
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.log(Level.FINER,
					"Axis distance: p1={0}, p2={1}, result={2}, torus={3}",
					new Object[] {
							Double.valueOf(position1),
							Double.valueOf(position2),
							Double.valueOf(result),
							Boolean.valueOf(this.onTorus)
					});
		}

		return result;
	}

	public double euclidean(double xmin, double xmax) {
		if (!this.onTorus) {
			return xmax;
		}

		double nxmin = normalize(xmin);
		double nxmax = normalize(xmax);

		// Si l'intervalle traverse le bord du tore, on le déroule localement.
		if (nxmax < nxmin) {
			nxmax += this.perimeter;
		}

		return nxmax;
	}

	private int integerPerimeter() {
		assert this.perimeter == Math.rint(this.perimeter) : "integer normalization requires an integer perimeter";

		return (int) Math.rint(this.perimeter);
	}

	public int delta(int from, int to) {
		int difference = to - from;

		if (!this.onTorus) {
			return difference;
		}

		int perimeter = integerPerimeter();

		difference = modp(difference, perimeter);

		if (difference > perimeter / 2) {
			difference -= perimeter;
		}

		return difference;
	}
}