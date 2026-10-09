package game.model.entity;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Rect;
import game.config.PengoRunState;
import game.model.player.Pengo;
import game.model.player.Player;
import game.model.slide.Glissade;

import java.util.List;

import oop.tasks.Runnable;
import oop.tasks.Runtime;

/**
 * Ennemi mobile du jeu Pengo.
 */
public final class Enemies extends Entity {
	public static final long CRUSH_DISPLAY_MS = 550L;

	private boolean frozen;
	private String galState;
	private boolean crushed;
	private boolean crushedHorizontal;
	private double crushedDisplayWidth_cm;
	private double crushedDisplayHeight_cm;
	private Entity crushPusher;
	private Entity crushObstacle;
	private int crushDirectionDegree;
	private PengoRunState runState;
	private String lastVisibleStateName = "Normal";

	public Enemies(Model model, String name) {
		super(model, name);

		this.frozen = false;
		this.galState = "normal";
		this.crushed = false;
		this.crushedHorizontal = true;
		this.crushedDisplayWidth_cm = model.isu().cmPerCell();
		this.crushedDisplayHeight_cm = model.isu().cmPerCell();
		this.crushPusher = null;
		this.crushObstacle = null;
		this.crushDirectionDegree = 0;
		this.runState = null;

		double size = model.isu().cmPerCell() * 0.78;
		setSize(model.isu().new Dimension(size, size));
		setStep(model.isu().new Dimension(model.isu().cmPerCell(), model.isu().cmPerCell()));

		setZOrder(3);
		setSOrder(2);

		updateBounding();
	}

	public void setRunState(PengoRunState runState) {
		this.runState = runState;
	}

	private boolean pausedByRunState() {
		return this.runState != null && this.runState.paused();
	}

	private boolean stoppedByRunState() {
		return this.runState != null && this.runState.stopped();
	}

	public String galState() {
		return this.galState;
	}

	public void setGalState(String galState) {
		if (galState == null || galState.trim().length() == 0) {
			this.galState = "normal";
			return;
		}

		this.galState = galState.trim();
	}

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();

		double headRadius = Math.min(size.x(), size.y()) * 0.30;
		ISU.Vector headOffset = center.isu().new Vector(0.0, -size.y() * 0.18);
		headOffset.turn(orientationDegree);
		result.add(new Circle(center.mkTranslated(headOffset), headRadius));

		ISU.Dimension bodySize = center.isu().new Dimension(size.x() * 0.80, size.y() * 0.55);
		ISU.Vector bodyOffset = center.isu().new Vector(0.0, size.y() * 0.18);
		bodyOffset.turn(orientationDegree);
		result.add(new Rect(center.mkTranslated(bodyOffset), bodySize, orientationDegree));

