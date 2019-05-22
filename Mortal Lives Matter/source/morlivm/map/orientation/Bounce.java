package morlivm.map.orientation;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import morlivm.content.mortal.Mortal;
import morlivm.map.Magnet;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Percent;
import morlivm.system.math.RangedDouble;

public class Bounce extends Motion
{
	private final static RangedDouble LEAP_LIM = new RangedDouble(1, 12);
	
	private double height, newHeight;
	private Point originHead, originLegs;
	private Magnet magnet;
	private Stack<Double> leaps;
	private boolean land, primaryEntity, hiddenBodyParts;
	
	public Bounce(Mortal mortal, double height, boolean primaryEntity, GameState gs) {
		this.mortal = mortal;
		this.gameState = gs;
		this.height = newHeight = height * 2;
		this.magnet = new Magnet();
		this.leaps = new Stack<Double>();
		this.primaryEntity = primaryEntity;
	}

	protected boolean start() {
		if (!super.start()) return false;
		
		height = newHeight;
		hiddenBodyParts = mortal.getHead().isHidden();
		mortal.getHead().hide(true);
		mortal.getMass().hide(true);
		mortal.getLegs().hide(true);
		leaps.clear();
		originHead = new Point(mortal.getPoint());
		originLegs = new Point(mortal.getMidX(), mortal.getLegs().getD().getY());
		attachToMagnet();
		return true;
	}
	
	protected boolean finish() {
		if (!super.finish()) return false;
		
		mortal.setY(originHead.getY());
		mortal.getHead().hide(hiddenBodyParts);
		mortal.getMass().hide(hiddenBodyParts);
		mortal.getLegs().hide(hiddenBodyParts);
		magnet.attachY();
		magnet.close();
		enable(false);
		land = false;
		return true;
	}
	
	public void update(double delta) {
		super.update(delta);
		if (!preperations) return;
		
		double remain, calculatedLeap = 0;
		double z = mortal.getLegs().getD().getY();
		
		//calculate the height of the next upper or lower leap
		while (calculatedLeap == 0) {
			if (!land) {
				remain = z - (originLegs.getY() - height);
				calculatedLeap = Percent.percentOfNum(Percent.numOfNum(remain, height), LEAP_LIM.getMax());
				
				if (Math.abs(calculatedLeap) < LEAP_LIM.getMin()) {
					calculatedLeap = LEAP_LIM.getMin();
					land = true;
				}
				
				leaps.push(calculatedLeap);
			}
			else calculatedLeap = (!leaps.isEmpty()) ? leaps.pop() : LEAP_LIM.getMax();
		}
		
		//perform jump
		if (isEnabled() && z > originLegs.getY() - height && !land) {
			mortal.setZ(z - calculatedLeap);
			magnet.translateY(calculatedLeap);
			translateZ(calculatedLeap);
		}
		else if (z < originLegs.getY()) {
			land = true;
			enable(false);
			
			//check that mortal doesn't exceed the last leap
			//if he does, soften the leap
			//reasoning behind this is to prevent a bump when landing
			double diff = originLegs.getY() - z;
			if (calculatedLeap > diff) calculatedLeap = Math.ceil(diff);
			
			mortal.setZ(z + calculatedLeap);
			magnet.translateY(-calculatedLeap);
			translateZ(-calculatedLeap);
		}
		else finish();
	}

	public void render(ExtendedGraphics2D g) {}
	
	private void attachToMagnet() {
		if (primaryEntity) {
			magnet.add(gameState.getSpawnManager(), false);
			magnet.add(gameState.getDamageManager(), false);
			magnet.add(gameState.getAftershockManager(), true);
			magnet.add(gameState.getArena(), false);
			magnet.add(gameState.getStainManager(), true);
			magnet.remove(mortal);
		}
	}
	
	public void translateZ(double y) {
		originHead.setY(getY() + y);
		originLegs.setY(getZ() + y);
	}
	
	public void setHeight(double h) { newHeight = h; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
	public void fail() { land = true; }
	public double getY() { return originHead.getY(); }
	public double getZ() { return originLegs.getY(); }
	public double getHeight() { return height; }
}