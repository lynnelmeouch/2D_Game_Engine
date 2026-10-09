package game.view;

import engine.view.ImageAvatar;
import engine.view.View;
import game.model.entity.Spawner;

public final class SpawnerAvatar extends ImageAvatar {


    private final Spawner spawner;

    private static final String[] COLOR_FRAMES = new String[] {
            "pengo_rsrc/spawner_bleu.png",
            "pengo_rsrc/spawner_green.png",
            "pengo_rsrc/spawner_jaune.png",
            "pengo_rsrc/spawner_orange.png",
            "pengo_rsrc/spawner_pink.png",
            "pengo_rsrc/spawner_purple.png",
            "pengo_rsrc/spawner_rouge.png"
    };

    public SpawnerAvatar(View view, Spawner spawner) {
        super(view, spawner, COLOR_FRAMES);
        this.spawner = spawner;
    }

    @Override
    public void tick(long elapsed) {
        super.tick(elapsed);
        if (this.spawner.alignmentEffectActive()) {
            setCurrentFrame(this.spawner.alignmentColorFrame() % frameCount());
        } else {
            setCurrentFrame(0);
        }
    }

	@Override
	protected boolean rotateWithEntity() {
		return false;
	}
}