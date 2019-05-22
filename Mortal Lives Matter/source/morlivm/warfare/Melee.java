package morlivm.warfare;
import java.awt.Dimension;
import morlivm.content.mortal.Mortal;
import morlivm.content.mortal.Player;
import morlivm.database.MortalData;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.math.Physics.Vector;
import morlivm.system.sound.Tune;

public class Melee
{
	public final static double SHORT = 4 / 5, LONG = 1;
	
	private ContactManager contactMngr;
	private Collider damageZone;
	private Mortal mortal;
	private DoT.Type effect;
	private GameState gameState;
	
	public Melee(Mortal mortal, double range, double strength, DoT.Type effect, Tune tune, GameState gs) {
		
		this.contactMngr = gs.getContactManager();
		this.mortal = mortal;
		this.gameState = gs;
		this.effect = effect;
		this.damageZone = createHitCollider(mortal, range);
		assault(range, strength, tune);
	}
	
	private void assault(double range, double strength, Tune tune) {
		double x, str;
		
		if (mortal instanceof Player) {
			if (range == SHORT) contactMngr.getAimedHit(mortal, damageZone, strength, null, false, false, tune);
			else if (range == LONG) contactMngr.inflictAimedAOE(mortal, damageZone, strength, tune);
		}
		else {
			x = (mortal.getDirectX() == Vector.RIGHT) ? mortal.getMidX() : mortal.getX() + mortal.getDimension().width;
			
			damageZone = new Collider(new Point(x, mortal.getY()),
								  	  new Dimension(Math.abs(mortal.getDimension().width / 2), mortal.getDimension().height));
			
			str = ((MortalData) mortal.getDatabase()).stats.offense;
			contactMngr.hit(mortal, gameState.getPlayer(), damageZone, str, true, effect, null);
		}
	}
	
	public static boolean canHit(Mortal offensive, Mortal defensive) {
		Collider hitCol = createHitCollider(offensive, LONG);
		return hitCol.touch(defensive.getHead()) ||
			   hitCol.touch(defensive.getMass()) ||
			   hitCol.touch(defensive.getLegs());
	}
	
	private static Collider createHitCollider(Mortal mortal, double range) {
		int width = mortal.getDimension().width;
		int height = mortal.getDimension().height;
		Point p = new Point(mortal.getMidX(), mortal.getY() + height / 3);
		Dimension dim = new Dimension((int) (range * width - width / 2), (int) (2 * height / 3));
		return new Collider(p, dim);
	}
}