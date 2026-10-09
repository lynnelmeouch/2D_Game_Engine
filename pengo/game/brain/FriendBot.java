package game.brain;

import java.util.List;
import java.util.Random;

import engine.brain.Bot;
import engine.brain.Brain;
import engine.model.BasicStunt;
import engine.model.Entity;
import game.model.player.Friend;

// Bot simple de l'allié : il choisit une direction praticable au hasard.
//Inutile quand comportement gal
public final class FriendBot extends Bot {

    private static final int[] DIRECTIONS = { 0, 90, 180, 270 };

    private final Friend friend;
    private final Random random;

    public FriendBot(Brain brain, BasicStunt stunt, Friend friend) {
        super(brain, stunt);

        assert friend != null;
        assert stunt.entity() == friend;

        this.friend = friend;
        this.random = new Random();
    }

    @Override
    public void think() {
        if (!valid() || this.friend.isControlled() || this.friend.isMoving()) {
            return;
        }

        int first = this.random.nextInt(DIRECTIONS.length);

        for (int offset = 0; offset < DIRECTIONS.length; offset++) {
            int direction = DIRECTIONS[(first + offset) % DIRECTIONS.length];

            if (this.stunt.walk(direction)) {
                return;
            }
        }
    }

    @Override
    public void done() {
        think();
    }

    @Override
    public void collision(Entity other) {
        assert other != null;
        think();
    }

    @Override
    public void collision(List<Entity> others) {
        assert others != null;
        think();
    }
}