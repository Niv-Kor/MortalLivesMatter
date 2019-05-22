package morlivm.warfare.aftershock;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Quake extends Aftershock
{
	public final static Tune SFX = new Tune("explosion", "/Sound/Main/SFX/Explosion.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false);

	public Quake(Animation spriteTicker, Point point, GameState gs) {
		super(spriteTicker, point, gs);
		musicBox.put(SFX);
		musicBox.export();
	}
	
	protected void init() {
		super.init();
		musicBox.play(SFX);
	}
	
	public void update(double delta) {
		if (!init) init();
		
		if (!gameState.getArena().getGround().isQuaking())
			gameState.getArena().getGround().setEarthquake(10, true);
		else terminate();
	}
	
	public void render(ExtendedGraphics2D g) {}
}