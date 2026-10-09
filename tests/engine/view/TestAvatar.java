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
import oop.graphics.Graphics;

@DisplayName("Avatar - base de rendu")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestAvatar {

	private static final class AvatarTemoin extends Avatar {
		int ticks;
		int paints;

		AvatarTemoin(View view, Entity entity) {
			super(view, entity);
		}

		@Override
		public void paint(Graphics g) {
			this.paints++;
		}

		@Override
		public void tick(long elapsed) {
			this.ticks++;
		}
	}

	@Test
	@Order(1)
	@DisplayName("01 - Avatar expose view/entity et reçoit les ticks via View.tick")
	void avatarSimpleEtViewTick() {
		Game game = new JeuTest();
		Model model = game.model();

		EntiteTest e = OutilsMoteur.ajoute(model, "e", 1, 1);
		View view = new View(model, 200, 200);

		AvatarTemoin avatar = new AvatarTemoin(view, e);
		e.setAvatar(avatar);

		assertSame(view, avatar.view());
		assertSame(e, avatar.entity());
		assertSame(avatar, e.avatar());

		view.tick(16L);

		assertEquals(1, avatar.ticks);

		e.removeAvatar();

		view.tick(16L);

		assertEquals(1, avatar.ticks);
	}

}
