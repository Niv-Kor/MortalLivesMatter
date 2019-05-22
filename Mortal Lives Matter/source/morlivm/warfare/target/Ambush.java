package morlivm.warfare.target;
import java.util.Random;

import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.Mortal;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RNG;
import morlivm.system.performance.Timer;

public class Ambush extends MobBehaviour
{
	private final static int ATTACK_DURATION = 400;
	
	private Ground ground;
	private Random rng;
	private boolean following, attWithinRng;
	
	public Ambush(Mortal mortal, Mortal followee, GameState gs) {
		super(mortal, followee);
		
		this.ground = gs.getArena().getGround();
		this.rng = new Random();
		timingDevice.addTimer("target generator", RNG.generate(1, 2) + rng.nextDouble());
		timingDevice.addTimer("assault", RNG.generate(0, 16) + rng.nextDouble());
		generateTarget();
	}
	
	public void generateTarget() {
		point.setX(RNG.generate(ground.getMinSpawnArea(), ground.getMaxSpawnArea()));
		point.setY(RNG.generate(-2 * entity.getDimension().height, (int) Topology.topLim(point.getX())) - 20);
	}
	
	public void follow(double delta) {
		if (!entity.isAlive()) return;
		
		Timer assault = timingDevice.getTimer("assault");
		Timer generator = timingDevice.getTimer("target generator");
		boolean isEnemy = entity instanceof Enemy;
		double entityZ = ((Mortal) entity).getMass().getD().getY();
		double barrier = ground.getClosedBarrierX();
		Point entMid = new Point(entity.getX() + entity.getDimension().width / 2, entityZ);
		boolean wallEncounter = entity.getDirectX() == Vector.LEFT && entMid.getX() <= ground.getX();
		
		timingDevice.setClocks();
		if (assault.progressedToRoof() || (following && assault.reach(ATTACK_DURATION))) {
			following = !following;
			assault.renew(RNG.generate(0, 16) + rng.nextDouble());
		}
		
		if (following && followee.isAlive()) setTarget(followee);
		else {
			if (generator.progressedToRoof()) {
				generateTarget();
				generator.init();
			}
		}
		
		//change direction
		if (!((Enemy) entity).isInStaticState()) {
			
			if ((point.getX() + 100 < entity.getX()	|| point.getX() <= ground.getX() + 10
				|| (barrier != -1 && entity.getDirectX() == Vector.RIGHT && entity.getX() >= barrier - 70))
				&& !wallEncounter)
				entity.setDirectX(Vector.LEFT);

			else if (point.getX() > entity.getX() + 100 || wallEncounter) entity.setDirectX(Vector.RIGHT);
			
			if (!following) {
				if (entityZ >= Topology.topLim(entity.getX()) - 20) entity.setDirectY(Vector.UP);
				else if (entityZ < -2 * entity.getDimension().height) entity.setDirectY(Vector.DOWN);
			}
			else {
				//higher than target
				if (point.getY() > entityZ + 50) entity.setDirectY(Vector.DOWN);
				//lower than target
				else if (point.getY() < entityZ - 50) entity.setDirectY(Vector.UP);
			}
		}
		
		if (isEnemy) {
			if (following && ((Enemy) entity).canAttack()) {
				if (entMid.withinRange(point, 100)) {
					attWithinRng = true;
					((Enemy) entity).requestAttack();
				}
				else {
					attWithinRng = false;
					((Enemy) entity).setState(Enemy.State.WALK);
				}
			}
		}
	}
	
	public boolean isAttackingWithinRange() { return attWithinRng; }
}