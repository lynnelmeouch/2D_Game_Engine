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

@DisplayName("PerformanceMonitor - mesures tick/paint/fps")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestPerformanceMonitor {

	@Test
	@Order(1)
	@DisplayName("01 - recordTick ignore les valeurs négatives")
	void recordTick() {
		System.out.println("PerformanceMonitor/01 : tick stats");
		PerformanceMonitor monitor = new PerformanceMonitor();

		monitor.recordTick(16L);
		monitor.recordTick(-1L);
		monitor.recordTick(20L);

		assertEquals(16.0, monitor.tickStats().min());
		assertEquals(20.0, monitor.tickStats().max());
	}

	@Test
	@Order(2)
	@DisplayName("02 - reset remet les stats à zéro")
	void reset() {
		System.out.println("PerformanceMonitor/02 : reset");
		PerformanceMonitor monitor = new PerformanceMonitor();
		monitor.recordTick(16L);
		monitor.reset();

		assertEquals(0.0, monitor.tickStats().average());
	}
	@Test
	@Order(8)
	@DisplayName("08 - PerformanceMonitor couvre recordPaintEvent et reset")
	void performanceMonitorPaintEvent() throws InterruptedException {
		PerformanceMonitor monitor = new PerformanceMonitor();

		monitor.recordPaintEvent();

		assertEquals(0.0, monitor.paintStats().average(), EPS);
		assertEquals(0.0, monitor.fpsStats().average(), EPS);

		Thread.sleep(5L);

		monitor.recordPaintEvent();

		assertTrue(monitor.paintStats().average() >= 0.0);
		assertTrue(monitor.fpsStats().average() >= 0.0);

		monitor.reset();

		assertEquals(0.0, monitor.paintStats().average(), EPS);
		assertEquals(0.0, monitor.fpsStats().average(), EPS);
	}

}
