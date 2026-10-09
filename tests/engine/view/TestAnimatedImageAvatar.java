package engine.view;

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

@DisplayName("AnimatedImageAvatar - animation d'images")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestAnimatedImageAvatar {

	private static final class AnimatedAvatarTest extends AnimatedImageAvatar {
		boolean animate = true;
		boolean resetWhenOrientationChanges = true;

		AnimatedAvatarTest(View view, Entity entity, long frameDuration) {
			super(view, entity, new String[] {
					"anim0.png",
					"anim1.png"
			}, frameDuration);
		}

		int frameCourante() {
			return currentFrame();
		}

		void frame(int index) {
			setCurrentFrame(index);
		}

		@Override
		protected boolean shouldAnimate() {
			return this.animate;
		}

		@Override
		protected boolean resetOnOrientationChange() {
			return this.resetWhenOrientationChanges;
		}
	}

	@Test
	@Order(3)
	@DisplayName("03 - AnimatedImageAvatar change de frame selon elapsed")
	void animatedAvatarChangeFrame() {
		Game game = new JeuTest();
		Model model = game.model();

		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		View view = new View(model, 200, 200);

		AnimatedAvatarTest avatar = new AnimatedAvatarTest(view, e, 100L);

		e.setLinearSpeed(model.isu().new Vector(1.0, 0.0));

		assertEquals(0, avatar.frameCourante());

		avatar.tick(99L);
		assertEquals(0, avatar.frameCourante());

		avatar.tick(1L);
		assertEquals(1, avatar.frameCourante());

		avatar.tick(100L);
		assertEquals(0, avatar.frameCourante());
	}

	@Test
	@Order(4)
	@DisplayName("04 - AnimatedImageAvatar reset quand l'entité ne bouge pas")
	void animatedAvatarResetSiPasDeMouvement() {
		Game game = new JeuTest();
		Model model = game.model();

		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		View view = new View(model, 200, 200);

		AnimatedAvatarTest avatar = new AnimatedAvatarTest(view, e, 100L);

		avatar.frame(1);
		avatar.animate = false;

		avatar.tick(50L);

		assertEquals(0, avatar.frameCourante());
	}

	@Test
	@Order(5)
	@DisplayName("05 - AnimatedImageAvatar reset lors d'un changement d'orientation")
	void animatedAvatarResetOrientation() {
		Game game = new JeuTest();
		Model model = game.model();

		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		View view = new View(model, 200, 200);

		AnimatedAvatarTest avatar = new AnimatedAvatarTest(view, e, 100L);

		e.setLinearSpeed(model.isu().new Vector(1.0, 0.0));

		avatar.tick(100L);
		assertEquals(1, avatar.frameCourante());

		e.setOrientation(90);

		avatar.tick(1L);

		assertEquals(0, avatar.frameCourante());
	}

	@Test
	@Order(6)
	@DisplayName("06 - AnimatedImageAvatar reset si l'entité est morte ou retirée")
	void animatedAvatarResetEntiteMorteOuRetiree() {
		Game game = new JeuTest();
		Model model = game.model();

		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		View view = new View(model, 200, 200);

		AnimatedAvatarTest avatar = new AnimatedAvatarTest(view, e, 100L);

		avatar.frame(1);
		e.death();

		avatar.tick(1L);

		assertEquals(0, avatar.frameCourante());

		EntiteTest autre = OutilsMoteur.ajoute(model, "autre", 2, 2);
		AnimatedAvatarTest avatar2 = new AnimatedAvatarTest(view, autre, 100L);

		avatar2.frame(1);
		model.remove(autre);

		avatar2.tick(1L);

		assertEquals(0, avatar2.frameCourante());
	}

}
