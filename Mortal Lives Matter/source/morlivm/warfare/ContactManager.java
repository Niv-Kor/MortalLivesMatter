package morlivm.warfare;
import java.awt.Dimension;
import java.util.List;
import morlivm.content.Entity;
import morlivm.content.Entity.Injury;
import morlivm.content.loot.Loot;
import morlivm.content.mortal.AeroEnemy;
import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.Mortal;
import morlivm.content.mortal.Player;
import morlivm.content.proj.Bomb;
import morlivm.content.proj.Projectile.Trigger;
import morlivm.database.MortalData;
import morlivm.main.Game;
import morlivm.map.StainManager;
import morlivm.map.orientation.Topology;
import morlivm.state.GameState;
import morlivm.system.UI.Cursor;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.RNG;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.user_input.MouseInput;
import morlivm.warfare.aftershock.Aftershock;
import morlivm.warfare.aftershock.AftershockManager;
import morlivm.warfare.aftershock.Blood;

public class ContactManager
{
	private final static int KNOCKBACK = 10;
	
	private List<Entity> eList;
	private Mortal mortal;
	private Player player;
	private MouseInput mouseInput;
	private AftershockManager aftershockMngr;
	private StainManager stainMngr;
	private GameState gameState;
	
	public ContactManager(GameState gs) {
		this.gameState = gs;
		this.eList = gs.getSpawnManager().getList();
		this.aftershockMngr = gs.getAftershockManager();
		this.stainMngr = gs.getStainManager();
		this.mouseInput = Game.getMouseInput();
	}
	
	public boolean getAimedHit(Mortal offensive, Collider collider, double strength,
							   DoT.Type condition, boolean bleed, boolean hitPlayer, Tune tune) {
		
		Collider cursorCollider = mouseInput.getMouseCollider(Cursor.CURSOR_PIX_DIM);
		Aftershock blood = null;
		int injuryLevel = 0, knockback;
		
		for (Entity temp : eList) {
			if (temp instanceof Mortal) mortal = (Mortal) temp;
			else continue;
			
			if (mortal == offensive) continue;
			if (hitPlayer && !(mortal instanceof Player)) continue;
			if (mortal.isInStaticState() && mortal.getState() != Enemy.State.HURT) continue;
			
			if (!hitPlayer) {
				if (mortal.getCripplingHit(cursorCollider)) injuryLevel = 1;
				else if (mortal.getHit(cursorCollider)) injuryLevel = 2;
				else if (mortal.getCriticalHit(cursorCollider)) injuryLevel = 3;
				else continue;
			}
			else injuryLevel = 2;
			
			switch(injuryLevel) {
				case 1: if (mortal.getCripplingHit(collider)) {
							mortal.setSpeed(mortal.getSpeed() / 2);
							strength /= 2;
							break;
						}
				case 2: if (mortal.getHit(collider)) {
							break;
						}
				case 3: if (mortal.getCriticalHit(collider)) {
							strength *= 2;
							break;
						}
				default: return false;
			}
			
			knockback = ((MortalData) ((Enemy) mortal).getDatabase()).stats.kbrate;
			if (bleed) blood = spatter(mortal);
			
			mortal.hurt(offensive, strength, knockback(offensive, mortal, knockback, false), blood, false);
			mortal.getImmuneSystem().infect(condition, strength);
			Sound.play(tune);
			shotDownFlying(mortal);
			return true;
		}
		return false;
	}
	
	public void inflictAimedAOE(Mortal offensive, Collider collider, double strength, Tune tune) {
		boolean hit = false;
		boolean flyingInRange = false;
		Aftershock blood = null;
		Collider cursorCollider = mouseInput.getMouseCollider(Cursor.CURSOR_PIX_DIM);
		
		for (Entity temp : eList) {
			if (temp instanceof Mortal) mortal = (Mortal) temp;
			else continue;
			
			if (!(mortal instanceof Enemy) || (mortal.isInStaticState() && mortal.getState() != Enemy.State.HURT)) continue;
			if (!(mortal.getCripplingHit(cursorCollider))
			&& (!(mortal.getHit(cursorCollider)))
			&& (!(mortal.getCriticalHit(cursorCollider))))
				continue;
			
			flyingInRange = (mortal instanceof AeroEnemy) ? ((AeroEnemy) mortal).isAttackingWithinRange() : false;
			int enemyProportion = mortal.getDimension().height / 10;
			boolean samePlateau = (player.getZ() + enemyProportion >= mortal.getZ() - enemyProportion)
							   && (player.getZ() - enemyProportion <= mortal.getZ() + enemyProportion);
			
			if (flyingInRange) {
				blood = spatter(mortal);
				((AeroEnemy) mortal).generateDeathSpot();
				mortal.hurt(player, strength, knockback(offensive, mortal,
						  ((MortalData) ((Enemy) mortal).getDatabase()).stats.kbrate, false), blood, false);
				return;
			}
			
			if ((mortal.getHit(collider) || mortal.getCriticalHit(collider)) && samePlateau) {
				blood = spatter(mortal);
				mortal.hurt(player, strength, knockback(offensive, mortal,
						((MortalData) ((Enemy) mortal).getDatabase()).stats.kbrate, false), blood, false);
				hit = true;
			}
		}
		if (hit) Sound.play(tune);
	}
	
