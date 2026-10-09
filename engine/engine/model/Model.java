package engine.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Model représente l'état du monde à un instant donné.
 *
 * Il contient les entités du jeu et c'est lui qui applique les mouvements
 * pendant les ticks. Il gère aussi les collisions et la sélection des entités
 * visibles dans le viewport.
 */
public class Model {
	private static final Logger logger = Logger.getLogger(Model.class.getName());
	private Game game;
	private Grid grid;
	private ISU isu;

	private List<Entity> entities;

	private ViewPort viewPort;

	public Model(Game game) {
		
		this.game = game;
		this.grid = game.getGrid();
		this.isu = game.getIsu();

		this.entities = new ArrayList<Entity>();

		double defaultViewWidth = defaultViewPortLength(this.isu.xAxis(), this.isu.width());

		double defaultViewHeight = defaultViewPortLength(this.isu.yAxis(), this.isu.height());

		this.viewPort = new ViewPort(this, this.isu.new Coord(0.0, 0.0),
				this.isu.new Dimension(defaultViewWidth, defaultViewHeight));
		
		logger.info("Model created");
	}

	/*
	 * Sur un axe non torique, le viewport par défaut peut couvrir tout l'axe.
	 *
	 * Sur un axe torique, on retire une cellule afin d'obtenir une dimension
	 * strictement plus petite.
	 */
	private double defaultViewPortLength(Axis axis, double mapLength) {
		assert mapLength > 0.0;

		if (!axis.onTorus()) {
			return mapLength;
		}

		double result = mapLength - this.isu.cmPerCell();

		assert result > 0.0 : "a toric map must contain at least two cells";

		return result;
	}

	public Game game() {
		return this.game;
	}

	public Grid grid() {
		return this.grid;
	}

	public ISU isu() {
		return this.isu;
	}

	public List<Entity> entities() {
		return new ArrayList<Entity>(this.entities);
	}

	public ViewPort viewPort() {
		return this.viewPort;
	}

	public void setViewPort(ViewPort viewPort) {
		assert viewPort.model() == this;

		this.viewPort = viewPort;
	}

	// ENTITY MANAGEMENT

	public void add(Entity entity) {

		add(entity, entity.position());
	}

	public void add(Entity entity, Grid.Position initialPosition) {
		assert entity.model() == this;
		assert !this.entities.contains(entity);

		/*
		 * On fixe d'abord la pose de l'entité alors qu'elle n'est pas encore installée
		 * dans les cellules.
		 */
		entity.prepareInitialPosition(initialPosition);

		this.entities.add(entity);
		entity.install();
		logger.log(Level.INFO,
				"Entity added to model: {0}",
				entity.name());
	}

	public void remove(Entity entity) {
		assert entity.model() == this;

		if (!this.entities.contains(entity)) {
			return;
		}

		/*
		 * Une entité retirée ne doit plus conserver une vitesse active.
		 */
		entity.setLinearSpeed(this.isu.new Vector(0.0, 0.0));

		entity.setAngularSpeed(0.0);

		entity.uninstall();
		this.entities.remove(entity);
		logger.log(Level.INFO,
				"Entity removed from model: {0}",
				entity.name());
	}

	// BASIC COLLISIONS

	/*
	 * Retourne toutes les entités qui touchent l'entité donnée.
	 *
	 * Cette méthode teste les collisions à la position actuelle.
	 */
	public List<Entity> collisions(Entity entity) {

		List<Entity> result = new ArrayList<Entity>();

		for (Entity other : this.entities) {
			if (other != entity && other.alive() && entity.intersects(other)) {
				result.add(other);
			}
		}

		return result;
	}

	// MOTION OBJECT ///////////////////////////////////////////////////////////

	/*
	 * Motion contient le mouvement demandé pendant un tick.
	 *
	 * Le Model testera les poses intermédiaires et conservera la dernière pose
	 * valide en cas de collision.
	 */
	private class Motion {

