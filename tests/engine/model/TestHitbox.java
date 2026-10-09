package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@DisplayName("Hitbox - filtre rapide de collision")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestHitbox {

	@Test
	@Order(1)
	@DisplayName("01 - deux hitbox alignées se touchent ou non")
	void intersectionEuclidienne() {
		System.out.println("Hitbox/01 : test AABB classique");
		Hitbox a = new Hitbox(0, 2, 0, 2);
		Hitbox b = new Hitbox(1, 3, 1, 3);
		Hitbox c = new Hitbox(3, 4, 3, 4);

		assertTrue(a.intersects(b));
		assertFalse(a.intersects(c));
	}

	@Test
	@Order(2)
	@DisplayName("02 - le tore permet une intersection par le bord")
	void intersectionTorique() {
		System.out.println("Hitbox/02 : collision entre bord gauche et bord droit");
		Hitbox left = new Hitbox(0, 1, 2, 3);
		Hitbox right = new Hitbox(9.5, 10.5, 2, 3);

		assertFalse(left.intersects(right));
		assertTrue(left.intersects(right, true, false, 10.0, 10.0));
	}

	@Test
	@Order(3)
	@DisplayName("03 - union englobe les deux boîtes")
	void unionEnglobante() {
		System.out.println("Hitbox/03 : union de deux boîtes");
		Hitbox u = new Hitbox(2, 5, 1, 4).union(new Hitbox(-1, 3, 3, 6));

		assertEquals(-1.0, u.xmin());
		assertEquals(5.0, u.xmax());
		assertEquals(1.0, u.ymin());
		assertEquals(6.0, u.ymax());
	}
}
