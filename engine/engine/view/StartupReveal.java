package engine.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import engine.model.Entity;

/*
 * Animation d'intro :
 * les entités apparaissent ligne par ligne, de gauche à droite.
 *
 * Chaque entité a :
 * - un temps de départ ;
 * - une durée d'apparition ;
 * - une échelle de 0.0 à 1.0.
 */
public final class StartupReveal {

	private static final long DEFAULT_DELAY_BETWEEN_ENTITIES_MS = 25L;
	private static final long DEFAULT_ENTITY_REVEAL_DURATION_MS = 260L;

	private final Map<Entity, Long> startTimes;
	private final long delayBetweenEntitiesMs;
	private final long entityRevealDurationMs;

	private long elapsedMs;
	private long totalDurationMs;

	public StartupReveal(List<Entity> entities) {
		this(
			entities,
			DEFAULT_DELAY_BETWEEN_ENTITIES_MS,
			DEFAULT_ENTITY_REVEAL_DURATION_MS
		);
	}

	public StartupReveal(
		List<Entity> entities,
		long delayBetweenEntitiesMs,
		long entityRevealDurationMs
	) {
		assert delayBetweenEntitiesMs >= 0L;
		assert entityRevealDurationMs > 0L;

		this.delayBetweenEntitiesMs = delayBetweenEntitiesMs;
		this.entityRevealDurationMs = entityRevealDurationMs;
		this.startTimes = new IdentityHashMap<Entity, Long>();

		buildOrder(entities);
	}

	private void buildOrder(List<Entity> entities) {
		List<Entity> ordered = new ArrayList<Entity>();

		for (Entity entity : entities) {
			if (entity != null && entity.alive() && entity.installed()) {
				ordered.add(entity);
			}
		}

		/*
		 * Ordre arcade :
		 * - d'abord les lignes du haut vers le bas ;
		 * - puis dans chaque ligne, gauche vers droite ;
		 * - puis zOrder/sOrder pour rester stable.
		 */
		ordered.sort(new Comparator<Entity>() {
			@Override
			public int compare(Entity first, Entity second) {
				int firstY = first.position().y();
				int secondY = second.position().y();

				if (firstY != secondY) {
					return Integer.compare(firstY, secondY);
				}

				int firstX = first.position().x();
				int secondX = second.position().x();

				if (firstX != secondX) {
					return Integer.compare(firstX, secondX);
				}

				if (first.zOrder() != second.zOrder()) {
					return Integer.compare(first.zOrder(), second.zOrder());
				}

				return Integer.compare(first.sOrder(), second.sOrder());
			}
		});

		long start = 0L;

		for (Entity entity : ordered) {
			this.startTimes.put(entity, start);
			start += this.delayBetweenEntitiesMs;
		}

		this.totalDurationMs = start + this.entityRevealDurationMs;
	}

	public void tick(long elapsed) {
		assert elapsed >= 0L;

		if (finished()) {
			return;
		}

		this.elapsedMs += elapsed;

		if (this.elapsedMs > this.totalDurationMs) {
			this.elapsedMs = this.totalDurationMs;
		}
	}

	public boolean finished() {
		return this.elapsedMs >= this.totalDurationMs;
	}

	public boolean active() {
		return !finished();
	}

	public boolean visible(Entity entity) {
		return scale(entity) > 0.0;
	}

	public double scale(Entity entity) {
		Long start = this.startTimes.get(entity);

		if (start == null) {
			return 1.0;
		}

		long localTime = this.elapsedMs - start.longValue();

		if (localTime <= 0L) {
			return 0.0;
		}

		if (localTime >= this.entityRevealDurationMs) {
			return 1.0;
		}

		double progress = (double) localTime / (double) this.entityRevealDurationMs;

		return easeOut(progress);
	}

	private static double easeOut(double progress) {
		if (progress < 0.0) {
			progress = 0.0;
		}

		if (progress > 1.0) {
			progress = 1.0;
		}

		return 1.0 - Math.pow(1.0 - progress, 3.0);
	}
}