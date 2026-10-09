package game.model.entity;

import engine.model.Grid;
import engine.model.Model;
import game.config.PengoRunState;
import game.model.slide.Glissade;

public final class Spawner extends Glissade {
	
	public static final long ALIGNMENT_EFFECT_DURATION_MS = 15000L;
	private static final long ALIGNMENT_COLOR_FRAME_MS = 110L;

	private long alignmentEffectEnd_ms;

	public Spawner(Model model, String name) {
		super(model, name, 1, 2);
		this.alignmentEffectEnd_ms = 0L;
	}

	@Override
	public void setRunState(PengoRunState runState) {
		super.setRunState(runState);
	}

	@Override
	protected boolean onPushBlocked(int direction, Grid.Position target) {
		/* Le spawner a le comportement de glissade du glaçon, mais ne se casse jamais. */
		return false;
	}

	@Override
	public String debugLabel() {
		return slideDebugLabel("Spawner");
	}
	
	public void activateAlignmentEffect() {
	    activateAlignmentEffect(ALIGNMENT_EFFECT_DURATION_MS);
	}

	public void activateAlignmentEffect(long durationMs) {
	    assert durationMs > 0L;
	    this.alignmentEffectEnd_ms = gameTimeMs() + durationMs;
	}

	public boolean alignmentEffectActive() {
	    return alive() && gameTimeMs() < this.alignmentEffectEnd_ms;
	}

	public int alignmentColorFrame() {
	    if (!alignmentEffectActive()) {
	        return 0;
	    }

	    return (int) ((gameTimeMs() / ALIGNMENT_COLOR_FRAME_MS) % 7L);
	}
}