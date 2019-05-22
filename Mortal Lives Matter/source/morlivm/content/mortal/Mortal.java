package morlivm.content.mortal;
import morlivm.content.Entity;
import morlivm.database.Data;
import morlivm.database.LevelData;
import morlivm.database.MortalData;
import morlivm.main.Structure;
import morlivm.map.orientation.Acceleration;
import morlivm.map.orientation.Bounce;
import morlivm.map.orientation.Orientation;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.TableOriented;
import morlivm.warfare.aftershock.Aftershock;
import morlivm.warfare.damage_filter.BodyPart;
import morlivm.warfare.damage_filter.Element;
import morlivm.warfare.damage_filter.ElementStack;
import morlivm.warfare.damage_filter.ImmuneSystem;
import morlivm.warfare.damage_filter.Stats;
import morlivm.warfare.gauge.HealthBar;

public abstract class Mortal extends Entity
{
	protected MortalData db;
	protected LevelData ldb;
	protected TableOriented state;
	protected int initSpeed;
	protected HealthBar hp, stamina;
	protected Stats stats;
	protected BodyPart head, mass, legs;
	protected Bounce bounce;
	protected Acceleration acceleration;
	protected ElementStack elementStack;
	protected ImmuneSystem immuneSys;
	protected Orientation orientationControl;
	
	public Mortal(MortalData db, Point point, GameState gs) {
		super(point, db.size.dimension, db.stats.agility, gs);
		
		this.db = db;
		this.initSpeed = speed;
		this.head = new BodyPart(db.head, this);
		this.mass = new BodyPart(db.mass, this);
		this.legs = new BodyPart(db.legs, this);
		this.orientationControl = new Orientation(this);
		this.stats = new Stats(db.stats);
		this.ldb = Structure.getDatabase();
		
		//elements initialization
		Element[] tempElements = new Element[db.elements.length];
		for (int i = 0; i < tempElements.length; i++)
			tempElements[i] = new Element(db.elements[i]);
		
		this.elementStack = new ElementStack(this, tempElements);
	}
	
	public Mortal() {
		super();
	}
	
	protected void fixBodyParts(Point p) {
		head.fixPart(getDirectX(), p, false, false);
		mass.fixPart(getDirectX(), p, false, false);
		legs.fixPart(getDirectX(), p, false, false);
	}
	
	public void revive() {
		if (hp != null) hp.recover();
		if (stamina != null) stamina.recover();
	}
	
	public boolean isAtState(String s) {
		return state.name().equals(s);
	}
	
	protected void align() {}
	public abstract boolean isJumping();
	public double getMidX() { return getLegs().getA().getMidBetween(getLegs().getB()).getX(); }
	public double getZ() { return getLegs().getD().getY(); }
	public void setOriginSpeed() { speed = initSpeed; }
	public boolean isInStaticState() { return false; }
	public boolean isInDynamicState() { return false; }
	public void hurt(Mortal m, double power, Injury injury, Aftershock a, boolean allowSelf) {}
	public void setState(TableOriented type) {}
	public TableOriented getState() { return state; }
	public boolean isAlive() { return hp != null ? !hp.over() : true; } //it matters
	public boolean getCripplingHit(Collider collider) {return collider.touch(legs.getCollider()); }
	public boolean getHit(Collider collider) { return collider.touch(mass.getCollider()); }
	public boolean getCriticalHit(Collider collider) { return collider.touch(head.getCollider()); }
	public abstract boolean isWalkingHorizontally();
	public abstract boolean isWalkingVertically();
	public Collider getHead() { return head.getCollider(); }
	public Collider getMass() { return mass.getCollider(); }
	public Collider getLegs() { return legs.getCollider(); }
	public Stats getStats() { return stats; }
	public ImmuneSystem getImmuneSystem() { return immuneSys; }
	public ElementStack getElementStack() { return elementStack; }
	public Orientation getOrientationControl() { return orientationControl; }
	public Data getDatabase() { return db; }
}