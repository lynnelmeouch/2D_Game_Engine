package engine.view;

public final class SpawnAnimation {

	private static final long DEFAULT_DURATION_MS = 900L;

	private final long durationMs;
	private long elapsedMs;

	public SpawnAnimation() {
		this(DEFAULT_DURATION_MS);
	}

	public SpawnAnimation(long durationMs) {
		assert durationMs > 0L;

		this.durationMs = durationMs;
	}

	public void tick(long elapsed) {
		assert elapsed >= 0L;

		if (this.elapsedMs >= this.durationMs) {
			return;
		}

		this.elapsedMs += elapsed;

		if (this.elapsedMs > this.durationMs) {
			this.elapsedMs = this.durationMs;
		}
	}

	public double scale() {
		double progress = (double) this.elapsedMs / (double) this.durationMs;

		if (progress < 0.0) {
			progress = 0.0;
		}

		if (progress > 1.0) {
			progress = 1.0;
		}

		if (progress < 0.70) {
			double p = progress / 0.70;
			return 0.05 + p * 1.15;
		}

		double p = (progress - 0.70) / 0.30;
		return 1.20 - p * 0.20;
	}

	public boolean finished() {
		return this.elapsedMs >= this.durationMs;
	}

	public void reset() {
		this.elapsedMs = 0L;
	}
}