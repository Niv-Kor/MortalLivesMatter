package morlivm.content.mortal;
import java.awt.Color;

import morlivm.database.MortalData;
import morlivm.map.Landmark;
import morlivm.memory.Loadable;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.TableOriented;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics;
import morlivm.system.math.Physics.Axis;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RNG;
import morlivm.system.performance.Timer;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Sound.Clique;
import morlivm.warfare.Attack;
import morlivm.warfare.DamageManager;
import morlivm.warfare.FloatingDamage;
import morlivm.warfare.FloatingDamage.DamageType;
import morlivm.warfare.NameTag;
import morlivm.warfare.Weapon;
import morlivm.warfare.aftershock.Aftershock;
import morlivm.warfare.aftershock.AftershockManager;
import morlivm.warfare.damage_filter.Element;
import morlivm.warfare.damage_filter.ImmuneSystem;
import morlivm.warfare.damage_filter.Shield;
import morlivm.warfare.gauge.BossHP;
import morlivm.warfare.gauge.DynamicHealthBar;
import morlivm.warfare.gauge.HealthBar;
import morlivm.warfare.target.MobBehaviour;

public class Enemy extends Mortal implements Loadable
{
	public static enum State implements TableOriented {
		SPAWN(1, false),
		IDLE(2, false),
		WALK(3, true),
		JUMP(4, true),
		DUCK(5, false),
		HURT(6, false),
		DIE(7, false),
		ATTACK_1(8, false),
		ATTACK_2(9, false),
		ATTACK_3(10, false),
		ATTACK_4(11, false);
		
		private int row, index;
		private boolean dynamic;
		
		private State(int row, boolean dynamic) {
			this.row = row;
			this.index = row - 1;
			this.dynamic = dynamic;
		}
		
		public int getRow() { return row; }
		public int getIndex() { return index; }
		public boolean isDynamic() { return dynamic; }
	}
	
	protected boolean spawned, dropped;
	protected int initSpeed, transparency, preTick;
	protected Player player;
	protected MobBehaviour target;
	protected NameTag nameTag;
	protected Point staticPoint;
	protected Landmark landmark;
	protected Shield shield;
	protected Weapon weapon;
	protected Timer attDelay;
	protected DamageManager damageMngr;
	protected AftershockManager aftershockMngr;
	protected boolean staticState, marked;
	
	public Enemy(MortalData db, Point point, GameState gs) {
		super(db, point, gs);
	}
	
	public Enemy() {
		super();
	}
	
	public LoadedSectionsQueue upload() {
		this.player = gameState.getPlayer();
		this.damageMngr = gameState.getDamageManager();
		this.aftershockMngr = gameState.getAftershockManager();
		this.nameTag = new NameTag(this, db.name);
		Landmark.add(this);
		
		//additional properties
		this.immuneSys = new ImmuneSystem(this, db.immuneSystem, gameState);
		this.hp = new DynamicHealthBar(this, db.stats.health, true, true, Color.RED);
		this.shield = new Shield(this, stats.shield);
		this.weapon = new Weapon(this, player, true, gameState);
		int originSpeed = db.stats.agility;
		this.speed = RNG.generateEpsilonPercentage(30, originSpeed);
		this.initSpeed = speed;
		this.transparency = -1;
		
		//sprite
		this.animation = new Animation(db.spriteSheet);
		if (player.getX() < getX()) setDirectX(Vector.LEFT);
		else setDirectX(Vector.RIGHT);
		this.staticPoint = new Point(getPoint());
		
		this.attDelay = new Timer("att delay", 1.8, false);
		timingDevice.addTimer(attDelay);
		
		//sound
		musicBox.put(db.SFX);
		musicBox.export();
		
		return null;
	}
	
	public void execute() {
		musicBox.play(Clique.M_SPAWN);
		fixBodyParts(point);
		align();
	}

