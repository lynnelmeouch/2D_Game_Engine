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

@DisplayName("Shape - classe abstraite des formes")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestShape {

	@Test
	@Order(1)
	@DisplayName("01 - Shape expose son centre et son ISU")
	void centreEtISU() {
		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		ISU isu = game.getIsu();

		Circle circle = new Circle(isu.new Coord(5.0, 6.0), 1.0);
		Shape shape = circle;

		assertSame(isu, shape.isu());
		assertEquals(5.0, shape.center().x(), EPS);
		assertEquals(6.0, shape.center().y(), EPS);
	}

	@Test
	@Order(2)
	@DisplayName("02 - Shape.translate déplace un Circle")
	void translateCircle() {
		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		ISU isu = game.getIsu();

		Circle circle = new Circle(isu.new Coord(5.0, 6.0), 1.0);
		Shape shape = circle;

		shape.translate(isu.new Vector(2.0, 3.0));

		assertEquals(7.0, shape.center().x(), EPS);
		assertEquals(9.0, shape.center().y(), EPS);
	}

	@Test
	@Order(3)
	@DisplayName("03 - Shape.translate déplace un Rect")
	void translateRect() {
		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		ISU isu = game.getIsu();

		Rect rect = new Rect(
				isu.new Coord(10.0, 10.0),
				isu.new Dimension(2.0, 4.0),
				0
		);

		Shape shape = rect;

		shape.translate(isu.new Vector(3.0, 4.0));

		assertEquals(13.0, shape.center().x(), EPS);
		assertEquals(14.0, shape.center().y(), EPS);
	}

	@Test
	@Order(4)
	@DisplayName("04 - Shape.translate respecte la normalisation torique")
	void translateAvecTore() {
		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		ISU isu = game.getIsu();

		Circle circle = new Circle(isu.new Coord(36.0, 28.0), 1.0);
		Shape shape = circle;

		shape.translate(isu.new Vector(3.0, 4.0));

		assertEquals(2.0, shape.center().x(), EPS);
		assertEquals(2.4, shape.center().y(), EPS);
	}

	@Test
	@Order(5)
	@DisplayName("05 - Shape couvre les contraintes toriques et non toriques")
	void contraintesToriquesEtNonToriques() {
		Game nonTore = new JeuTest(10, 8, false, false, 3.7, 2);
		ISU isuNonTore = nonTore.getIsu();

		Circle grandCercle = new Circle(isuNonTore.new Coord(5.0, 5.0), 100.0);
		assertEquals(100.0, grandCercle.radius(), EPS);

		Game toreX = new JeuTest(10, 8, true, false, 3.7, 2);
		ISU isuToreX = toreX.getIsu();

		Circle cercleToreX = new Circle(isuToreX.new Coord(5.0, 5.0), 1.0);
		assertEquals(1.0, cercleToreX.radius(), EPS);

		Game toreY = new JeuTest(10, 8, false, true, 3.7, 2);
		ISU isuToreY = toreY.getIsu();

		Circle cercleToreY = new Circle(isuToreY.new Coord(5.0, 5.0), 1.0);
		assertEquals(1.0, cercleToreY.radius(), EPS);

		Game toreXY = new JeuTest(10, 8, true, true, 3.7, 2);
		ISU isuToreXY = toreXY.getIsu();

		Rect rect = new Rect(
				isuToreXY.new Coord(5.0, 5.0),
				isuToreXY.new Dimension(2.0, 3.0),
				0
		);

		assertEquals(1.0, rect.halfWidth(), EPS);
		assertEquals(1.5, rect.halfHeight(), EPS);
	}
}