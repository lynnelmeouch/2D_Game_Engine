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
import engine.tests.OutilsMoteur.JeuTest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@DisplayName("Picture - conversions cm/pixels")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestPicture {

	private PrintStream sortieMuette() {
		return new PrintStream(new ByteArrayOutputStream());
	}


	@Test
	@Order(1)
	@DisplayName("01 - dimensions en pixels dérivées de Game")
	void dimensionsPixels() {
		System.out.println("Picture/01 : largeur/hauteur en pixels");
		Game game = new JeuTest(10, 8, true, false, 3.7, 2);
		Picture picture = game.getPicture();

		assertEquals(74, picture.width());
		assertEquals(60, picture.height());
		assertEquals(2, picture.pixelPerCm());
	}

	@Test
	@Order(2)
	@DisplayName("02 - conversion aller-retour Pixel -> ISU")
	void conversionPixelISU() {
		System.out.println("Picture/02 : Pixel vers coordonnées ISU");
		Game game = new JeuTest();
		Picture picture = game.getPicture();
		ISU.Coord coord = game.getIsu().new Coord(5.5, 6.5);

		Picture.Pixel pixel = picture.from(coord);

		assertEquals(11, pixel.x());
		assertEquals(13, pixel.y());
		assertEquals(5.5, pixel.toISUCoord().x(), EPS);
		assertEquals(6.5, pixel.toISUCoord().y(), EPS);
	}

	@Test
	@Order(3)
	@DisplayName("03 - Pixel.copy et equals respectent le Picture propriétaire")
	void copiePixel() {
		System.out.println("Picture/03 : copie de pixel");
		Picture picture = new JeuTest().getPicture();
		Picture.Pixel pixel = picture.new Pixel(3, 4);

		assertEquals(pixel, pixel.copy());
		assertEquals(pixel.hashCode(), pixel.copy().hashCode());
	}
	@Test
	@Order(7)
	@DisplayName("07 - Picture couvre show, from(Position), copy, equals et hashCode")
	void picturePlusComplet() {
		Game game = new JeuTest(10, 8, true, false, 3.7, 2);
		Picture picture = game.getPicture();

		assertSame(game, picture.game());
		assertSame(game.getIsu(), picture.isu());

		assertEquals(10, picture.toPixel(5.0));
		assertEquals(5.0, picture.toCm(10), EPS);

		Picture.Pixel pixelDepuisPosition = picture.from(game.getGrid().new Position(1, 2));
		assertEquals(7, pixelDepuisPosition.x());
		assertEquals(14, pixelDepuisPosition.y());

		Picture.Pixel pixelDepuisCentre = picture.from(game.getGrid().new Position(1, 2).toISUCoordCentered());

		assertEquals(11, pixelDepuisCentre.x());
		assertEquals(18, pixelDepuisCentre.y());

		Picture.Pixel p1 = picture.new Pixel(3, 4);
		Picture.Pixel p2 = p1.copy();

		assertEquals(p1, p2);
		assertEquals(p1.hashCode(), p2.hashCode());
		assertTrue(p1.equals(p1));
		assertFalse(p1.equals("pas un pixel"));

		Picture autrePicture = new JeuTest().getPicture();
		Picture.Pixel autrePixel = autrePicture.new Pixel(3, 4);

		assertFalse(p1.equals(autrePixel));

		picture.show(sortieMuette());
		p1.show(sortieMuette());
	}

}
