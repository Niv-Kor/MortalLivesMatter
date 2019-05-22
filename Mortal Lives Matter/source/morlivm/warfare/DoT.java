package morlivm.warfare;
import java.awt.Dimension;

import morlivm.content.Entity;
import morlivm.content.mortal.Mortal;
import morlivm.database.DataManager;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Percent;
import morlivm.system.performance.TimingDevice;

public class DoT implements Graphable
{
	public static enum Type {
		NONE(null, 0, 0, 0),
		POISON("ss$g$poison", 0.55, 20, 45),
		BURN("ss$g$burn", 0.4, 15, 65);
		
		public int contagion;
		public double duration, power;
		public Pamphlet pamphlet;
		public Dimension dim;
		
		private Type(String spriteCode, double power, double duration, int contagion) {
			if (spriteCode != null) {
				this.pamphlet = DataManager.retSheet(spriteCode);
				this.duration = duration;
				this.contagion = (int) Percent.limit(contagion);
				this.dim = new Dimension(pamphlet.getSprite().getDimension());
				this.power = power;
			}
		}
	}
	
	private final static double HURT_PULSE = 2;
	
	private boolean doNothing;
	private Type type;
	private Mortal mortal;
	private Point point;
	private int pulses;
	private double strength;
	private TimingDevice timingDevice;
	private Animation animation;
	private GameState gameState;
	
	public DoT(Mortal mortal, Type type, double strength, GameState gs) {
		this.mortal = mortal;
		this.type = type;
		
		if (type == Type.NONE) doNothing = true;
		
		if (!doNothing) {
			this.strength = strength * type.power;
			this.pulses = (int) (type.duration / HURT_PULSE);
			this.gameState = gs;
			this.point = new Point();
			this.animation = new Animation(type.pamphlet);
			this.timingDevice = new TimingDevice();
			timingDevice.addTimer("hurt", HURT_PULSE);
			gameState.getEpidemicManager().add(mortal);
		}
	}
	
	public void update(double delta) {
		if (doNothing) return;
		
		if (pulses == 0) {
			mortal.getImmuneSystem().heal();
			return;
		}

		hurt();
		updatePoint();
		timingDevice.setClocks();
		animation.update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		if (doNothing) return;
		
		if (!mortal.isAlive()) return;
		g.drawImage(animation.getImage(), point, type.dim);
	}
	
	private void updatePoint() {
		if (doNothing) return;
		
		setX(mortal.getHead().getX() + mortal.getHead().getDimension().width / 3);
		setY(mortal.getHead().getY() - type.dim.height / 2);
	}
	
	private void hurt() {
		if (doNothing) return;
		
		if (timingDevice.getTimer("hurt").progressedToRoof()) {
			mortal.hurt(gameState.getPlayer(), strength, Entity.Injury.UNCONCERNED, null, true);
			pulses--;
			timingDevice.getTimer("hurt").init();
		}
	}
	
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return type.dim; }
	public Point getPoint() { return point; }
	public Type getType() { return type; }
	public double getStrength() { return strength; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}