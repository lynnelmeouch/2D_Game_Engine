package game.config;

import game.config.map.PengoMapChoice;

public final class PengoConfig {

	private final PengoMapChoice mapChoice;// map choisie
	private final boolean continuousWalk;// pour activer et desactiver la marche continue du joueur

	private final double cmPerCell;
	private final int pixelPerCm;

	private final int windowWidthPixel;
	private final int windowHeightPixel;

	private final int viewportWidthCells;
	private final int viewportHeightCells;

	private final int tickDelayMs;
	private final int paintDelayMs;

	private final long spawnDelayMs;
	private final int maxLivingEnemies;
	private final int maxTotalSpawnedEnemies;

	public PengoConfig(PengoMapChoice mapChoice, boolean continuousWalk) {
		this(mapChoice, continuousWalk, 4.0, 12, 720, 600, mapChoice.width(), mapChoice.height(), 33, 33, 3500L, 3, 5);
	}

	public PengoConfig(PengoMapChoice mapChoice, boolean continuousWalk, double cmPerCell, int pixelPerCm,
			int windowWidthPixel, int windowHeightPixel, int viewportWidthCells, int viewportHeightCells,
			int tickDelayMs, int paintDelayMs, long spawnDelayMs, int maxLivingEnemies, int maxTotalSpawnedEnemies) {

		assert cmPerCell > 0.0;
		assert pixelPerCm > 0;
		assert windowWidthPixel > 0;
		assert windowHeightPixel > 0;
		assert viewportWidthCells > 0;
		assert viewportHeightCells > 0;
		assert tickDelayMs > 0;
		assert paintDelayMs > 0;
		assert spawnDelayMs > 0L;
		assert maxLivingEnemies > 0;

		this.mapChoice = mapChoice;
		this.continuousWalk = continuousWalk;
		this.cmPerCell = cmPerCell;
		this.pixelPerCm = pixelPerCm;
		this.windowWidthPixel = windowWidthPixel;
		this.windowHeightPixel = windowHeightPixel;
		this.viewportWidthCells = viewportWidthCells;
		this.viewportHeightCells = viewportHeightCells;
		this.tickDelayMs = tickDelayMs;
		this.paintDelayMs = paintDelayMs;
		this.spawnDelayMs = spawnDelayMs;
		this.maxLivingEnemies = maxLivingEnemies;
		this.maxTotalSpawnedEnemies = maxTotalSpawnedEnemies;
	}

	// GETTER

	public PengoMapChoice mapChoice() {
		return this.mapChoice;
	}

	public String mapPath() {
		return this.mapChoice.path();
	}

	public boolean torusX() {
		return this.mapChoice.torusX();
	}

	public boolean torusY() {
		return this.mapChoice.torusY();
	}

	public boolean continuousWalk() {
		return this.continuousWalk;
	}

	public double cmPerCell() {
		return this.cmPerCell;
	}

	public int pixelPerCm() {
		return this.pixelPerCm;
	}

	public int windowWidthPixel() {
		return this.windowWidthPixel;
	}

	public int windowHeightPixel() {
		return this.windowHeightPixel;
	}

	public int viewportWidthCells() {
		return this.viewportWidthCells;
	}

	public int viewportHeightCells() {
		return this.viewportHeightCells;
	}

	public int tickDelayMs() {
		return this.tickDelayMs;
	}

	public int paintDelayMs() {
		return this.paintDelayMs;
	}

	public long spawnDelayMs() {
		return this.spawnDelayMs;
	}

	public int maxLivingEnemies() {
		return this.maxLivingEnemies;
	}

	public int maxTotalSpawnedEnemies() {
		return this.maxTotalSpawnedEnemies;
	}
}