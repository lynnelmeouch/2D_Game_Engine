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

@DisplayName("TimeStats - statistiques simples")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestTimeStats {

	@Test
	@Order(1)
	@DisplayName("01 - min, max et moyenne évoluent avec les valeurs positives")
	void stats() {
		System.out.println("TimeStats/01 : min/max/average");
		TimeStats stats = new TimeStats();

		stats.record(10.0);
		stats.record(20.0);
		stats.record(-5.0);
		stats.record(30.0);

		assertEquals(10.0, stats.min(), EPS);
		assertEquals(30.0, stats.max(), EPS);
		assertEquals(20.0, stats.average(), EPS);
	}

	@Test
	@Order(2)
	@DisplayName("02 - reset vide les mesures")
	void reset() {
		System.out.println("TimeStats/02 : reset");
		TimeStats stats = new TimeStats();
		stats.record(10.0);
		stats.reset();

		assertEquals(0.0, stats.min(), EPS);
		assertEquals(0.0, stats.max(), EPS);
		assertEquals(0.0, stats.average(), EPS);
	}
}
