package morlivm.warfare.target;
import morlivm.content.mortal.EarthEnemy;
import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.Mortal;
import morlivm.main.Structure;
import morlivm.map.orientation.Topology;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.math.Physics.Axis;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RNG;
import morlivm.system.performance.Timer;
import morlivm.warfare.Melee;

public class Lynch extends MobBehaviour
{
	private boolean confused, dizzy;
	private GameState gameState;
	
	public Lynch(Mortal mortal, Mortal followee, GameState gs) {
		super(mortal, followee);
		this.gameState = gs;
		this.follower = (Mortal) mortal;
	}
	
	public void generateTarget() {
		int rngX, rngY;;
		
		do rngX = RNG.generate(0, Structure.getDatabase().rightWall);
		while (rngX > followee.getX() + 300 || rngX < followee.getX() - 300);
		
		rngY = RNG.generate((int) Topology.topLim(rngX), (int) Topology.bottomLim(rngX));
		
		point = new Point(rngX, rngY);
		timingDevice.addTimer("restless", RNG.generateDouble(1.5, 5));
	}
	
	public void follow(double delta) {
		if (!follower.isAlive()) return;
		
		timingDevice.setClocks();
		manageConfusion();
		
		Point entMid = new Point(follower.getMidX(), follower.getZ());
		boolean wallEncounter = follower.getDirectX() == Vector.LEFT
							 && entMid.getX() <= gameState.getArena().getGround().getX();
		
		boolean bottomBoundaryEncounter = follower.getZ() >= Topology.bottomLim(follower.getMidX());
		boolean topBoundaryEncounter = follower.getZ() <= Topology.topLim(follower.getMidX());
		
		//change direction
		if (!((Enemy) follower).isInStaticState()) {
			//try to attack
			if (!confused && ((Enemy) follower).canAttack() && Melee.canHit(follower, followee)) {
				((EarthEnemy) follower).requestAttack();
				if (entMid.smallerThan(point, Axis.X, 60)) follower.setDirectX(Vector.RIGHT);
				else if (entMid.largerThan(point, Axis.X, 60)) follower.setDirectX(Vector.LEFT);
			}
			
			else if (entMid.largerThan(point, Axis.X, 60)) follower.setDirectX(Vector.LEFT);
			else if (entMid.smallerThan(point, Axis.X, 60) || wallEncounter) follower.setDirectX(Vector.RIGHT);
			
			//same height
			if (entMid.withinRange(point, Axis.Y, 20)) follower.setDirectY(Vector.NONE);
			
			//move enemy freely towards the target
			else {
				//higher than target
				if (point.getY() > follower.getZ() + 30 && !bottomBoundaryEncounter)
					follower.setDirectY(Vector.DOWN);
				
				//lower than target
				else if (point.getY() < follower.getZ() - 30 && !topBoundaryEncounter)
					follower.setDirectY(Vector.UP);
			}
		}
	}
	
	private void manageConfusion() {
		boolean isEnemy = follower instanceof Enemy;
		Timer restless = timingDevice.getTimer("restless");
		Timer standing = timingDevice.getTimer("standing");
		Timer dizziness = timingDevice.getTimer("dizzy");
		
		if (dizziness != null && dizziness.progressedToRoof()) {
			timingDevice.removeTimer("dizzy");
			dizzy = false;
		}
		
		if (!confused) {
			if (followee.isAlive()) setTarget(followee);
			else generateTarget();
			
			if (isEnemy && (((Mortal) follower).isInDynamicState() || ((Mortal) follower).getState() == Enemy.State.IDLE))
			   ((Enemy) follower).setState(Enemy.State.WALK);
			
			timingDevice.removeTimer("standing");
			timingDevice.removeTimer("restless");
		}
		else if (isEnemy) {
			if (restless == null && standing == null) generateTarget();
			if (standing == null && restless != null) restless.play();
			if (standing != null && restless == null) standing.play();
			
			if (restless != null && restless.reachRoof()) {
				((Enemy) follower).setState(Enemy.State.IDLE);
				timingDevice.removeTimer("restless");
				timingDevice.addTimer("standing", RNG.generateDouble(1.2, 4));
			}
			
			if (standing != null && standing.reachRoof()) {
				timingDevice.removeTimer("standing");
				confused = false;
				((Enemy) follower).setState(Enemy.State.WALK);
				confused = true;
				generateTarget();
			}
		}
	}
	
	public void setTarget(Mortal e) {
		super.setTarget(e);
		confused = false;
	}
	
	public void setDizziness(boolean flag, double time) {
		dizzy = flag;
		setConfusion(flag);
		if (dizzy) timingDevice.addTimer("dizzy", time);
	}
	
	public boolean setConfusion(boolean flag) {
		if (!follower.isAlive()) {
			dizzy = false;
			confused = false;
			return false;
		}
		if (((Mortal) follower).getState() == Enemy.State.SPAWN) return false;
		if ((flag && confused) || (!flag && dizzy)) return false; 
		
		confused = flag;
		
		if (flag) {
			timingDevice.addTimer("standing", RNG.generateDouble(1.2, 4));
			((Enemy) follower).setState(Enemy.State.IDLE);
		}
		else {
			timingDevice.removeTimer("standing");
			timingDevice.removeTimer("restless");
		}
		
		return flag;
	}
	
	public boolean getDizziness() { return dizzy; }
	public boolean getConfusion() {	return confused; }
}