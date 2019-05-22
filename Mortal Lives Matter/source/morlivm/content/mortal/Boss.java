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
import morlivm.system.graphics.TableOriented;
import morlivm.system.math.Physics;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RNG;
import morlivm.warfare.aftershock.Aftershock;
import morlivm.warfare.gauge.BossHP;
import morlivm.warfare.target.Fortress;

public class Boss extends Enemy
{
	private final static int SILENT_PERIOD = 3;
	private final static int ATTACK_PERIOD = 15;
	private final static int SLEEP_PERIOD = 60;
	private final static int ATTACK_PULSE = 5;
	
	private boolean sleep, spawnSilent;
	
	public Boss(MortalData db, Point point, GameState gameState) {
		super(db, new Point(point), gameState);
	}
	
	public Boss() {
		super();
	}
	
	public LoadedSectionsQueue upload() {
		super.upload();
		
		this.hp = new BossHP(this, db.stats.health);
		this.target = new Fortress(this, player, gameState);
		this.spawnSilent = true;
		staticState = true;
		timingDevice.addTimer("attack", ATTACK_PULSE);
		timingDevice.addTimer("spawn", SILENT_PERIOD);
		timingDevice.addTimer("sleep", SLEEP_PERIOD);
		setState(State.IDLE);
		
		return null;
	}
	
	public void update(double delta) {
		super.update(delta);
		
		if (!SpawnManager.isActivated()) return;
		if (spawnSilent && timingDevice.getTimer("spawn").progressedToRoof()) {
			spawnSilent = false;
			sleep(false);
		}
		
		if (isAlive()) {
			if (sleep) {
				if (timingDevice.getTimer("sleep").progressedToRoof()
				|| (!((Fortress) target).areThereMinions() && !((Fortress) target).creatingMinions()))
					sleep(false);
				
				if (!((Fortress) target).areThereMinions()) gameState.getSpawnManager().spawnMinions();;
			}
			else if (!spawnSilent) {
				((Fortress) target).follow(delta);
				if (confrontsPlayer()) requestAttack();
				if (timingDevice.getTimer("sleep").progressedToRoof()) sleep(true);
			}
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		align();
	}
	
	public void setState(TableOriented type) {
		if (type == state) return;
		
		if (state != type) {
			if (type == State.WALK) musicBox.resumeLoop("breath");
			else if (type == State.HURT) musicBox.stop("breath");
		}
		
		state = type;
		if (state == State.SPAWN  || state == State.IDLE || state == State.HURT || state == State.DIE
		||  state == State.ATTACK_1 || state == State.ATTACK_2 || state == State.ATTACK_3) {
			speed = 0;
			staticPoint = new Point(getPoint());
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

		if (getY() < Topology.topLim(getMidX()) - getZ() - 20) setY(Topology.topLim(getMidX()) - getZ() - 20);
		if (!Magnet.isActive() && getZ() > Topology.bottomLim(getMidX())) setZ(Topology.bottomLim(getMidX()));
		if (getDirectX() == Vector.RIGHT && getX() + dim.width > ground.getX() + ldb.rightWall)
			setX(ground.getX() + ldb.rightWall - dim.width);
	}
	
	public void hurt(Mortal offensive, double percentage, Injury injury, Aftershock bleed, boolean allowSelf) {
		super.hurt(offensive, percentage, Injury.UNCONCERNED, null, false);
	}
	
	public double getZ() {
		int standingZ = (int) legs.getCollider().getD().getY();
		return isAlive() ? standingZ : (int) (standingZ - dim.height / 10); 
	}
	
	public void sleep(boolean flag) {
		TableOriented state = (flag) ? State.IDLE : State.WALK;
		int time = (flag) ? SLEEP_PERIOD : ATTACK_PERIOD;
		sleep = flag;
		setState(state);
		staticState = false;
		timingDevice.getTimer("attack").init();
		timingDevice.getTimer("sleep").renew(time);
	}
	
	public void requestAttack() {
		if (timingDevice.getTimer("attack").progressedToRoof()) {
			super.requestAttack();
			timingDevice.getTimer("attack").init();
		}
	}
	
	public void terminate(boolean cry) {
		super.terminate(cry);
		((Fortress) target).eliminateMinions();
	}
	
	public boolean confrontsPlayer() {
		Point p, e;
		p = new Point(player.getX() + player.getDimension().width / 2, 0);
		e = new Point(getX() + getDimension().width / 2, 0);
		
		return getDirectX() == Physics.Vector.RIGHT && e.smallerThan(p, Physics.Axis.X, 0)
			|| getDirectX() == Physics.Vector.LEFT && e.largerThan(p, Physics.Axis.X, 0);
	}
	
	protected void drop() {
		int tempX = 0, tempY = 0;
		int coinAmount = RNG.generate(20, 30);
		int lootAmount = RNG.generate(8, 15);
		
		for (int i = 1; i <= coinAmount; i++) {
			tempX = RNG.generate((int) getX() - Math.abs(dim.width),
								 (int) getX() + Math.abs(dim.width)) + getDimension().width / 2;
			
			tempY = RNG.generate((int) Topology.topLim(tempX / 2),
								 (int) Topology.bottomLim(tempX) - 60);
			
			spawnMngr.createCoinLoot(new Point(tempX, tempY));
		}
		
		for (int i = 1; i <= lootAmount; i++) {
			if (RNG.unstableCondition(100)) {
				tempX = RNG.generate((int) getX() - Math.abs(dim.width),
									 (int) getX() + Math.abs(dim.width)) + getDimension().width / 2;
				
				tempY = RNG.generate((int) Topology.topLim(tempX),
									 (int) Topology.bottomLim(tempX) - 60);
				
				spawnMngr.createDropLoot(new Point(tempX, tempY));
			}
		}

		spawnMngr.createSpecialLoot(new Point(tempX, tempY), Loot.QuestItem.CHEST, 100);
		dropped = true;
	}
	
	public void showComponents(boolean flag) {
		elementStack.show(true);
		nameTag.show(false);
		hp.show(true);
	}
	
	public void setZ(double z) { setY(z - (legs.getCollider().getD().getY() - getY())); }
}