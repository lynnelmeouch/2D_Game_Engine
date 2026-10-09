// == ENTITY ==
package engine.model;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import engine.view.Avatar;

/*
 * Classe de base de toutes les entités du jeu.
 *
 * Une entité possède une position dans le monde, une taille, une orientation,
 * une vitesse, un bounding pour les collisions, et éventuellement un stunt
 * pour gérer ses actions.
 */
public abstract class Entity {
	private static final Logger logger = Logger.getLogger(Entity.class.getName());

	// Références vers les objets principaux du modèle.
	protected final Model model;
	protected final Grid grid;
	protected final ISU isu;
	protected final String name;

	// Collision et occupation de la grille.
	protected Bounding bounding;
	protected final Set<Grid.Cell> occupied;

	// Le stunt exécute les actions de l'entité.
	protected Stunt stunt;

	// Vitesses utilisées par le Model pendant les ticks.
	protected ISU.Vector lSpeed;
	protected double aSpeed;

	// Ordres d'affichage : profondeur et priorité dans une même couche.
	protected int zOrder;
	protected int sOrder;

	// Informations géométriques de l'entité.
	protected ISU.Dimension size;
	protected ISU.Dimension step;
	protected Grid.Position position;
	protected ISU.Coord center;

	// Représentation graphique de l'entité.
	protected Avatar avatar;
	private boolean installed;
	protected boolean alive;

	// Orientation en degrés : 0 Est, 90 Sud, 180 Ouest, 270 Nord.
	protected int orientation_degree;

	public Entity(Model model, String name) {
		this.model = model;
		this.grid = model.grid();
		this.isu = model.isu();
		this.name = name;

		this.occupied = new HashSet<Grid.Cell>();

		/*
		 * Par défaut, une entité a la taille d'une cellule et se déplace d'une cellule
		 * à chaque pas.
		 */
		this.size = isu.new Dimension(isu.cmPerCell(), isu.cmPerCell());
		this.step = isu.new Dimension(isu.cmPerCell(), isu.cmPerCell());

		this.position = grid.new Position(0, 0);
		this.center = this.position.toISUCoordCentered();

		this.lSpeed = isu.new Vector(0.0, 0.0);
		this.aSpeed = 0.0;

		this.alive = true;

	}

	// SETTERS

	public final void setPosition(Grid.Position position) {
		model.place(this, position);
	}

	public final void setCoord(ISU.Coord center) {
		model.place(this, center);
	}

	public final void translate(Grid.Vector vector) {
		model.translate(this, vector);
	}

	public final void translate(ISU.Vector vector) {
		model.translate(this, vector);
	}

	public final void turn(int angleDegree) {
		model.rotate(this, angleDegree);
	}

	public final void setOrientation(int orientationDegree) {
		model.setOrientation(this, orientationDegree);
	}

	protected final void setSize(Grid.Dimension dimension) {
		setSize(dimension.toISUDimension());
	}

	protected final void setStep(ISU.Dimension step) {
		assert step.isu() == this.isu;

		this.step = this.isu.new Dimension(step.x(), step.y());
	}

	protected final void setSize(ISU.Dimension dimension) {
		assert dimension.isu() == this.isu;

		boolean wasInstalled = this.installed;

		if (wasInstalled) {
			retract();
		}

		this.size = this.isu.new Dimension(dimension.x(), dimension.y());

		/*
		 * Pendant le constructeur d'une sous-classe, le Bounding peut ne pas encore
		 * être construit.
		 */
		if (this.bounding != null) {
			updateBounding();
		}

		if (wasInstalled) {
			deploy();
			checkGeometry();
		}
	}

