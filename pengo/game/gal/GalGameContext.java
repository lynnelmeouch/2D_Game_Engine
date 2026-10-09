package game.gal;

import java.util.List;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.Orientation;
import game.model.entity.Enemies;
import game.model.entity.Glacon;
import game.model.entity.Mur;
import game.model.entity.Power;
import game.model.entity.Spawner;
import game.model.player.Friend;
import game.model.player.Pengo;

public final class GalGameContext {

	private final Entity self;
	private final BasicStunt stunt;
	private final CurrentPlayer currentPlayer;
	private final Grid grid;
	private final boolean torusOnXaxis;
	private final boolean torusOnYaxis;
	private final int gridWidth;
	private final int gridHeight;
	private final java.lang.Runnable eggAction;

	public GalGameContext(Entity self, BasicStunt stunt, CurrentPlayer currentPlayer) {
	    this(self, stunt, currentPlayer, null);
	}

	public GalGameContext(Entity self, BasicStunt stunt, CurrentPlayer currentPlayer, java.lang.Runnable eggAction) {
	    assert stunt.entity() == self;

	    this.self = self;
	    this.stunt = stunt;
	    this.currentPlayer = currentPlayer;
	    this.grid = self.model().grid();
	    this.torusOnXaxis = self.model().game().isTorusOnXaxis();
	    this.torusOnYaxis = self.model().game().isTorusOnYaxis();
	    this.gridWidth = this.grid.width();
	    this.gridHeight = this.grid.height();
	    this.eggAction = eggAction;
	}

	public void waitAction() {
		// Wait / Rest : aucune action moteur.
	}

	public void moveAction(String direction) {
		stunt.walk(resolveDirection(direction));
	}

	public void turnAction(String direction) {
		self.setOrientation(resolveDirection(direction));
	}
	
	public void eggAction() {
	    if (this.eggAction != null) {
	        this.eggAction.run();
	    }
	}

	public boolean cellCondition(String directionName, String categoryName) {
		int direction = resolveDirection(directionName);
		Grid.Position target = Orientation.neighbor(grid, self.position(), direction);

		if (target == null) {
			return false;
		}

		Grid.Cell cell = grid.cellAt(target);
		String category = normalizeName(categoryName);
		List<Entity> entities = cell.entities();

		// case vide doit etre accessible
		if (isVoidCategory(category)) {
			return free(cell);
		}

		// contient le joueur controlé
		if (isPlayerCategory(category)) {
			Entity player = currentPlayer.currentPlayer();
			return player != null && player.alive() && cell.contains(player);
		}

		// contoent ennemi vivant
		if (isEnemy(category)) {
			return containsAlive(entities, Enemies.class);
		}

		// contient soit pengo soit friend vivant
		if (isTeam(category)) {
			return containsAlive(entities, Friend.class) || containsAlive(entities, Pengo.class);
		}

		// un obstacle
		if (isObstacle(category)) {
			return containsAlive(entities, Mur.class) || containsAlive(entities, Glacon.class)
					|| containsAlive(entities, Spawner.class);
		}

		// pushable item
		if (isPushable(category)) {
			return containsAlive(entities, Glacon.class) || containsAlive(entities, Spawner.class);
		}

		// contient power
		if (isPower(category)) {
			return containsAlive(entities, Power.class);
		}

		return false;
	}

	// pour Closest (poursuivre le pengo when angry)
	public boolean closestCondition(String targetName, String directionName) {
		if (!isPlayerCategory(normalizeName(targetName))) {
			return false;
		}
		Entity target = findPengo();
		if (target == null || !target.alive()) {
			return false;
		}
		int wantedDirection = resolveDirection(directionName);

		Grid.Position from = self.position();
		Grid.Position to = target.position();

		int dx = signedDelta(from.x(), to.x(), gridWidth, torusOnXaxis);
		int dy = signedDelta(from.y(), to.y(), gridHeight, torusOnYaxis);

		int closestDirection;
		if (Math.abs(dx) >= Math.abs(dy)) {
			closestDirection = dx >= 0 ? 0 : 180;
		} else {
			closestDirection = dy >= 0 ? 90 : 270;
		}
		return closestDirection == wantedDirection;
	}

	// verifie si lentite est face acette direction
	public boolean faces(String directionName) {
		int orientation = Orientation.normalize(self.orientation());
		int direction = resolveDirection(directionName);
		return orientation == direction;
	}

