package morlivm.content.loot;
import java.awt.Dimension;

import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.warfare.target.AimedTarget;

public class Key extends Loot
{
	public final static String PATH = "/sheets/general/key/";
	private final static Tune TUNE = new Tune("key", "/Sound/Loot/Key.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false);
	private final static Dimension DIM = new Dimension(50, 50);
	public final static int DROP_CHANCE = 100;
	
	private AimedTarget target;
	private boolean fly;
	
	public Key(Point point, GameState gs) {
		super(point, DIM, gs);
		
		this.grantEffectWhenStep = true;
		this.target = new AimedTarget(this, gs.getControlPanel().getKeyCounter());
		this.animation = new Animation(PATH + "key.png");
		this.pickTune = new Tune(TUNE);
		init();
	}
	
	public void update(double delta) {
		if (!fly) super.update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		if (!fly) super.render(g);
		else {
			target.slide(g, true);
			if (target.reach()) {
				gameState.getControlPanel().getKeyCounter().add();
				terminate();
			}
		}
	}
	
	public void grantEffect() {
		if (!fly) {
			musicBox.play(pickTune);
			fly = true;
		}
	}
	
	protected Tune getPickTune() { return TUNE; }
}