package morlivm.content.proj;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import morlivm.content.mortal.Mortal;
import morlivm.content.mortal.Player;
import morlivm.content.mortal.SpawnManager;
import morlivm.control_panel.Megaphone;
import morlivm.database.ProjectileData;
import morlivm.main.Game;
import morlivm.map.parallex.Ground;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Physics;
import morlivm.system.math.Physics.Vector;
import morlivm.system.performance.FPS;
import morlivm.system.sound.Sound;
import morlivm.warfare.target.AimedTarget;

public class Bomb extends Projectile
{
	private final static int[] PLANTED_FLICKER = {30, 8};
	
	private boolean moves, triggered;
	private double explosionTime;
	private int transparency;
	private Ground ground;
	private BufferedImage burstIcon;
	
	public Bomb(Mortal owner, double strength, ProjectileData pdb,
				int projHeight, GameState gs, Point deviation) {
		
		super(owner, strength, pdb, projHeight, gs, deviation);

		this.ground = gs.getArena().getGround();
		this.directY = Vector.NONE;
		this.burstIcon = db.spriteSheet.getSprite().grabSprite(1, 2);
		this.explosionTime = db.clock > 0 ? db.clock / 3 : 0.5;
		this.transparency = 0;
		
		if (db.clock > 0) timingDevice.addTimer("second burst", 2 * db.clock / 3);
	}
	
	public Bomb() {
		super();
	}
	
	protected void init(SubType subType) {
		Point endPoint;
		
		if (subType == SubType.PLANT) {
			setX(offensive.getX() + offensive.getDimension().width / 2);
			setY(offensive.getZ());
			this.animation = new Animation(db.spriteSheet);
			animation.setCeiling(PLANTED_FLICKER[0]);
		}
		else if (db.subType == SubType.ROLL) {
			endPoint = new Point(Game.getMouseInput().getX() * 1.3, offensive.getZ());
			this.target = new AimedTarget(this, endPoint);
			this.pinpoint = new Collider(new Point(point, 0, dim.height / 2, 0), new Dimension(dim.width, dim.height / 2));
			this.moves = true;
		}
		
		this.sound = db.SFX;
		musicBox.put(sound);
		musicBox.export();
	}
	
	public boolean failureCheck() {
		SpawnManager sMngr;
		int amount;
		boolean genuine = true;
		Point point;
		
		switch (db.subType) {
			case PLANT: {
				if (db.trigger != Trigger.IGNITION) {
					point = new Point(getX() + getDimension().width / 2, getZ());
					if (!spawnMngr.checkMineAvailability(point, 50)) {
						Megaphone.announce("Unable to plant a mine at this spot.");
						genuine = false;
					}
				}
				else {
					sMngr = gameState.getSpawnManager();
					amount = sMngr.count(Bomb.class);
					for (int i = 0; i < amount; i++) {
						if (((Bomb) sMngr.get(Bomb.class, i)).getDatabase().trigger == Trigger.IGNITION) {
							Megaphone.announce("Unable to plant more than one explosive charge at a time.");
							genuine = false;							
						}
					}
				}
				break;
			}
			
			case ROLL: genuine = super.failureCheck();
			default: genuine = super.failureCheck();
		}
		if (genuine) playSound();
		return genuine;
	}
	
	public void update(double delta) {
		super.update(delta);
		if (!running) return;
		
		if (!triggered) {
			if (db.trigger == Trigger.STEP) updateStep(delta);
			if (db.trigger == Trigger.IGNITION) updateTrigger(delta);
			if (db.clock > 0) updateClock(delta);
		}
		else explode();
		
		if (db.subType == SubType.ROLL) moveTowardsTarget(delta);
		updateCoordinates(delta);
		animation.update(delta);
	}
	
	protected void moveTowardsTarget(double delta) {
		setX(getX() + target.getCurrentDistX() / speed * getDirectX().straight());
		setY(getY() - target.getCurrentDistY() / speed * getDirectY().straight());
		pinpoint.setX(pinpoint.getX() + target.getCurrentDistX() / speed * getDirectX().straight());
		pinpoint.setY(pinpoint.getY() - target.getCurrentDistY() / speed * getDirectY().straight());
	}
	
	protected void moveTowardsTarget(ExtendedGraphics2D g) {
		target.aim(g);
	}
	
	public void updateStep(double delta) {
		if (contactMngr.getStepHit(offensive, false, strength, new Collider(point, dim)))
			trigger();
	}
	
	public void updateTrigger(double delta) {}
	
	public void updateClock(double delta) {
		if (db.subType == SubType.ROLL && moves) {
			if (reachedEndPoint()) moves = false;
			if (moves) return;
		}
		
		if (timingDevice.getTimer("second burst").progressedToRoof()) {
			timingDevice.removeTimer("second burst");
			animation.setCeiling(PLANTED_FLICKER[1]);
			trigger();
		}
	}
	
	public void explode() {
		transparency += 0xFF / FPS.toUnstableFrames(explosionTime);
		if (transparency >= 0xFF) terminate(true);
	}
	
	public void render(ExtendedGraphics2D g) {
		if (!running) return;
		
		if (db.subType == SubType.ROLL) moveTowardsTarget(g);
		else g.drawImage(animation.getImage(), point, dim);
		
		if (triggered) g.drawImage(burstIcon, new Point(getX(), getY()), dim, transparency);
	}
	
	private void updateCoordinates(double delta) {
		Player p;
		
		if (offensive instanceof Player) {
			p = (Player) offensive;
			if ((p.isWalkingHorizontally() || p.isJumpingForward())	&& ground.getImpactZone() == true)
						setX(getX() + delta * p.getSpeed() * -p.getUserDirect(Physics.Axis.X).straight());
		}
	}
	
	public void trigger() {
		musicBox.stop("activate 1");
		musicBox.loop("activate 2");
		triggered = true;
	}
	
	public void terminate(boolean explode) {
		super.terminate(false);
		
		running = false;
		musicBox.stop("activate 2");
		
		if (explode) {
			musicBox.play(Sound.Clique.P_EXPLODE);
			contactMngr.getAreaHit(offensive, strength, point, dim.width, 100);
			gameState.getAftershockManager().addShock(db.aftershock,
					new Point(getX() + dim.width / 2 - db.aftershock.getDimension().width / 2,
							  getZ() - db.aftershock.getDimension().height / 2));
		}
		
		spawnMngr.remove(this);
	}
	
	public double getZ() { return getY() + dim.height; }
}