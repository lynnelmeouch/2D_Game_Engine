package game.model.slide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import game.model.entity.Enemies;

final class EnemyTrain {

	// bloc glissant qui transporte les ennemis
	private final Glissade owner;

	// liste des ennemis actuellement pousses par le bloc
	private final List<Enemies> carriedEnemies;

	// position de chaque ennemi dans le train.
	// Plusieurs ennemis peuvent avoir le meme slot si deux poissons sont dans la
	// meme case.
	private final Map<Enemies, Integer> carriedSlots;

	EnemyTrain(Glissade owner) {
		this.owner = owner;
		this.carriedEnemies = new ArrayList<Enemies>();
		this.carriedSlots = new HashMap<Enemies, Integer>();
	}

	boolean isEmpty() {
		return this.carriedEnemies.isEmpty();
	}

	int size() {
		return this.carriedEnemies.size();
	}

	boolean contains(Enemies enemy) {
		return this.carriedEnemies.contains(enemy);
	}

	// retourne le dernier ennemi du train
	Enemies lastEnemy() {
		if (this.carriedEnemies.isEmpty()) {
			return null;
		}

		return this.carriedEnemies.get(this.carriedEnemies.size() - 1);
	}

	void clear() {
		this.carriedEnemies.clear();
		this.carriedSlots.clear();
	}

