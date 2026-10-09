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

@DisplayName("ISU - coordonnées continues en centimètres")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestISU {

	private PrintStream sortieMuette() {
		return new PrintStream(new ByteArrayOutputStream());
	}


	@Test
	@Order(1)
	@DisplayName("01 - Coord se normalise sur le tore")
	void coordNormaliseeSurTore() {
		System.out.println("ISU/01 : coordonnée au-delà du bord X");
		Game game = new JeuTest();
		ISU isu = game.getIsu();

		ISU.Coord c = isu.new Coord(38.0, 4.0);

		assertEquals(1.0, c.x(), EPS);
		assertEquals(4.0, c.y(), EPS);
	}

	@Test
	@Order(2)
	@DisplayName("02 - Coord vers Grid.Position utilise floor")
	void coordVersPosition() {
		System.out.println("ISU/02 : coordonnée précise vers cellule");
		Game game = new JeuTest();
		ISU.Coord c = game.getIsu().new Coord(8.0, 12.0);
		Grid.Position p = c.toGridPosition();

		assertEquals(2, p.x());
		assertEquals(3, p.y());
	}

	@Test
	@Order(3)
	@DisplayName("03 - mkVectorToward choisit le plus court chemin torique")
	void vecteurVersCibleSurTore() {
		System.out.println("ISU/03 : de x=1 vers x=36 sur une largeur 37 => -2");
		Game game = new JeuTest();
		ISU isu = game.getIsu();

		ISU.Vector v = isu.new Coord(1.0, 5.0).mkVectorToward(isu.new Coord(36.0, 7.0));

		assertEquals(-2.0, v.x(), EPS);
		assertEquals(2.0, v.y(), EPS);
	}

	@Test
	@Order(4)
	@DisplayName("04 - Vector garde sa valeur, se scale et se normalise en unité")
	void vecteurOperations() {
		System.out.println("ISU/04 : opérations sur les vecteurs");
		ISU.Vector v = new JeuTest().getIsu().new Vector(3.0, 4.0);

		assertEquals(5.0, v.norm(), EPS);
		v.unity();
		assertEquals(1.0, v.norm(), EPS);
		v.scale(10.0);
		assertEquals(10.0, v.norm(), EPS);
	}

	@Test
	@Order(5)
	@DisplayName("05 - rotation autour d'un point conserve la distance")
	void rotationAutourDUnCentre() {
		System.out.println("ISU/05 : rotation de 90 degrés autour d'un centre");
		ISU isu = new JeuTest().getIsu();
		ISU.Coord center = isu.new Coord(10.0, 10.0);
		ISU.Coord point = isu.new Coord(12.0, 10.0);

		point.rotateAround(center, 90);

		assertEquals(10.0, point.x(), EPS);
		assertEquals(12.0, point.y(), EPS);
		assertEquals(2.0, center.distanceTo(point), EPS);
	}
	@Test
	@Order(1)
	@DisplayName("01 - ISU.Dimension couvre copie, égalité, scale, show et normalisation torique")
	void isuDimensionPlusComplete() {
		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		ISU isu = game.getIsu();

		ISU.Dimension d = isu.new Dimension(74.0, 59.2);
		assertEquals(37.0, d.x(), EPS);
		assertEquals(29.6, d.y(), EPS);

		d.setxy(38.0, 30.6);
		assertEquals(1.0, d.x(), EPS);
		assertEquals(1.0, d.y(), EPS);

		ISU.Dimension copie = d.copy();
		assertEquals(d, copie);
		assertTrue(d.equiv(copie));
		assertFalse(d.equiv(null));
		assertFalse(d.equals("pas une dimension"));

		ISU.Vector v1 = d.mkVector();
		ISU.Vector v2 = d.mkScaledVector(2.0);
		ISU.Vector v3 = d.mkScaledVector(2.0, 3.0);

		assertEquals(1.0, v1.x(), EPS);
		assertEquals(2.0, v2.x(), EPS);
		assertEquals(3.0, v3.y(), EPS);

		d.show(sortieMuette());
	}

	@Test
	@Order(2)
	@DisplayName("02 - ISU.Coord et ISU.Vector couvrent conversions, rotations, dot, copy et show")
	void isuCoordVectorPlusComplete() {
		Game gameTore = new JeuTest(10, 8, true, true, 3.7, 2);
		ISU isuTore = gameTore.getIsu();

		ISU.Coord negatif = isuTore.new Coord(-2.0, -3.0);
		assertEquals(gameTore.getWidthCm() - 2.0, negatif.x(), EPS);
		assertEquals(gameTore.getHeightCm() - 3.0, negatif.y(), EPS);

		Grid.Position pNegatif = negatif.toGridPosition();
		assertEquals(9, pNegatif.x());
		assertEquals(7, pNegatif.y());

		ISU.Coord tropGrand = isuTore.new Coord(gameTore.getWidthCm() + 1.0, gameTore.getHeightCm() + 1.0);
		assertEquals(1.0, tropGrand.x(), EPS);
		assertEquals(1.0, tropGrand.y(), EPS);

		Grid.Position pGrand = tropGrand.toGridPosition();
		assertEquals(0, pGrand.x());
		assertEquals(0, pGrand.y());

		Game game = new JeuTest(10, 8, false, false, 3.7, 2);
		ISU isu = game.getIsu();

		ISU.Coord c = isu.new Coord(4.0, 5.0);
		assertEquals(c, c.mkCopy());
		assertTrue(c.equiv(c.mkCopy()));
		assertFalse(c.equiv(null));
		assertFalse(c.equals("pas une coord"));

		c.translate(isu.new Vector(1.0, 2.0));
		assertEquals(5.0, c.x(), EPS);
		assertEquals(7.0, c.y(), EPS);

		ISU.Coord translated = c.mkTranslated(isu.new Vector(3.0, 4.0));
		assertEquals(8.0, translated.x(), EPS);
		assertEquals(11.0, translated.y(), EPS);

		ISU.Coord r = isu.new Coord(2.0, 0.0);
		r.rotation(90);
		assertEquals(0.0, r.x(), EPS);
		assertEquals(2.0, r.y(), EPS);

		ISU.Coord centre = isu.new Coord(10.0, 10.0);
		ISU.Coord point = isu.new Coord(12.0, 10.0);
		point.rotateAround(centre, 180);
		assertEquals(8.0, point.x(), EPS);
		assertEquals(10.0, point.y(), EPS);

		ISU.Vector v = isu.new Vector(1.0, 2.0);
		v.add(isu.new Vector(3.0, 4.0));
		assertEquals(4.0, v.x(), EPS);
		assertEquals(6.0, v.y(), EPS);

		v.scale(2.0, 3.0);
		assertEquals(8.0, v.x(), EPS);
		assertEquals(18.0, v.y(), EPS);

		assertEquals(44.0, v.dot(isu.new Vector(1.0, 2.0)), EPS);

		ISU.Vector copy = v.copy();
		assertEquals(v.x(), copy.x(), EPS);
		assertEquals(v.y(), copy.y(), EPS);

		v.turn(90);
		assertEquals(-18.0, v.x(), EPS);
		assertEquals(8.0, v.y(), EPS);

		c.show(sortieMuette());
		v.show(sortieMuette());
	}

}
