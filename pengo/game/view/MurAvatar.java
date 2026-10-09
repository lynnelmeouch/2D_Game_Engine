package game.view;

import engine.view.ImageAvatar;
import engine.view.View;
import game.model.entity.Mur;

/** Avatar d'un mur fixe. */
public final class MurAvatar extends ImageAvatar {

	public MurAvatar(View view, Mur mur) {
		super(view, mur, new String[] { "pengo_rsrc/wall.png" });
	}

	@Override
	protected boolean rotateWithEntity() {
		return false;
	}
}