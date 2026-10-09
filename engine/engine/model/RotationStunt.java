package engine.model;

public class RotationStunt extends Stunt {

	private static final double DEFAULT_ANGULAR_SPEED = 180.0;

	private final double angularSpeed;

	private boolean turningLeft;
	private boolean turningRight;

	public RotationStunt(Model model, Entity entity) {
		this(model, entity, DEFAULT_ANGULAR_SPEED);
	}

	public RotationStunt(Model model, Entity entity, double angularSpeed) {
		super(model, entity);

		assert angularSpeed > 0.0 : angularSpeed;

		this.angularSpeed = angularSpeed;
	}

	public void startTurnRight() {
		if (!valid()) {
			return;
		}

		this.turningRight = true;
		this.turningLeft = false;

		this.entity.setAngularSpeed(this.angularSpeed);
	}

	public void startTurnLeft() {
		if (!valid()) {
			return;
		}

		this.turningLeft = true;
		this.turningRight = false;

		this.entity.setAngularSpeed(-this.angularSpeed);
	}

	public void stopTurnRight() {
		if (!this.turningRight) {
			return;
		}

		this.turningRight = false;

		if (!this.turningLeft) {
			this.entity.setAngularSpeed(0.0);
		}
	}

	public void stopTurnLeft() {
		if (!this.turningLeft) {
			return;
		}

		this.turningLeft = false;

		if (!this.turningRight) {
			this.entity.setAngularSpeed(0.0);
		}
	}

	public boolean turningLeft() {
		return this.turningLeft;
	}

	public boolean turningRight() {
		return this.turningRight;
	}

	public boolean rotating() {
		return this.turningLeft || this.turningRight;
	}

	public boolean isTurningLeft() {
		return turningLeft();
	}

	public boolean isTurningRight() {
		return turningRight();
	}

	public boolean isRotating() {
		return rotating();
	}

	@Override
	protected void onInvalidate() {
		this.turningLeft = false;
		this.turningRight = false;
		this.entity.setAngularSpeed(0.0);
	}

	@Override
	public boolean walk(int degree) {
		return false;
	}
}
