package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("Orientation - directions cardinales")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestOrientation {

	private static final class OrientationGameTest extends Game {
		OrientationGameTest() {
			super(10, 10, true, false, 1.0, 1);
		}
	}

	@Test
	@Order(1)
	@DisplayName("01 - un angle quelconque est ramené à une direction cardinale")
	void normalizeCardinal() {
		assertEquals(Orientation.EAST, Orientation.normalize(10));
		assertEquals(Orientation.SOUTH, Orientation.normalize(80));
		assertEquals(Orientation.WEST, Orientation.normalize(200));
		assertEquals(Orientation.NORTH, Orientation.normalize(-90));
	}

	@Test
	@Order(2)
	@DisplayName("02 - gauche, droite et arrière sont cohérents")
	void rotationsCardinales() {
		assertEquals(Orientation.NORTH, Orientation.leftOf(Orientation.EAST));
		assertEquals(Orientation.SOUTH, Orientation.rightOf(Orientation.EAST));
		assertEquals(Orientation.WEST, Orientation.backOf(Orientation.EAST));
	}

	@Test
	@Order(3)
	@DisplayName("03 - opposite détecte les directions opposées")
	void directionsOpposees() {
		assertTrue(Orientation.opposite(Orientation.EAST, Orientation.WEST));
		assertTrue(Orientation.opposite(Orientation.NORTH, Orientation.SOUTH));

		assertFalse(Orientation.opposite(Orientation.EAST, Orientation.SOUTH));
		assertFalse(Orientation.opposite(Orientation.NORTH, Orientation.WEST));
	}

	@Test
	@Order(4)
	@DisplayName("04 - vector convertit une orientation en déplacement grille")
	void vectorSelonDirection() {
		Grid grid = new OrientationGameTest().getGrid();

		assertEquals(grid.new Vector(1, 0), Orientation.vector(grid, Orientation.EAST));
		assertEquals(grid.new Vector(0, 1), Orientation.vector(grid, Orientation.SOUTH));
		assertEquals(grid.new Vector(-1, 0), Orientation.vector(grid, Orientation.WEST));
		assertEquals(grid.new Vector(0, -1), Orientation.vector(grid, Orientation.NORTH));
	}

	@Test
	@Order(5)
	@DisplayName("05 - neighbor respecte le tore X et le bord fermé Y")
	void voisinSelonGeometrie() {
		Grid grid = new OrientationGameTest().getGrid();

		Grid.Position toric = Orientation.neighbor(grid, grid.new Position(0, 0), Orientation.WEST);

		Grid.Position blocked = Orientation.neighbor(grid, grid.new Position(0, 0), Orientation.NORTH);

		assertEquals(9, toric.x());
		assertEquals(0, toric.y());

		assertNull(blocked);
	}

	@Test
	@Order(6)
	@DisplayName("06 - neighbor avance normalement dans la grille")
	void voisinNormal() {
		Grid grid = new OrientationGameTest().getGrid();

		Grid.Position start = grid.new Position(4, 4);

		assertEquals(grid.new Position(5, 4), Orientation.neighbor(grid, start, Orientation.EAST));
		assertEquals(grid.new Position(4, 5), Orientation.neighbor(grid, start, Orientation.SOUTH));
		assertEquals(grid.new Position(3, 4), Orientation.neighbor(grid, start, Orientation.WEST));
		assertEquals(grid.new Position(4, 3), Orientation.neighbor(grid, start, Orientation.NORTH));
	}
}