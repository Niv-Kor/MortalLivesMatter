package morlivm.content.loot;
import java.awt.Dimension;

import morlivm.database.MortalData;
import morlivm.database.ProjectileData;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Ammunition extends Loot
{
	private final static Tune TUNE = new Tune("gunload", "/Sound/Loot/Gunload.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false);
	private final static Dimension DIM = new Dimension(68, 65);
	public final static int DROP_CHANCE = 100;
	
	private ProjectileData projDataBase;
	
	public Ammunition(Point point, GameState gs) {
		super(point, DIM, gs);
		
		this.grantEffectWhenStep = true;
		this.projDataBase = ((MortalData) player.getDatabase()).arsenal.generateCollectableProj();
		this.animation = new Animation("/sheets/general/ammunition/ammunition.png");
		this.pickTune = new Tune(TUNE);
		init();
	}
	
	public void grantEffect() {
		super.grantEffect();
		gameState.getControlPanel().getAmmoManager().add(projDataBase);
	}
}