		final Entity entity;

		final ISU.Coord oldCenter;
		final int oldOrientation;
		final int targetOrientation;

		/*
		 * Vecteur réellement parcouru pendant ce tick. Il ne doit pas être normalisé.
		 */
		final ISU.Vector movement;

		Motion(Entity entity, long elapsed) {
			assert elapsed >= 0;

			this.entity = entity;

			double dt = elapsed / 1000.0;

			this.oldCenter = entity.center();

			this.oldOrientation = entity.orientation();

			ISU.Vector speed = entity.linearSpeed();

			this.movement = isu.new Vector(speed.x() * dt, speed.y() * dt);

			this.targetOrientation = normalizeOrientation(
					this.oldOrientation + (int) Math.round(entity.angularSpeed() * dt));
		}
	}

	// MOVE //////////////////////////////////////////////////////////////////

	/*
	 * Version principale du déplacement.
	 *
	 * Elle tient compte du temps écoulé, cherche les collisions possibles pendant
	 * tout le mouvement, puis applique ou annule le déplacement.
	 */
	public boolean move(Entity entity, long elapsed) {
		assert elapsed >= 0;

		assertOwned(entity);

		Motion motion = new Motion(entity, elapsed);

		List<Entity> candidates = motionCandidates(motion);

		MotionResult result = resolveMotion(motion.entity, motion.oldCenter, motion.movement, motion.oldOrientation,
				motion.targetOrientation, candidates);

		applyResolvedMotion(entity, result);

		if (entity.alive() && entity.installed()) {
			entity.commit();
		}

		return !result.blocked();
	}

	private void assertOwned(Entity entity) {
		assert entity.model() == this;
		assert this.entities.contains(entity);
		assert entity.installed();
	}

	private List<Entity> motionCandidates(Motion motion) {

		List<Entity> candidates = new ArrayList<Entity>();

		List<Entity> pathCandidates = collisionsAlongMotion(motion.entity, motion.oldCenter, motion.movement);

		List<Entity> hitboxCandidates = potentialCollisionsByMotionHitbox(motion.entity, motion.oldCenter,
				motion.movement, motion.oldOrientation, motion.targetOrientation);

		addAllWithoutDuplicates(candidates, pathCandidates);

		addAllWithoutDuplicates(candidates, hitboxCandidates);

		return candidates;
	}

	/*
	 * Fait avancer toutes les entités du modèle.
	 *
	 * On travaille sur une copie de la liste, car certaines entités peuvent être
	 * supprimées pendant le tick.
	 */
	public void tick(long elapsed) {
		assert elapsed >= 0;

		List<Entity> copy = new ArrayList<Entity>(this.entities);

		for (Entity entity : copy) {
			if (entity.alive() && entity.isMoving()) {
				move(entity, elapsed);
			}
		}

		cleanupDeadEntities();
	}

	public void place(Entity entity, Grid.Position position) {
		assert position.grid() == this.grid;

		assertOwned(entity);

		entity.applyPoseFromModel(position.toISUCoordCentered(), entity.orientation(), true);
	}

	public void place(Entity entity, ISU.Coord center) {
		assert center.isu() == this.isu;

		assertOwned(entity);

		entity.applyPoseFromModel(center, entity.orientation(), true);
	}

	public void translate(Entity entity, ISU.Vector vector) {
		assert vector.isu() == this.isu;

		assertOwned(entity);

		ISU.Coord target = entity.center();

		target.translate(vector);

		entity.applyPoseFromModel(target, entity.orientation(), true);
	}

	public void translate(Entity entity, Grid.Vector vector) {
		assert vector.grid() == this.grid;

		assertOwned(entity);

		Grid.Position target = entity.position();

		target.translate(vector);

		entity.applyPoseFromModel(target.toISUCoordCentered(), entity.orientation(), true);
	}