	public void update(double delta) {
		super.update(delta);
		Timer dying = timingDevice.getTimer("dying");
		
		nameTag.update(delta);
		elementStack.update(delta);
		if (!SpawnManager.isActivated()) return;
		
		//spawn
		if (state == State.SPAWN && animation.lastTick()) {
			setState(State.WALK);
			musicBox.loop(Clique.M_BREATH);
		}
		
		//death
		if (dying != null) {
			setState(State.DIE);
			animation.setCeiling(5);
			if (!dropped) drop();
			if (animation.lastTick()) animation.setCeiling(0);
			if (dying.progressedToRoof() && transparency == -1) {
				dying.renew(0.1);
				transparency = 0xFF;
			}
			else if (transparency != -1) {
				transparency -= 5;
				if (transparency <= 0) spawnMngr.remove(this);
			}
		}
		
		preTick = animation.getTick();
		animation.update(delta);
		align();
		move(delta);
		fixBodyParts(point);
		manageMarking();
		target.follow(delta);
		hp.update(delta);
		shield.update(delta);
		immuneSys.update(delta);
		if (animation.getTick() != preTick) attack(animation.getTick(), delta);
		if (db.projectiles.length > 0) weapon.setActiveProj(db.arsenal.generate(Attack.Combat.PROJECTILE).getProjDatabase());
		if (attDelay.isEnabled() && attDelay.progressedToRoof()) attDelay.enable(false);
		
		if (state.getRow() >= State.ATTACK_1.row && state.getRow() <= State.ATTACK_3.row)
			if (animation.lastTick()) setState(State.WALK);
	}

	public void render(ExtendedGraphics2D g) {
		if (transparency == -1)	g.drawImage(animation.getImage(), point, dim);
		else g.drawImage(animation.getImage(), point, dim, transparency);
		
		if (state != State.SPAWN) immuneSys.render(g);
		else if (hp instanceof BossHP) hp.render(g);
	}
	
	public void move(double delta) {
		boolean stuck = !orientationControl.canMove();
		boolean playerMoves = player.isWalkingHorizontally() || player.isJumpingForward();
		
		//y axis
		if (!stuck) setY(getY() + delta * speed * orientationControl.generateSoftY());
		
		//x axis and map movement
		//player doesn't move
		if ((!playerMoves || !gameState.getArena().getGround().getImpactZone()) && !stuck) {
			if (state.isDynamic()) setX(getX() + delta * speed * getDirectX().straight());
		}
		//player moves
		else if (playerMoves && gameState.getArena().getGround().getImpactZone()) {
			//enemy cannot move
			//if (stuck) gameState.getArena().getGround().moveAlong(this, delta);
			
			//move enemy along with map and adjust to the player's own speed
			 if (player.getUserDirect(Axis.X) == Vector.RIGHT) {
				if (getDirectX() == Vector.RIGHT) setX(getX() + delta * (speed - player.getSpeed()));
				else setX(getX() - delta * (player.getSpeed() + speed));
			}
			else if (player.getUserDirect(Axis.X) == Vector.LEFT) {
				if (getDirectX() == Vector.RIGHT) setX(getX() + delta * (player.getSpeed() + speed));
				else setX(getX() - delta * (speed - player.getSpeed()));
			}
		}
	}
	
	public boolean isWalkingHorizontally() {
		return getDirectX() != Vector.NONE && state == State.WALK;
	}
	
	public boolean isWalkingVertically() {
		return getDirectY() != Vector.NONE && state == State.WALK;
	}
	
	public void terminate(boolean cry) {
		super.terminate(cry);
		Landmark.remove(this);
		
		if (timingDevice.getTimer("dying") == null) {
			timingDevice.addTimer("dying", 60);
			musicBox.stop(Sound.Clique.M_BREATH);
			if (cry) musicBox.play(Sound.Clique.M_DIE);
		}
	}
	
