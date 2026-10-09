package game.config;

public final class PengoRunState {

	private boolean paused;
	private boolean stopped;
	private long pauseStarted_ms;
	private long totalPaused_ms;

	public PengoRunState() {
		this.pauseStarted_ms = 0L;
		this.totalPaused_ms = 0L;
	}

	public boolean paused() {
		return this.paused;
	}

	public boolean stopped() {
		return this.stopped;
	}

	public boolean running() {
		return !this.paused && !this.stopped;
	}

	// Retourne le temps courant du jeu sans compter le temps passe en pause
	public long nowMs() {
		long now = System.currentTimeMillis();
		long pausedNow = 0L;
		if (this.paused) {
			pausedNow = now - this.pauseStarted_ms;
		}
		return now - totalPaused_ms - pausedNow;
	}

	// met le jeu en pauses
	public void pause() {
		if (stopped || paused) {
			return;
		}
		this.paused = true;
		this.pauseStarted_ms = System.currentTimeMillis();
	}

	// reprend le jeu
	public long resume() {
		if (!this.paused) {
			return 0L;
		}
		long now = System.currentTimeMillis();
		long pauseDuration = now - this.pauseStarted_ms;
		if (pauseDuration < 0L) {
			pauseDuration = 0L;
		}
		this.totalPaused_ms += pauseDuration;
		this.pauseStarted_ms = 0L;
		this.paused = false;
		return pauseDuration;
	}

	//arrete la session
	public void stop() {
		this.stopped = true;
		this.paused = false;
		this.pauseStarted_ms = 0L;
	}
}