package morlivm.content;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import morlivm.content.mortal.SpawnManager;
import morlivm.content.proj.RicochetManager;
import morlivm.database.Data;
import morlivm.map.Attachable;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.math.Physics.Vector;
import morlivm.system.performance.TimingDevice;
import morlivm.system.sound.MusicBox;
import morlivm.warfare.ContactManager;

public abstract class Entity implements Attachable
{
	public static enum Size {
		SMALL(new Dimension(180, 180)),
		MEDIUM(new Dimension(300, 300)),
		LARGE(new Dimension(430, 430)),
		XLARGE(new Dimension(512, 512)),
		HUGE(new Dimension(700, 700));
		
		public Dimension dimension;
		
		private Size(Dimension dim) {
			this.dimension = dim;
		}
	}
	
	public static enum Genus {
		EARTH, AERO, AQUEOUS, AMPHIBIAN
	}
	
	public static enum Injury {
		UNCONCERNED, DIZZY, KNOCKED_OUT;
	}
	
	protected Point point;
	protected Dimension dim;
	protected int speed, transparency;
	protected Vector directX, directY;
	protected GameState gameState;
	protected SpawnManager spawnMngr;
	protected RicochetManager ricoMngr;
	protected ContactManager contactMngr;
	protected Animation animation;
	protected TimingDevice timingDevice;
	protected MusicBox musicBox;
	protected Data db;
	
	public Entity(Point point, Dimension dim, int speed, GameState gs) {
		this.gameState = gs;
		this.spawnMngr = gs.getSpawnManager();
		this.ricoMngr = gs.getRicochetManager();
		this.contactMngr = gs.getContactManager();
		this.dim = new Dimension(dim);
		this.speed = speed;
		this.timingDevice = new TimingDevice();
		this.musicBox = new MusicBox();
		this.point = new Point(point);
		this.directX = Vector.RIGHT;
		this.directY = Vector.DOWN;
		this.transparency = 0xFF;
	}
	
	public Entity() {}
	
	public void setDirectX(Vector d) {
		boolean oppose = d != directX && d != Vector.NONE;
		directX = d;
		if (oppose) {
			setX(getX() + dim.width);
			dim.width *= -1;
		}
	}
	
	public void update(double delta) {
		timingDevice.setClocks();
	}
	
	public void fixSprite() {
		if ((getDirectX() == Vector.RIGHT && dim.width < 0)
		 || (getDirectX() == Vector.LEFT && dim.width > 0)) {
			setX(getX() + dim.width);
			dim.width *= -1;
		}
	}
	
	public void setTransparency(int trans) {
		if (transparency >= 0 && transparency <= 0xFF) transparency = trans;
	}
	
	public void terminate(boolean cry) {
		musicBox.requestRemovalAll();
		gameState.getEpidemicManager().remove(this);
	}
	
	public BufferedImage getImage() { return animation.getImage(); };
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return point; }
	public Point getFixedPoint() { return getPoint(); }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public void setZ(double z) { point.setY(z); }
	public double getMidX() { return getX() + getDimension().width / 2; }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public double getZ() { return point.getY() + dim.height - 20; }
	public int getSpeed() { return speed; }
	public void setSpeed(int s) { speed = s; }
	public Vector getDirectX() { return directX; }
	public Vector getDirectY() { return directY; }
	public void setDirectY(Vector d) { directY = d; }
	public Data getDatabase() { return db; }
	public int compareTo(Entity other) { return (int) (getZ() - other.getZ()); }
	public boolean isAlive() { return true; }
}