	public void hurt(Mortal offensive, double percentage, Injury injury, Aftershock bleed, boolean allowSelf) {
		if (!isAlive() || (!(offensive instanceof Player) && !allowSelf)) return;
		
		int inaccuracy = 100 - offensive.getStats().accuracy;
		double summed, elementMultiplier;
		boolean critical = false;
		boolean insufficientAttack = offensive.getStats().offense < db.stats.defense;
		DamageType damageType = DamageType.ENEMY;
		
		//attack weaker than shield
		if (insufficientAttack) {
			summed = Percent.numOfNum(offensive.getStats().offense, db.stats.defense);
			summed = Percent.percentOfNum(summed, percentage);
		}
		else summed = percentage;
		
		//critical hit scenario
		summed = RNG.generateEpsilonPercentage(inaccuracy, summed) + 1;
		if (RNG.unstableCondition(offensive.getStats().criticalRate)) {
			critical = true;
			summed *= 1.5;
		}
		
		//elements calculation
		elementMultiplier = offensive.getElementStack().fight(elementStack);
		if (elementMultiplier >= 1 + Element.IMPACT)
			damageType = FloatingDamage.getElementDamageType(offensive.getElementStack().getElement(1).getType());
		summed *= elementMultiplier;
		
		//miss scenario
		if (RNG.unstableCondition(inaccuracy / 5 + 1) && insufficientAttack) {
			summed = 0;
			damageType = DamageType.MISS;
		}

		if (shield.isBroken()) hp.decrease(summed);
		else shield.crackShield(summed);
		marked = true;
		
		//aftershock
		if (damageType != DamageType.MISS) {
			if (bleed != null) aftershockMngr.addShock(bleed);
			musicBox.play(Sound.Clique.M_HURT);
		}
		damageMngr.addDamage(damageType, (int) Math.round(summed), critical,
			new Point(getHead().getA().getMidBetween(getHead().getB())),
					  getDirectX().straight() * -1);
	}
	
	protected void drop() {
		int tempX, tempY;
		int coinAmount = RNG.generate(1, 5);
		int lootAmount = RNG.generate(0, 2);
		
		for (int i = 1; i <= coinAmount; i++) {
			tempX = RNG.generate((int) getX() - 60, (int) getX() + 60) + getDimension().width / 2;
			tempY = RNG.generate((int) getY() - 30, (int) getY() + 30) + getDimension().height - 40;
			spawnMngr.createCoinLoot(new Point(tempX, tempY));
		}
		
		for (int i = 1; i <= lootAmount; i++) {
			if (RNG.unstableCondition(50)) {
				tempX = RNG.generate((int) getX() - 60, (int) getX() + 60) + getDimension().width / 2;
				tempY = RNG.generate((int) getY() - 30, (int) getY() + 30) + getDimension().height - 40;
				spawnMngr.createDropLoot(new Point(tempX, tempY));
			}
		}
		dropped = true;
	}
	
	public void requestAttack() {
		weapon.generateAttack();
		attDelay.enable(true);
		setState(weapon.getAttack().getState());
	}
	
	public boolean canAttack() {
		return db.arsenal.count() > 0 && !attDelay.isEnabled();
	}
	
	protected void attack(int tick, double delta) {
		if (state.getRow() < State.ATTACK_1.row || state.getRow() > State.ATTACK_3.row) return;

		if (weapon.getAttack() != null && tick == weapon.getAttack().getClimaxPoint()) weapon.update(delta);
		if (animation.lastTick()) setState(State.WALK);
	}
	
	public void showComponents(boolean flag) {
		if (marked) {
			if (!hp.isShowing()) flag = true;
			else return;
		}
		
		elementStack.show(flag);
		nameTag.show(flag);
		hp.show(flag);
		shield.getExtraHP().show(flag);
	}
	
	private void manageMarking() {
		if (marked) {
			if (timingDevice.getTimer("mark") == null) timingDevice.addTimer("mark", 10);
			if (timingDevice.getTimer("mark").progressedToRoof()) {
				marked = false;
				timingDevice.removeTimer("mark");
			}
		}
	}
	
	public void setDirectX(Physics.Vector d) {
		super.setDirectX(d);
		shield.updateCoordinates();
	}
	
	public void revive() {
		super.revive();
		setState(State.IDLE);
		timingDevice.removeTimer("dying");
		musicBox.put(db.SFX);
		musicBox.export();
		musicBox.loop(Sound.Clique.M_BREATH);
	}
	
	public void setRulerEntity(Player p) {
		player = p;
		weapon = new Weapon(this, player, true, gameState);
	}
	
	public boolean uploadTest() {
		return musicBox.exists(db.SFX[0]);
	}
	
	public Point getFixedPoint() { return staticState ? staticPoint : super.getFixedPoint(); }
	public NameTag getNameTag() { return nameTag; }
	public HealthBar getHP() { return hp; }
	public Shield getShield() { return shield; }
	public Landmark getLandmark() { return landmark; }
	public boolean isInStaticState() { return staticState || !isAlive(); }
	public boolean isInDynamicState() { return !isInStaticState(); }
	public String getLoadedUnitCode() { return toString(); }
	public boolean isJumping() { return false; }
}