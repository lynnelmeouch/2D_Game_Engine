package engine.model;

import static engine.tests.OutilsMoteur.EPS;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.tests.OutilsMoteur.JeuTest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@DisplayName("Grid - cellules, positions et vecteurs")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestGrid {

	private PrintStream sortieMuette() {
		return new PrintStream(new ByteArrayOutputStream());
	}


	@Test
	@Order(1)
	@DisplayName("01 - Position se normalise sur les axes toriques seulement")
	void positionNormaliseeSelonLeTore() {
		System.out.println("Grid/01 : X torique, Y ouvert");
		Game game = new JeuTest(10, 8, true, false, 3.7, 2);
		Grid grid = game.getGrid();

		Grid.Position p = grid.new Position(11, 7);
		assertEquals(1, p.x());
		assertEquals(7, p.y());
	}

	@Test
	@Order(2)
	@DisplayName("02 - Vector représente un déplacement et ne se normalise pas")
	void vecteurNeSeNormalisePas() {
		System.out.println("Grid/02 : un vecteur peut être plus grand que la carte");
		Grid grid = new JeuTest().getGrid();

		Grid.Vector v = grid.new Vector(12, -9);
		v.add(grid.new Vector(2, 1));

		assertEquals(14, v.x());
		assertEquals(-8, v.y());
	}

	@Test
	@Order(3)
	@DisplayName("03 - Position + Vector déplace vers la bonne cellule")
	void positionTranslate() {
		System.out.println("Grid/03 : déplacement discret de cellule en cellule");
		Grid grid = new JeuTest().getGrid();
		Grid.Position p = grid.new Position(9, 2);

		p.translate(grid.new Vector(2, 3));

		assertEquals(1, p.x(), "X est torique donc 9 + 2 revient à 1");
		assertEquals(5, p.y());
	}

	@Test
	@Order(4)
	@DisplayName("04 - Position se convertit vers le coin et le centre ISU")
	void positionVersISU() {
		System.out.println("Grid/04 : conversion ncell -> cm");
		Game game = new JeuTest();
		Grid.Position p = game.getGrid().new Position(2, 3);

		assertEquals(7.4, p.toISUCoord().x(), EPS);
		assertEquals(11.1, p.toISUCoord().y(), EPS);
		assertEquals(9.25, p.toISUCoordCentered().x(), EPS);
		assertEquals(12.95, p.toISUCoordCentered().y(), EPS);
	}

	@Test
	@Order(5)
	@DisplayName("05 - Cell garde les entités qui l'occupent")
	void celluleAjouteEtRetireUneEntite() {
		System.out.println("Grid/05 : occupation d'une cellule");
		Game game = new JeuTest();
		Grid grid = game.getGrid();
		Grid.Cell cell = grid.cellAt(grid.new Position(1, 1));
		engine.tests.OutilsMoteur.EntiteTest entity = new engine.tests.OutilsMoteur.EntiteTest(game.model(), "e");

		cell.add(entity);
		assertTrue(cell.contains(entity));
		cell.remove(entity);
		assertFalse(cell.contains(entity));
	}
	@Test
	@Order(3)
	@DisplayName("03 - Grid couvre Dimension, Position, Vector, Cell et conversions Picture")
	void gridPlusComplet() {
		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		Grid grid = game.getGrid();

		Grid.Dimension d = grid.new Dimension(12, 9);
		assertEquals(2, d.x());
		assertEquals(1, d.y());

		Grid.Dimension copieD = d.copy();
		assertEquals(d, copieD);
		assertTrue(d.equiv(copieD));
		assertFalse(d.equiv(null));
		assertFalse(d.equals("pas une dimension"));
		assertEquals(7.4, d.toISUDimension().x(), EPS);
		d.show(sortieMuette());

		Grid.Vector v = grid.new Vector(1, 2);
		Grid.Vector copieV = v.copy();
		assertEquals(1, copieV.x());
		assertEquals(2, copieV.y());
		v.show(sortieMuette());

		Grid.Position centre = grid.new Position(5, 4);

		Grid.Position p = grid.new Position(6, 4);
		p.rotateAround(centre, 0);
		assertEquals(6, p.x());
		assertEquals(4, p.y());

		p = grid.new Position(6, 4);
		p.rotateAround(centre, 90);
		assertEquals(5, p.x());
		assertEquals(5, p.y());

		p = grid.new Position(6, 4);
		p.rotateAround(centre, 180);
		assertEquals(4, p.x());
		assertEquals(4, p.y());

		p = grid.new Position(6, 4);
		p.rotateAround(centre, 270);
		assertEquals(5, p.x());
		assertEquals(3, p.y());

		Grid.Position invalide = grid.new Position(6, 4);
		assertThrows(IllegalArgumentException.class, () -> invalide.rotateAround(centre, 45));

		Grid.Position move = grid.new Position(0, 0);
		move.moveEast(1);
		move.moveSouth(2);
		move.moveWest(1);
		move.moveNorth(1);
		assertEquals(0, move.x());
		assertEquals(1, move.y());

		assertFalse(move.equals("pas une position"));
		assertEquals(1.0, move.distanceTo(grid.new Position(0, 2)), EPS);

		assertNotNull(move.toPicturePixel());
		assertNotNull(move.toPicturePixelCentered());
		move.show(sortieMuette());

		Grid.Cell cell = grid.cellAt(grid.new Position(1, 1));
		assertEquals(1, cell.size().x());
		assertEquals(1, cell.size().y());
		assertEquals(1, cell.position().x());
		assertTrue(cell.entities().isEmpty());
		cell.show(sortieMuette());
	}

}
