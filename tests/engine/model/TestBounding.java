package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("Bounding - union de formes simples")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestBounding {

	@Test
	@Order(1)
	@DisplayName("01 - Bounding vide ne touche rien")
	void boundingVide() {
		System.out.println("Bounding/01 : collection vide");
		ISU isu = new JeuTest().getIsu();
		Bounding empty = new Bounding();

		assertTrue(empty.isEmpty());
		assertFalse(empty.intersects(new Circle(isu.new Coord(1, 1), 0.5)));
	}

	@Test
	@Order(2)
	@DisplayName("02 - Bounding avec plusieurs formes teste chaque paire")
	void boundingMultiFormes() {
		System.out.println("Bounding/02 : cercle + rectangle");
		ISU isu = new JeuTest().getIsu();
		Bounding a = new Bounding();
		a.add(new Circle(isu.new Coord(5, 5), 1));
		a.add(new Rect(isu.new Coord(15, 5), isu.new Dimension(3, 3), 0));

		Bounding b = new Bounding();
		b.add(new Circle(isu.new Coord(15.5, 5), 1));

		Bounding c = new Bounding();
		c.add(new Circle(isu.new Coord(25, 5), 1));

		assertEquals(2, a.size());
		assertTrue(a.intersects(b));
		assertFalse(a.intersects(c));
	}

	@Test
	@Order(3)
	@DisplayName("03 - shapes() retourne une copie non dangereuse")
	void shapesRetourneUneCopie() {
		System.out.println("Bounding/03 : encapsulation de la collection interne");
		ISU isu = new JeuTest().getIsu();
		Bounding b = new Bounding();
		b.add(new Circle(isu.new Coord(5, 5), 1));

		b.shapes().clear();

		assertEquals(1, b.size(), "vider la copie ne doit pas vider le Bounding");
	}
}
