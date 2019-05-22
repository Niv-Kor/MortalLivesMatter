package morlivm.warfare;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import morlivm.database.MortalData;
import morlivm.database.ProjectileData;
import morlivm.system.graphics.TableOriented;
import morlivm.system.math.RNG;

public class Arsenal
{
	private LinkedList<Attack> attacks;
	private MortalData mdb;
	
	public Arsenal(MortalData mdb) {
		this.attacks = new LinkedList<Attack>();
		this.mdb = mdb;
	}
	
	public Arsenal(Arsenal other) {
		this.attacks = new LinkedList<Attack>();
		for (Attack a : other.attacks) attacks.add(a);
	}
	
	public Attack generate(Attack.Combat combatType) {
		return generate(getAll(combatType), combatType);
	}
	
	public Attack generate(Attack.Combat combatType, TableOriented state) {
		return generate(getAll(combatType, state), combatType);
	}
	
	public Attack generate(TableOriented state) {
		List<Attack> raffle = getAll(state);
		return !raffle.isEmpty() ? RNG.select(raffle) : null;
	}
	
	private Attack generate(List<Attack> raffle, Attack.Combat combatType) {
		Attack temp;
		
		//gurantee the projectile is not a quest item
		if (combatType == Attack.Combat.PROJECTILE && hasNonQuestProj() && raffle.size() > 0) {
			do temp = (Attack) RNG.select(raffle);
			while (temp.getProjDatabase().questItem);
		}
		//generates a melee attack
		else temp = (Attack) RNG.select(raffle);
		
		return temp;
	}
	
	public Attack generateDefault() {
		List<Attack> raffle = new ArrayList<Attack>();
		
		for (Attack a : attacks) if (a.isDefaultive()) raffle.add(a);
		if (raffle.isEmpty()) raffle = attacks;
		
		return RNG.select(raffle);
	}
	
	public ProjectileData generateCollectableProj() {
		List<Attack> raffle;
		ProjectileData temp;
		
		if (!hasCollectableProj()) temp = mdb.projectiles[0];
		else { //retrieve a non quest-item, collectable projectile
			raffle = getAll(Attack.Combat.PROJECTILE);
			do temp = ((Attack) RNG.select(raffle)).getProjDatabase();
			while (temp.collectableAmount.getMax() == 0 || temp.questItem);
		}
		
		return temp;
	}
	
	public Attack getByProj(ProjectileData pdb) {
		for (Attack a : attacks)
			if (a.getProjDatabase() == pdb) return a;
		
		return null;
	}
	
	public List<Attack> getAll(Attack.Combat combatType) {
		List<Attack> temp = new ArrayList<Attack>(); 
		
		for (Attack a : attacks)
			if (a.getCombatType() == combatType) temp.add(a);
		
		return temp;
	}
	
	public List<Attack> getAll(Attack.Combat combatType, TableOriented state) {
		List<Attack> temp = new ArrayList<Attack>(); 
		
		for (Attack a : attacks)
			if (a.getCombatType() == combatType && a.getState() == state) temp.add(a);
		
		return temp;
	}
	
	public List<Attack> getAll(TableOriented state) {
		List<Attack> temp = new ArrayList<Attack>(); 
		
		for (Attack a : attacks)
			if (a.getState() == state) temp.add(a);
		
		return temp;
	}
	
	public int count(Attack.Combat combatType, TableOriented state) {
		int counter = 0;
		
		for (Attack a : attacks)
			if (a.getCombatType() == combatType && a.getState() == state) counter++;
			
		return counter;
	}
	
	public int count(Attack.Combat combatType) {
		int counter = 0;
		
		for (Attack a : attacks)
			if (a.getCombatType() == combatType) counter++;
			
		return counter;
	}
	
	public int count(TableOriented state) {
		int counter = 0;
		
		for (Attack a : attacks)
			if (a.getState() == state) counter++;
			
		return counter;
	}
	
	private boolean hasNonQuestProj() {
		List<Attack> temp = getAll(Attack.Combat.PROJECTILE);
		ProjectileData pdb;
		
		for (Attack a : temp) {
			pdb = a.getProjDatabase();
			if (!pdb.questItem) return true;
		}
		
		return false;
	}
	
	private boolean hasCollectableProj() {
		List<Attack> temp = getAll(Attack.Combat.PROJECTILE);
		ProjectileData pdb;
		
		for (Attack a : temp) {
			pdb = a.getProjDatabase();
			if (!pdb.questItem && pdb.collectableAmount.getMax() > 0) return true;
		}
		
		return false;
	}
	
	public int count() { return attacks.size(); }
	public void learnMove(Attack a) { attacks.add(a); }
	public void learnMove(Attack a, ProjectileData pdb) {
		a.involveWeapon(pdb);
		attacks.add(a);
	}
	
	public void forgetMove(Attack a) { attacks.remove(a); }
	public void forgetAll() { attacks.clear(); }
}