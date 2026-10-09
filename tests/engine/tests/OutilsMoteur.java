package engine.tests;

import java.util.ArrayList;
import java.util.List;

import engine.brain.Brain;
import engine.model.BasicStunt;
import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.Game;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Rect;

public final class OutilsMoteur {

	public static final double EPS = 0.000001;

	private OutilsMoteur() {
	}

	public static class JeuTest extends Game {
		public JeuTest() {
			super(10, 8, true, false, 3.7, 2);
		}

		public JeuTest(boolean torusX, boolean torusY) {
			super(10, 8, torusX, torusY, 3.7, 2);
		}

		public JeuTest(int widthCell, int heightCell, boolean torusX, boolean torusY, double cmPerCell,
				int pixelPerCm) {
			super(widthCell, heightCell, torusX, torusY, cmPerCell, pixelPerCm);
		}

		public JeuTest(double widthCm, double heightCm, boolean torusX, boolean torusY, double cmPerCell,
				int pixelPerCm) {
			super(widthCm, heightCm, torusX, torusY, cmPerCell, pixelPerCm);
		}
	}

	public static enum FormeTest {
		CERCLE, RECTANGLE
	}

	public static class EntiteTest extends Entity {
		private FormeTest forme;
		private boolean bloquante;
		private int collisions;
		private List<Entity> dernieresCollisions;

		public EntiteTest(Model model, String name) {
			this(model, name, FormeTest.CERCLE, false);
		}

		public EntiteTest(Model model, String name, FormeTest forme, boolean bloquante) {
			super(model, name);
			this.forme = forme;
			this.bloquante = bloquante;
			this.collisions = 0;
			this.dernieresCollisions = new ArrayList<Entity>();
			updateBounding();
		}

		public EntiteTest avecTaille(double widthCm, double heightCm) {
			setSize(model().isu().new Dimension(widthCm, heightCm));
			return this;
		}

		public EntiteTest avecPas(double widthCm, double heightCm) {
			setStep(model().isu().new Dimension(widthCm, heightCm));
			return this;
		}

		public EntiteTest avecForme(FormeTest forme) {
			this.forme = forme;
			updateBounding();
			return this;
		}

		public EntiteTest bloquante(boolean value) {
			this.bloquante = value;
			return this;
		}

		public int collisionsRecues() {
			return this.collisions;
		}

		public List<Entity> dernieresCollisions() {
			return new ArrayList<Entity>(this.dernieresCollisions);
		}

		@Override
		protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
			Bounding result = new Bounding();

			if (this.forme == FormeTest.RECTANGLE) {
				result.add(new Rect(center, size, orientationDegree));
			} else {
				double radius = Math.min(size.x(), size.y()) / 2.0;
				result.add(new Circle(center, radius));
			}

			return result;
		}

		@Override
		public boolean blocks(Entity other) {
			return this.bloquante;
		}

		@Override
		public void collision(Entity e) {
			super.collision(e);
			this.collisions++;
			this.dernieresCollisions.clear();
			this.dernieresCollisions.add(e);
		}

		@Override
		public void collision(List<Entity> entities) {
			super.collision(entities);
			this.collisions++;
			this.dernieresCollisions = new ArrayList<Entity>(entities);
		}
	}

	public static JeuTest jeu() {
		return new JeuTest();
	}

	public static EntiteTest ajoute(Model model, String name, int x, int y) {
		EntiteTest entity = new EntiteTest(model, name);
		model.add(entity, model.grid().new Position(x, y));
		return entity;
	}

	public static EntiteTest ajouteRectangle(Model model, String name, int x, int y, boolean bloque) {
		EntiteTest entity = new EntiteTest(model, name, FormeTest.RECTANGLE, bloque);
		model.add(entity, model.grid().new Position(x, y));
		return entity;
	}

	public static BasicStunt stuntDeMarche(Model model, EntiteTest entity) {
		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);
		return stunt;
	}

	public static Brain cerveau() {
		return new Brain();
	}
}
