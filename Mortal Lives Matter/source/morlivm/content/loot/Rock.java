package morlivm.content.loot;
import java.awt.Dimension;

import morlivm.database.DataManager;
import morlivm.database.ProjectileData;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;

public class Rock extends Loot
{
	private ProjectileData projDataBase;
	
	public Rock(Point point, GameState gs) {
		super(point, new Dimension(0, 0), gs);
		
		this.grantEffectWhenStep = true;
		this.projDataBase = DataManager.retProj("Rock", 1);
		this.animation = new Animation(projDataBase.spriteSheet.getSprite().grabSprite(1, 1));
		this.dim = new Dimension(projDataBase.size);
	}
	
	public void grantEffect() {
		musicBox.play("general loot");
		gameState.getControlPanel().getAmmoManager().add(projDataBase);
		terminate();
	}
}