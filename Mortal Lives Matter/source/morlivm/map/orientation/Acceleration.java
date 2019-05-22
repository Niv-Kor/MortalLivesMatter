package morlivm.map.orientation;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import morlivm.content.mortal.Mortal;
import morlivm.main.Structure;
import morlivm.map.Incline;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RangedDouble;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;
import morlivm.warfare.aftershock.Aftershock;
import morlivm.warfare.gauge.HealthBar;

public class Acceleration extends Motion
{
	private final static int ACCELERATION = 3, MAX_SPEED = 450;
	private final static RangedDouble DESCENTED_GLIDE = new RangedDouble(1, 10);
	private final static RangedDouble PLAIN_GLIDE = new RangedDouble(5, 22);
	private final static RangedDouble ASCENTED_GLIDE = new RangedDouble(50, 100);
	
	private HealthBar stamina;
	private boolean gliding, failure;
	private TimingDevice timingDevice;
	private Timer staminaDecrease;
	private Vector direction;
	private int baseSpeed, speedDifference;
	private double slowDown;
	private double[] slowDownRate;
	
	public Acceleration(Mortal mortal, HealthBar stamina, GameState gs) {
		this.gameState = gs;
		this.mortal = mortal;
		this.stamina = stamina;
		this.baseSpeed = mortal.getStats().agility;
		this.direction = Vector.NONE;
		this.timingDevice = new TimingDevice();
		this.staminaDecrease = new Timer("decrease", 0.16);
		timingDevice.addTimer(staminaDecrease);
		
		double glidingPercent = 100 - Structure.getDatabase().gliding;
		this.slowDownRate = new double[3];
		slowDownRate[0] = DESCENTED_GLIDE.percent(glidingPercent);
		slowDownRate[1] = PLAIN_GLIDE.percent(glidingPercent);
		slowDownRate[2] = ASCENTED_GLIDE.percent(glidingPercent);
	}

	public void update(double delta) {
		super.update(delta);
		if (!preperations) return;
		if (stamina.over()) enable(false);
		timingDevice.setClocks();
		
		int speed = mortal.getSpeed();
		
		if (gliding && speed <= baseSpeed) {
			direction = Vector.NONE;
			gliding = false;
		}
		
		if (isEnabled()) {
			if (speed < MAX_SPEED) speed += ACCELERATION;
			else if (speed >= MAX_SPEED) {
				speed = MAX_SPEED;
				enable(false);
			}
			
			if (staminaDecrease.progressedToRoof()) {
				stamina.decrease(1);
				staminaDecrease.init();
			}
		}
		else if (speed > baseSpeed) {
			if (mortal.isJumping()) fail();
			
			if (!failure) {
				if (!gliding && !failure) setupGlide(speed);
				
				//slow down
				speed -= Percent.percentOfNum(slowDown, speedDifference);
				
				if (speed < baseSpeed) {
					speed = baseSpeed;
					if (!closure) finish();
				}
			}
		}
		else finish();
		
		mortal.setSpeed(speed);
	}
	
	public void render(ExtendedGraphics2D g) {}
	
	private void setupGlide(int speed) {
		Incline inc = Topology.relevantIncline(mortal.getMidX());
		boolean inclined = inc.isInclinedTowards(mortal.getDirectX());
		speedDifference = speed - baseSpeed;
		
		//define slow down speed
		if (inclined) slowDown = slowDownRate[0];
		else if (inc.isPlain()) slowDown = slowDownRate[1];
		else slowDown = slowDownRate[2];
		
		//create dust effect
		//only when not running up the hill and speed is high enough
		if (slowDown != slowDownRate[2] && speedDifference >= (MAX_SPEED - baseSpeed) / 2) {
			Point backPoint;
			Aftershock dust;
			
			switch(mortal.getDirectX()) {
				case LEFT: backPoint = mortal.getLegs().getB(); break;
				case RIGHT: backPoint = mortal.getLegs().getA(); break;
				default: backPoint = mortal.getLegs().getA();
			}
		
			dust = Aftershock.Type.DUST.createInstance(backPoint, gameState);
			
			//flip
			if (backPoint.equals(mortal.getLegs().getA())) {
				Dimension originDim = dust.getDimension();
				Dimension newDim = new Dimension(originDim.width * -1, originDim.height);
				dust.setDimension(newDim);
			}
			
			dust.setX(dust.getX() - dust.getDimension().width / 3);
			dust.setY(dust.getY() - dust.getDimension().height / 2);
			gameState.getAftershockManager().addShock(dust);
		}
		
		gliding = true;
	}
	
	public void enable(boolean flag) {
		super.enable(flag);
		failure = false;
		if (flag) direction = mortal.getDirectX();
	}
	
	public void fail() {
		enable(false);
		gliding = false;
		failure = true;
		mortal.setSpeed(baseSpeed);
	}
	
	protected boolean start() {
		if (!super.start()) return false;
		
		direction = Vector.NONE;
		mortal.setSpeed(baseSpeed);
		failure = false;
		gliding = false;
		return true;
	}
	
	protected boolean finish() {
		if (!super.finish()) return false;
		
		mortal.setSpeed(baseSpeed);
		failure = false;
		gliding = false;
		return true;
	}
	
	public Vector getDirection() { return direction; }
	public boolean isGliding() { return gliding; }
	public boolean isEnabled() { return enabled; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}