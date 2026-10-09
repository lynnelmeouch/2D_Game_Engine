package game.model.entity;

import engine.model.Bounding;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Rect;

/** Mur fixe occupant une cellule. */
public final class Mur extends Entity {

    public Mur(Model model, String name) {
        super(model, name);

        double cell = model.isu().cmPerCell();
        setSize(model.isu().new Dimension(cell, cell));

        setZOrder(1);
        setSOrder(0);

        updateBounding();
    }

    @Override
    protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
        Bounding result = new Bounding();
        result.add(new Rect(center, size, orientationDegree));
        return result;
    }

    @Override
    public boolean blocks(Entity other) {
        assert other != null;
        return true;
    }
}