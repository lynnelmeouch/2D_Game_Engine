package game.model.entity;

import engine.model.Grid;
import engine.model.Model;
import game.config.PengoRunState;
import game.model.slide.Glissade;
import oop.tasks.Runnable;

public final class Glacon extends Glissade {

	private boolean breaking;

	public Glacon(Model model, String name) {
		super(model, name, 1, 1);
	}

	@Override
	public void setRunState(PengoRunState runState) {
		super.setRunState(runState);
	}

	@Override
	protected boolean onPushBlocked(int direction, Grid.Position target) {
		breakIce();
		return true;
	}

	/** Détruit le glaçon lorsque Pengo appuie sur Espace contre un obstacle. */
	public void breakIce() {
		if (!alive() || this.breaking) {
			return;
		}

		stopSliding();

		this.breaking = true;

		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);

		oop.tasks.Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (stoppedByRunState()) {
					return;
				}

				if (pausedByRunState()) {
					oop.tasks.Runtime.post(this, 50);
					return;
				}

				if (alive()) {
					death();
				}
			}
		}, (int) 250L);
	}

	public boolean isBreaking() {
		return this.breaking;
	}

	public static double collisionSizeFactor() {
		return Glissade.collisionSizeFactor();
	}

	@Override
	public String debugLabel() {
		if (this.breaking) {
			return "Ice BREAK";
		}

		return slideDebugLabel("Ice");
	}
}