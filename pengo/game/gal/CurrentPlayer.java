package game.gal;

import engine.model.Entity;

public final class CurrentPlayer {

    private Entity currentPlayer;

    public CurrentPlayer(Entity initialPlayer) {
        this.currentPlayer = initialPlayer;
    }

    public Entity currentPlayer() {
        return this.currentPlayer;
    }

    public void setCurrentPlayer(Entity currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}