package game.config.session;

import java.io.IOException;

import engine.brain.Brain;
import engine.model.Game;
import engine.model.ISU;
import engine.model.Model;
import engine.model.ViewPort;
import engine.view.DebugOptions;
import engine.view.Painter;
import engine.view.PainterTask;
import engine.view.PerformanceMonitor;
import engine.view.Ticker;
import engine.view.View;
import gal.ast.AST;
import gal.ast.Automaton;
import gal.parser.Parser;
import game.BuildMap;
import game.config.PengoConfig;
import game.config.PengoRunState;
import game.config.map.MapInfo;
import game.config.spawn.PengoBotInstaller;
import game.config.spawn.PengoPowerSpawner;
import game.controller.PengoController;
import game.controller.Switch;
import game.gal.CurrentPlayer;
import game.model.PengoGameState;
import game.model.entity.Enemies;
import game.model.entity.Glacon;
import game.model.entity.Spawner;
import game.model.player.Friend;
import game.model.player.Pengo;
import game.view.PengoClassicOverlay;
import oop.graphics.Canvas;
import oop.tasks.Runnable;
import oop.tasks.Runtime;

public final class PengoGameRunner {

	private static final String ENEMY_GAL_PATH = "EnemyChase.gal";
	private static final String FRIEND_GAL_PATH = "FriendRandom.gal";
	private static final String SPAWNER_LOAD_GAL_PATH = "SpawnerLoadTest.gal";
	private static final String LOAD_TEST_MAP_NAME = "gal_load_test";

	private PengoGameRunner() {
	}

	public static PengoGameSession start(Canvas canvas, PengoConfig config) {

		MapInfo mapInfo = readMapInfo(config);

		Game game = new Game(mapInfo.width(), mapInfo.height(), config.torusX(), config.torusY(), config.cmPerCell(),
				config.pixelPerCm()) {
		};

		Model model = game.model();

		boolean viewportDemo = useViewportDemo(config);
		ViewPort viewPort = createViewPort(model, config, viewportDemo);
		model.setViewPort(viewPort);

		Brain brain = new Brain();
		DebugOptions debugOptions = new DebugOptions();
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		PengoRunState runState = new PengoRunState();

		View view = new View(model, config.windowWidthPixel(), config.windowHeightPixel());
		view.setDebugOptions(debugOptions);
		view.setPerformanceMonitor(performanceMonitor);

		Painter painter = new Painter(view, performanceMonitor);

		// charge la map et cree les entites
		BuildMap builder = buildMap(game, view, config);
		Pengo pengo = builder.pengo();

		// donne l'horloge de jeu aux entites.
		installRunState(builder, pengo, runState);

		// etat principal du jeu
		PengoGameState gameState = new PengoGameState(model, pengo, builder.enemies().size(), builder.spawners().size(),
				config.maxTotalSpawnedEnemies());

		view.setOverlay(new PengoClassicOverlay(gameState));
		view.startStartupReveal();

		// charge automate gal
		Automaton enemyAutomaton = loadGalAutomaton(ENEMY_GAL_PATH);
		Automaton friendAutomaton = loadGalAutomaton(FRIEND_GAL_PATH);

		Automaton spawnerLoadAutomaton = null;
		if (isGalLoadTestMap(config)) {
		    spawnerLoadAutomaton = loadGalAutomaton(SPAWNER_LOAD_GAL_PATH);
		}

		CurrentPlayer currentPlayer = new CurrentPlayer(pengo);

		if (viewportDemo) {
			viewPort.centerOn(pengo);
		}

		// ticker pricipal du jeu
		Ticker ticker = new Ticker(model, brain, view, config.tickDelayMs(), viewportDemo ? pengo : null,
				performanceMonitor);

		// switch avec friend
		Switch controlSwitcher = new Switch(brain, model, ticker, pengo, builder.friend(), friendAutomaton,
				currentPlayer);

		// prepare les compteurs des ennemis
		PengoBotInstaller.resetEnemyCounters(builder.enemies().size());

		// installe tous les bots
		PengoBotInstaller.installFriendGalBot(brain, model, builder.friend(), friendAutomaton, currentPlayer,
				controlSwitcher);
		PengoBotInstaller.installEnemyGalBots(brain, model, builder, enemyAutomaton, currentPlayer);
		if (isGalLoadTestMap(config)) {
		    PengoBotInstaller.installSpawnerLoadGalBots(brain, model, view, builder, spawnerLoadAutomaton,
		            enemyAutomaton, currentPlayer, gameState, runState);
		} else {
		    PengoBotInstaller.installSpawnerBots(brain, model, view, builder, enemyAutomaton, currentPlayer, config,
		            gameState, runState);
		}
		PengoBotInstaller.installDebugLoadSpawner(debugOptions, model, view, brain, enemyAutomaton, currentPlayer,
				builder, gameState, runState);
		PengoBotInstaller.installIceBots(brain, model, builder, gameState);
		PengoBotInstaller.installGameStateBot(brain, model, gameState);

		// effet special si les spawners sont alignes
		PengoBotInstaller.installSpawnerAlignmentEffect(builder, pengo, runState, config.tickDelayMs());

		// cree le controller clavier
		PengoController controller = new PengoController(controlSwitcher, view, config.continuousWalk(), debugOptions);
		controller.setGameState(gameState);

		if (controller.continuousWalk()) {
			installContinuousWalkTask(controller, config.tickDelayMs(), runState);
		}

		// met a jour les powers de Pengo et du Friend
		installPlayerPowerTimer(pengo, builder.friend(), config.tickDelayMs(), runState);

		// installe le spawn aleatoire des powers
		PengoPowerSpawner.resetPowerSpawnCounters();
		PengoPowerSpawner.installRandomPowerSpawner(model, view, gameState, builder, runState, currentPlayer);

		// lance l'affichage
		PainterTask painterTask = new PainterTask(painter, config.paintDelayMs());

		Runtime.post(ticker);
		Runtime.post(painterTask);

		// cree la session de jeu
		PengoGameSession session = new PengoGameSession(painter, controller, ticker, painterTask, view, runState);
		session.attach(canvas);

		return session;
	}

