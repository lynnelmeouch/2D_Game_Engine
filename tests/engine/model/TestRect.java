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

@DisplayName("Rect - collisions avec rectangles tournés")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestRect {

	@Test
	@Order(1)
	@DisplayName("01 - rectangle/cercle : cercle au centre, au bord, puis loin")
	void intersectionRectangleCercle() {
		System.out.println("Rect/01 : intersection rectangle/cercle");
		ISU isu = new JeuTest().getIsu();
		Rect rect = new Rect(isu.new Coord(10, 10), isu.new Dimension(6, 4), 0);

		assertTrue(rect.intersects(new Circle(isu.new Coord(10, 10), 1.0)));
		assertTrue(rect.intersects(new Circle(isu.new Coord(13.5, 10), 0.6)));
		assertFalse(rect.intersects(new Circle(isu.new Coord(16, 10), 0.5)));
	}

	@Test
	@Order(2)
	@DisplayName("02 - rectangle/rectangle utilise SAT même avec rotation")
	void intersectionRectangleRectangleTourne() {
		System.out.println("Rect/02 : intersection entre rectangles tournés");
		ISU isu = new JeuTest().getIsu();
		Rect a = new Rect(isu.new Coord(12, 12), isu.new Dimension(6, 2), 45);
		Rect b = new Rect(isu.new Coord(14, 12), isu.new Dimension(4, 2), 0);
		Rect c = new Rect(isu.new Coord(20, 20), isu.new Dimension(2, 2), 0);

		assertTrue(a.intersects(b));
		assertFalse(a.intersects(c));
	}

	@Test
	@Order(3)
	@DisplayName("03 - rotate normalise l'angle entre 0 et 359")
	void rotationNormalisee() {
		System.out.println("Rect/03 : normalisation d'angle");
		ISU isu = new JeuTest().getIsu();
		Rect rect = new Rect(isu.new Coord(10, 10), isu.new Dimension(4, 2), -90);

		assertEquals(270, rect.angle());
		rect.rotate(180);
		assertEquals(90, rect.angle());
		assertEquals(2.0, rect.halfWidth(), EPS);
		assertEquals(1.0, rect.halfHeight(), EPS);
	}
}