	public final void setStunt(Stunt stunt) {
		assert stunt.model() == this.model;
		assert stunt.entity() == this;

		/*
		 * Le Stunt demandé est déjà installé.
		 */
		if (this.stunt == stunt) {
			return;
		}

		/*
		 * L'ancien Stunt ne doit plus recevoir de notifications ni commander l'entité.
		 */
		if (this.stunt != null) {
			this.stunt.invalidate();
		}

		/*
		 * Une action commencée par l'ancien Stunt ne doit pas continuer après son
		 * remplacement.
		 */
		this.lSpeed = this.isu.new Vector(0.0, 0.0);

		this.aSpeed = 0.0;

		this.stunt = stunt;
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "Entity stunt changed: entity={0}, stunt={1}",
					new Object[] { this.name, stunt.getClass().getSimpleName() });
		}
	}

	public void setAvatar(Avatar avatar) {
		this.avatar = avatar;
	}

	public void setZOrder(int zOrder) {
		this.zOrder = zOrder;
	}

	public void setSOrder(int sOrder) {
		this.sOrder = sOrder;
	}

	public void setLinearSpeed(ISU.Vector speed) {
		assert speed.isu() == this.isu;

		this.lSpeed = this.isu.new Vector(speed.x(), speed.y());
	}

	public void setAngularSpeed(double speed) {
		this.aSpeed = speed;
	}

	public final void removeStunt() {
		if (this.stunt == null) {
			return;
		}

		this.stunt.invalidate();
		this.stunt = null;

		this.lSpeed = this.isu.new Vector(0.0, 0.0);

		this.aSpeed = 0.0;
		logger.log(Level.FINE, "Entity stunt removed: {0}", this.name);
	}

	public void removeAvatar() {
		this.avatar = null;
	}

	// GETTERS

	public int zOrder() {
		return this.zOrder;
	}

	public int sOrder() {
		return this.sOrder;
	}

	public ISU.Vector linearSpeed() {
		return this.lSpeed.copy();
	}

	public double angularSpeed() {
		return this.aSpeed;
	}

	public Avatar avatar() {
		return this.avatar;
	}

	public Model model() {
		return this.model;
	}

	public ISU.Coord center() {
		return this.center.mkCopy();
	}

	public Grid.Position position() {
		return this.position.copy();
	}

	public int orientation() {
		return this.orientation_degree;
	}

	public ISU.Dimension size() {
		return this.size.copy();
	}

	public ISU.Dimension step() {
		return this.step.copy();
	}

	public String name() {
		return this.name;
	}

	public Bounding bounding() {
		return this.bounding;
	}

	public Set<Grid.Cell> occupied() {
		return new HashSet<Grid.Cell>(this.occupied);
	}

	public Stunt stunt() {
		return this.stunt;
	}

	public boolean alive() {
		return this.alive;
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.println("Entity: " + this.name);
		ps.println("orientation = " + this.orientation_degree + " degree");

		if (this.position != null) {
			ps.print("position = ");
			this.position.show(ps);
		}

		if (this.center != null) {
			ps.print("center = ");
			this.center.show(ps);
		}

		if (this.size != null) {
			ps.print("size = ");
			this.size.show(ps);
		}

		if (this.step != null) {
			ps.print("step = ");
			this.step.show(ps);
		}
	}

	// MOVE

	public void moveNorth(int nStep) {
		assert nStep >= 0 : nStep;

		ISU.Vector v = isu.new Vector(0.0, -nStep * this.step.y());
		translate(v);
	}

	public void moveSouth(int nStep) {
		assert nStep >= 0 : nStep;

		ISU.Vector v = isu.new Vector(0.0, nStep * this.step.y());
		translate(v);
	}

	public void moveEast(double length_cm) {
		assert length_cm >= 0.0 : length_cm;

		ISU.Vector v = isu.new Vector(length_cm, 0.0);
		translate(v);
	}

	public void moveWest(double length_cm) {
		assert length_cm >= 0.0 : length_cm;

		ISU.Vector v = isu.new Vector(-length_cm, 0.0);
		translate(v);
	}

	// COLLISION

	/*
	 * Deux entités se touchent si leurs bounding shapes se touchent.
	 */
	public boolean intersects(Entity e) {

		return this.bounding.intersects(e.bounding);
	}

	public double distanceCenterToCenter(Entity e) {

		return this.center.distanceTo(e.center);
	}

	/*
	 * Chaque sous-classe construit son propre bounding.
	 */
	protected abstract Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree);

	/*
	 * Quand l'entité change de position, taille ou orientation, son bounding doit
	 * être reconstruit.
	 */
	protected final void updateBounding() {
		this.bounding = createBounding(this.center, this.size, this.orientation_degree);
	}

	/*
	 * Construit le Bounding de l'entité à une pose future, sans modifier son
	 * centre, sa position ou ses cellules.
	 */
	final Bounding boundingAt(ISU.Coord center, int orientationDegree) {
		assert center.isu() == this.isu;

		int normalizedOrientation = ((orientationDegree % 360) + 360) % 360;

		ISU.Coord centerCopy = this.isu.new Coord(center.x(), center.y());

		ISU.Dimension sizeCopy = this.isu.new Dimension(this.size.x(), this.size.y());

		return createBounding(centerCopy, sizeCopy, normalizedOrientation);
	}
	// OCCUPATION DE LA GRILLE

	protected final void occupy(Grid.Position position) {
		assert this.installed;

		Grid.Cell cell = this.grid.cellAt(position);

		if (!cell.contains(this)) {
			cell.add(this);
		}

		this.occupied.add(cell);
	}

	protected final void retract() {
		Set<Grid.Cell> previousCells = new HashSet<Grid.Cell>(this.occupied);

		for (Grid.Cell cell : previousCells) {
			cell.remove(this);
		}

		this.occupied.clear();
	}

	/*
	 * Une entité normale occupe la cellule contenant son centre. Boss peut
	 * redéfinir cette méthode.
	 */
	protected void deploy() {
		assert this.installed;

		occupy(this.position);
	}

	// NOTIFICATIONS DE COLLISION

	public void collision(Entity e) {

		if (this.stunt != null) {
			this.stunt.collision(e);
		}
	}

	public void collision(java.util.List<Entity> entities) {

		if (this.stunt != null) {
			this.stunt.collision(entities);
		}
	}

	/*
	 * Marque l'entité comme morte.
	 *
	 * L'entité n'est pas retirée immédiatement du Model :
	 * Model.cleanupDeadEntities() le fera à la fin du tick.
	 */
	public final void death() {
		if (!this.alive) {
			return;
		}

		this.alive = false;
		logger.log(Level.FINE, "Entity died: {0}", this.name);

		/*
		 * Une entité morte ne doit plus continuer son mouvement.
		 */
		this.lSpeed = this.isu.new Vector(0.0, 0.0);

		this.aSpeed = 0.0;

		/*
		 * Le Stunt ne doit plus transmettre de commande ou de notification au Bot.
		 */
		if (this.stunt != null) {
			this.stunt.invalidate();
		}
	}

	public String debugLabel() {
		String status = isMoving() ? "MOVING" : "IDLE";
		return this.name + " " + status;
	}

	// NOTIFICATIONS VERS LE STUNT

	private void notifyOrientationChanged() {
		if (this.stunt != null) {
			this.stunt.set(this.orientation_degree);
		}
	}

	private void notifyPositionChanged() {
		if (this.stunt != null) {
			this.stunt.set(this.center.x(), this.center.y());
		}
	}

	private void notifyCellChanged(Grid.Cell cell) {
		if (this.stunt != null) {
			this.stunt.set(cell);
		}
	}

	void commit() {
		if (this.stunt != null) {
			this.stunt.commit();
		}
	}

	// HITBOX DU MOUVEMENT

	/*
	 * Construit une Hitbox qui recouvre toutes les poses intermédiaires du
	 * mouvement.
	 */
	final Hitbox motionHitboxBetween(ISU.Coord from, ISU.Vector movement, int fromOrientation, int toOrientation,
			int nsteps) {
		assert from.isu() == this.isu;
		assert movement.isu() == this.isu;
		assert nsteps >= 1;

		int orientationDelta = shortestAngleDelta(fromOrientation, toOrientation);

		Bounding firstBounding = boundingAt(from, fromOrientation);

		Hitbox result = firstBounding.hitbox();

		for (int i = 1; i <= nsteps; i++) {
			double ratio = (double) i / nsteps;

			ISU.Coord testedCenter = this.isu.new Coord(from.x() + movement.x() * ratio,
					from.y() + movement.y() * ratio);

			int testedOrientation = normalizeOrientation(fromOrientation + (int) Math.round(orientationDelta * ratio));

			Bounding testedBounding = boundingAt(testedCenter, testedOrientation);

			result = result.union(testedBounding.hitbox());
		}

		return result;
	}

	/*
	 * Indique si cette entité bloque physiquement l'autre.
	 */
	public boolean blocks(Entity other) {
		return false;
	}

	/*
	 * Calcule une hitbox globale sur tout un mouvement.
	 *
	 * On échantillonne plusieurs positions entre le départ et l'arrivée, puis on
	 * fait l'union des hitbox obtenues. Cela limite les collisions ratées quand une
	 * entité se déplace vite.
	 */
	public Hitbox hitboxBetween(ISU.Coord from, ISU.Coord to) {

		Bounding startBounding = boundingAt(from, this.orientation_degree);

		Hitbox current = startBounding.hitbox();

		ISU.Vector delta = from.mkVectorToward(to);

		Hitbox moved = new Hitbox(current.xmin() + delta.x(), current.xmax() + delta.x(), current.ymin() + delta.y(),
				current.ymax() + delta.y());

		return current.union(moved);
	}

	private int shortestAngleDelta(int fromOrientation, int toOrientation) {
		return ((toOrientation - fromOrientation + 540) % 360) - 180;
	}

	public boolean isMoving() {
		return this.lSpeed.norm() > 0.000001 || Math.abs(this.aSpeed) > 0.000001;
	}

	private void checkGeometry() {

		/*
		 * Position Grid et centre ISU doivent toujours représenter le même emplacement.
		 */
		assert this.position.equals(this.center.toGridPosition()) : "center and Grid position are inconsistent";

		/*
		 * Une entité non installée ne doit être présente dans aucune cellule.
		 */
		if (!this.installed) {
			assert this.occupied.isEmpty() : "an uninstalled entity occupies cells";

			return;
		}

		/*
		 * Une entité installée doit au minimum occuper la cellule contenant son centre.
		 */
		Grid.Cell centerCell = this.grid.cellAt(this.position);

		assert this.occupied.contains(centerCell) : "the center cell is not occupied";

		/*
		 * La relation Entity/Cell doit être symétrique.
		 */
		for (Grid.Cell cell : this.occupied) {

			assert cell.contains(this) : "Entity/Cell occupation is inconsistent";
		}
	}

	/*
	 * Prépare la position initiale d'une entité.
	 *
	 * Cette méthode est appelée par Model.add(...) avant l'installation dans les
	 * cellules.
	 */
	final void prepareInitialPosition(Grid.Position initialPosition) {
		assert !this.installed;
		assert this.occupied.isEmpty();

		this.position = initialPosition.copy();

		this.position.normalize();

		this.center = this.position.toISUCoordCentered();

		updateBounding();

		checkGeometry();
	}

	/*
	 * Installe l'entité dans les cellules correspondant à sa position et à sa
	 * forme.
	 */
	final void install() {
		assert !this.installed;
		assert this.occupied.isEmpty();

		this.installed = true;

		deploy();

		checkGeometry();

		logger.log(Level.FINE, "Entity installed: {0}", this.name);
	}

	/*
	 * Retire complètement l'entité des cellules.
	 */
	final void uninstall() {
		if (!this.installed) {
			return;
		}

		retract();

		this.installed = false;

		checkGeometry();

		logger.log(Level.FINE, "Entity uninstalled: {0}", this.name);
	}

	public final boolean installed() {
		return this.installed;
	}

	/*
	 * Seul le Model utilise cette méthode.
	 *
	 * Elle synchronise en une seule opération : - le centre ISU ; - la position
	 * Grid ; - l'orientation ; - le Bounding ; - les cellules occupées.
	 */
	final void applyPoseFromModel(ISU.Coord newCenter, int newOrientation, boolean notifyStunt) {
		assert newCenter.isu() == this.isu;

		ISU.Coord oldCenter = this.center.mkCopy();

		int oldOrientation = this.orientation_degree;

		Grid.Cell oldCenterCell = null;

		if (this.installed) {
			oldCenterCell = this.grid.cellAt(this.position);

			retract();
		}

		/*
		 * Le constructeur de Coord normalise automatiquement la coordonnée.
		 */
		this.center = this.isu.new Coord(newCenter.x(), newCenter.y());

		/*
		 * toGridPosition produit une Position normalisée.
		 */
		this.position = this.center.toGridPosition();

		this.orientation_degree = normalizeOrientation(newOrientation);

		updateBounding();

		if (this.installed) {
			deploy();
		}

		if (notifyStunt && this.stunt != null) {

			if (!oldCenter.equals(this.center)) {
				notifyPositionChanged();
			}

			if (oldOrientation != this.orientation_degree) {
				notifyOrientationChanged();
			}

			if (this.installed) {
				Grid.Cell newCenterCell = this.grid.cellAt(this.position);

				if (oldCenterCell != newCenterCell) {
					notifyCellChanged(newCenterCell);
				}
			}
		}

		checkGeometry();
	}

	private int normalizeOrientation(int angleDegree) {
		return ((angleDegree % 360) + 360) % 360;
	}
}