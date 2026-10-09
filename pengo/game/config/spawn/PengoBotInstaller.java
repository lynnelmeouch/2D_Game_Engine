package game.config.spawn;

import java.util.ArrayList;
import java.util.List;

import engine.brain.Bot;
import engine.brain.Brain;
import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.Model;
import engine.view.DebugOptions;
import engine.view.View;
import gal.ast.Automaton;
import game.BuildMap;
import game.config.PengoConfig;
import game.config.PengoRunState;
import game.controller.Switch;
import game.gal.CurrentPlayer;
import game.gal.GalBot;
import game.model.PengoGameState;
import game.model.entity.Enemies;
import game.model.entity.Glacon;
import game.model.entity.Mur;
import game.model.entity.Spawner;
import game.model.player.Friend;
import game.model.player.Pengo;
import game.view.EnemiesAvatar;
import oop.tasks.Runnable;
import oop.tasks.Runtime;

public final class PengoBotInstaller {

	private static final double FRIEND_SPEED = 4.0;
	private static final double ENEMY_SPEED = 3.0;

	private static int spawnedEnemyCounter = 0;
	private static int totalSpawnedEnemies = 0;

	private PengoBotInstaller() {
	}

	public static void resetEnemyCounters(int initialEnemyCount) {
		spawnedEnemyCounter = 0;
		totalSpawnedEnemies = initialEnemyCount;
	}

	// GAL COMPORTEMENT

	// met le gal du bot non controlé
	public static void installFriendGalBot(Brain brain, Model model, Friend friend, Automaton friendAutomaton,
			CurrentPlayer currentPlayer, Switch controlSwitcher) {
		if (friend == null) {
			return;
		}
		if (friend == controlSwitcher.controlledEntity()) {
			return;
		}
		BasicStunt friendStunt = new BasicStunt(model, friend, FRIEND_SPEED);
		friend.setStunt(friendStunt);
		GalBot friendBot = new GalBot(brain, friendStunt, friend, friendAutomaton, currentPlayer);
		brain.add(friendBot);
	}

