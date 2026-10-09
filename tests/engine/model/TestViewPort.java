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

import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("ViewPort - fenêtre dynamique du modèle")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestViewPort {

	@Test
	@Order(1)
	@DisplayName("01 - sur un axe non torique, l'origine est clampée dans la carte")
	void origineClampNonTorique() {
		System.out.println("ViewPort/01 : contrainte non-torique");
		Game game = new JeuTest(false, false);
		Model model = game.model();
		ISU isu = model.isu();
		ViewPort vp = new ViewPort(model, isu.new Coord(30.0, 20.0), isu.new Dimension(10.0, 10.0));

		assertEquals(isu.width() - 10.0, vp.x(), EPS);
		assertEquals(isu.height() - 10.0, vp.y(), EPS);
	}

	@Test
	@Order(2)
	@DisplayName("02 - sur un axe torique, l'origine peut faire le tour")
	void origineNormaliseeTorique() {
		System.out.println("ViewPort/02 : origine torique");
		Game game = new JeuTest(true, true);
		Model model = game.model();
		ISU isu = model.isu();
		ViewPort vp = new ViewPort(model, isu.new Coord(isu.width() + 1.0, isu.height() + 2.0),
				isu.new Dimension(10.0, 10.0));

		assertEquals(1.0, vp.x(), EPS);
		assertEquals(2.0, vp.y(), EPS);
	}

	@Test
	@Order(3)
	@DisplayName("03 - centerOn place l'entité au centre visible quand c'est possible")
	void centerOnEntite() {
		System.out.println("ViewPort/03 : suivi immédiat d'une entité");
		Game game = new JeuTest(false, false);
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 5, 4);
		ISU isu = model.isu();
		ViewPort vp = new ViewPort(model, isu.new Coord(0.0, 0.0), isu.new Dimension(10.0, 10.0));

		vp.centerOn(e);

		assertEquals(e.center().x() - 5.0, vp.x(), EPS);
		assertEquals(e.center().y() - 5.0, vp.y(), EPS);
	}

	@Test
	@Order(4)
	@DisplayName("04 - follow avec élasticité bouge progressivement")
	void followProgressif() {
		System.out.println("ViewPort/04 : suivi progressif");
		Game game = new JeuTest(false, false);
		Model model = game.model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 5, 4);
		ISU isu = model.isu();
		ViewPort vp = new ViewPort(model, isu.new Coord(0.0, 0.0), isu.new Dimension(10.0, 10.0));

		vp.follow(e, 0.5);

		assertEquals((e.center().x() - 5.0) * 0.5, vp.x(), EPS);
		assertEquals((e.center().y() - 5.0) * 0.5, vp.y(), EPS);
	}
	@Test
	@Order(7)
	@DisplayName("07 - ViewPort couvre clamp non torique, tore, follow et changement de dimension")
	void viewportPlusComplet() {
		Game nonTore = new JeuTest(10, 8, false, false, 3.7, 2);
		Model model = nonTore.model();

		ViewPort vp = new ViewPort(model, model.isu().new Coord(1.0, 1.0),
				model.isu().new Dimension(4 * nonTore.getCmPerCell(), 3 * nonTore.getCmPerCell()));

		assertEquals(1.0, vp.x(), EPS);
		assertEquals(1.0, vp.y(), EPS);

		EntiteTest hautGauche = OutilsMoteur.ajoute(model, "hautGauche", 0, 0);
		vp.centerOn(hautGauche);

		assertEquals(0.0, vp.x(), EPS);
		assertEquals(0.0, vp.y(), EPS);

		EntiteTest basDroite = OutilsMoteur.ajoute(model, "basDroite", 9, 7);
		vp.centerOn(basDroite);

		assertEquals(nonTore.getWidthCm() - vp.width(), vp.x(), EPS);
		assertEquals(nonTore.getHeightCm() - vp.height(), vp.y(), EPS);

		vp.follow(hautGauche);
		assertEquals(0.0, vp.x(), EPS);
		assertEquals(0.0, vp.y(), EPS);

		vp.follow(basDroite, 0.0);
		assertEquals(0.0, vp.x(), EPS);
		assertEquals(0.0, vp.y(), EPS);

		vp.follow(basDroite, 1.0);
		assertEquals(nonTore.getWidthCm() - vp.width(), vp.x(), EPS);
		assertEquals(nonTore.getHeightCm() - vp.height(), vp.y(), EPS);

		vp.setDimension(model.isu().new Dimension(nonTore.getWidthCm(), nonTore.getHeightCm()));
		assertEquals(nonTore.getWidthCm(), vp.width(), EPS);
		assertEquals(nonTore.getHeightCm(), vp.height(), EPS);
		assertEquals(0.0, vp.x(), EPS);
		assertEquals(0.0, vp.y(), EPS);

		assertSame(model, vp.model());
		assertSame(model.isu(), vp.isu());
		assertNotNull(vp.origin());
		assertNotNull(vp.dimension());

		Game tore = new JeuTest(10, 8, true, true, 3.7, 2);
		Model modelTore = tore.model();

		ViewPort vpTore = new ViewPort(modelTore,
				modelTore.isu().new Coord(tore.getWidthCm() + 1.0, tore.getHeightCm() + 1.0),
				modelTore.isu().new Dimension(4 * tore.getCmPerCell(), 3 * tore.getCmPerCell()));

		assertEquals(1.0, vpTore.x(), EPS);
		assertEquals(1.0, vpTore.y(), EPS);

		EntiteTest torique = OutilsMoteur.ajoute(modelTore, "torique", 9, 7);
		vpTore.follow(torique, 0.5);
		assertNotNull(vpTore.origin());
	}

}
