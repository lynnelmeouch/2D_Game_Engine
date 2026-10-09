package game.config.map;

public final class PengoMapChoice {

	private final String label;
	private final String path;

	private final int width;
	private final int height;

	private final boolean torusX;
	private final boolean torusY;

	public PengoMapChoice(String label, String path, int width, int height, boolean torusX, boolean torusY) {
		this.label = label;
		this.path = path;
		this.width = width;
		this.height = height;
		this.torusX = torusX;
		this.torusY = torusY;
	}

	public String label() {
		return this.label;
	}

	public String path() {
		return this.path;
	}

	public int width() {
		return this.width;
	}

	public int height() {
		return this.height;
	}

	public boolean torusX() {
		return this.torusX;
	}

	public boolean torusY() {
		return this.torusY;
	}

	public String geometryLabel() {
		if (this.torusX && this.torusY) {
			return "tore X/Y";
		}
		if (this.torusX) {
			return "tore horizontal";
		}
		if (this.torusY) {
			return "tore vertical";
		}
		return "non torique";
	}
}