	public void rotate(Entity entity, int angleDegree) {
		assertOwned(entity);
		entity.applyPoseFromModel(entity.center(), entity.orientation() + angleDegree, true);
	}

	public void setOrientation(Entity entity, int orientationDegree) {
		assertOwned(entity);
		entity.applyPoseFromModel(entity.center(), orientationDegree, true);
	}

	private void notifyContacts(Entity moving, List<Entity> contacts) {

		if (contacts.isEmpty()) {
			return;
		}

		/*
		 * L'entité qui effectue le mouvement traite les contacts.
		 *
		 * On ne fait pas de notification symétrique ici, sinon certains effets, comme
		 * les dégâts, sont appliqués deux fois.
		 */
		moving.collision(contacts);
	}

	/*
	 * Applique uniquement la dernière pose acceptée.
	 */
	private void applyResolvedMotion(Entity entity, MotionResult result) {

		entity.applyPoseFromModel(result.acceptedCenter, result.acceptedOrientation, true);

		/*
		 * Le mouvement bloqué est arrêté avant la notification.
		 *
		 * Ainsi, le Bot pourra choisir une nouvelle direction pendant collision(), et
		 * sa nouvelle vitesse ne sera pas écrasée ensuite.
		 */
		if (result.blocked()) {
			entity.setLinearSpeed(this.isu.new Vector(0.0, 0.0));

			entity.setAngularSpeed(0.0);
			

			if (logger.isLoggable(Level.FINER)) {
				logger.log(Level.FINER,
						"Motion blocked: entity={0}, blockers={1}",
						new Object[] {
								entity.name(),
								Integer.valueOf(result.blockers.size())
						});
		}
		}

		notifyContacts(entity, result.contacts);
	}

	/*
	 * Récupère les entités présentes dans les cellules traversées.
	 *
	 * On échantillonne le déplacement au lieu de faire un simple min/max, ce qui
	 * est plus fiable avec un monde torique.
	 */
	public List<Entity> collisionsAlongMotion(Entity entity, ISU.Coord from, ISU.Vector movement) {
		assert from.isu() == this.isu;
		assert movement.isu() == this.isu;

		Set<Entity> result = new LinkedHashSet<Entity>();

		double maxStep = this.isu.cmPerCell() / 4.0;

		int nsteps = Math.max(1, (int) Math.ceil(movement.norm() / maxStep));

		for (int i = 1; i <= nsteps; i++) {
			double ratio = (double) i / nsteps;

			ISU.Coord testedCenter = this.isu.new Coord(from.x() + movement.x() * ratio,
					from.y() + movement.y() * ratio);

			Grid.Position position = testedCenter.toGridPosition();

			Grid.Cell cell = this.grid.cellAt(position);

			for (Entity other : cell.entities()) {

				if (other != entity && other.alive()) {
					result.add(other);
				}
			}
		}

		return new ArrayList<Entity>(result);
	}

	// Pour les tests
	public List<Entity> potentialCollisionsByMotionHitbox(Entity entity, ISU.Coord from, ISU.Coord to) {

		ISU.Vector movement = from.mkVectorToward(to);

		return potentialCollisionsByMotionHitbox(entity, from, movement, entity.orientation(), entity.orientation());
	}

	/*
	 * Utilise une hitbox qui englobe tout le mouvement.
	 *
	 * C'est un filtre rapide : les entités retournées ne sont pas forcément en
	 * collision, mais ce sont de bons candidats à vérifier précisément.
	 */
	private List<Entity> potentialCollisionsByMotionHitbox(Entity entity, ISU.Coord from, ISU.Vector movement,
			int fromOrientation, int toOrientation) {
		assert from.isu() == this.isu;
		assert movement.isu() == this.isu;

		int nsteps = motionStepCount(movement, fromOrientation, toOrientation);

		Hitbox motionHitbox = entity.motionHitboxBetween(from, movement, fromOrientation, toOrientation, nsteps);

		List<Entity> result = new ArrayList<Entity>();

		for (Entity other : this.entities) {
			if (other == entity || !other.alive()) {
				continue;
			}

			if (motionHitbox.intersects(other.bounding().hitbox())) {
				result.add(other);
			}
		}

		return result;
	}

