package engine.model;

import java.util.List;

public class BasicStunt extends Stunt {

	private static final double EPS = 0.000001;
	private static final double TURN_SNAP_RATIO = 0.30;

	private static final double DEFAULT_WALK_SPEED = 5.0;

	private double walkSpeed;

	private boolean walking;

	private ISU.Coord startCenter;
	private ISU.Coord targetCenter;

	private boolean pendingDone;

	public BasicStunt(Model model, Entity entity) {
		this(model, entity, DEFAULT_WALK_SPEED);
	}

	public BasicStunt(Model model, Entity entity, double walkSpeed) {
		super(model, entity);

		assert walkSpeed > 0.0 : walkSpeed;

		this.walkSpeed = walkSpeed;

	}

	public boolean walking() {
		return this.walking;
	}

	// i added this because the enemy’s BasicStunt still thinks it is in the middle
	// of a walk
	public void cancelCurrentAction() {
		if (!valid()) {
			return;
		}

		resetAction();

		entity.setLinearSpeed(model.isu().new Vector(0.0, 0.0));
		entity.setAngularSpeed(0.0);

		if (entity.installed()) {
			model.placeSilently(entity, entity.position().toISUCoordCentered());
		}
	}

	public double walkSpeed() {
		return this.walkSpeed;
	}

	public void setWalkSpeed(double walkSpeed) {
		if (walkSpeed <= 0.0) {
			return;
		}

		this.walkSpeed = walkSpeed;
	}

	@Override
	public boolean walk(int degree) {
		if (!valid()) {
			return false;
		}
		int orientation = normalizeCardinal(degree);

		/*
		 * Si l'entité marche déjà, on ne refuse plus automatiquement. On essaie de
		 * changer la direction pendant le mouvement.
		 */
		if (this.walking) {
			return changeDirectionWhileWalking(orientation);
		}

		return startWalkFromCurrentCell(orientation);
	}

	@Override
	public void set(double x, double y) {

		if (!valid()) {
			return;
		}

		if (!this.walking || this.pendingDone) {
			return;
		}

		ISU.Coord current = model.isu().new Coord(x, y);

		double walked = this.startCenter.distanceTo(current);

		double expected = this.startCenter.distanceTo(this.targetCenter);

		if (walked + EPS < expected) {
			return;
		}

		this.pendingDone = true;

		entity.setLinearSpeed(model.isu().new Vector(0.0, 0.0));

		entity.setAngularSpeed(0.0);
	}

	@Override
	public void commit() {

		if (!valid()) {
			return;
		}

		if (!this.pendingDone) {
			return;
		}

		ISU.Coord exactTarget = this.targetCenter.mkCopy();

		/*
		 * Le recentrage est effectué après validation complète du mouvement par le
		 * Model.
		 */
		model.placeSilently(entity, exactTarget);

		resetAction();

		notifyDone();
	}

	@Override
	protected void onInvalidate() {
		this.walking = false;
		this.pendingDone = false;
		this.startCenter = null;
		this.targetCenter = null;
	}

	private void resetAction() {
		this.walking = false;
		this.pendingDone = false;
		this.startCenter = null;
		this.targetCenter = null;
	}

	@Override
	public void collision(Entity other) {

		if (model.isBlocking(entity, other)) {
			stopBecauseOfCollision();
		}

		notifyCollision(other);
	}

	@Override
	public void collision(List<Entity> entities) {

		boolean blocked = false;

		for (Entity other : entities) {
			if (other != entity && model.isBlocking(entity, other)) {
				blocked = true;
				break;
			}
		}

		if (blocked) {
			stopBecauseOfCollision();
		}

		notifyCollision(entities);
	}

	private void stopBecauseOfCollision() {
		resetAction();

		entity.setLinearSpeed(model.isu().new Vector(0.0, 0.0));

		entity.setAngularSpeed(0.0);
	}

	@Override
	public void set(int orientation) {
		// Notification : orientation changée.
	}

	@Override
	public void set(Grid.Cell cell) {
		// Notification : cellule changée.
	}

	private int normalizeCardinal(int degree) {
		int d = ((degree % 360) + 360) % 360;

		if (d < 45 || d >= 315) {
			return 0;
		}

		if (d < 135) {
			return 90;
		}

		if (d < 225) {
			return 180;
		}

		return 270;
	}

