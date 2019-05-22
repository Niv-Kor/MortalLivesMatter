package morlivm.content.mortal;
import morlivm.control_panel.Ammo;
import morlivm.control_panel.Backpack;
import morlivm.control_panel.Magazine;
import morlivm.control_panel.Megaphone;
import morlivm.control_panel.ModeManager;
import morlivm.control_panel.Notifier;
import morlivm.database.MortalData;
import morlivm.database.ProjectileData;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.map.Incline;
import morlivm.map.orientation.Acceleration;
import morlivm.map.orientation.Bounce;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.memory.DeathControl;
import morlivm.state.GameState;
import morlivm.system.UI.Button;
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
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.user_input.KeyProcessor;
import morlivm.user_input.KeyProcessor.Key;
import morlivm.user_input.MouseInput;
import morlivm.warfare.Attack;
import morlivm.warfare.DamageManager;
import morlivm.warfare.FloatingDamage;
import morlivm.warfare.Weapon;
import morlivm.warfare.aftershock.Aftershock;
import morlivm.warfare.aftershock.AftershockManager;
import morlivm.warfare.damage_filter.ImmuneSystem;
import morlivm.warfare.gauge.HealthBar;
import morlivm.warfare.gauge.PlayerHP;
import morlivm.warfare.gauge.PlayerStamina;
import morlivm.warfare.sight.Sight;

public class Player extends Mortal
{
	public static enum State implements TableOriented {
		IDLE(1, false),
		WALK(2, true),
		RUN(3, true),
		JUMP(4, true),
		DUCK(5, false),
		HURT(6, false),
		DIE(7, false),
		ATTACK_1(8, false),
		ATTACK_2(9, false),
		ATTACK_3(10, false),
		ATTACK_4(11, false);
		
		private int row, index;
		public boolean dynamic;
		
		private State(int row, boolean dynamic) {
			this.row = row;
			this.index = row - 1;
			this.dynamic = dynamic;
		}

		public int getRow() { return row; }
		public int getIndex() {	return index; }
		public boolean isDynamic() { return dynamic; }
	}
	
	private int initSpeed, preTick;
	private MouseInput mouseInput;
	private DamageManager damageMngr;
	private AftershockManager aftershockMngr;
	private Weapon weapon;
	private boolean dying, mobile, isHurt;
	private boolean forced, lock;
	private boolean fistAtt, knifeAtt;
	
