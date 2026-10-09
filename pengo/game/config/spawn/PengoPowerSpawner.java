package game.config.spawn;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.model.Entity;
import engine.model.Grid;
import engine.model.Model;
import engine.view.View;
import game.BuildMap;
import game.config.PengoRunState;
import game.gal.CurrentPlayer;
import game.model.PengoGameState;
import game.model.entity.Power;
import game.model.entity.PowerType;
import game.model.player.Friend;
import game.view.PowerAvatar;
import oop.tasks.Runnable;
import oop.tasks.Runtime;

public final class PengoPowerSpawner {

	private static final Logger logger = Logger.getLogger(PengoPowerSpawner.class.getName());

	private static final int POWER_MIN_DELAY_MS = 12000;
	private static final int POWER_MAX_DELAY_MS = 18000;
	private static final int POWER_MAX_SPAWN_PER_TYPE = 2;

	private static final Random RANDOM = new Random();

	private static final Map<PowerType, Integer> spawnedPowers = new EnumMap<PowerType, Integer>(PowerType.class);

	private PengoPowerSpawner() {
	}

	public static void resetPowerSpawnCounters() {
		spawnedPowers.clear();
		for (PowerType type : PowerType.values()) {
			spawnedPowers.put(type, Integer.valueOf(0));
		}
	}

	// comme les bots installe un premier
	public static void installRandomPowerSpawner(final Model model, final View view, final PengoGameState gameState,
			final BuildMap builder, final PengoRunState runState, CurrentPlayer current) {

		Runtime.post(new Runnable() {
			private long nextSpawnMs = runState.nowMs() + randomPowerDelay();

			@Override
			public void run() {
				if (runState.stopped()) {
					return;
				}
				// si jeu en pause on attend
				if (runState.paused()) {
					Runtime.post(this, 100);
					return;
				}
				gameState.update();
				if (gameState.finished()) {
					return;
				}
				long now = runState.nowMs();

				// on tente de spawn
				if (now >= this.nextSpawnMs) {
					spawnOneRandomPowerIfPossible(model, view, builder, current);
					this.nextSpawnMs = runState.nowMs() + randomPowerDelay();
				}
				// relance apres delay
				Runtime.post(this, delayUntil(this.nextSpawnMs, runState));
			}
		}, 100);
	}

	// calcule le delai pour recheck
	private static int delayUntil(long targetGameTimeMs, PengoRunState runState) {
		long remaining = targetGameTimeMs - runState.nowMs();
		if (remaining < 1L) {
			return 1;
		}
		if (remaining > 1000L) {
			return 1000;
		}
		return (int) remaining;
	}

	// choisit un delai aleatoire pour le prochain power
	private static int randomPowerDelay() {
		return POWER_MIN_DELAY_MS + RANDOM.nextInt(POWER_MAX_DELAY_MS - POWER_MIN_DELAY_MS + 1);
	}

	// fait spawn un power aleatoire
	private static void spawnOneRandomPowerIfPossible(Model model, View view, BuildMap builder, CurrentPlayer current) {
		List<PowerType> possibleTypes = possiblePowerTypes(model, current);

		if (possibleTypes.isEmpty()) {
			return;
		}

		Grid.Position position = randomFreePosition(model, builder);

		if (position == null) {
			logger.fine("No free position for random power.");
			return;
		}

		PowerType type = possibleTypes.get(RANDOM.nextInt(possibleTypes.size()));

		Power power = new Power(model, "random-power-" + type + "-" + System.currentTimeMillis(), type);

		power.setAvatar(new PowerAvatar(view, power));

		model.add(power, position);

		int currentCount = spawnedPowers.get(type).intValue();
		spawnedPowers.put(type, Integer.valueOf(currentCount + 1));

		logger.log(Level.INFO, "Random power spawned: {0} at ({1}, {2})",
				new Object[] { type, Integer.valueOf(position.x()), Integer.valueOf(position.y()) });
	}

	// les types de powers quon peut faire apparaitre encore
	private static List<PowerType> possiblePowerTypes(Model model, CurrentPlayer currentPlayer) {
		List<PowerType> result = new ArrayList<PowerType>();

		for (PowerType type : PowerType.values()) {
			int count = spawnedPowers.get(type).intValue();

			if (count >= POWER_MAX_SPAWN_PER_TYPE) {
				continue;
			}

			if (hasActivePower(model, type)) {
				continue;
			}

			if (type == PowerType.FRIEND_ACTIVATOR) {
				if (!hasLivingFriend(model)) {
					continue;
				}
				if (currentPlayer.currentPlayer() instanceof Friend) {
					continue;
				}
				if (count >= 1) {
					continue;
				}
			}
			result.add(type);
		}

		return result;
	}

	// verifie si un power du meme type est deja present sur la map
	private static boolean hasActivePower(Model model, PowerType type) {
		for (Entity entity : model.entities()) {
			if (entity instanceof Power && entity.alive()) {
				Power power = (Power) entity;
				if (power.getType() == type) {
					return true;
				}
			}
		}
		return false;
	}

	// cherche une position libre pour un power
	public static Grid.Position randomFreePosition(Model model, BuildMap builder) {
		List<Grid.Position> freePositions = new ArrayList<Grid.Position>();
		Grid grid = model.grid();
		for (int y = 0; y < grid.height(); y++) {
			for (int x = 0; x < grid.width(); x++) {
				Grid.Position position = grid.new Position(x, y);
				if (isFreeForPower(model, builder, position)) {
					freePositions.add(position);
				}
			}
		}
		if (freePositions.isEmpty()) {
			return null;
		}
		return freePositions.get(RANDOM.nextInt(freePositions.size()));
	}

	// verifie si une case peut recevoir un power
	private static boolean isFreeForPower(Model model, BuildMap builder, Grid.Position position) {
		char[][] map = builder.getMap();

		int x = position.x();
		int y = position.y();

		if (y < 0 || y >= map.length || x < 0 || x >= map[y].length) {
			return false;
		}

		if (map[y][x] != '.') {
			return false;
		}

		for (Entity entity : model.entities()) {
			if (entity == null) {
				continue;
			}

			if (!entity.alive() || !entity.installed()) {
				continue;
			}

			Grid.Position entityPosition = entity.position();

			if (entityPosition == null) {
				continue;
			}

			if (entityPosition.x() == x && entityPosition.y() == y) {
				return false;
			}
		}

		return true;
	}

	// verifie s'il y a un Friend vivant
	private static boolean hasLivingFriend(Model model) {
		for (Entity entity : model.entities()) {
			if (entity instanceof Friend && entity.alive()) {
				return true;
			}
		}

		return false;
	}
}