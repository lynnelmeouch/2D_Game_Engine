package game.model.slide;

import engine.model.Entity;
import engine.model.ISU;

final class SlideMotion {

	private boolean sliding; // indique si le bloc est en glissade
	private int direction;// direction actuelle de la glissade
	private double currentSpeed_cmPerSecond; // vitesse actuelle du bloc en cm par seconde
	private double deceleration_cmPerSecond2; // deceleration appliquee pendant la glissade
	private double traveledDistance_cm;// distance deja parcourue pendant la glissade
	private double maximumDistance_cm;// distance maximale autorisee pour la glissade
	private ISU.Coord previousCenter;// position precedente utilisee pour calculer la distance parcourue
	private long lastUpdate_ms;// dernier temps utilise pour calculer la deceleration

	boolean isSliding() {
		return this.sliding;
	}

	int direction() {
		return this.direction;
	}

	double currentSpeedCmPerSecond() {
		return this.currentSpeed_cmPerSecond;
	}

	double traveledDistanceCm() {
		return this.traveledDistance_cm;
	}

	double maximumDistanceCm() {
		return this.maximumDistance_cm;
	}

	// initialise une nouvelle glissade
	void start(Entity entity, int direction, double initialSpeed, double maximumDistance, long nowMs) {
		this.sliding = true;
		this.direction = direction;

		this.currentSpeed_cmPerSecond = initialSpeed;
		this.maximumDistance_cm = maximumDistance;
		this.traveledDistance_cm = 0.0;

		this.previousCenter = entity.center();
		this.lastUpdate_ms = nowMs;

		this.deceleration_cmPerSecond2 = (initialSpeed * initialSpeed) / (2.0 * maximumDistance);
	}

	// ajoute la distance parcourue depuis la derniere mise a jour
	void updateDistance(Entity entity) {
		ISU.Coord currentCenter = entity.center();

		if (this.previousCenter != null) {
			this.traveledDistance_cm += this.previousCenter.distanceTo(currentCenter);
		}

		this.previousCenter = currentCenter;
	}

	// reduit la vitesse selon le temps ecoule
	void decelerate(long nowMs) {
		long elapsed = this.lastUpdate_ms == 0L ? 0L : nowMs - this.lastUpdate_ms;
		this.lastUpdate_ms = nowMs;

		double dt = Math.max(0.0, elapsed / 1000.0);

		this.currentSpeed_cmPerSecond = Math.max(0.0,
				this.currentSpeed_cmPerSecond - this.deceleration_cmPerSecond2 * dt);
	}

	// verifie si la distance maximale est atteinte
	boolean reachedMaximumDistance(double eps) {
		return this.traveledDistance_cm + eps >= this.maximumDistance_cm;
	}

	// verifie si la vitesse est trop faible pour continuer
	boolean isTooSlow(double eps) {
		return this.currentSpeed_cmPerSecond <= eps;
	}

	// remet a zero letat de glissade
	void stop() {
		this.sliding = false;
		this.currentSpeed_cmPerSecond = 0.0;
		this.deceleration_cmPerSecond2 = 0.0;
		this.previousCenter = null;
		this.lastUpdate_ms = 0L;
	}
}