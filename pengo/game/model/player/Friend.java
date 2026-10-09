package game.model.player;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.ISU;
import engine.model.Model;

/**
 * Allié de Pengo. Il peut être piloté par un bot ou marqué comme contrôlé.
 */
public final class Friend extends Player {

	private static final int nlife = 1;

	public Friend(Model model) {
		this(model, "Friend");
	}

	public Friend(Model model, String name) {

		super(model, name, nlife);

		double diameter = model.isu().cmPerCell() * 0.75;
		setSize(model.isu().new Dimension(diameter, diameter));
		setStep(model.isu().new Dimension(model.isu().cmPerCell(), model.isu().cmPerCell()));

		setZOrder(3);
		setSOrder(1);

		updateBounding();
	}

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();
		result.add(new Circle(center, Math.min(size.x(), size.y()) / 2.0));
		return result;
	}

}