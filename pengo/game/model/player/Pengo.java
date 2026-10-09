package game.model.player;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.ISU;
import engine.model.Model;

public final class Pengo extends Player {

	private static final int nlife = 3;

	public Pengo(Model model) {
		this(model, "Pengo");
	}

	public Pengo(Model model, String name) {
		super(model, name, nlife);

		double diameter = model.isu().cmPerCell() * 0.80;

		setSize(model.isu().new Dimension(diameter, diameter));

		setZOrder(3);
		setSOrder(0);

		updateBounding();
	}

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();

		result.add(new Circle(center, Math.min(size.x(), size.y()) / 2.0));

		return result;
	}

	@Override
	protected void afterRespawn() {
		activateRespawnInvincibility();
	}
}