	// met les bots gal des ennemies
	public static void installEnemyGalBots(Brain brain, Model model, BuildMap builder, Automaton enemyAutomaton,
			CurrentPlayer currentPlayer) {
		for (Enemies enemy : builder.enemies()) {
			BasicStunt enemyStunt = new BasicStunt(model, enemy, ENEMY_SPEED);
			enemy.setStunt(enemyStunt);
			GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);
			brain.add(enemyBot);
		}
	}

	// SPAWNER ET ICE

	public static void installSpawnerBots(Brain brain, Model model, View view, BuildMap builder,
			Automaton enemyAutomaton, CurrentPlayer currentPlayer, PengoConfig config, PengoGameState gameState,
			PengoRunState runState) {

		for (final Spawner spawner : builder.spawners()) {
			BasicStunt spawnerStunt = new BasicStunt(model, spawner);
			spawner.setStunt(spawnerStunt);
			Bot spawnerBot = new Bot(brain, spawnerStunt) {
				private long lastSpawnMs = runState.nowMs();

				@Override
				public void think() {
					gameState.update();

					if (gameState.finished()) {
						gameState.stopAllEntities();
						return;
					}

					spawner.updateSliding();
					if (spawner.isSliding()) {
						return;
					}

					long now = runState.nowMs();
					if (now - this.lastSpawnMs < config.spawnDelayMs()) {
						return;
					}
					this.lastSpawnMs = now;

					if (livingEnemies(model) >= config.maxLivingEnemies()) {
						return;
					}
					if (totalSpawnedEnemies >= config.maxTotalSpawnedEnemies()) {
						return;
					}

					if (!spawnCellIsFree(spawner)) {
						return;
					}
					createSpawnedEnemy(model, view, brain, enemyAutomaton, currentPlayer, spawner.position(), gameState,
							runState);
				}
			};
			brain.add(spawnerBot);
		}
	}
	
	public static void installSpawnerLoadGalBots(Brain brain, Model model, View view, BuildMap builder,
	        Automaton spawnerAutomaton, Automaton enemyAutomaton, CurrentPlayer currentPlayer,
	        PengoGameState gameState, PengoRunState runState) {

	    for (final Spawner spawner : builder.spawners()) {
	        BasicStunt spawnerStunt = new BasicStunt(model, spawner);
	        spawner.setStunt(spawnerStunt);

	        GalBot spawnerBot = new GalBot(brain, spawnerStunt, spawner, spawnerAutomaton, currentPlayer,
	                new java.lang.Runnable() {
	                    @Override
	                    public void run() {
	                        if (runState.stopped() || runState.paused() || gameState.finished()) {
	                            return;
	                        }

	                        Grid.Position position = randomLoadTestPosition(model, builder);

	                        if (position == null) {
	                            return;
	                        }

	                        createSpawnedEnemy(model, view, brain, enemyAutomaton, currentPlayer, position, gameState,
	                                runState);
	                    }
	                });

	        brain.add(spawnerBot);
	    }
	}
	
	private static Grid.Position randomLoadTestPosition(Model model, BuildMap builder) {
		char[][] map = builder.getMap();
		Grid grid = model.grid();

		List<Grid.Position> positions = new ArrayList<Grid.Position>();

		for (int y = 0; y < grid.height(); y++) {
			for (int x = 0; x < grid.width(); x++) {
				if (y >= 0 && y < map.length && x >= 0 && x < map[y].length && map[y][x] == '.') {
					positions.add(grid.new Position(x, y));
				}
			}
		}

		if (positions.isEmpty()) {
			return null;
		}

		return positions.get((int) (Math.random() * positions.size()));
	}

	public static void installIceBots(Brain brain, Model model, BuildMap builder, PengoGameState gameState) {
		for (final Glacon ice : builder.iceBlocks()) {
			BasicStunt stunt = new BasicStunt(model, ice);
			Bot bot = new Bot(brain, stunt) {
				@Override
				public void think() {
					gameState.update();
					if (gameState.finished()) {
						gameState.stopAllEntities();
						return;
					}
					ice.updateSliding();
				}
			};
			brain.add(bot);
		}
	}

	public static void installGameStateBot(Brain brain, Model model, PengoGameState gameState) {
		Entity dummy = null;
		for (Entity entity : model.entities()) {
			if (entity.alive()) {
				dummy = entity;
				break;
			}
		}
		if (dummy == null) {
			return;
		}
		BasicStunt dummyStunt = new BasicStunt(model, dummy);
		Bot stateBot = new Bot(brain, dummyStunt) {
			@Override
			public void think() {
				gameState.update();

				if (gameState.finished()) {
					gameState.stopAllEntities();
				}
			}
		};
		brain.add(stateBot);
	}

	private static int livingEnemies(Model model) {
		int count = 0;
		for (Entity entity : model.entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				count++;
			}
		}
		return count;
	}

	private static boolean spawnCellIsFree(Spawner spawner) {
		Grid.Cell cell = spawner.model().grid().cellAt(spawner.position());
		for (Entity entity : cell.entities()) {
			if (entity != spawner && entity.alive()) {
				if (entity instanceof Mur || entity instanceof Glacon || entity instanceof Pengo
						|| entity instanceof Friend || entity instanceof Enemies) {
					return false;
				}
			}
		}
		return true;
	}

	private static void createSpawnedEnemy(Model model, View view, Brain brain, Automaton enemyAutomaton,
			CurrentPlayer currentPlayer, Grid.Position position, PengoGameState gameState, PengoRunState runState) {

		Enemies enemy = new Enemies(model, "spawned-enemy-" + spawnedEnemyCounter++);
		enemy.setRunState(runState);
		enemy.setAvatar(new EnemiesAvatar(view, enemy));
		model.add(enemy, position);
		totalSpawnedEnemies++;
		gameState.enemySpawned();
		BasicStunt enemyStunt = new BasicStunt(model, enemy, ENEMY_SPEED);
		enemy.setStunt(enemyStunt);
		GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);
		brain.add(enemyBot);
	}

	// Debug : permet de faire spawn un ennemi avec une option debug.
	public static void installDebugLoadSpawner(final DebugOptions debugOptions, final Model model, final View view,
			final Brain brain, final Automaton enemyAutomaton, final CurrentPlayer currentPlayer,
			final BuildMap builder, final PengoGameState gameState, final PengoRunState runState) {

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (runState.stopped()) {
					return;
				}
				if (!runState.paused() && debugOptions.loadTest()) {
					Grid.Position position = randomLoadTestPosition(model, builder);

					if (position != null) {
						createSpawnedEnemy(model, view, brain, enemyAutomaton, currentPlayer, position, gameState,
								runState);
					}
				}
				Runtime.post(this, 300);
			}
		}, 300);
	}
	
	// lance la verif des spawner allignes
	public static void installSpawnerAlignmentEffect(final BuildMap builder, final Pengo pengo,
			final PengoRunState runState, final int delayMs) {
		assert delayMs > 0;
		Runtime.post(new Runnable() {
			// sert a ne pas relancer l'effet en boucle si les spawners restent alignes.
			private boolean alreadyAligned = allLivingSpawnersAligned(builder.spawners());
			@Override
			public void run() {
				if (runState.stopped()) {
					return;
				}
				if (!runState.paused()) {
					boolean aligned = allLivingSpawnersAligned(builder.spawners());
					// on declenche l'effet seulement au moment ou ils deviennent alignes.
					if (aligned && !this.alreadyAligned) {
						triggerSpawnerAlignmentEffect(builder, pengo);
					}
					this.alreadyAligned = aligned;
				}
				// relance la verification apres delayMs.
				Runtime.post(this, delayMs);
			}
		}, delayMs);
	}

	// verifie si tous les spawners vivants sont alignes et colles.
	private static boolean allLivingSpawnersAligned(List<Spawner> spawners) {
		assert spawners != null;
		List<Spawner> living = new ArrayList<Spawner>();
		for (Spawner spawner : spawners) {
			if (spawner != null && spawner.alive() && spawner.installed()) {
				// si un spawner glisse encore, on attend.
				if (spawner.isSliding()) {
					return false;
				}
				living.add(spawner);
			}
		}
		// il faut au moins 2 spawners pour faire un alignement.
		if (living.size() < 2) {
			return false;
		}
		int firstX = living.get(0).position().x();
		int firstY = living.get(0).position().y();
		boolean sameColumn = true;
		boolean sameRow = true;
		for (Spawner spawner : living) {
			Grid.Position position = spawner.position();
			if (position.x() != firstX) {
				sameColumn = false;
			}
			if (position.y() != firstY) {
				sameRow = false;
			}
		}
		if (!sameRow && !sameColumn) {
			return false;
		}
		if (sameRow) {
			living.sort((a, b) -> Integer.compare(a.position().x(), b.position().x()));

			for (int i = 1; i < living.size(); i++) {
				int previousX = living.get(i - 1).position().x();
				int currentX = living.get(i).position().x();

				if (currentX != previousX + 1) {
					return false;
				}
			}

			return true;
		}
		living.sort((a, b) -> Integer.compare(a.position().y(), b.position().y()));
		for (int i = 1; i < living.size(); i++) {
			int previousY = living.get(i - 1).position().y();
			int currentY = living.get(i).position().y();

			if (currentY != previousY + 1) {
				return false;
			}
		}
		return true;
	}

	// applique l'effet quand les spawners sont alignes
	private static void triggerSpawnerAlignmentEffect(BuildMap builder, Pengo pengo) {
		long durationMs = Spawner.ALIGNMENT_EFFECT_DURATION_MS;

		pengo.activateFreezePower(durationMs);
		pengo.activateStar(durationMs);

		for (Spawner spawner : builder.spawners()) {
			if (spawner.alive() && spawner.installed()) {
				spawner.activateAlignmentEffect(durationMs);
			}
		}
	}
}