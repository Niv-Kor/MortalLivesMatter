package morlivm.warfare;
import morlivm.database.ProjectileData;
import morlivm.system.graphics.TableOriented;
import morlivm.system.performance.FPS;

public class Attack
{
	public static enum Combat {
		PROJECTILE, MELEE;
	}
	
	private Combat combat;
	private TableOriented state;
	private int climax, launchHeight;
	private double speed, strength;
	private boolean defaultive;
	private ProjectileData pdb;
	
	public Attack(TableOriented state, int climax, double speed, double strength, int launcHeight, boolean defaultive) {
		this.combat = Combat.MELEE;
		this.state = state;
		this.climax = climax;
		this.launchHeight = launcHeight;
		this.speed = speed;
		this.strength = strength;
		this.defaultive = defaultive;
	}
	
	public Attack(Attack other) {
		this.combat = other.combat;
		this.state = other.state;
		this.climax = other.climax;
		this.launchHeight = other.launchHeight;
		this.speed = other.speed;
		this.strength = other.strength;
		this.defaultive = other.defaultive;
	}
	
	public void involveWeapon(ProjectileData p) {
		combat = Combat.PROJECTILE;
		pdb = p;
	}
	
	public boolean usesProjectile() { return getProjDatabase() != null; }
	public Combat getCombatType() { return combat; }
	public TableOriented getState() { return state; }
	public ProjectileData getProjDatabase() { return pdb; }
	public int getClimaxPoint() { return climax; }
	public int getLaunchHeight() { return launchHeight; }
	public int getSpeed() { return Math.round(FPS.toFrames(speed)); }
	public double getStrength() { return strength; }
	public boolean isDefaultive() { return defaultive; }
}