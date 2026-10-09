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

@DisplayName("Circle - collision cercle/cercle")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestCircle {

	@Test
	@Order(1)
	@DisplayName("01 - deux cercles se touchent si la distance <= somme des rayons")
	void collisionCercleCercle() {
		System.out.println("Circle/01 : collision à distance 3 avec deux rayons 2");
		ISU isu = new JeuTest().getIsu();
		Circle a = new Circle(isu.new Coord(5, 5), 2.0);
		Circle b = new Circle(isu.new Coord(8, 5), 2.0);
		Circle c = new Circle(isu.new Coord(10, 5), 1.0);

		assertTrue(a.intersects(b));
		assertFalse(a.intersects(c));
	}

	@Test
	@Order(2)
	@DisplayName("02 - le cercle utilise la distance torique")
	void collisionCercleCercleSurTore() {
		System.out.println("Circle/02 : collision par le bord du monde");
		ISU isu = new JeuTest().getIsu();
		Circle left = new Circle(isu.new Coord(1, 10), 2.0);
		Circle right = new Circle(isu.new Coord(36, 10), 2.0);

		assertTrue(left.intersects(right));
	}

	@Test
	@Order(3)
	@DisplayName("03 - hitbox d'un cercle vaut centre +/- rayon")
	void hitboxCercle() {
		System.out.println("Circle/03 : boîte englobante d'un cercle");
		ISU isu = new JeuTest().getIsu();
		Hitbox h = new Circle(isu.new Coord(10, 9), 2.5).hitbox();

		assertEquals(7.5, h.xmin(), EPS);
		assertEquals(12.5, h.xmax(), EPS);
		assertEquals(6.5, h.ymin(), EPS);
		assertEquals(11.5, h.ymax(), EPS);
	}
}
