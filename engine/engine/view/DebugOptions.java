package engine.view;

public final class DebugOptions {

	private boolean enabled;
	private boolean showActions;
	private boolean showBoundingBoxes;
	private boolean showPerformance;
	private boolean loadTest;

	public DebugOptions() {
		this.enabled = false;
		this.showActions = true;
		this.showBoundingBoxes = true;
		this.showPerformance = true;
		this.loadTest = false;
	}

	public boolean enabled() {
		return this.enabled;
	}

	public boolean showActions() {
		return this.enabled && this.showActions;
	}

	public boolean showBoundingBoxes() {
		return this.enabled && this.showBoundingBoxes;
	}

	public boolean showPerformance() {
		return this.enabled && this.showPerformance;
	}

	public boolean loadTest() {
		return this.enabled && this.loadTest;
	}

	public void toggleEnabled() {
		this.enabled = !this.enabled;
	}

	public void toggleActions() {
		this.showActions = !this.showActions;
	}

	public void toggleBoundingBoxes() {
		this.showBoundingBoxes = !this.showBoundingBoxes;
	}

	public void togglePerformance() {
		this.showPerformance = !this.showPerformance;
	}

	public void toggleLoadTest() {
		this.loadTest = !this.loadTest;
	}
}