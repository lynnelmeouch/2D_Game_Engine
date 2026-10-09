package game.model.entity;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;
import game.model.player.Player;

/**
 * Bonus ramassable par Pengo.
 */
public final class Power extends Obstacle {

    private PowerType type;

    public Power(Model model, String name, PowerType type) {
        super(model, name);
        this.type = type;

        double diameter = model.isu().cmPerCell() * 0.55;
        setSize(model.isu().new Dimension(diameter, diameter));

        setZOrder(2);
        setSOrder(0);

        updateBounding();
    }

    @Override
    protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
        Bounding result = new Bounding();
        result.add(new Circle(center, Math.min(size.x(), size.y()) / 2.0));
        return result;
    }

    public PowerType getType() {
        return this.type;
    }

    public void setType(PowerType type) {
        assert type != null;
        this.type = type;
    }

    public void apply(Player player) {
        assert player.model() == model();

        switch (this.type) {
        case SHIELD:
            player.activateShield();
            break;

        case SPEED:
            player.activateSpeed();
            break;

        case FREEZE:
            player.activateFreezePower();
            break;

        case STAR:
            player.activateStar();
            break;

        case EXTRA_LIFE:
            player.gainLife();
            break;

        case BOOMFISH:
        	player.explode();
        	break;

        case FRIEND_ACTIVATOR:
            player.activateFriendActivator();
            break;

        default:
            throw new AssertionError("Unhandled power type: " + this.type);
        }
    }
    
    @Override
    public boolean blocks(Entity other) {
        return false;
    }

}