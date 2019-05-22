package morlivm.warfare.aftershock;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Dust extends Aftershock
{
	public final static Tune SFX = new Tune("dust", "/Sound/Main/SFX/Dust.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false);

	public Dust(Animation spriteTicker, Point point, GameState gs) {
		super(spriteTicker, point, gs);
		musicBox.put(SFX);
		musicBox.export();
	}
	
	protected void init() {
		super.init();
		musicBox.play(SFX);
	}
}