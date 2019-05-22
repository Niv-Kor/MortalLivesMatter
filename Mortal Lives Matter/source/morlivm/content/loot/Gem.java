package morlivm.content.loot;
import java.awt.Dimension;

import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.math.RNG;
import morlivm.system.math.RangedInt;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Gem extends Loot
{
	private final static Tune TUNE = new Tune("gem", "/Sound/Loot/Gem.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false);
	public final static int DROP_CHANCE = 100;
	private final static int MP = 0, HP = 1;
	private final static Dimension DIM = new Dimension(48, 41);
	private final static RangedInt VALUE = new RangedInt(50, 100);
	private final static String[] PATHS = {"/sheets/general/gem/blue.png",
	   									   "/sheets/general/gem/red.png"};

	private int type, value;
	
	public Gem(Point point, GameState gs) {
		super(point, DIM, gs);
		
		this.grantEffectWhenStep = true;
		this.type = RNG.generate(0, 1);
		this.value = VALUE.generate();
		this.animation = new Animation(PATHS[type]);
		this.pickTune = new Tune(TUNE);
		init();
	}
	
	public void grantEffect() {
		super.grantEffect();

		switch (type) {
			case MP: player.getStamina().increase(value);
			case HP: player.getHP().increase(value);
		}
	}
}