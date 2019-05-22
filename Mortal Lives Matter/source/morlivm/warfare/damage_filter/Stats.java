package morlivm.warfare.damage_filter;

import morlivm.system.performance.FPS;

public class Stats
{
	private final static double SHIELD_MASS_MULTIPLIER = 1.6;
	
	public int offense;
	public int defense;
	public int health;
	public int mana;
	public int stamina;
	public int agility;
	public int accuracy, criticalRate;
	public int shield;
	public int kbrate;
	public int generalPower;
	
	public Stats(int offence, int defense, boolean shield, int health, int mana,
				 int stamina, double agility, int accuracy, int kbrate) {
		
		this.offense = offence;
		this.defense = defense;
		this.health = health;
		this.mana = mana;
		this.stamina = stamina;
		this.agility = FPS.toFrames(agility);
		this.accuracy = accuracy;
		this.criticalRate = accuracy / 5;
		this.shield = shield ? (int) (health * SHIELD_MASS_MULTIPLIER) : 0;
		this.kbrate = kbrate;
		this.generalPower = calcOverallPow(offence, defense, health);
	}
	
	public Stats(Stats other) {
		this.offense = other.offense;
		this.defense = other.defense;
		this.health = other.health;
		this.mana = other.mana;
		this.stamina = other.stamina;
		this.agility = other.agility;
		this.accuracy = other.accuracy;
		this.criticalRate = other.criticalRate;
		this.shield = other.shield;
		this.kbrate = other.kbrate;
		this.generalPower = other.generalPower;
	}
	
	public static int calcOverallPow(int offense, int defense, int health) {
		return (offense * 4 + defense * 4 + health * 2) / 3;
	}
}