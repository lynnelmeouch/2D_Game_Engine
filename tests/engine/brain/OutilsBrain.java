package engine.brain;

import java.util.ArrayList;
import java.util.List;

import engine.model.Entity;
import engine.model.Model;
import engine.model.Stunt;
import java.util.HashSet;
import java.util.Set;
import engine.tests.OutilsMoteur.EntiteTest;

final class OutilsBrain {
	private OutilsBrain() {
	}

	static class StuntMemoire extends Stunt {
		private final List<Integer> directions;
		private boolean accepte;

		StuntMemoire(Model model, Entity entity) {
			super(model, entity);
			this.directions = new ArrayList<Integer>();
			this.accepte = true;
		}

		void accepte(boolean value) {
			this.accepte = value;
		}

		List<Integer> directions() {
			return new ArrayList<Integer>(this.directions);
		}

		int derniereDirection() {
			return this.directions.get(this.directions.size() - 1).intValue();
		}

		@Override
		public boolean walk(int degree) {
			this.directions.add(Integer.valueOf(degree));
			return this.accepte && valid();
		}
	}

	static class BotMemoire extends Bot {
		int think;
		int done;
		int collisions;

		BotMemoire(Brain brain, Stunt stunt) {
			super(brain, stunt);
		}

		@Override
		public void think() {
			this.think++;
		}

		@Override
		public void done() {
			this.done++;
		}

		@Override
		public void collision(Entity entity) {
			this.collisions++;
		}
	}
	static final class StuntChoix extends Stunt {
		private final List<Integer> directions = new ArrayList<Integer>();
		private final Set<Integer> directionsAcceptees = new HashSet<Integer>();
		private boolean accepteTout;

		StuntChoix(Model model, Entity entity) {
			super(model, entity);
			this.accepteTout = true;
		}

		void accepteTout() {
			this.accepteTout = true;
			this.directionsAcceptees.clear();
		}

		void refuseTout() {
			this.accepteTout = false;
			this.directionsAcceptees.clear();
		}

		void accepteSeulement(int... directions) {
			this.accepteTout = false;
			this.directionsAcceptees.clear();

			for (int direction : directions) {
				this.directionsAcceptees.add(Integer.valueOf(direction));
			}
		}

		List<Integer> directions() {
			return new ArrayList<Integer>(this.directions);
		}

		int nbCommandes() {
			return this.directions.size();
		}

		int derniereDirection() {
			return this.directions.get(this.directions.size() - 1).intValue();
		}

		void clear() {
			this.directions.clear();
		}

		@Override
		public boolean walk(int degree) {
			this.directions.add(Integer.valueOf(degree));

			if (!valid()) {
				return false;
			}

			return this.accepteTout || this.directionsAcceptees.contains(Integer.valueOf(degree));
		}
	}

	static StuntChoix stunt(Model model, EntiteTest entity) {
		StuntChoix stunt = new StuntChoix(model, entity);
		entity.setStunt(stunt);
		return stunt;
	}

}