	public Player(MortalData db, Point point, GameState gs) {
		super(db, point, gs);
		
		this.damageMngr = gs.getDamageManager();
		this.aftershockMngr = gs.getAftershockManager();
		this.mouseInput = Game.getMouseInput();
		
		setY(Topology.topLim(getMidX()) + 100 - dim.height);
		this.directX = Physics.Vector.RIGHT;
		this.directY = Physics.Vector.NONE;
		this.dim.width *= directX.straight();
		this.weapon = new Weapon(this, null, false, gameState);
		weapon.setActiveProj(db.arsenal.generate(Attack.Combat.PROJECTILE, State.ATTACK_1).getProjDatabase());
		
		timingDevice.addTimer("hurt", 1.33);
		timingDevice.addTimer("increase stamina", 0.5);
		timingDevice.addTimer("increase health", 5);
		timingDevice.addTimer("decrease stamina", 0.16);
		this.state = State.IDLE;
		
		this.immuneSys = new ImmuneSystem(this, db.immuneSystem, gs);
		this.speed = stats.agility;
		this.initSpeed = speed;
		this.stamina = new PlayerStamina(this, stats.stamina);
		this.hp = new PlayerHP(this, stats.health);
		this.acceleration = new Acceleration(this, stamina, gs);
		this.bounce = new Bounce(this, 90, true, gs);
		this.mobile = true;
		this.animation = new Animation(db.spriteSheet);
		
		this.musicBox = new MusicBox();
		musicBox.put(new Tune("step", "/Sound/Player/SFX/Step.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.put(new Tune("running step", "/Sound/Player/SFX/RunningStep.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.put(db.SFX);
		musicBox.export();
	}
	
	public Player() {
		super();
	}
	
	public void setState(TableOriented s) {
		if (s == state) return;
		else if (s == State.HURT && !timingDevice.getTimer("hurt").reachRoof()) return;
		else if (s.getRow() >= State.ATTACK_1.getRow() && s.getRow() <= State.ATTACK_4.getRow()) lock = true;
		else if (isJumping() || lock) return;
		
		state = s;
		animation.setRow(state.getRow(), true);
	}
	
	public TableOriented getAction() {
		Magazine magazine = gameState.getControlPanel().getMagazine();
		
		if (isJumping()) return State.JUMP;
		if (magazine.isReloading() && isInStaticState()) return State.IDLE;
		if (dying || !isAlive()) return State.DIE;
		
		if (weapon.isOccupied()) {
			if (!KeyProcessor.isDown(KeyProcessor.Key.ATTACK)) {
				weapon.stop();
				return State.IDLE;
			}
			else if (magazine.getBullets() == 0 && magazine.getSlots() > 1) {
				weapon.stop();
				magazine.reload();
				lock = false;
				return State.IDLE;
			}
			else {
				lock = true;
				requestAttack();
				return weapon.getAttack().getState();
			}
		}
		
		boolean move = ((KeyProcessor.isDown(Key.RIGHT) ^ KeyProcessor.isDown(Key.LEFT))
					 || (KeyProcessor.isDown(Key.DOWN) ^ KeyProcessor.isDown(Key.UP)))
					 && !KeyProcessor.isDown(Key.JUMP);
		
		boolean jump = !(KeyProcessor.isDown(Key.RIGHT) && KeyProcessor.isDown(Key.LEFT))
					  && KeyProcessor.isDown(Key.JUMP);
		
		boolean rangedAttack = !(KeyProcessor.isDown(Key.RIGHT) || KeyProcessor.isDown(Key.LEFT))
							  && KeyProcessor.isDown(Key.ATTACK);
		
		boolean knifeAttack = KeyProcessor.isDown(Key.STAB);
		boolean fistAttack = KeyProcessor.isDown(Key.PUNCH);
		
		if (move)
			return (KeyProcessor.isDown(Key.RUN) && getUserDirect(Axis.X) == directX) ?
					State.RUN : State.WALK;
		
		if (jump) {
			if (!gameState.getArena().getGround().getEarthquakeMoveability())
				Megaphone.announce("The ground feels a bit too shakey to do that...");
			else {
				bounce.enable(true);
				return State.JUMP;
			}
		}
		
		//range
		if (rangedAttack) {
			if (magazine.isEmpty() && magazine.getSlots() > 1) {
				magazine.reload();
				return State.IDLE;
			}
			else {
				requestAttack();
				Attack att = weapon.getAttack();
				return (att != null) ? att.getState() : State.IDLE;
			}
		}
		
		//melee
		if (knifeAttack) return knifeAttack();
		else if (fistAttack) return fistAttack();
		
		return State.IDLE;
	}
	
	public void update(double delta) {
		super.update(delta);
		if (!SpawnManager.isActivated()) return;
		
		//continous states
		if (!forced && !lock) {
			switch((State) getAction()) {
				case IDLE: { setState(State.IDLE); animation.setCeiling(10); break; }
				case WALK: { setState(State.WALK); animation.setCeiling(5); break; }
				case RUN: { setState(State.RUN); animation.setCeiling(5); break; }
				case JUMP: { setState(State.JUMP); break; }
				case ATTACK_1: { setState(State.ATTACK_1); break; }
				case ATTACK_2: { setState(State.ATTACK_2); break; }
				case ATTACK_3: { setState(State.ATTACK_3); break; }
				case ATTACK_4: { setState(State.ATTACK_4); break; }
				case DIE: { setState(State.DIE); animation.setCeiling(5); break; }
				default: break;
			}
		}
		
		//locked states
		if (lock) {
			if (state == State.JUMP) animation.setCeiling(1);
			else {
				Attack compatibleAtt = db.arsenal.generate(state);
				int speed = compatibleAtt != null ? compatibleAtt.getSpeed() : 5;
				animation.setCeiling(speed);
			}
		}
		
		preTick = animation.getTick();
		if (forced && animation.lastTick()) forced = false;
		if (lock && animation.lastTick()) lock = false;
		if (isAlive() || dying)	animation.update(delta);
		if (animation.getTick(1) || animation.getTick() != preTick) attack(animation.getTick(), delta);
		
		if (state == State.IDLE) {
			fistAtt = false;
			knifeAtt = false;
		}
		
		refuel();
		immuneSys.update(delta);
		
		if (mobile) {
			changeDirection();
			moveHorizontally(delta);
			moveVertically(delta);
			if (getState() == State.JUMP) bounce.update(delta);
			accelerate(delta);
			align();
			
			if (!isHurt && contactMngr.getPlayerTouchDamage(mass.getCollider())) {
				isHurt = true;
				forceState(State.HURT);
			}
			hitTimer();
		}
		else if (state == State.DIE) {
			if (animation.lastTick() && dying) {
				Notifier.notify("YOU DIED", "Do you want to return to the last saved spot?", true);
				dying = false;
			}
			
			if (Notifier.attendAccept("YOU DIED", Button.Action.CLICK, Vector.LEFT))
				DeathControl.load();
			
			if (Notifier.attendDecline("YOU DIED", Button.Action.CLICK, Vector.LEFT))
				gameState.getGame().returnToMainScreen(true);
		}
		
		fixBodyParts(point);
	}
	
	public void render(ExtendedGraphics2D g) {
		g.drawImage(animation.getImage(), new Point(getX(), super.getY()), dim, transparency);
		immuneSys.render(g);
	}
	
	private void attack(int tick, double delta) {
		if (state.getRow() < State.ATTACK_1.getRow() || state.getRow() > State.ATTACK_4.getRow()) return;
		
		Attack att = weapon.getAttack();
		if (att != null && tick == att.getClimaxPoint()) weapon.update(delta);
	}
	
	private void changeDirection() {
		if (isInStaticState()) return;
		
		//horizontal
		if (ModeManager.isOn("mouse mode")) {
			if (mouseInput.getX() > getX() + dim.width / 2 && getDirectX() == Vector.LEFT) {
				setDirectX(Vector.RIGHT);
			}
			else if (mouseInput.getX() < getX() + dim.width / 2 && getDirectX() == Vector.RIGHT) {
				setDirectX(Vector.LEFT);
			}
		}
		else { //mouse mode is off
			if (KeyProcessor.isDown(Key.LEFT) && !KeyProcessor.isDown(Key.RIGHT)) setDirectX(Vector.LEFT);
			else if (KeyProcessor.isDown(Key.RIGHT) && !KeyProcessor.isDown(Key.LEFT)) setDirectX(Vector.RIGHT);
		}
		
		//vertical
		if (KeyProcessor.isDown(Key.UP) && directY != Vector.UP	&&
		   !KeyProcessor.isDown(Key.DOWN) && getZ() > Topology.topLim(getMidX()))
			setDirectY(Vector.UP);
		
		else if (KeyProcessor.isDown(Key.DOWN) && directY != Vector.DOWN &&
				!KeyProcessor.isDown(Key.UP) && getZ() < Topology.bottomLim(getMidX())) {
			setDirectY(Vector.DOWN);
		}
		
		else setDirectY(Vector.NONE);
	}
	
	private void moveHorizontally(double delta) {
		if (!(isWalking(Vector.RIGHT, Axis.X) ^ isWalking(Vector.LEFT, Axis.X)) && !acceleration.isGliding()) return;
		
		//forbid trying to bypass the bottom lim using any sort of inclines
		if (bypassingInclines(getMidX())) {
			gameState.getArena().setImpactZone(false);
			return;
		}
		
		Ground ground = gameState.getArena().getGround();
		boolean blockedToTheRight =
			(isWalking(Vector.RIGHT, Axis.X) && getX() + dim.width / 1.5 < Game.WIDTH)
			&& (getX() + dim.width / 2 >= Game.WIDTH / 1.7 && ground.getX() > -(ground.getWidth() - Game.WIDTH));
		
		boolean blockedToTheLeft =
			(isWalking(Vector.LEFT, Axis.X) && getX() + dim.width / 2 > 100)
			&& (getX() + dim.width / 2 <= Game.WIDTH / 5 && ground.getX() < 0);
		
		double vector = delta * speed * Topology.vectorY(getMidX(), getUserDirect(Axis.X)) * 2;
		
		if (ModeManager.isOn("mouse mode")) {
			if (directX != getUserDirect(Axis.X)) speed = initSpeed / 3;
			else if (speed < initSpeed) speed = initSpeed;
		}
		
		//movement
		if (blockedToTheRight || blockedToTheLeft) { //move map itself
			if (orientationControl.canMove(Axis.X)) {
				gameState.getArena().setImpactZone(true);
				if (!isJumping()) setY(getY() + vector);
				else lowerInitJump(vector);
			}
			else gameState.getArena().setImpactZone(false);
		}
		else { //move player
			gameState.getArena().setImpactZone(false);
			if (orientationControl.canMove(Axis.X)) {
				setX(getX() + delta * speed * getUserDirect(Axis.X).straight());
				
				//slightly move y as long as player is walking across inclines
				if (!isJumping()) setY(getY() + vector);
				else lowerInitJump(vector);
			}
		}
		
		//sound
		if (state == State.WALK && (animation.getTick(1) || animation.getTick(6)))
			musicBox.play("step");
		else if (state == State.RUN && (animation.getTick(1) || animation.getTick(5) || animation.getTick(9)))
			musicBox.play("running step");
	}
	
	private void lowerInitJump(double vector) {
		Incline inc = Topology.relevantIncline(getMidX());
		
		if (inc.isPlain()) return;
		boolean inclined = Topology.relevantIncline(getMidX()).isInclinedTowards(getDirectX());
		boolean outOfTopLim = bounce.getZ() <= Topology.topLim(getMidX());
		boolean outOfBottomLim = bounce.getZ() >= Topology.bottomLim(getMidX());
		boolean outOfLim = inclined ? outOfBottomLim : outOfTopLim;
		
		if (!outOfLim) bounce.translateZ(vector);
	}
	
	private boolean bypassingInclines(double x) {
		Incline inc = Topology.relevantIncline(x);
		boolean blockedToTheTop = getZ() <= Topology.topLim(x);
		boolean blockedToTheBottom = getZ() >= Topology.bottomLim(x);
		boolean walkingRight = isWalking(Vector.RIGHT, Axis.X);
		boolean walkingLeft = isWalking(Vector.LEFT, Axis.X);
		
		if (blockedToTheTop) {
			if (inc.isAscented() && walkingLeft) return true;
			else if (inc.isDescented() && walkingRight) return true;
		}
		else if (blockedToTheBottom) {
			if (inc.isAscented() && walkingRight) return true;
			else if (inc.isDescented() && walkingLeft) return true;
		}
		
		return false;
	}
	
	private void moveVertically(double delta) {
		if (!(isWalking(Vector.DOWN, Axis.Y) ^ isWalking(Vector.UP, Axis.Y)) || state == State.JUMP) return;
		else if (orientationControl.canMove(Axis.Y))
			setY(getY() + delta * speed * getDirectY().straight());
		
		//sound
		if (!isWalkingHorizontally() && !acceleration.isGliding()) {
			if (state == State.WALK && (animation.getTick(1) || animation.getTick(6)))
				musicBox.play("step");
			else if (state == State.RUN && (animation.getTick(1) || animation.getTick(5) || animation.getTick(9)))
				musicBox.play("running step");
		}
	}
	
	private void accelerate(double delta) {
		//fail the acceleration
		if (getDirectX() != getUserDirect(Axis.X) || getDirectX() == acceleration.getDirection().oppose() ||
		   (isJumping() && !isWalkingHorizontally())) acceleration.fail();
		
		if (state != State.RUN && state != State.JUMP) acceleration.enable(false);
		else if (isWalkingHorizontally() || isWalkingVertically()) acceleration.enable(true);
		
		acceleration.update(delta);
	}
	
	private void hitTimer() {
		if (isHurt) {
			if (timingDevice.getTimer("hurt").progressedToRoof()) {
				isHurt = false;
				timingDevice.getTimer("hurt").init();
			}
		}
	}
	
	private void refuel() {
		if (dying || !isAlive()) return;
		Timer staminaT = timingDevice.getTimer("increase stamina");
		Timer healthT = timingDevice.getTimer("increase health");
		
		if ((speed == initSpeed) && staminaT.progressedToRoof()) {
			stamina.increase(1);
			staminaT.init();
		}
		if (healthT.progressedToRoof()) {
			hp.increase(10);
			healthT.init();
		}
	}

	private Attack requestAttack() {
		return !fistAtt? weapon.generateAttack() : null;
	}

	public void lockState(boolean flag) { lock = flag; }
	
	public boolean isInDynamicState() {
		return (state == State.WALK || state == State.RUN)
				&& (isWalking(Vector.RIGHT, Axis.X)
				 || isWalking(Vector.LEFT, Axis.X)
				 || isWalking(Vector.UP, Axis.Y)
				 || isWalking(Vector.DOWN, Axis.Y));
	}
	
	public boolean isJumpingForward() {
		if (!orientationControl.canMove(Axis.X)) return false;
		else return state == State.JUMP	&& (isWalking(Vector.RIGHT, Axis.X) ^ isWalking(Vector.LEFT, Axis.X));
	}
	
	public void terminate(boolean cry) {
		super.terminate(cry);
		dying = true;
		mobile = false;
	}

	public void hurt(Mortal mortal, double percentage, Injury injury, Aftershock bleed, boolean allowSelf) {
		if ((!(mortal instanceof Enemy) && !allowSelf) || isHurt) return;
		getSight().lock(false);
		isHurt = true;
		if (injury != Injury.UNCONCERNED) forceState(State.HURT);
		
		int inaccuracy = 100 - mortal.db.stats.accuracy;
		double summed;
		boolean critical = false;
		boolean insufficientAttack = mortal.db.stats.offense < stats.defense;
		FloatingDamage.DamageType damageType = FloatingDamage.DamageType.PLAYER;
		
		//attack weaker than shield
		if (insufficientAttack) {
			summed = Percent.numOfNum(mortal.db.stats.offense, stats.defense);
			summed = Percent.percentOfNum(summed, percentage);
		}
		else summed = percentage;
		
		//critical hit scenario
		summed = RNG.generateEpsilonPercentage(inaccuracy, summed) + 1;
		if (RNG.unstableCondition(mortal.db.stats.criticalRate)) {
			critical = true;
			summed *= 1.5;
		}
		
		//elements calculation
		summed *= mortal.getElementStack().fight(elementStack);
		
		//miss scenario
		if (RNG.unstableCondition(inaccuracy / 10 + 1) && insufficientAttack) {
			summed = 0;
			damageType = FloatingDamage.DamageType.MISS;
		}
		
		hp.decrease(summed);
		
		//aftershock
		if (damageType != FloatingDamage.DamageType.MISS) {
			if (bleed != null) aftershockMngr.addShock(bleed);
			if (isAlive()) generateHitSound();
		}
		
		if (bleed != null && damageType != FloatingDamage.DamageType.MISS) aftershockMngr.addShock(bleed);
		damageMngr.addDamage(damageType, (int) Math.round(summed), critical,
			new Point(getHead().getA().getMidBetween(getHead().getB())),
					  getDirectX().straight() * -1);
	}
	
	public void heal(double percentage) { hp.increase(percentage); }
	
	private void generateHitSound() {
		musicBox.play(Sound.Clique.M_HURT);
	}
	
	public void forceState(State type) {
		lockState(false);
		forced = true;
		state = type;
		animation.setRow(type.getRow(), true);
		if (state == State.ATTACK_3 || state == State.HURT) animation.setCeiling(3);
	}
	
	public boolean isWalking(Vector direction, Axis axis) {
		if ((axis != Axis.X && axis != Axis.Y) || (direction == Vector.NONE && !acceleration.isGliding())) return false;
		
		Key key = null;
		
		switch(direction.straight()) {
			case 1: key = (axis == Axis.X) ? Key.RIGHT : Key.DOWN; break;
			case -1: key = (axis == Axis.X) ? Key.LEFT : Key.UP; break;
		}
		
		boolean activeMovement = (state == State.WALK || state == State.RUN || state == State.JUMP) && KeyProcessor.isDown(key);
		boolean passiveMovement = acceleration.isGliding() && acceleration.getDirection() == direction;
		return activeMovement || passiveMovement;
	}
	
	public boolean isWalkingHorizontally() {
		return isWalking(Vector.RIGHT, Axis.X) ^ isWalking(Vector.LEFT, Axis.X);
	}
	
	public boolean isWalkingVertically() {
		return isWalking(Vector.UP, Axis.Y) ^ isWalking(Vector.DOWN, Axis.Y);
	}
	
	public Vector getUserDirect(Axis a) {
		switch (a) {
			case X: {
				if (KeyProcessor.isDown(KeyProcessor.Key.RIGHT)) return Vector.RIGHT;
				else if (KeyProcessor.isDown(KeyProcessor.Key.LEFT)) return Vector.LEFT;
				return directX;
			}
			case Y: {
				if (KeyProcessor.isDown(KeyProcessor.Key.UP)) return Vector.UP;
				else if (KeyProcessor.isDown(KeyProcessor.Key.DOWN)) return Vector.DOWN;
				return directY;
			}
			default: return Vector.NONE;
		}
	}
	
	protected void align() {
		double left = Game.WIDTH / 15 - dim.width / 2;
		double right = Game.WIDTH - dim.width / 2;
		
		if (getX() < left) setX(left);
		else if (getX() > right) setX(right);
	}
	
	public int selectAmmo(Ammo ammo) {
		for (int i = 0; i < db.projectiles.length; i++)
			if (db.projectiles[i].name.equals(ammo.getProjData().name)) return i;
		return -1; //formal return statement
	}
	
	public void revive() {
		super.revive();
		setState(State.IDLE);
	}
	
	public void unpack(GameState gs) {
		this.gameState = gs;
		this.ricoMngr = gs.getRicochetManager();
		this.spawnMngr = gs.getSpawnManager();
		this.contactMngr = gs.getContactManager();
		this.damageMngr = gs.getDamageManager();
		this.aftershockMngr = gs.getAftershockManager();
		this.bounce.setRulerState(gs);
		this.acceleration.setRulerState(gs);
		this.weapon = new Weapon(this, null, false, gs);
		setX(Game.WIDTH / 5 - 90);
		setY(Topology.topLim(getMidX()) + 100 - dim.height);
		setDirectX(Vector.RIGHT);
		setDirectY(Vector.NONE);
		gameState.getControlPanel().getAmmoManager().reset();
		
		if (Structure.getLevel() == 0) {
			((PlayerHP) hp).recover();
			((PlayerStamina) stamina).recover();
		}
	}
	
	private State fistAttack() {
		State state = State.ATTACK_4;
		fistAtt = true;
		weapon.forceAttack(db.arsenal.generate(Attack.Combat.MELEE, state));
		return state;
	}
	
	private State knifeAttack() {
		State state = State.ATTACK_3;
		knifeAtt = true;
		weapon.forceAttack(db.arsenal.generate(Attack.Combat.MELEE, state));
		return state;
	}
	
	public void setActiveProj(ProjectileData pdb) { weapon.setActiveProj(pdb); }
	public boolean meleeAttacking() { return fistAtt || knifeAtt; }
	public Sight getSight() { return gameState.getControlPanel().getSight(); }
	public double getY() { return (bounce == null || !bounce.isActive()) ? super.getY() : bounce.getY(); }
	public double getZ() { return (bounce == null || !bounce.isActive()) ? super.getZ() : bounce.getZ(); }
	public void setZ(double z) { setY(z - (super.getZ() - super.getY())); }
	public HealthBar getHP() { return hp; }
	public HealthBar getStamina() { return stamina; }
	public Backpack getCurrency() { return gameState.getControlPanel().getCurrency(); }
	public boolean isJumping() { return bounce.isActive(); }
}