	private boolean startWalkFromCurrentCell(int orientation) {
		Grid.Position currentPosition = entity.position();
		Grid.Position targetPosition = neighborPosition(currentPosition, orientation);

		if (!canWalkTo(targetPosition)) {
			return false;
		}

		entity.turn(orientation - entity.orientation());

		this.startCenter = currentPosition.toISUCoordCentered();
		this.targetCenter = targetPosition.toISUCoordCentered();

		model.placeSilently(entity, this.startCenter);

		this.walking = true;
		this.pendingDone = false;

		entity.setLinearSpeed(speedForOrientation(orientation));
		entity.setAngularSpeed(0.0);

		return true;
	}

	private boolean changeDirectionWhileWalking(int newOrientation) {
		int currentOrientation = entity.orientation();

		/*
		 * Même direction : rien à changer.
		 */
		if (newOrientation == currentOrientation) {
			return true;
		}

		/*
		 * Demi-tour : autorisé immédiatement. Exemple : droite -> gauche, haut -> bas.
		 */
		if (isOpposite(currentOrientation, newOrientation)) {
			reverseImmediately(newOrientation);
			return true;
		}

		/*
		 * Direction perpendiculaire : on autorise seulement si on est proche du centre
		 * de sa cellule. Sinon on garde la commande en mémoire dans PlayerBot, elle
		 * sera retentée au prochain think().
		 */
		if (isCloseToCellCenter()) {
			Grid.Position currentPosition = entity.position();
			Grid.Position targetPosition = neighborPosition(currentPosition, newOrientation);

			if (!canWalkTo(targetPosition)) {
				return false;
			}

			ISU.Coord cellCenter = currentPosition.toISUCoordCentered();

			model.placeSilently(entity, cellCenter);
			entity.turn(newOrientation - entity.orientation());

			this.startCenter = cellCenter;
			this.targetCenter = targetPosition.toISUCoordCentered();

			entity.setLinearSpeed(speedForOrientation(newOrientation));
			entity.setAngularSpeed(0.0);

			return true;
		}

		return false;
	}

	private void reverseImmediately(int newOrientation) {
		ISU.Coord currentCenter = entity.center().mkCopy();

		/*
		 * En cas de demi-tour, la nouvelle cible devient l'ancien départ.
		 */
		ISU.Coord oldStart = this.startCenter.mkCopy();

		entity.turn(newOrientation - entity.orientation());

		this.startCenter = currentCenter;
		this.targetCenter = oldStart;

		entity.setLinearSpeed(speedForOrientation(newOrientation));
		entity.setAngularSpeed(0.0);

		this.pendingDone = false;
	}

	private Grid.Position neighborPosition(Grid.Position position, int orientation) {

		Grid.Position targetPosition = position.copy();

		switch (orientation) {
		case 0:
			targetPosition.translate(model.grid().new Vector(1, 0));
			break;

		case 90:
			targetPosition.translate(model.grid().new Vector(0, 1));
			break;

		case 180:
			targetPosition.translate(model.grid().new Vector(-1, 0));
			break;

		case 270:
			targetPosition.translate(model.grid().new Vector(0, -1));
			break;

		default:
			throw new AssertionError();
		}

		return targetPosition;
	}

	private ISU.Vector speedForOrientation(int orientation) {
		switch (orientation) {
		case 0:
			return model.isu().new Vector(entity.step().x() * this.walkSpeed, 0.0);

		case 90:
			return model.isu().new Vector(0.0, entity.step().y() * this.walkSpeed);

		case 180:
			return model.isu().new Vector(-entity.step().x() * this.walkSpeed, 0.0);

		case 270:
			return model.isu().new Vector(0.0, -entity.step().y() * this.walkSpeed);

		default:
			throw new AssertionError();
		}
	}

	private boolean isOpposite(int a, int b) {
		a = normalizeCardinal(a);
		b = normalizeCardinal(b);

		return ((a + 180) % 360) == b;
	}

	private boolean canWalkTo(Grid.Position targetPosition) {

		if (!model.grid().contains(targetPosition)) {
			return false;
		}

		Grid.Cell targetCell = model.grid().cellAt(targetPosition);

		for (Entity other : targetCell.entities()) {
			if (other != entity && model.isBlocking(entity, other)) {
				return false;
			}
		}

		return true;
	}

	private boolean isCloseToCellCenter() {
		Grid.Position currentPosition = entity.position();
		ISU.Coord cellCenter = currentPosition.toISUCoordCentered();

		double distance = entity.center().distanceTo(cellCenter);

		return distance <= model.isu().cmPerCell() * TURN_SNAP_RATIO;
	}
}