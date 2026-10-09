package engine.model;

public final class Orientation {

	// Classe utilitaire pour ne pas répéter le code des directions cardinales.
	
	public static final int EAST = 0;
	public static final int SOUTH = 90;
	public static final int WEST = 180;
	public static final int NORTH = 270;

	private Orientation() {
	}

	public static int normalize(int degree) {
		return normalizeCardinal(degree);
	}

	public static int normalizeCardinal(int degree) {
		int d = ((degree % 360) + 360) % 360;

		if (d < 45 || d >= 315) {
			return EAST;
		}

		if (d < 135) {
			return SOUTH;
		}

		if (d < 225) {
			return WEST;
		}

		return NORTH;
	}

	public static int leftOf(int orientation) {
		return normalizeCardinal(orientation - 90);
	}

	public static int rightOf(int orientation) {
		return normalizeCardinal(orientation + 90);
	}

	public static int backOf(int orientation) {
		return normalizeCardinal(orientation + 180);
	}

	public static boolean opposite(int first, int second) {
		return backOf(first) == normalizeCardinal(second);
	}

	public static Grid.Vector vector(Grid grid, int direction) {

		switch (normalizeCardinal(direction)) {
		case EAST:
			return grid.new Vector(1, 0);
		case SOUTH:
			return grid.new Vector(0, 1);
		case WEST:
			return grid.new Vector(-1, 0);
		case NORTH:
			return grid.new Vector(0, -1);
		default:
			throw new AssertionError();
		}
	}

	public static Grid.Position neighbor(Grid grid, Grid.Position from, int direction) {

		return neighbor(grid, from, direction, grid.xAxis().onTorus(), grid.yAxis().onTorus());
	}

	public static Grid.Position neighbor(Grid grid, Grid.Position from, int direction, boolean torusOnXaxis,
			boolean torusOnYaxis) {

		int normalizedDirection = Orientation.normalizeCardinal(direction);
		int dx = 0;
		int dy = 0;

		switch (normalizedDirection) {
		case EAST:
			dx = 1;
			break;
		case SOUTH:
			dy = 1;
			break;
		case WEST:
			dx = -1;
			break;
		case NORTH:
			dy = -1;
			break;
		default:
			throw new AssertionError();
		}

		int x = from.x() + dx;
		int y = from.y() + dy;

		boolean outsideX = x < 0 || x >= grid.width();
		boolean outsideY = y < 0 || y >= grid.height();

		if (!torusOnXaxis && outsideX) {
			return null;
		}

		if (!torusOnYaxis && outsideY) {
			return null;
		}

		return grid.new Position(x, y);
	}
}
