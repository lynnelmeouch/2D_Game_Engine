package engine.view;

import static engine.tests.OutilsMoteur.EPS;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@DisplayName("SpawnAnimation - apparition progressive")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestSpawnAnimation {

	@Test
	@Order(1)
	@DisplayName("01 - scale commence petit, dépasse un peu, puis finit à 1")
	void scaleProgressif() {
		System.out.println("SpawnAnimation/01 : courbe de zoom");
		SpawnAnimation anim = new SpawnAnimation(100L);

		assertEquals(0.05, anim.scale(), EPS);
		anim.tick(70L);
		assertTrue(anim.scale() > 1.0);
		anim.tick(30L);
		assertTrue(anim.finished());
		assertEquals(1.0, anim.scale(), EPS);
	}

	@Test
	@Order(2)
	@DisplayName("02 - reset relance l'animation")
	void reset() {
		System.out.println("SpawnAnimation/02 : reset");
		SpawnAnimation anim = new SpawnAnimation(50L);
		anim.tick(50L);
		assertTrue(anim.finished());
		anim.reset();
		assertFalse(anim.finished());
	}
}
