package morlivm.content.loot;
import java.awt.Dimension;
import morlivm.content.Entity;
import morlivm.content.mortal.Player;
import morlivm.map.orientation.Topology;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Physics;
import morlivm.system.math.RNG;
import morlivm.system.performance.Timer;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public abstract class Loot extends Entity
{
	public static enum QuestItem {
		ROCK(Rock.class),
		CHEST(ElementChest.class);
		
		public Class<?> relatedClass;
		
		private QuestItem(Class<?> c) {
			this.relatedClass = c;
		}
	}
	
	private final static int FLOAT_RANGE = 20;
	protected final static Tune GENERAL_TUNE = new Tune("general loot", "/Sound/Main/SFX/loot.wav",
														Sound.Clique.GENERAL, Sound.Genre.SFX, false);
	
	protected Player player;
	protected int transparency;
	protected boolean show, step, floating, grantEffectWhenStep;
	protected double initY, topY;
	protected Tune pickTune;
	
	public Loot(Point point, Dimension dim, GameState gs) {
		super(point, dim, 20, gs);
		
		this.player = gs.getPlayer();
		this.initY = getY();
		this.topY = initY - FLOAT_RANGE;
		timingDevice.addTimer("appear", RNG.generateDouble(0, 1.5));
		musicBox.put(GENERAL_TUNE);
	}
	
	public Loot() {
		super();
	}
	
	protected void init() {
		musicBox.put(pickTune);
		musicBox.export();
	}
	
	public void update(double delta) {
		contactMngr.collect();
		timingDevice.setClocks();
		Timer appear = timingDevice.getTimer("appear");
		
		if (appear != null && appear.progressedToRoof()) {
			timingDevice.removeTimer("appear");
			show = true;
		}
		
		if (show) {
			if (transparency < 0xFF) transparency += 15;
			if (transparency > 0xFF) transparency = 0xFF;
			
			if (getY() >= initY && !floating) floating = true;
			else if (getY() <= topY && floating) floating = false;
			
			if (floating) super.setY((getY() - delta * speed));
			else super.setY((getY() + delta * speed));
			
			if ((player.isWalkingHorizontally() || player.isJumpingForward())
				&& gameState.getArena().getGround().getImpactZone()) {
					setX(getX() + delta * player.getSpeed() * -player.getUserDirect(Physics.Axis.X).straight());
			}
			
			if (step && grantEffectWhenStep) grantEffect();
		}
	}

	public void render(ExtendedGraphics2D g) {
		if (!show) return;
		align();
		g.drawImage(animation.getImage(), point, dim, transparency);
	}
	
	protected void align() { 
		if (getY() < topY) setY(topY);
		if (initY < Topology.topLim(getX())) {
			initY = Topology.topLim(getX());
			topY = initY - 20;
		}
	}
	
	public void terminate() {
		super.terminate(false);
		spawnMngr.remove(this);
	}
	
	public void grantEffect() {
		musicBox.play(pickTune);
		terminate();
	}
	
	public void setY(double y) {
		double diff = y - getY();
		super.setY(y);
		initY += diff;
		topY += diff;
	}
	
	public boolean isSteppedOn() { return step; }
	public void step(boolean flag) { step = flag; }
	public void hurt(double p, boolean react) {}
	public double getZ() { return initY + 20; }
}