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

import engine.model.Game;
import engine.model.ISU;
import engine.model.Model;
import engine.model.ViewPort;
import engine.tests.OutilsMoteur.JeuTest;
import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import oop.graphics.Graphics;

@DisplayName("View - conversions viewport/canvas")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestView {

	private static final class OverlayTemoin implements Overlay {
		int width;
		int height;
		int ticks;
		int paints;
		long lastElapsed;

		OverlayTemoin(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public int widthPixel() {
			return this.width;
		}

		@Override
		public int heightPixel() {
			return this.height;
		}

		@Override
		public void paint(Graphics graphics, View view, int x, int y) {
			this.paints++;
		}

		@Override
		public void tick(long elapsed) {
			this.ticks++;
			this.lastElapsed = elapsed;
		}
	}


	static class OverlayTest implements Overlay {
		@Override
		public int widthPixel() {
			return 40;
		}

		@Override
		public int heightPixel() {
			return 20;
		}

		@Override
		public void paint(oop.graphics.Graphics graphics, View view, int x, int y) {
		}
	}

	@Test
	@Order(1)
	@DisplayName("01 - worldScale garde les proportions du viewport")
	void echelleUniforme() {
		System.out.println("View/01 : scale uniforme");
		Game game = new JeuTest(false, false);
		Model model = game.model();
		ISU isu = model.isu();
		model.setViewPort(new ViewPort(model, isu.new Coord(0, 0), isu.new Dimension(20, 10)));
		View view = new View(model, 200, 200);

		assertEquals(10.0, view.worldScale(), EPS);
		assertEquals(200, view.renderedWorldWidthPixel());
		assertEquals(100, view.renderedWorldHeightPixel());
		assertEquals(50, view.worldOffsetYPixel());
	}

	@Test
	@Order(2)
	@DisplayName("02 - toPixel traduit puis scale la coordonnée ISU")
	void toPixel() {
		System.out.println("View/02 : ISU vers pixel avec origine viewport");
		Game game = new JeuTest(false, false);
		Model model = game.model();
		ISU isu = model.isu();
		model.setViewPort(new ViewPort(model, isu.new Coord(5, 4), isu.new Dimension(10, 10)));
		View view = new View(model, 100, 100);

		assertEquals(0, view.toPixelX(5));
		assertEquals(0, view.toPixelY(4));
		assertEquals(50, view.toPixelX(10));
		assertEquals(60, view.toPixelY(10));
	}

	@Test
	@Order(3)
	@DisplayName("03 - toISUCoord inverse la conversion en clampant dans le viewport")
	void toISUCoord() {
		System.out.println("View/03 : pixel vers ISU");
		Game game = new JeuTest(false, false);
		Model model = game.model();
		ISU isu = model.isu();
		model.setViewPort(new ViewPort(model, isu.new Coord(5, 4), isu.new Dimension(10, 10)));
		View view = new View(model, 100, 100);

		ISU.Coord coord = view.toISUCoord(50, 60);

		assertEquals(10.0, coord.x(), EPS);
		assertEquals(10.0, coord.y(), EPS);
	}

	@Test
	@Order(4)
	@DisplayName("04 - overlay ajoute une zone au-dessus/sur le côté")
	void overlay() {
		System.out.println("View/04 : dimensions avec overlay");
		View view = new View(new JeuTest().model(), 200, 100, new OverlayTest());

		assertEquals(40, view.overlayWidthPixel());
		assertEquals(20, view.overlayHeightPixel());
		assertEquals(240, view.totalWidthPixel());
		assertEquals(120, view.totalHeightPixel());
		view.removeOverlay();
		assertEquals(0, view.overlayHeightPixel());
	}
	@Test
	@Order(1)
	@DisplayName("01 - View couvre getters, resize, overlay, debug et performance")
	void viewGettersEtOverlay() {
		Game game = new JeuTest(false, false);
		Model model = game.model();

		View view = new View(model, 200, 100);

		assertSame(model, view.model());
		assertSame(model.viewPort(), view.viewPort());
		assertEquals(200, view.widthPixel());
		assertEquals(100, view.heightPixel());

		assertNull(view.overlay());
		assertEquals(0, view.overlayWidthPixel());
		assertEquals(0, view.overlayHeightPixel());
		assertEquals(200, view.totalWidthPixel());
		assertEquals(100, view.totalHeightPixel());

		OverlayTemoin overlay = new OverlayTemoin(40, 25);
		view.setOverlay(overlay);

		assertSame(overlay, view.overlay());
		assertEquals(40, view.overlayWidthPixel());
		assertEquals(25, view.overlayHeightPixel());
		assertEquals(240, view.totalWidthPixel());
		assertEquals(125, view.totalHeightPixel());

		view.resize(300, 150);
		assertEquals(300, view.widthPixel());
		assertEquals(150, view.heightPixel());
		assertEquals(340, view.totalWidthPixel());
		assertEquals(175, view.totalHeightPixel());

		DebugOptions debug = new DebugOptions();
		PerformanceMonitor monitor = new PerformanceMonitor();

		view.setDebugOptions(debug);
		view.setPerformanceMonitor(monitor);

		assertSame(debug, view.debugOptions());
		assertSame(monitor, view.performanceMonitor());

		view.removeOverlay();

		assertNull(view.overlay());
		assertEquals(0, view.overlayWidthPixel());
		assertEquals(0, view.overlayHeightPixel());
	}

	@Test
	@Order(2)
	@DisplayName("02 - View couvre scale, offsets et tailles rendues")
	void viewScaleOffsetsEtDimensions() {
		Game game = new JeuTest(10, 8, false, false, 3.7, 2);
		Model model = game.model();
		ISU isu = model.isu();

		model.setViewPort(new ViewPort(model, isu.new Coord(0.0, 0.0), isu.new Dimension(20.0, 10.0)));

		View view = new View(model, 200, 200);

		assertEquals(10.0, view.worldScale(), EPS);
		assertEquals(10.0, view.scaleX(), EPS);
		assertEquals(10.0, view.scaleY(), EPS);

		assertEquals(200, view.renderedWorldWidthPixel());
		assertEquals(100, view.renderedWorldHeightPixel());

		assertEquals(0, view.worldOffsetXPixel());
		assertEquals(50, view.worldOffsetYPixel());

		assertEquals(20, view.toPixelWidth(2.0));
		assertEquals(30, view.toPixelHeight(3.0));
	}

	@Test
	@Order(3)
	@DisplayName("03 - View.toPixel traduit d'abord puis applique l'échelle")
	void conversionISUVersPixelNonTorique() {
		Game game = new JeuTest(10, 8, false, false, 3.7, 2);
		Model model = game.model();
		ISU isu = model.isu();

		model.setViewPort(new ViewPort(model, isu.new Coord(5.0, 4.0), isu.new Dimension(10.0, 10.0)));

		View view = new View(model, 100, 100);

		assertEquals(0, view.toPixelX(5.0));
		assertEquals(0, view.toPixelY(4.0));

		assertEquals(50, view.toPixelX(10.0));
		assertEquals(60, view.toPixelY(10.0));
	}

	@Test
	@Order(4)
	@DisplayName("04 - View.toPixel gère le wrapping sur un tore")
	void conversionISUVersPixelTorique() {
		Game game = new JeuTest(10, 8, true, true, 3.7, 2);
		Model model = game.model();
		ISU isu = model.isu();

		model.setViewPort(new ViewPort(model, isu.new Coord(35.0, 27.0), isu.new Dimension(10.0, 10.0)));

		View view = new View(model, 100, 100);

		assertEquals(30, view.toPixelX(1.0));
		assertEquals(46, view.toPixelY(2.0));
	}

	@Test
	@Order(5)
	@DisplayName("05 - View.toISUCoord inverse la conversion et clampe dans le viewport")
	void conversionPixelVersISU() {
		Game game = new JeuTest(10, 8, false, false, 3.7, 2);
		Model model = game.model();
		ISU isu = model.isu();

		model.setViewPort(new ViewPort(model, isu.new Coord(5.0, 4.0), isu.new Dimension(10.0, 10.0)));

		View view = new View(model, 100, 100);

		ISU.Coord centre = view.toISUCoord(50, 60);

		assertEquals(10.0, centre.x(), EPS);
		assertEquals(10.0, centre.y(), EPS);

		ISU.Coord tropPetit = view.toISUCoord(-100, -100);

		assertEquals(5.0, tropPetit.x(), EPS);
		assertEquals(4.0, tropPetit.y(), EPS);

		ISU.Coord tropGrand = view.toISUCoord(999, 999);

		assertTrue(tropGrand.x() <= 15.0);
		assertTrue(tropGrand.y() <= 14.0);
	}

	@Test
	@Order(6)
	@DisplayName("06 - View.tick met à jour overlay et startup reveal")
	void tickOverlayEtStartupReveal() {
		Game game = new JeuTest();
		Model model = game.model();

		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);

		OverlayTemoin overlay = new OverlayTemoin(40, 20);
		View view = new View(model, 200, 200, overlay);

		assertEquals(1.0, view.revealScale(e), EPS);

		view.startStartupReveal();

		assertTrue(view.startupRevealActive());
		assertEquals(0.0, view.revealScale(e), EPS);

		view.tick(10L);

		assertEquals(1, overlay.ticks);
		assertEquals(10L, overlay.lastElapsed);
		assertTrue(view.revealScale(e) > 0.0);

		view.tick(1000L);

		assertFalse(view.startupRevealActive());
		assertEquals(1.0, view.revealScale(e), EPS);
	}

}