	// retire les ennemis qui ne peuvent plus etre transportes
	void removeInvalidCarriedEnemies() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);

		for (Enemies enemy : copy) {
			if (!enemy.alive() || !enemy.installed()) {
				this.carriedEnemies.remove(enemy);
				this.carriedSlots.remove(enemy);
			}
		}
	}

	// gere le contact entre le bloc glissant et un ennemi
	boolean handleSlidingContact(Entity other) {
		if (!this.owner.isSliding() || this.owner.powerStop().active() || !(other instanceof Enemies)
				|| !other.alive()) {
			return false;
		}

		Enemies enemy = (Enemies) other;

		if (enemy.isCrushed()) {
			return false;
		}

		int slot = carriedSlot(enemy);

		// lennemi doit etre dans le train ou dans une zone que le train peut absorber.
		if (slot <= 0) {
			slot = findMatchingCarrySlot(enemy);
		}

		if (slot <= 0 && (isEnemyCrossingSlidingBody(enemy) || isEnemyInSolidTrainZone(enemy))) {
			slot = nearestSafeSlotFor(enemy);
		}

		if (slot <= 0) {
			return false;
		}

		prepareCarriedEnemy(enemy, slot);

		return true;
	}

	// met a jour les ennemis transportes pendant la glissade
	void carryEnemies() {
		catchEnemiesInFrontOfTrain();

		removeInvalidCarriedEnemies();

		if (this.carriedEnemies.isEmpty()) {
			return;
		}

		// cellule situee juste apres le dernier ennemi transporte
		Grid.Position afterTrain = this.owner.positionInFront(frontSlot() + 1);

		if (afterTrain == null) {
			crushCarriedEnemiesWithoutObstacle();
			return;
		}

		// verifie si un obstacle se trouve apres le train
		Entity obstacleAfterTrain = enemyStoppingObstacleAt(afterTrain);

		if (obstacleAfterTrain != null) {
			if (this.owner.isCrushingObstacle(obstacleAfterTrain)) {
				crushCarriedEnemies(obstacleAfterTrain);
			} else {
				Enemies lastEnemy = lastEnemy();
				this.owner.powerStop().start(lastEnemy, this.owner.powerStop().blockStopBeforePower(afterTrain),
						afterTrain.copy());
			}

			return;
		}

		placeCarriedEnemiesAsTrain();
	}

	// verifie les ennemis qui croisent la trajectoire du bloc
	void againstCrossingEnemies() {
		if (!this.owner.isSliding() || !this.owner.alive() || !this.owner.installed()) {
			return;
		}

		List<Entity> snapshot = this.owner.model().entities();

		for (Entity entity : snapshot) {
			if (!(entity instanceof Enemies)) {
				continue;
			}

			Enemies enemy = (Enemies) entity;

			if (!enemy.alive() || enemy.isCrushed() || this.carriedEnemies.contains(enemy)) {
				continue;
			}

			int slot = findMatchingCarrySlot(enemy);

			// capture lennemi sil est devant le train, meme si plusieurs poissons sont
			// dans la meme case. On ne stoppe plus la glissade sur un poisson :
			// un poisson dans le couloir doit etre emporte, pas traverser ni bloquer.
			if (!this.owner.powerStop().active() && slot > 0) {
				prepareCarriedEnemy(enemy, slot);
				continue;
			}

			if (!this.owner.powerStop().active()
					&& (isEnemyCrossingSlidingBody(enemy) || isEnemyInSolidTrainZone(enemy))) {
				prepareCarriedEnemy(enemy, nearestSafeSlotFor(enemy));
				continue;
			}
		}
	}

	// indique si un ennemi bloque le bloc pendant la glissade
	boolean blocksEnemyWhileSliding(Enemies enemy) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		if (this.carriedEnemies.contains(enemy)) {
			return false;
		}

		return isEnemyInSolidTrainZone(enemy);
	}

	// ajoute un ennemi au train et le fige.
	// Important : si l'ennemi est deja dans le train, on conserve son slot.
	// Sinon, pendant l'arret special devant un Power, un poisson deja au slot 1
	// etait remis au slot 2, ce qui faisait arreter le glacon a power - 2.
	void prepareCarriedEnemy(Enemies enemy) {
		int existingSlot = carriedSlot(enemy);

		if (existingSlot > 0) {
			prepareCarriedEnemy(enemy, existingSlot);
			return;
		}

		prepareCarriedEnemy(enemy, Math.max(1, frontSlot() + 1));
	}

	// ajoute un ennemi a un slot precis du train.
	// Deux poissons peuvent partager le meme slot quand ils etaient sur la meme
	// case.
	void prepareCarriedEnemy(Enemies enemy, int slot) {
		if (enemy == null || slot <= 0) {
			return;
		}

		if (!this.carriedEnemies.contains(enemy)) {
			this.carriedEnemies.add(enemy);
		}

		this.carriedSlots.put(enemy, Integer.valueOf(slot));

		enemy.freeze();
		enemy.setLinearSpeed(this.owner.model().isu().new Vector(0.0, 0.0));
		enemy.setAngularSpeed(0.0);
	}

	// place chaque ennemi devant le bloc pour former un train
	void placeCarriedEnemiesAsTrain() {
		for (int i = 0; i < this.carriedEnemies.size(); i++) {
			Enemies enemy = this.carriedEnemies.get(i);

			if (enemy.alive() && enemy.installed()) {
				enemy.freeze();
				enemy.setLinearSpeed(this.owner.model().isu().new Vector(0.0, 0.0));
				enemy.setAngularSpeed(0.0);
				this.owner.model().place(enemy, carriedEnemyCenter(carriedSlotOrDefault(enemy, i + 1)));
			}
		}
	}

	// relache un ennemi a une position sure
	void releaseCarriedEnemy(Enemies enemy, ISU.Coord safeCenter) {
		this.carriedEnemies.remove(enemy);
		this.carriedSlots.remove(enemy);

		if (!enemy.alive()) {
			return;
		}

		if (safeCenter != null && enemy.installed()) {
			this.owner.model().place(enemy, safeCenter);
		}

		enemy.releaseAfterBeingCarried();
	}

	// relache tous les ennemis transportes
	void releaseCarriedEnemies() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();
		this.carriedSlots.clear();

		for (Enemies enemy : copy) {
			if (enemy.alive()) {
				enemy.releaseAfterBeingCarried();
			}
		}
	}

	// relache les ennemis apres larret special devant un power
	void releaseCarriedEnemiesAfterSmoothPowerStop(Grid.Position blockStopPosition) {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (!enemy.alive() || !enemy.installed()) {
				this.carriedEnemies.remove(enemy);
				this.carriedSlots.remove(enemy);
				continue;
			}

			Grid.Position finalPosition = this.owner.positionInFrontFrom(blockStopPosition,
					carriedSlotOrDefault(enemy, i + 1));
			ISU.Coord safeCenter = finalPosition == null ? enemy.center() : finalPosition.toISUCoordCentered();

			releaseCarriedEnemy(enemy, safeCenter);
		}
	}

	// ecrase les ennemis si le train sort de la grille
	void crushCarriedEnemiesWithoutObstacle() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (enemy.alive()) {
				enemy.crushAt(this.owner.slideDirection(), carriedEnemyCenter(carriedSlotOrDefault(enemy, i + 1)));
			}
		}

		this.carriedSlots.clear();
	}

	// ecrase les ennemis contre un obstacle
	void crushCarriedEnemies(Entity obstacle) {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (enemy.alive()) {
				enemy.crushBetweenAt(this.owner, obstacle, this.owner.slideDirection(),
						carriedEnemyCenter(carriedSlotOrDefault(enemy, i + 1)));
			}
		}

		this.carriedSlots.clear();
	}

	// capture les ennemis directement alignes devant le train
	private void catchEnemiesInFrontOfTrain() {
		boolean added;

		do {
			added = false;

			int nextSlot = frontSlot() + 1;
			List<Enemies> enemies = carryableEnemiesAtSlot(nextSlot);

			for (Enemies enemy : enemies) {
				prepareCarriedEnemy(enemy, nextSlot);
				added = true;
			}
		} while (added);
	}

	// cherche tous les ennemis transportables sur un slot donne.
	// Important : si deux poissons sont dans la meme case, on les prend tous au
	// meme slot.
	private List<Enemies> carryableEnemiesAtSlot(int slot) {
		List<Enemies> result = new ArrayList<Enemies>();
		Grid.Position position = this.owner.positionInFront(slot);

		if (position == null) {
			return result;
		}

		Grid.Cell cell = this.owner.model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other instanceof Enemies && other.alive()) {
				Enemies enemy = (Enemies) other;

				if (!enemy.isCrushed() && !this.carriedEnemies.contains(enemy)
						&& isCarryCandidateAtIndex(enemy, slot)) {
					result.add(enemy);
				}
			}
		}

		return result;
	}

	private int carriedSlot(Enemies enemy) {
		Integer slot = this.carriedSlots.get(enemy);
		return slot == null ? 0 : slot.intValue();
	}

	private int carriedSlotOrDefault(Enemies enemy, int fallback) {
		int slot = carriedSlot(enemy);
		return slot <= 0 ? fallback : slot;
	}

	int frontSlot() {
		int result = 0;

		for (Integer slot : this.carriedSlots.values()) {
			if (slot != null && slot.intValue() > result) {
				result = slot.intValue();
			}
		}

		return result;
	}

	private int findMatchingCarrySlot(Enemies enemy) {
		int maxSlot = frontSlot() + 1;

		for (int slot = 1; slot <= maxSlot; slot++) {
			if (isCarryCandidateAtIndex(enemy, slot)) {
				return slot;
			}
		}

		return 0;
	}

	private int nearestSafeSlotFor(Enemies enemy) {
		if (enemy == null) {
			return Math.max(1, frontSlot() + 1);
		}

		ISU.Vector towardEnemy = this.owner.center().mkVectorToward(enemy.center());
		double forward = this.owner.geometry().forwardProjection(towardEnemy, this.owner.slideDirection());
		double cell = this.owner.model().isu().cmPerCell();

		int slot = (int) Math.round(forward / cell);

		if (slot < 1) {
			slot = 1;
		}

		int maxSlot = Math.max(1, frontSlot() + 1);

		if (slot > maxSlot) {
			slot = maxSlot;
		}

		return slot;
	}

	// verifie si lennemi est bien place pour etre ajoute au train
	private boolean isCarryCandidateAtIndex(Enemies enemy, int expectedIndex) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed() || expectedIndex <= 0) {
			return false;
		}

		Grid.Position expectedPosition = this.owner.positionInFront(expectedIndex);

		if (expectedPosition == null) {
			return false;
		}

		ISU.Coord expectedCenter = expectedPosition.toISUCoordCentered();
		ISU.Vector towardExpected = this.owner.center().mkVectorToward(expectedCenter);
		ISU.Vector towardEnemy = this.owner.center().mkVectorToward(enemy.center());

		double expectedForward = this.owner.geometry().forwardProjection(towardExpected, this.owner.slideDirection());
		double enemyForward = this.owner.geometry().forwardProjection(towardEnemy, this.owner.slideDirection());
		double enemyLateral = Math
				.abs(this.owner.geometry().lateralProjection(towardEnemy, this.owner.slideDirection()));

		double cell = this.owner.model().isu().cmPerCell();

		return Math.abs(enemyForward - expectedForward) <= cell * 0.78 && enemyLateral <= cell * 0.46;
	}

	// verifie si un ennemi traverse le corps du bloc
	private boolean isEnemyCrossingSlidingBody(Enemies enemy) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		if (this.owner.sameGridCell(enemy) || enemy.bounding().intersects(this.owner.bounding())) {
			return true;
		}

		return isEnemyInBlockBody(enemy);
	}

	// verifie si un ennemi est dans la zone physique du bloc
	private boolean isEnemyInBlockBody(Enemies enemy) {
		if (enemy == null || !this.owner.isSliding()) {
			return false;
		}

		ISU.Vector towardEnemy = this.owner.center().mkVectorToward(enemy.center());
		double forward = this.owner.geometry().forwardProjection(towardEnemy, this.owner.slideDirection());
		double lateral = Math.abs(this.owner.geometry().lateralProjection(towardEnemy, this.owner.slideDirection()));
		double cell = this.owner.model().isu().cmPerCell();

		return forward >= -cell * 0.72 && forward <= cell * 0.86 && lateral <= cell * 0.48;
	}

	// verifie si un ennemi se trouve dans la zone occupee par le train
	private boolean isEnemyInSolidTrainZone(Enemies enemy) {
		if (enemy == null || !this.owner.isSliding() || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		ISU.Vector towardEnemy = this.owner.center().mkVectorToward(enemy.center());
		double forward = this.owner.geometry().forwardProjection(towardEnemy, this.owner.slideDirection());
		double lateral = Math.abs(this.owner.geometry().lateralProjection(towardEnemy, this.owner.slideDirection()));
		double cell = this.owner.model().isu().cmPerCell();

		double rearLimit = -cell * 0.72;
		double frontLimit = cell * (Math.max(0, frontSlot()) + 0.92);

		return forward >= rearLimit && forward <= frontLimit && lateral <= cell * 0.48;
	}

	// calcule le centre dun ennemi transporte selon sa position dans le train
	private ISU.Coord carriedEnemyCenter(int distanceInCells) {
		return this.owner.center().mkTranslated(this.owner.geometry().forwardOffset(this.owner.slideDirection(),
				this.owner.model().isu().cmPerCell() * distanceInCells));
	}

	// cherche un obstacle qui doit arreter le train
	private Entity enemyStoppingObstacleAt(Grid.Position position) {
		if (position == null) {
			return null;
		}

		Grid.Cell cell = this.owner.model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other != this.owner && other.alive() && isEnemyStoppingObstacle(other)) {
				return other;
			}
		}

		return null;
	}

	// obstacles qui arretent les ennemis transportes
	private boolean isEnemyStoppingObstacle(Entity other) {
		return this.owner.isCrushingObstacle(other);
	}
}