package morlivm.content.mortal;
import morlivm.database.MortalData;
import morlivm.main.Game;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.TableOriented;
import morlivm.system.math.Physics;
import morlivm.system.math.RNG;
import morlivm.warfare.target.Ambush;

public class AeroEnemy extends Enemy
{
	private double deathSpot;
	
	public AeroEnemy(MortalData db, Point point, GameState gameState) {
		super(db, new Point(point.getX(), -db.size.dimension.height), gameState);
	}
	
	public AeroEnemy() {
		super();
	}
	
	public LoadedSectionsQueue upload() {
		super.upload();
		
		this.target = new Ambush(this, player, gameState);
		timingDevice.addTimer("breath", RNG.generateDouble(0, 2));
		setDirectY(Physics.Vector.DOWN);
		setState(State.WALK);
		
		return null;
	}
	
	public void update(double delta) {
		super.update(delta);
		
		if (isAlive() && timingDevice.getTimer("breath") != null) {
			if (timingDevice.getTimer("breath").progressedToRoof()) {
				musicBox.loop("breath");
				timingDevice.removeTimer("breath");
			}
		}
		
		if (!isAlive()) {
			if (getY() <= deathSpot) {
				setY(getY() + 10);
				staticPoint = new Point(getPoint());
			}
			else if (!dropped) drop();
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		align();
	}
	
	public void setState(TableOriented type) {
		if (type == state) return;
		
		state = type;
		if (state == State.DIE) {
			speed = 0;
			staticState = true;
		}
		else {
			speed = initSpeed;
			staticState = false;
		}
		
		animation.setRow(state.getRow(), true);
	}
	
	protected void align() {
		Ground ground = gameState.getArena().getGround();
		double barrier = ground.getClosedBarrierX();
		
		if (barrier != -1 && getDirectX() == Physics.Vector.RIGHT && getX() >= barrier - 70)
			setX(barrier - 70);
		
		if (getDirectX() == Physics.Vector.RIGHT && getX() + dim.width > ground.getX() + ldb.rightWall)
			setX(ground.getX() + ldb.rightWall - dim.width);
	}
	
	public void generateDeathSpot() {
		boolean hovering = getY() + dim.height / 2 < Topology.topLim(getMidX());
		
		if (((Ambush) target).isAttackingWithinRange()) deathSpot = getY() + 60;
		else if (hovering) {
			deathSpot = RNG.generate((int) Topology.topLim(getMidX()) + 20,
									 (int) Topology.bottomLim(getMidX()) - dim.height / 2);
		}
		else deathSpot = RNG.generate((int) getY(), (int) Topology.bottomLim(getMidX()) - dim.height / 2);
	}
	
	public boolean isAttackingWithinRange() { return ((Ambush) target).isAttackingWithinRange(); }
	public double getZ() { return (isAlive()) ? Game.HEIGHT - 50 : legs.getCollider().getD().getY(); }
	public void setZ(double z) { setY(z - (mass.getCollider().getD().getY() - getY())); }
}