package engine.view;

public final class PerformanceMonitor {

	private final TimeStats tickStats;
	private final TimeStats paintStats;
	private final TimeStats fpsStats;

	private long lastPaintTime;

	public PerformanceMonitor() {
		this.tickStats = new TimeStats();
		this.paintStats = new TimeStats();
		this.fpsStats = new TimeStats();
	}

	public void recordTick(long elapsedMs) {
		if (elapsedMs < 0L) {
			return;
		}

		this.tickStats.record(elapsedMs);
	}

	public void recordPaintEvent() {
		long now = System.currentTimeMillis();

		if (this.lastPaintTime != 0L) {
			long elapsed = now - this.lastPaintTime;

			this.paintStats.record(elapsed);

			if (elapsed > 0L) {
				double fps = 1000.0 / elapsed;
				this.fpsStats.record(fps);
			}
		}

		this.lastPaintTime = now;
	}

	public TimeStats tickStats() {
		return this.tickStats;
	}

	public TimeStats paintStats() {
		return this.paintStats;
	}

	public TimeStats fpsStats() {
		return this.fpsStats;
	}

	public void reset() {
		this.tickStats.reset();
		this.paintStats.reset();
		this.fpsStats.reset();
		this.lastPaintTime = 0L;
	}
}