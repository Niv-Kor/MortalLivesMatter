package morlivm.warfare.sight;
import java.awt.Dimension;

import morlivm.state.GameState;
import morlivm.system.graphics.ExtendedGraphics2D;

public class UnarmedSight extends Sight
{
	public final static String PATH = "/sheets/sight/default.png";
	public final static Dimension DIM = new Dimension(45, 45);
	
	public UnarmedSight(SightInventory sightInv, GameState gs) {
		super(sightInv, gs);
		this.currentSprite = sightInv.getDefSightImg();
	}
	
	public void update(double delta) {
		super.update(delta);
		updateCoordinates(DIM);
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		if (!show) return;
		
		g.drawImage(currentSprite,
				   (int) circularPoint.getX(),
				   (int) circularPoint.getY(),
				   UnarmedSight.DIM.width, UnarmedSight.DIM.height, null);
	}
	
	public SightInventory.Type getSightType() { return SightInventory.Type.UNARMED; }
}