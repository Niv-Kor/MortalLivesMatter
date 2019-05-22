package morlivm.content.mortal;
import morlivm.database.MortalData;
import morlivm.map.Magnet;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.TableOriented;
import morlivm.system.math.Physics.Vector;
import morlivm.warfare.aftershock.Aftershock;
import morlivm.warfare.target.Lynch;

public class EarthEnemy extends Enemy
{
	public EarthEnemy(MortalData db, Point point, GameState gameState) {
		super(db, new Point(point), gameState);
	}
	
	public EarthEnemy() {
		super();
	}
	
	public LoadedSectionsQueue upload() {
		super.upload();
		
		this.target = new Lynch(this, player, gameState);
		timingDevice.addTimer("attack", 0.083);
		setState(State.SPAWN);
		
		return null;
	}
	
	public void update(double delta) {
		super.update(delta);
		
		if (state == State.HURT && animation.lastTick()) setState(State.WALK);
		if (timingDevice.getTimer("dying") != null) if (!dropped) drop();
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
	}
	
	public void setState(TableOriented type) {
		if (type == state) return;
		if (((Lynch) target).getConfusion() && isAlive() && state == State.IDLE) return;
		
		if (state != type) {
			if (type == State.WALK) musicBox.resumeLoop("breath");
			else if (type == State.HURT) musicBox.stop("breath");
		}
		
		state = type;
		
		if (state == State.SPAWN  || state == State.IDLE || state == State.HURT || state == State.DIE ||
		    state == State.ATTACK_1 || state == State.ATTACK_2 || state == State.ATTACK_3) {
			speed = 0;
			
			if (!(((Lynch) target).getConfusion() && state == State.IDLE)) {
				staticPoint = new Point(getPoint());
				staticState = true;
			}
		}
		else {
			speed = initSpeed;
			staticState = false;
		}
		
		animation.setRow(state.getRow(), true);
	}
	
	public void requestAttack() {
		if (timingDevice.getTimer("attack").progressedToRoof()) {
			super.requestAttack();
			timingDevice.getTimer("attack").init();
		}
	}
	
	protected void align() {
		if (!isAlive()) return;
		
		Ground ground = gameState.getArena().getGround();
		
		if (getZ() < Topology.topLim(getMidX())) setZ(Topology.topLim(getMidX()));
		if (!Magnet.isActive() && getZ() > Topology.bottomLim(getMidX())) setZ(Topology.bottomLim(getMidX()));
		if (getDirectX() == Vector.RIGHT && getX() + dim.width > ground.getX() + ldb.rightWall)
			setX(ground.getX() + ldb.rightWall - dim.width);
	}
	
	public void hurt(Mortal offensive, double percentage, Injury injury, Aftershock bleed, boolean allowSelf) {
		super.hurt(offensive, percentage, injury, bleed, false);
		
		((Lynch) target).setDizziness(false, 0);
		
		switch (injury) {
			case UNCONCERNED: break;
			case KNOCKED_OUT: setState(State.HURT); break;
			case DIZZY: {
				((Lynch) target).setDizziness(true, 1.5);
				setState(State.IDLE);
				break;
			}
		}
	}
	
	public void setRulerEntity(Player p) {
		super.setRulerEntity(p);
		target = new Lynch(this, player, gameState);
	}
	
	public double getZ() {
		int standingZ = (int) super.getZ();
		return isAlive() ? standingZ : (int) (standingZ - dim.height / 10); 
	}
	
	public void setZ(double z) { setY(z - (getZ() - getY())); }
}