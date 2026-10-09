package engine.view;

public final class TimeStats {

	private long count;
	private double min;
	private double max;
	private double sum;

	public TimeStats() {
		reset();
	}

	public void reset() {
		this.count = 0L;
		this.min = 0.0;
		this.max = 0.0;
		this.sum = 0.0;
	}

	public void record(double value) {
		if (value < 0.0) {
			return;
		}

		if (this.count == 0L) {
			this.min = value;
			this.max = value;
		} else {
			this.min = Math.min(this.min, value);
			this.max = Math.max(this.max, value);
		}

		this.sum += value;
		this.count++;
	}

	public double min() {
		return this.min;
	}

	public double max() {
		return this.max;
	}

	public double average() {
		if (this.count == 0L) {
			return 0.0;
		}

		return this.sum / this.count;
	}
}