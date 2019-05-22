package morlivm.content.loot;
import morlivm.database.DataManager;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.RNG;
import morlivm.system.math.RangedInt;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.warfare.target.AimedTarget;

public class Coin extends Loot implements Graphable
{
	private final static Tune TUNE = new Tune("coin", "/Sound/Loot/Coin.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false);
	private final static RangedInt[] RANGE = {new RangedInt(10, 50), new RangedInt(50, 110)};
	private final static Pamphlet[] SS = {DataManager.retSheet("ss$l$silver_coin"),
										  DataManager.retSheet("ss$l$gold_coin")};
	
	private int type, value, side;
	private boolean fly;
	private AimedTarget target;
	
	public Coin(Point point, GameState gs) {
		super(point, SS[0].getSprite().getDimension(), gs);
		
		this.grantEffectWhenStep = true;
		this.type = RNG.generate(0, 1);
		this.value = RANGE[type].generate();
		this.side = RNG.generate(1, 2);
		this.animation = new Animation(SS[type]);
		this.target = new AimedTarget(this, player.getCurrency());
		this.pickTune = new Tune(TUNE);
		animation.setRow(side, true);
		animation.randomize();
		init();
	}
	
	public void update(double delta) {
		if (!fly) super.update(delta);
		if (!show) return;
		animation.update(delta);
		flip();
	}
	
	public void render(ExtendedGraphics2D g) {
		if (!fly) super.render(g);
		else {
			target.slide(g, true);
			if (target.reach()) terminate();
		}
	}
	
	public void grantEffect() {
		if (!fly) {
			musicBox.play(pickTune);
			player.getCurrency().earn(value);
			fly = true;
		}
	}
	
	private void flip() {
		int newSide = (side == 1) ? 2 : 1;
		if (animation.lastTick()) animation.setRow(newSide, true);
	}
	
	protected Tune getPickTune() { return TUNE; }
}