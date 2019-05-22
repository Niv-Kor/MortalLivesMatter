package morlivm.content.proj;
import morlivm.content.Entity;
import morlivm.content.mortal.Mortal;
import morlivm.content.mortal.Player;
import morlivm.control_panel.Megaphone;
import morlivm.database.ProjectileData;
import morlivm.main.Game;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Physics;
import morlivm.system.performance.FPS;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.warfare.DoT;
import morlivm.warfare.target.AimedTarget;

public abstract class Projectile extends Entity
{
	public static enum MainType { BULLET, BOMB };
	public static enum SubType { AIM, STRAIGHT, PLANT, ROLL };
	public static enum Trigger { COLLISION, STEP, IGNITION, TIME };
	
	protected boolean running;
	protected double strength;
	protected Collider collider;
	protected DoT.Type conditionType;
	protected Mortal offensive;
	protected AimedTarget target;
	protected RicochetManager ricoMngr;
	protected Collider pinpoint;
	protected ProjectileData db;
	protected Tune[] sound;
	
	public Projectile(Mortal offensive, double strength, ProjectileData pdb,
					  int projHeight, GameState gs, Point deviation) {
		
		super(new Point(), pdb.size, FPS.toFrames(pdb.speed), gs);
		
		this.db = pdb;
		this.offensive = offensive;
		this.ricoMngr = gs.getRicochetManager();
		
		//sprite properties
		this.directX = offensive.getDirectX();
		if (Game.getMouseInput().getY() <= offensive.getY() + offensive.getDimension().height / 2)
			this.directY = Physics.Vector.DOWN;
		else this.directY = Physics.Vector.UP;
			
		this.dim.width *= directX.straight();
		this.strength = strength;
		this.animation = new Animation(db.spriteSheet);
		
		if (directX == Physics.Vector.RIGHT)
			setX(offensive.getX() + offensive.getDimension().width / 2 * directX.straight() + deviation.getX());
		
		else setX(offensive.getX() + dim.width + deviation.getX());
		setY(offensive.getY() + projHeight + deviation.getY());
		this.collider = new Collider(point, dim);
		
		this.running = true;
		this.conditionType = db.mayhem;
		
		init(pdb.subType);
	}
	
	public Projectile() {
		super();
	}
	
	protected abstract void init(SubType subType);
	
	public boolean failureCheck() {
		Player p;
		
		if (offensive instanceof Player) {
			p = (Player) offensive;
			if (!p.getStamina().decrease(db.cost)) {
				Megaphone.announce("Insufficient stamina.");
				return false;
			}
		}
		return true;
	}
	
	protected boolean reachedEndPoint() {
		return (getDirectX() == Physics.Vector.RIGHT && getX() + dim.width / 2 >= target.getX() + 100) ||
			   (getDirectX() == Physics.Vector.LEFT && getX() - dim.width / 2 <= target.getX() + 100)
			   || getY() >= Game.HEIGHT;
	}
	
	public void playSound() {
		Tune rand = musicBox.getTune(Sound.Clique.P_LAUNCH);
		
		if (rand != null) {
			if (rand.isLoopable()) musicBox.loop(rand);
			else musicBox.play(rand);
		}
	}
	
	public void setX(double x) {
		super.setX(x);
		if (collider != null) collider.setX(x);
	}
	
	public void setY(double y) {
		super.setY(y);
		if (collider != null) collider.setY(y);
	}
	
	public boolean touch(Collider cursorCollider) { return collider.touch(cursorCollider); }
	protected abstract void moveTowardsTarget(double delta);
	protected abstract void moveTowardsTarget(ExtendedGraphics2D g);
	public int getState() {	return -1; }
	public ProjectileData getDatabase() { return db; }
}