	// verife si free pour self
	private boolean free(Grid.Cell cell) {
		if (this.self instanceof Enemies) {
			Grid.Position center = this.grid.new Position(this.gridWidth / 2, this.gridHeight / 2);

			if (cell.position().equals(center)) {
				return false;
			}
		}
		List<Entity> entities = cell.entities();

		for (Entity e : entities) {
			if (e != self && e.alive()) {
				if (e instanceof Enemies) {
					return false;
				}

				if (self.blocks(e) || e.blocks(self)) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean gotCondition(String categoryName) {
		String category = normalizeName(categoryName);
		// Got(A) il reste juste un enemy alive
		if (isEnemy(category)) {
			return nEnemyLeft() == 1;
		}
		// Got(D) Pengo has only one life left
		if (isDanger(category)) {
			Pengo pengo = findPengo();
			return pengo != null && pengo.nLives() <= 1;
		}
		return false;

	}

	// retourne le nbr d enemy vivant
	private int nEnemyLeft() {
		int count = 0;
		List<Entity> entities = self.model().entities();
		for (Entity entity : entities) {
			if (entity instanceof Enemies e && e.alive()) {
				count++;
			}
		}
		return count;
	}

	private Pengo findPengo() {
		List<Entity> entities = self.model().entities();
		for (Entity entity : entities) {
			if (entity instanceof Pengo p && p.alive()) {
				return p;
			}
		}
		return null;
	}

	private int resolveDirection(String directionName) {
		String direction = normalizeName(directionName);
		int orientation = Orientation.normalize(self.orientation());

		// forward
		if (direction.length() == 0 || direction.equals("F") || direction.equals("FORWARD") || direction.equals("FRONT")
				|| direction.equals("H") || direction.equals("HERE")) {
			return orientation;
		}

		// backwards
		if (direction.equals("B") || direction.equals("BACK") || direction.equals("BACKWARD")) {
			return Orientation.backOf(orientation);
		}

		// left
		if (direction.equals("L") || direction.equals("LEFT")) {
			return Orientation.leftOf(orientation);
		}

		// right
		if (direction.equals("R") || direction.equals("RIGHT")) {
			return Orientation.rightOf(orientation);
		}

		if (direction.equals("N") || direction.equals("NORTH")) {
			return 270;
		}

		if (direction.equals("S") || direction.equals("SOUTH")) {
			return 90;
		}

		if (direction.equals("E") || direction.equals("EAST")) {
			return 0;
		}

		if (direction.equals("W") || direction.equals("WEST") || direction.equals("O")) {
			return 180;
		}

		throw new UnsupportedOperationException("Direction GAL non supportee : " + directionName);
	}

	// distance signée entre 2 coord si torique le plus court
	private int signedDelta(int from, int to, int size, boolean torus) {
		int delta = to - from;

		if (torus) {
			if (delta > size / 2) {
				delta -= size;
			} else if (delta < -size / 2) {
				delta += size;
			}
		}

		return delta;
	}

	// HELPERS

	private static String normalizeName(String name) {
		return name == null ? "" : name.trim().toUpperCase();
	}

	private static boolean isVoidCategory(String category) {
		return category.equals("V") || category.equals("VOID");
	}

	private static boolean isPlayerCategory(String category) {
		return category.equals("@") || category.equals("PLAYERT") || category.equals("PLAYER");
	}

	private static boolean isEnemy(String category) {
		return category.equals("A") || category.equals("ADVERSARY");
	}

	private static boolean isTeam(String category) {
		return category.equals("T") || category.equals("TEAM");
	}

	private static boolean isObstacle(String category) {
		return category.equals("O") || category.equals("OBSTACLE");
	}

	private static boolean isPushable(String category) {
		return category.equals("M") || category.equals("MOVEABLE");
	}

	private static boolean isPower(String category) {
		return category.equals("P") || category.equals("PICKABLE") || category.equals("POWER");
	}

	private static boolean isDanger(String category) {
		return category.equals("D") || category.equals("DANGER");
	}

	// estce que la liste contient une entity de ce typevivant?
	private static boolean containsAlive(List<Entity> entities, Class<?> type) {
		for (Entity entity : entities) {
			if (type.isInstance(entity) && entity.alive()) {
				return true;
			}
		}
		return false;
	}
}