package morlivm.content.mortal;
import morlivm.content.loot.Loot;
import morlivm.database.MortalData;
import morlivm.map.Magnet;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Physics;
import morlivm.system.math.RNG;

public class Minion extends EarthEnemy
{
	private final static int FALLING_SPEED = 800;
	
	private int fallingSpot;
	private boolean onGround;
	
	public Minion(MortalData db, Point point, GameState gs) {
		super(db, new Point(point), gs);
	}
	
	public LoadedSectionsQueue upload() {
		super.upload();
		
		state = State.JUMP;
		super.setDirectX(Physics.Vector.NONE);
		generateFallingSpot();
		hp.show(false);
		
		return null;
	}
	
	public void update(double delta) {
		super.update(delta);
		
		if (!onGround) {
			if (getY() < fallingSpot) {
				speed = FALLING_SPEED;
				super.setDirectY(Physics.Vector.DOWN);
				setY(getY() + delta * speed * getDirectY().straight());
				staticState = true;
				if ((player.isWalkingHorizontally() || player.isJumpingForward())
				   && gameState.getArena().getGround().getImpactZone())
					if (staticState) setX(getX() + delta * player.getSpeed() * player.getUserDirect(Physics.Axis.X).straight() * -1);
						
				return;
			}
			else {
				hp.show(true);
				speed = db.stats.agility;
				onGround = true;
				setDirectX(Physics.Vector.RIGHT);
				fixSprite();
				setState(State.WALK);
			}
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
	}
	
	protected void align() {
		Ground ground = gameState.getArena().getGround();

		if (onGround) if (getY() < Topology.topLim(getX()) - getZ()) setY(Topology.topLim(getX()) - getZ());
		if (!Magnet.isActive() && getZ() > Topology.bottomLim(getX())) setZ(Topology.bottomLim(getX()));
		if (getDirectX() == Physics.Vector.RIGHT && getX() + dim.width > ground.getX() + ldb.rightWall)
			setX(ground.getX() + ldb.rightWall - dim.width);
	}
	
	public void generateFallingSpot() {
		fallingSpot = RNG.generate((int) Topology.topLim(getX()) - dim.height,
								   (int) Topology.bottomLim(getX()) - dim.height);
	}
	
	public void setDirectY(Physics.Vector direction) {
		if (!onGround) return;
		else super.setDirectY(direction);
	}
	
	public void setDirecX(Physics.Vector direction) {
		if (!onGround) return;
		else super.setDirectX(direction);
	}
	
	protected void drop() {
		int tempX, tempY;
		int lootAmount = RNG.generate(1, 4);
		
		for (int i = 1; i <= lootAmount; i++) {
			if (RNG.unstableCondition(30)) {
				tempX = RNG.generate((int) getX() - 60, (int) getX() + 60) + getDimension().width / 2;
				tempY = RNG.generate((int) getY() - 30, (int) getY() + 30) + getDimension().height - 40;
				spawnMngr.createDropLoot(new Point(tempX, tempY));
			}
		}
		
		tempX = RNG.generate((int) getX() - 60, (int) getX() + 60) + getDimension().width / 2;
		tempY = RNG.generate((int) getY() - 30, (int) getY() + 30) + getDimension().height - 40;
		spawnMngr.createSpecialLoot(new Point(tempX, tempY), Loot.QuestItem.ROCK, 50);
		dropped = true;
	}
}