	/*
	 * Placement interne sans notification vers le Stunt.
	 *
	 * Utilisé pour corriger exactement la position à la fin d'une action sans
	 * déclencher une notification récursive.
	 */
	void placeSilently(Entity entity, ISU.Coord center) {
		assert center.isu() == this.isu;

		assertOwned(entity);

		entity.applyPoseFromModel(center, entity.orientation(), false);
	}

	/*
	 * Résultat de la résolution d'un mouvement.
	 *
	 * acceptedCenter / acceptedOrientation : dernière pose autorisée.
	 *
	 * contacts : toutes les entités réellement touchées pendant le trajet.
	 *
	 * blockers : entités qui ont provoqué l'arrêt du mouvement.
	 */
	private static final class MotionResult {

		final ISU.Coord acceptedCenter;
		final int acceptedOrientation;

		final List<Entity> contacts;
		final List<Entity> blockers;

		MotionResult(ISU.Coord acceptedCenter, int acceptedOrientation, List<Entity> contacts, List<Entity> blockers) {

			this.acceptedCenter = acceptedCenter;
			this.acceptedOrientation = acceptedOrientation;
			this.contacts = contacts;
			this.blockers = blockers;
		}

		boolean blocked() {
			return !this.blockers.isEmpty();
		}
	}

	private int normalizeOrientation(int angleDegree) {
		return ((angleDegree % 360) + 360) % 360;
	}

	/*
	 * Recherche la première collision bloquante du mouvement.
	 *
	 * La vraie entité n'est jamais déplacée pendant cette recherche. On construit
	 * uniquement des Bounding temporaires avec boundingAt().
	 */
	private MotionResult resolveMotion(Entity entity, ISU.Coord from, ISU.Vector movement, int fromOrientation,
			int toOrientation, List<Entity> candidates) {

		assert from.isu() == this.isu;
		assert movement.isu() == this.isu;

		int nsteps = motionStepCount(movement, fromOrientation, toOrientation);

		int angleDelta = shortestAngleDelta(fromOrientation, toOrientation);

		ISU.Coord acceptedCenter = from.mkCopy();

		int acceptedOrientation = normalizeOrientation(fromOrientation);

		Set<Entity> allContacts = new LinkedHashSet<Entity>();

		for (int i = 1; i <= nsteps; i++) {
			double ratio = (double) i / nsteps;

			ISU.Coord testedCenter = this.isu.new Coord(from.x() + movement.x() * ratio,
					from.y() + movement.y() * ratio);

			int testedOrientation = normalizeOrientation(fromOrientation + (int) Math.round(angleDelta * ratio));

			Bounding testedBounding = entity.boundingAt(testedCenter, testedOrientation);

			List<Entity> currentBlockers = new ArrayList<Entity>();

			for (Entity other : candidates) {
				if (other == entity || !other.alive()) {
					continue;
				}

				if (!testedBounding.intersects(other.bounding())) {
					continue;
				}

				allContacts.add(other);

				if (isBlocking(entity, other)) {
					currentBlockers.add(other);
				}
			}

			if (!currentBlockers.isEmpty()) {
				return new MotionResult(acceptedCenter, acceptedOrientation, new ArrayList<Entity>(allContacts),
						currentBlockers);
			}

			acceptedCenter = testedCenter;

			acceptedOrientation = testedOrientation;
		}

		return new MotionResult(acceptedCenter, acceptedOrientation, new ArrayList<Entity>(allContacts),
				new ArrayList<Entity>());
	}

