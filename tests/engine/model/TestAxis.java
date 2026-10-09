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

@DisplayName("Axis - géométrie d'un axe")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestAxis {

	@Test
	@Order(1)
	@DisplayName("01 - un axe torique normalise les positions entières")
	void torusNormaliseLesEntiers() {
		System.out.println("Axis/01 : modulo positif sur un tore");
		Axis axis = new Axis(true, 10);

		assertEquals(0, axis.normalize(0), "0 reste 0");
		assertEquals(0, axis.normalize(10), "10 revient à 0");
		assertEquals(1, axis.normalize(11), "11 revient à 1");
		assertEquals(9, axis.normalize(-1), "-1 revient à 9");
	}

	@Test
	@Order(2)
	@DisplayName("02 - un axe non torique ne normalise pas les positions")
	void axeNonToriqueNeNormalisePas() {
		System.out.println("Axis/02 : axe ouvert, pas de wrapping");
		Axis axis = new Axis(false, 10);

		assertEquals(11, axis.normalize(11));
		assertEquals(-1, axis.normalize(-1));
	}

	@Test
	@Order(3)
	@DisplayName("03 - la distance sur un tore prend le plus court chemin")
	void distanceToriquePlusCourte() {
		System.out.println("Axis/03 : distance torique courte entre 1 et 9");
		Axis axis = new Axis(true, 10.0);

		assertEquals(2.0, axis.distance(1.0, 9.0), EPS);
		assertEquals(-2.0, axis.delta(1.0, 9.0), EPS);
		assertEquals(2.0, axis.delta(9.0, 1.0), EPS);
	}

	@Test
	@Order(4)
	@DisplayName("04 - une dimension garde une taille strictement positive")
	void dimensionPositiveSurUnTourComplet() {
		System.out.println("Axis/04 : une dimension de 20 sur un tore de 10 vaut 10, pas 0");
		Axis axis = new Axis(true, 10.0);

		assertEquals(10.0, axis.normalizeDimension(20.0), EPS);
		assertEquals(2.5, axis.normalizeDimension(12.5), EPS);
	}
}