	public boolean getStepHit(Mortal offensive, boolean hit, double strength, Collider collider) {
		Aftershock blood;
		
		for (Entity temp : eList) {
			if (temp instanceof Mortal) mortal = (Mortal) temp;
			else continue;
			
			if (mortal == offensive) continue;
			if (!(mortal instanceof Enemy) || mortal.isInStaticState()) continue;
			else if (collider.touch(mortal.getLegs())) {
				if (hit) {
					blood = spatter(mortal);
					mortal.hurt(offensive, strength, knockback(offensive, mortal, 100, true), blood, false);
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean getAreaHit(Mortal offensive, double strength, Point point, int objWidth, int epsilon) {
		boolean hit = false;
		Aftershock blood;
		Collider collider = new Collider(new Point(point.getX() - epsilon, Topology.topLim(point.getX() - epsilon)),
								   		 new Dimension(epsilon * 2 + objWidth, Topology.groundDepth(offensive.getMidX())));
		
		for (Entity temp : eList) {
			if (temp instanceof Mortal) mortal = (Mortal) temp;
			else continue;
			
			if (!mortal.isAlive()) continue;
			else if (collider.touch(mortal.getLegs())) {
				blood = spatter(mortal);
				mortal.hurt(offensive, strength / 1.5, knockback(offensive, mortal, 100, true), blood, true);
				hit = true;
			}
		}
		return hit;
	}
	
	public boolean getPlayerTouchDamage(Collider shape) {
		if (!player.isAlive()) return false;
		
		for (Entity temp : eList) {
			if (temp instanceof Mortal) mortal = (Mortal) temp;
			else continue;

			if (!(mortal instanceof Enemy) || mortal.isInStaticState()) continue;
			else if (shape.touch(mortal.getMass()) || shape.touch(mortal.getLegs())) {
				player.hurt(mortal, mortal.getStats().offense / 2, Entity.Injury.KNOCKED_OUT, null, false);
				return true;
			}
		}
		return false;
	}
	
	public boolean hit(Mortal offensive, Mortal defensive, Collider shape, double strength,
					   boolean bleed, DoT.Type effect, Aftershock.Type shock) {
		
		Aftershock blood = null;
		
		if (shape.touch(defensive.getMass())) {
			if (bleed) blood = spatter(defensive);
			
			if (shock != null)
				aftershockMngr.addShock(shock, new Point(defensive.getMass().getA()));
			
			defensive.getImmuneSystem().infect(effect, strength);
			defensive.hurt(offensive, strength, Injury.KNOCKED_OUT, blood, false);
			return true;
		}
		return false;
	}
	
	public void collect() {
		Loot loot;
		Collider feet, lootC;
		
		for (Entity temp : eList) {
			if (temp instanceof Loot) loot = (Loot) temp;
			else continue;
			
			feet = player.getLegs();
			lootC = new Collider(loot.getPoint(), loot.getDimension());
			
			loot.step(feet.touch(lootC));
		}
	}
	
	public void enemyOnTarget() {
		Collider cursorCollider = mouseInput.getMouseCollider(Cursor.CURSOR_PIX_DIM);
		Enemy enemy;
		
		for (Graphable temp : eList) {
			if (temp instanceof Enemy) enemy = (Enemy) temp;
			else continue;
			
			if (!enemy.isAlive()) continue;
			if (enemy.getHit(cursorCollider)
			 || enemy.getCriticalHit(cursorCollider)
			 || enemy.getCripplingHit(cursorCollider)) {
				mouseInput.setFocusOnTarget(true);
				player.getSight().setTarget(enemy);
				enemy.showComponents(true);
				return;
			}
			else enemy.showComponents(false);
		}
		mouseInput.setFocusOnTarget(false);
	}
	
	public boolean explosiveOnTarget() {
		Collider cursorCollider = mouseInput.getMouseCollider(Cursor.CURSOR_PIX_DIM);
		Bomb bomb;
		
		for (Graphable temp : eList) {
			if (temp instanceof Bomb) bomb = (Bomb) temp;
			else continue;
			
			if (bomb.getDatabase().trigger != Trigger.IGNITION) continue;
			if (bomb.touch(cursorCollider)) {
				player.getSight().setTarget(bomb);
				return true;
			}
		}
		return false;
	}
	
	private Injury knockback(Mortal offensive, Mortal defensive, int chance, boolean dizzy) {
		if (RNG.unstableCondition(chance)) {
			defensive.setX(defensive.getX() + KNOCKBACK * offensive.getDirectX().straight());
			return dizzy ? Injury.DIZZY : Injury.KNOCKED_OUT;
		}
		else return Injury.UNCONCERNED;
	}
	
	private void shotDownFlying(Entity e) {
		if (e instanceof AeroEnemy) ((AeroEnemy) e).generateDeathSpot();
	}
	
	public Blood spatter(Mortal defensive) {
		//stain the ground
		stainMngr.spatter(defensive);
		
		//create a spatter
		Point p = new Point(defensive.getMass().getA());
		return (Blood) Aftershock.Type.BLOOD.createInstance(p, gameState);
	}
	
	public void connectRulerEntity(Player p) { player = p; }
	public List<Graphable> getList() { return null; }
}