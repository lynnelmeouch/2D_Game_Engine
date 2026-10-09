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

import engine.model.Entity;
import engine.model.Game;
import engine.model.Model;
import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("ImageAvatar - images et frames")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestImageAvatar {

	private static final class ImageAvatarTest extends ImageAvatar {
		ImageAvatarTest(View view, Entity entity) {
			super(view, entity, new String[] {
					"image0.png",
					"image1.png",
					"image2.png"
			});
		}

		int nbFrames() {
			return frameCount();
		}

		int frameCourante() {
			return currentFrame();
		}

		void frame(int index) {
			setCurrentFrame(index);
		}

		void suivante() {
			nextFrame();
		}

		void resetPublic() {
			resetFrame();
		}

		String chemin(int index) {
			return pathAt(index);
		}

		double scaleSpawn() {
			return spawnAnimationScale();
		}

		@Override
		protected double visualScale() {
			return 2.0;
		}

		double visualScalePublic() {
			return visualScale();
		}

		boolean rotatePublic() {
			return rotateWithEntity();
		}
	}

	@Test
	@Order(2)
	@DisplayName("02 - ImageAvatar couvre frames, chemins, scale visuel et spawn animation")
	void imageAvatarFramesEtSpawn() {
		Game game = new JeuTest();
		Model model = game.model();

		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		View view = new View(model, 200, 200);

		ImageAvatarTest avatar = new ImageAvatarTest(view, e);

		assertEquals(3, avatar.nbFrames());
		assertEquals(0, avatar.frameCourante());
		assertEquals("image0.png", avatar.chemin(0));
		assertEquals("image1.png", avatar.chemin(1));
		assertEquals("image2.png", avatar.chemin(2));

		avatar.frame(2);
		assertEquals(2, avatar.frameCourante());

		avatar.suivante();
		assertEquals(0, avatar.frameCourante());

		avatar.frame(1);
		avatar.resetPublic();
		assertEquals(0, avatar.frameCourante());

		assertEquals(2.0, avatar.visualScalePublic(), EPS);
		assertTrue(avatar.rotatePublic());

		assertEquals(0.05, avatar.scaleSpawn(), EPS);

		avatar.tick(900L);

		assertEquals(1.0, avatar.scaleSpawn(), EPS);
	}

}
