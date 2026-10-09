package game.config.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class MapInfo {

	private final int width;
	private final int height;

	private final boolean torusX;
	private final boolean torusY;

	private MapInfo(int width, int height, boolean torusX, boolean torusY) {
		assert width > 0;
		assert height > 0;

		this.width = width;
		this.height = height;
		this.torusX = torusX;
		this.torusY = torusY;
	}

	public int width() {
		return this.width;
	}

	public int height() {
		return this.height;
	}

	public boolean torusX() {
		return this.torusX;
	}

	public boolean torusY() {
		return this.torusY;
	}

	// li le fichier de map et calcule ses informations principales
	public static MapInfo read(String path) throws IOException {
		assert !path.isEmpty();
		String[] lines = readLines(path);

		int height = lines.length;
		int width = lines[0].length();

		// si il ya une ouverture sur les bords cest torique
		boolean torusX = hasHorizontalOpening(lines, width, height);
		boolean torusY = hasVerticalOpening(lines, width, height);

		return new MapInfo(width, height, torusX, torusY);
	}

	// lit toutes les lignes du fichier et verifie que la map est rectangulaire
	private static String[] readLines(String path) throws IOException {
		String[] lines = new String[256];
		int count = 0;
		int width = -1;

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					throw new IllegalStateException("Empty line in map: " + path);
				}
				// la premiere ligne fix la largeur de la map
				if (width == -1) {
					width = line.length();
				} else if (line.length() != width) {
					throw new IllegalStateException("Map is not rectangular: " + path);
				}
				// "aggrandi" le tableau si la map est trop grande
				if (count == lines.length) {
					lines = grow(lines);
				}
				lines[count] = line;
				count++;
			}
		}
		if (count == 0) {
			throw new IllegalStateException("Empty map: " + path);
		}
		// cree une copie des lignes vraiment utilisé
		String[] result = new String[count];
		for (int i = 0; i < count; i++) {
			result[i] = lines[i];
		}
		return result;
	}

	// ""aggrandi" la taille *2 (copie)
	private static String[] grow(String[] array) {
		String[] bigger = new String[array.length * 2];

		for (int i = 0; i < array.length; i++) {
			bigger[i] = array[i];
		}

		return bigger;
	}

	// verifie une ouverture sur les bords a gauche ou a droite
	private static boolean hasHorizontalOpening(String[] lines, int width, int height) {
		for (int y = 0; y < height; y++) {
			char left = lines[y].charAt(0);
			char right = lines[y].charAt(width - 1);
			if (!isBorderBlock(left) || !isBorderBlock(right)) {
				return true;
			}
		}
		return false;
	}

	// verifie une ouverture en haut et en bas
	private static boolean hasVerticalOpening(String[] lines, int width, int height) {
		String topLine = lines[0];
		String bottomLine = lines[height - 1];
		for (int x = 0; x < width; x++) {
			char top = topLine.charAt(x);
			char bottom = bottomLine.charAt(x);
			if (!isBorderBlock(top) || !isBorderBlock(bottom)) {
				return true;
			}
		}
		return false;
	}

	// le mur est bloquant il bloque le tore
	// le spawner si il est "en face" dun mur ou dun spawner est bloquant comme il
	// se casse pas
	private static boolean isBorderBlock(char c) {
		return c == '#' || c == 'S';
	}
}