	private static MapInfo readMapInfo(PengoConfig config) {
		try {
			return MapInfo.read(config.mapPath());
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to read Pengo map: " + config.mapPath(), exception);
		}
	}

	private static BuildMap buildMap(Game game, View view, PengoConfig config) {
		try {
			BuildMap builder = new BuildMap(game, view, config.mapPath());
			builder.build();
			return builder;
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to load Pengo map: " + config.mapPath(), exception);
		}
	}

	private static void installRunState(BuildMap builder, Pengo pengo, PengoRunState runState) {
		pengo.setRunState(runState);
		if (builder.friend() != null) {
			builder.friend().setRunState(runState);
		}
		for (Enemies enemy : builder.enemies()) {
			enemy.setRunState(runState);
		}
		for (Glacon ice : builder.iceBlocks()) {
			ice.setRunState(runState);
		}
		for (Spawner spawner : builder.spawners()) {
			spawner.setRunState(runState);
		}
	}

	// on utilise le viewPort si on est en tore
	private static boolean useViewportDemo(PengoConfig config) {
		return config.torusX() || config.torusY();
	}

	private static ViewPort createViewPort(Model model, PengoConfig config, boolean viewportDemo) {
		ISU isu = model.isu();
		if (!viewportDemo) {
			return new ViewPort(model, isu.new Coord(0.0, 0.0), isu.new Dimension(isu.width(), isu.height()));
		}
		double width = Math.min(config.viewportWidthCells() * isu.cmPerCell(), isu.width() - isu.cmPerCell());
		double height = Math.min(config.viewportHeightCells() * isu.cmPerCell(), isu.height() - isu.cmPerCell());
		if (width <= 0.0) {
			width = isu.width();
		}
		if (height <= 0.0) {
			height = isu.height();
		}
		return new ViewPort(model, isu.new Coord(0.0, 0.0), isu.new Dimension(width, height));
	}

	// on lance la tache une premiere fois
	private static void installContinuousWalkTask(final PengoController controller, final int delayMs,
			final PengoRunState runState) {
		Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (runState.stopped()) {
					return;
				}
				if (!runState.paused()) {
					controller.updateContinuousWalk();
				}
				// on la relance epres delay por cree continuous walk
				Runtime.post(this, delayMs);
			}
		}, delayMs);
	}

	// pareil lance le timer des power
	private static void installPlayerPowerTimer(final Pengo pengo, final Friend friend, final int delayMs,
			final PengoRunState runState) {
		Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (runState.stopped()) {
					return;
				}

				if (!runState.paused()) {
					pengo.updateTimedPowers();

					if (friend != null) {
						friend.updateTimedPowers();
					}
				}
				// relance apres delay
				Runtime.post(this, delayMs);
			}
		}, delayMs);
	}
	
	private static boolean isGalLoadTestMap(PengoConfig config) {
	    return config.mapPath().contains(LOAD_TEST_MAP_NAME);
	}

	private static Automaton loadGalAutomaton(String path) {
		try {
			AST ast = Parser.from_file(path);
			if (ast.aut_list.isEmpty()) {
				throw new IllegalStateException("No GAL automaton in " + path);
			}
			return ast.aut_list.get(0);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to load GAL file: " + path, exception);
		}
	}
}