	/*
	 * Une collision est bloquante si l'une des deux entités déclare bloquer
	 * l'autre.
	 */
	boolean isBlocking(Entity moving, Entity other) {
		assert moving != other;

		return moving.blocks(other) || other.blocks(moving);
	}

	private int motionStepCount(ISU.Vector movement, int fromOrientation, int toOrientation) {

		double maxLinearStep = this.isu.cmPerCell() / 4.0;

		int linearSteps = (int) Math.ceil(movement.norm() / maxLinearStep);

		int angleDelta = Math.abs(shortestAngleDelta(fromOrientation, toOrientation));

		int angularSteps = (int) Math.ceil(angleDelta / 5.0);

		return Math.max(1, Math.max(linearSteps, angularSteps));
	}

	private int shortestAngleDelta(int fromOrientation, int toOrientation) {
		return ((toOrientation - fromOrientation + 540) % 360) - 180;
	}

	// VIEWPORT ///////////////////////////////////////////////////////////////

	public List<Entity> visibleEntities() {

		return visibleEntities(this.viewPort);
	}

	/*
	 * Retourne les entités qui se trouvent dans les cellules couvertes par le
	 * viewport.
	 *
	 * On utilise un Set pour éviter les doublons, car une entité peut occuper
	 * plusieurs cellules.
	 */
	public List<Entity> visibleEntities(ViewPort vp) {

		Set<Entity> result = new LinkedHashSet<Entity>();

		List<Integer> xs = coveredCellIndices(vp.x(), vp.width(), this.isu.width(), this.grid.width());

		List<Integer> ys = coveredCellIndices(vp.y(), vp.height(), this.isu.height(), this.grid.height());

		for (int x : xs) {
			for (int y : ys) {
				Grid.Cell cell = this.grid.cellAt(this.grid.new Position(x, y));

				for (Entity entity : cell.entities()) {
					result.add(entity);
				}
			}
		}

		return new ArrayList<Entity>(result);
	}

	/*
	 * Calcule les indices des cellules couvertes sur un axe.
	 *
	 * Si l'intervalle dépasse la fin du monde, on le coupe en deux parties. C'est
	 * ce qui permet de gérer un viewport sur un tore.
	 */
	private List<Integer> coveredCellIndices(double start, double length, double mapLength, int cellCount) {
		assert length > 0.0;
		assert length <= mapLength;

		List<Integer> result = new ArrayList<Integer>();

		double end = start + length;

		if (end <= mapLength) {
			addCellRange(result, start, end, cellCount);
		} else {
			addCellRange(result, start, mapLength, cellCount);
			addCellRange(result, 0.0, end - mapLength, cellCount);
		}

		return result;
	}

	private void addCellRange(List<Integer> result, double start, double end, int cellCount) {
		if (end <= start) {
			return;
		}

		double cellSize = this.isu.cmPerCell();
		double eps = 0.000001;

		int first = (int) Math.floor(start / cellSize);
		int last = (int) Math.floor((end - eps) / cellSize);

		if (first < 0) {
			first = 0;
		}

		if (last >= cellCount) {
			last = cellCount - 1;
		}

		for (int i = first; i <= last; i++) {
			if (!result.contains(i)) {
				result.add(i);
			}
		}
	}

	// UTILS //////////////////////////////////////////////////////////////////

	private void addAllWithoutDuplicates(List<Entity> destination, List<Entity> source) {

		for (Entity entity : source) {
			if (!destination.contains(entity)) {
				destination.add(entity);
			}
		}
	}

	private void cleanupDeadEntities() {
		List<Entity> deadEntities = new ArrayList<>();

		for (Entity entity : entities) {
			if (!entity.alive()) {
				deadEntities.add(entity);
			}
		}
		if (!deadEntities.isEmpty()) {
			logger.log(Level.FINE,
					"Cleaning dead entities: count={0}",
					Integer.valueOf(deadEntities.size()));
		}

		for (Entity entity : deadEntities) {
			remove(entity);
		}
	}
}