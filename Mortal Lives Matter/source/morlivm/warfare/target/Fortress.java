package morlivm.warfare.target;
import java.util.LinkedList;
import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.Minion;
import morlivm.content.mortal.Mortal;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.math.Physics;

public class Fortress extends MobBehaviour
{
	private final static double SPAWN_DELAY = 2.5;
	
	private GameState gameState;
	private LinkedList<Minion> mList;
	private boolean creatingMinions;
	
	public Fortress(Mortal entity, Mortal followee, GameState gs) {
		super(entity, followee);
		this.gameState = gs;
		this.mList = new LinkedList<Minion>();
		timingDevice.addTimer("spawn", SPAWN_DELAY);
	}
	
	public void follow(double delta) {
		if (!entity.isAlive()) return;
		setTarget(followee);
		timingDevice.setClocks();
		
		Point entMid = new Point(entity.getX() + entity.getDimension().width / 2, entity.getZ());
		boolean wallEncounter = entity.getDirectX() == Physics.Vector.LEFT
							 && entMid.getX() <= gameState.getArena().getGround().getX();
		
		//change direction
		if (!((Enemy) entity).isInStaticState()) {
			
			if (entMid.largerThan(point, Physics.Axis.X, 60))
				entity.setDirectX(Physics.Vector.LEFT);
			else if (entMid.smallerThan(point, Physics.Axis.X, 60) || wallEncounter) {
				entity.setDirectX(Physics.Vector.RIGHT);
			}
			
			//same height
			if (entMid.withinRange(point, Physics.Axis.Y, 20)) entity.setDirectY(Physics.Vector.NONE);
			
			//higher than target
			else if (point.getY() > entity.getZ() + 30) entity.setDirectY(Physics.Vector.DOWN);
			
			//lower than target
			else if (point.getY() < entity.getZ() - 30) entity.setDirectY(Physics.Vector.UP);
		}
	}
	
	public void eliminateMinions() {
		for (int i = 0; i < mList.size(); i++)
			mList.get(i).terminate(true);
	}
	
	public boolean areThereMinions() {
		boolean remainingMinions = mList.size() > 0;
		
		if (remainingMinions) {
			for (int i = 0; i < mList.size(); i++)
				if (!mList.get(i).isAlive()) mList.remove(i);
		}
		
		return remainingMinions;
	}
	
	public boolean creatingMinions() { return creatingMinions; }
}