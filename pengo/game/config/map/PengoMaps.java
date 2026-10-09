package game.config.map;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public final class PengoMaps {

	private static final String MAP_FOLDER = "pengo_maps";

	private static PengoMapChoice[] maps;

	private PengoMaps() {
	}

	public static int count() {
		loadIfNeeded();
		return maps.length;
	}

	public static PengoMapChoice get(int index) {
		loadIfNeeded();
		assert index >= 0;
		assert index < maps.length;
		return maps[index];
	}

	public static PengoMapChoice[] all() {
		loadIfNeeded();
		PengoMapChoice[] copy = new PengoMapChoice[maps.length];
		for (int i = 0; i < maps.length; i++) {
			copy[i] = maps[i];
		}
		return copy;
	}

	public static String folder() {
		return MAP_FOLDER;
	}

	private static void loadIfNeeded() {
		if (maps != null) {
			return;
		}
		File folder = new File(MAP_FOLDER);

		if (!folder.exists()) {
			throw new IllegalStateException("Map folder does not exist: " + MAP_FOLDER);
		}
		if (!folder.isDirectory()) {
			throw new IllegalStateException("Not a map folder: " + MAP_FOLDER);
		}

		// recupere seulement les fichiers .txt du folder map
		File[] files = folder.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".txt"));

		if (files == null) {
			maps = new PengoMapChoice[0];
			return;
		}

		// trie les maps par nom de fichier pour avoir un ordre stable dans le menu
		Arrays.sort(files, Comparator.comparing(File::getName));
		maps = new PengoMapChoice[files.length];

		// transforme chaque .txt en choix de map
		for (int i = 0; i < files.length; i++) {
			maps[i] = buildChoice(files[i]);
		}
	}

	// cree un PengoMapChoice a partir dune map
	private static PengoMapChoice buildChoice(File file) {
		try {
			MapInfo info = MapInfo.read(file.getPath());
			return new PengoMapChoice(labelFromFile(file), file.getPath(), info.width(), info.height(), info.torusX(),
					info.torusY());
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to read map: " + file.getPath(), exception);
		}
	}

	// Pour afficher le nom dans le menu
	private static String labelFromFile(File file) {
		String name = file.getName();
		// remove .txt
		if (name.endsWith(".txt")) {
			name = name.substring(0, name.length() - 4);
		}
		// _ devient espace
		return name.replace('_', ' ');
	}
}