		return result;
	}

	public boolean isFrozen() {
		return this.frozen;
	}

	public void freeze() {
		/*
		 * On laisse finir le pas en cours. Le Bot n'en démarrera pas de nouveau tant
		 * que frozen reste vrai. Cela conserve la cohérence interne de BasicStunt.
		 */
		this.frozen = true;
	}

	public void unfreeze() {
		this.frozen = false;
	}

	public boolean isAlive() {
		return alive();
	}

	/**
	 * Un ennemi mort reste vivant quelques instants pour que son avatar puisse
	 * afficher l'image ecrasee_horizontal.png ou ecrasee_vertical.png.
	 */
	public void kill() {
	    if (!alive()) {
	        return;
	    }

	    this.crushed = false;
	    this.crushPusher = null;
	    this.crushObstacle = null;
	    this.frozen = false;

	    death();
	}

	public void crush(int directionDegree) {
		crushAt(directionDegree, center());
	}

	/**
	 * Lance un ecrasement dynamique entre un bloc pousseur et un obstacle final.
	 * L'avatar calcule ensuite l'image ecrasee avec la distance bord-a-bord en ISU.
	 */
	public void crushBetween(Entity pusher, Entity obstacle, int directionDegree) {
		crushBetweenAt(pusher, obstacle, directionDegree, center());
	}

	public void crushBetweenAt(Entity pusher, Entity obstacle, int directionDegree, ISU.Coord fallbackCenter) {
		this.crushPusher = pusher;
		this.crushObstacle = obstacle;
		this.crushDirectionDegree = normalizeCardinal(directionDegree);
		crushAt(directionDegree, fallbackCenter);
	}

	/**
	 * Place l'ennemi a l'endroit exact ou il doit etre affiche ecrase, puis
	 * lance la mort differee.
	 */
	public void crushAt(int directionDegree, ISU.Coord crushCenter) {
		if (!alive() || this.crushed) {
			return;
		}

		int direction = normalizeCardinal(directionDegree);
		this.crushDirectionDegree = direction;

		if (crushCenter != null && installed()) {
			model().place(this, crushCenter);
		}

		this.crushedHorizontal = direction == 0 || direction == 180;
		this.crushedDisplayWidth_cm = model().isu().cmPerCell() * 1.08;
		this.crushedDisplayHeight_cm = model().isu().cmPerCell() * 1.08;
		this.crushed = true;
		this.frozen = true;

		removeStunt();
		setOrientation(direction);
		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);
		setSOrder(10);

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (stoppedByRunState()) {
					return;
				}

				if (pausedByRunState()) {
					Runtime.post(this, 50);
					return;
				}

				if (isCrushed() && alive()) {
					finishCrushDeath();
				}
			}
		}, (int) CRUSH_DISPLAY_MS);
	}

	public static final class CrushGeometry {
		private final ISU.Coord center;
		private final ISU.Dimension size;

		private CrushGeometry(ISU.Coord center, ISU.Dimension size) {
			this.center = center;
			this.size = size;
		}

		public ISU.Coord center() {
			return this.center.mkCopy();
		}

		public ISU.Dimension size() {
			return this.size.copy();
		}

		public double centerX() {
			return this.center.x();
		}

		public double centerY() {
			return this.center.y();
		}

		public double width() {
			return this.size.x();
		}

		public double height() {
			return this.size.y();
		}
	}

	/**
	 * Retourne la zone exacte occupée par l'image écrasée, en ISU.
	 *
	 * Le calcul est bord-à-bord :
	 * centre pousseur -> centre obstacle - demi pousseur - demi obstacle.
	 * Cela évite une boîte debug qui reste à la taille normale du poisson.
	 */
	public CrushGeometry currentCrushGeometry() {
		if (!this.crushed) {
			return null;
		}

		Entity pusher = this.crushPusher;
		Entity obstacle = this.crushObstacle;

		if (pusher == null || obstacle == null || !pusher.alive() || !obstacle.alive()) {
			return fallbackCrushGeometry();
		}

		int direction = normalizeCardinal(this.crushDirectionDegree);
		double cell = model().isu().cmPerCell();
		double minThickness = cell * 0.025;
		double crossSize = cell * 1.03;

		ISU.Vector towardObstacle = pusher.center().mkVectorToward(obstacle.center());
		double centerDistance = forwardProjection(towardObstacle, direction);

		if (centerDistance <= 0.0) {
			return fallbackCrushGeometry();
		}

		double pusherHalf = solidHalfSizeOnAxis(pusher, direction, cell);
		double obstacleHalf = solidHalfSizeOnAxis(obstacle, direction, cell);
		double realGap = centerDistance - pusherHalf - obstacleHalf;
		double visibleGap = Math.max(minThickness, realGap);

		double distanceFromPusherCenter = pusherHalf + visibleGap / 2.0;
		ISU.Coord center = pusher.center().mkTranslated(offset(direction, distanceFromPusherCenter));

		ISU.Dimension size;

		if (direction == 0 || direction == 180) {
			size = model().isu().new Dimension(visibleGap, crossSize);
		} else {
			size = model().isu().new Dimension(crossSize, visibleGap);
		}

		return new CrushGeometry(center, size);
	}

	@Override
	public Bounding bounding() {
		if (this.crushed) {
			Bounding crushBounding = currentCrushBounding();

			if (crushBounding != null) {
				return crushBounding;
			}
		}

		return super.bounding();
	}

	private Bounding currentCrushBounding() {
		CrushGeometry geometry = currentCrushGeometry();

		if (geometry == null) {
			return null;
		}

		Bounding result = new Bounding();
		result.add(new Rect(geometry.center(), geometry.size(), 0));
		return result;
	}

	private CrushGeometry fallbackCrushGeometry() {
		ISU.Dimension size = model().isu().new Dimension(this.crushedDisplayWidth_cm, this.crushedDisplayHeight_cm);
		return new CrushGeometry(center(), size);
	}

	private double solidHalfSizeOnAxis(Entity entity, int direction, double cell) {
		if (entity instanceof Glacon || entity instanceof Mur || entity instanceof Spawner) {
			return cell / 2.0;
		}

		if (direction == 0 || direction == 180) {
			return entity.size().x() / 2.0;
		}

		return entity.size().y() / 2.0;
	}

	private ISU.Vector offset(int direction, double distance_cm) {
		switch (direction) {
		case 0:
			return model().isu().new Vector(distance_cm, 0.0);

		case 90:
			return model().isu().new Vector(0.0, distance_cm);

		case 180:
			return model().isu().new Vector(-distance_cm, 0.0);

		case 270:
			return model().isu().new Vector(0.0, -distance_cm);

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	private static double forwardProjection(ISU.Vector vector, int direction) {
		switch (direction) {
		case 0:
			return vector.x();

		case 90:
			return vector.y();

		case 180:
			return -vector.x();

		case 270:
			return -vector.y();

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	public boolean isCrushed() {
		return this.crushed;
	}

	public boolean crushedHorizontal() {
		return this.crushedHorizontal;
	}

	public double crushedDisplayWidthCm() {
		return this.crushedDisplayWidth_cm;
	}

	public double crushedDisplayHeightCm() {
		return this.crushedDisplayHeight_cm;
	}

	public Entity crushPusher() {
		return this.crushPusher;
	}

	public Entity crushObstacle() {
		return this.crushObstacle;
	}

	public int crushDirection() {
		return this.crushDirectionDegree;
	}

	public void finishCrushDeath() {
		if (alive()) {
			death();
		}
	}

	private static int normalizeCardinal(int degree) {
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

	@Override
	public boolean blocks(Entity other) {
		/*
		 * Quand un poisson est transporté par un glaçon/spawner, il est frozen.
		 * Le train doit devenir solide pour les autres poissons, sinon un autre
		 * poisson peut traverser le poisson porté et donner l'impression de traverser
		 * le glaçon.
		 *
		 * On ne bloque jamais le bloc pousseur lui-même, sinon la glissade s'arrête
		 * sur son propre poisson transporté.
		 */
		if (this.frozen && !this.crushed) {
			if (other instanceof Glissade || other instanceof Power) {
				return false;
			}

			if (other instanceof Enemies) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void collision(Entity other) {
		if (this.crushed) {
			return;
		}

		if (other instanceof Player && other.alive()) {
			Player player = (Player) other;
			if (player instanceof Pengo) {
				Pengo pengo = (Pengo) player;

				if (pengo.hasStar()) {
					kill();
					return;
				}
			}
			player.hitByEnemy();
		}

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		if (this.crushed) {
			return;
		}

		Player touchedPlayer = null;

		for (Entity entity : entities) {
			if (entity instanceof Player && entity.alive()) {
				touchedPlayer = (Player) entity;
				break;
			}
		}

		if (touchedPlayer != null) {
			if (touchedPlayer instanceof Pengo) {
				Pengo pengo = (Pengo) touchedPlayer;

				if (pengo.hasStar()) {
					kill();
					return;
				}
			}

			touchedPlayer.hitByEnemy();
		}

		super.collision(entities);
	}

	@Override
	public String debugLabel() {
		if (this.frozen) {
			return "Enemy FROZEN " + visibleStateName(this.galState);
		}

		if (isMoving()) {
			return "Enemy " + visibleStateName(this.galState) + " WALK";
		}

		return "Enemy " +visibleStateName(this.galState);
	}
	
	private String visibleStateName(String stateName) {
		if (stateName == null || stateName.trim().length() == 0) {
			return this.lastVisibleStateName;
		}
		String cleanStateName = stateName.trim();
		if ("Brain".equalsIgnoreCase(cleanStateName)) {
			return this.lastVisibleStateName;
		}
		this.lastVisibleStateName = cleanStateName;
		return cleanStateName;
	}
	
	
	
	public void releaseAfterBeingCarried() {
		unfreeze();

		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);

		if (stunt() != null) {
			stunt().collision(new Mur(model(), "__release_enemy_blocker__"));
		}
	}
}