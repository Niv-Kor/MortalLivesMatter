package morlivm.warfare;
import java.util.LinkedList;
import java.util.Queue;

import morlivm.content.mortal.Mortal;
import morlivm.content.mortal.Player;
import morlivm.content.proj.Bomb;
import morlivm.content.proj.Bullet;
import morlivm.content.proj.Projectile;
import morlivm.database.MortalData;
import morlivm.database.ProjectileData;
import morlivm.main.Game;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.math.Percent;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.warfare.sight.Sight;

public class Weapon
{
	private Mortal mortal;
	private Player player;
	private MortalData db;
	private Queue<Projectile> standby;
	private Attack attack;
	private ProjectileData activeProj;
	private MusicBox musicBox;
	private GameState gameState;
	private boolean AI, failed;
	
	public Weapon(Mortal mortal, Player player, boolean AI, GameState gs) {
		this.mortal = mortal;
		this.player = player;
		this.AI = AI;
		this.db = (MortalData) mortal.getDatabase();
		this.gameState = gs;
		this.standby = new LinkedList<Projectile>();
		this.musicBox = new MusicBox();
		musicBox.put(new Tune("stab", "/sound/Player/SFX/Stab.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.put(new Tune("stab hit", "/sound/Player/SFX/StabHit.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.put(new Tune("punch", "/sound/Player/SFX/Punch.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.put(new Tune("punch hit", "/sound/Player/SFX/PunchHit.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.export();
	}
	
	public Attack generateAttack() {
		return !AI? generateAtt() : makeAIDecision();
	}
	
	private Attack generateAtt() {
		Projectile tempProj;
		boolean genuine = false;
		Sight sight = db.playable ? ((Player) mortal).getSight() : null;
		double damageFilter = sight != null ? sight.getDexFilter() : 100;
		attack = db.arsenal.getByProj(activeProj);
		standby.clear();
		
		for (int i = 0, x = 0, y = 0, z = 1; i < activeProj.thrownAmount;
			 i++, z *= -1, y *= -1, x += 50, y = (y + 50 * z) * -1) {
		
			switch (activeProj.mainType) {
				case BULLET: {
					tempProj = new Bullet(mortal,
							   Percent.percentOfNum(damageFilter, db.stats.offense * activeProj.power),
							   activeProj, db.arsenal.getByProj(activeProj).getLaunchHeight(),
							   gameState, new Point(x, y));
					break;
				}
				case BOMB: {
					tempProj = new Bomb(mortal,
							   Percent.percentOfNum(damageFilter, db.stats.offense * activeProj.power),
							   activeProj, db.arsenal.getByProj(activeProj).getLaunchHeight(),
							   gameState, new Point(x, y));
					break;
				}
				default: tempProj = null; 
			}
			
			if (tempProj != null) {
				genuine = tempProj.failureCheck();
				if (genuine) standby.add(tempProj);
			}
		}
		
		if (genuine) {
			if (sight != null) {
				if (Game.getMouseInput().isOnTarget()) sight.lock(true);
				sight.pump();
			}
			
			failed = false;
			return attack;
		}
		else {
			failed = true;
			if (sight != null) sight.lock(false);
			standby.clear();
			attack = db.arsenal.generateDefault();
			return null; //fail
		}
	}
	
	private Attack makeAIDecision() {
		Point e = new Point(mortal.getX() + mortal.getDimension().width, 0);
		Point p = new Point(player.getX() + player.getDimension().width / 2, 0);
		
		//return melee attack
		if (Math.abs(p.getX() - e.getX()) < mortal.getDimension().width / 10 || db.projectiles.length == 0) {
			attack = db.arsenal.generate(Attack.Combat.MELEE);
			return attack;
		}
		//proj attack
		else return generateAtt();
	}
	
	public void update(double delta) {
		if (attack == null && !failed) return;
		
		Projectile proj;
		
		if (!failed && attack.usesProjectile()) {
			while(!standby.isEmpty()) {
				proj = standby.poll();
				
				if (gameState.getControlPanel().getMagazine().useOne() || !db.playable) {
					if (db.playable) gameState.getControlPanel().getAmmoManager().use();
					switch (proj.getDatabase().mainType) {
						case BULLET: gameState.getRicochetManager().add(proj); break;
						case BOMB: gameState.getSpawnManager().spawn(proj); break;
					}
				}
				else standby.clear();
			}
		}
		else if (failed || (!db.playable && !attack.usesProjectile())) {
			musicBox.play("punch");
			new Melee(mortal, Melee.SHORT,
					  db.stats.offense * attack.getStrength(),
					  mortal.getImmuneSystem().passRandomInfection(),
					  musicBox.getTune("punch hit"), gameState);
		}
		else if (!attack.usesProjectile() && db.playable) {
			musicBox.play("stab");
			new Melee(mortal, Melee.LONG,
					  db.stats.offense * attack.getStrength(),
					  mortal.getImmuneSystem().passRandomInfection(),
					  musicBox.getTune("stab hit"), gameState);
		}
		
		//init
		attack = null;
		failed = false;
	}
	
	public Attack getAttack() { return attack; }
	public void setActiveProj(ProjectileData proj) { activeProj = proj; }
	public ProjectileData getActiveProj() { return activeProj; }
	public void stop() { attack = null; }
	public boolean isOccupied() { return attack != null; }
	public void forceAttack(Attack a) { if (attack == null) attack = a; }
}