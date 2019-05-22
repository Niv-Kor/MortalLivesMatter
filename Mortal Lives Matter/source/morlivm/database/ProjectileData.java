package morlivm.database;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import morlivm.content.proj.Projectile;
import morlivm.database.primary_key.PrimaryKey;
import morlivm.database.primary_key.ProjectilePK;
import morlivm.database.query.SQLRetriever;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.RangedInt;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.warfare.DoT;
import morlivm.warfare.aftershock.Aftershock;
import morlivm.warfare.sight.SightInventory;

public class ProjectileData extends Data
{
	public static final String TABLE_NAME = "projectiles";
	
	public String name;
	public int version;
	public Dimension size;
	public Pamphlet spriteSheet;
	public Projectile.MainType mainType;
	public Projectile.SubType subType;
	public double power, speed, clock;
	public int magazine, thrownAmount, cost;
	public Projectile.Trigger trigger;
	public RangedInt collectableAmount;
	public SightInventory.Type sight;
	public boolean questItem;
	public Aftershock.Type aftershock;
	public DoT.Type mayhem;
	public Tune[] SFX;
	
	public ProjectileData(ProjectilePK pk) {
		super(pk);
		tableName = TABLE_NAME;
		download();
	}
	
	public void download() {
		//sprite
		String spriteCode = extractPropertyVARCHAR("sprite_code");
		this.spriteSheet = DataManager.retSheet(spriteCode);
		this.size = new Dimension(spriteSheet.getDimension());
		
		//type
		String main = extractPropertyVARCHAR("main_type");
		String sub = extractPropertyVARCHAR("sub_type");
		
		switch (main.toUpperCase()) {
			case "BULLET": this.mainType = Projectile.MainType.BULLET; break;
			case "BOMB": this.mainType = Projectile.MainType.BOMB; break;
			default: {
				System.err.println(main.toUpperCase() + " is not a valid main type for the projectile \"" + name + "\"");
				this.mainType = Projectile.MainType.BULLET;
			}
		}
		
		switch (sub.toUpperCase()) {
			case "AIM": this.subType = Projectile.SubType.AIM; break;
			case "STRAIGHT": this.subType = Projectile.SubType.STRAIGHT; break;
			case "PLANT": this.subType = Projectile.SubType.PLANT; break;
			case "ROLL": this.subType = Projectile.SubType.ROLL; break;
			default: {
				System.err.println(sub.toUpperCase() + " is not a valid sub type for the projectile \"" + name + "\"");
				this.subType = Projectile.SubType.STRAIGHT;
			}
		}
		
		//properties
		this.power = extractPropertyDECIMAL("power");
		this.speed = extractPropertyDECIMAL("speed");
		this.magazine = extractPropertyINT("magazine");
		this.thrownAmount = extractPropertyINT("thrown_amount");
		this.cost = extractPropertyINT("stamina_cost");
		this.clock = extractPropertyDECIMAL("clock");
		this.questItem = extractPropertyBOOLEAN("quest_item");
		
		String tempTrig = extractPropertyVARCHAR(tableName + ".trigger");
		switch (tempTrig.toUpperCase()) {
			case "COLLISION": this.trigger = Projectile.Trigger.COLLISION; break;
			case "STEP": this.trigger = Projectile.Trigger.STEP; break;
			case "IGNITION": this.trigger = Projectile.Trigger.IGNITION; break;
			default: {
				System.err.println(tempTrig.toUpperCase() + " is not a valid trigger for the projectile \"" + name + "\"");
				this.trigger = Projectile.Trigger.COLLISION;
			}
		}
		
		int minColl = extractPropertyINT("min_collectable");
		int maxColl = extractPropertyINT("max_collectable");
		this.collectableAmount = new RangedInt(minColl, maxColl);
		
		//external devices
		String tempSight = extractPropertyVARCHAR("sight");
		switch (tempSight.toUpperCase()) {
			case "NONE": this.sight = SightInventory.Type.NONE; break;
			case "UNARMED": this.sight = SightInventory.Type.UNARMED; break;
			case "SCOPE": this.sight = SightInventory.Type.SCOPE; break;
			case "REFLECTOR": this.sight = SightInventory.Type.REFLECTOR; break;
			case "REMOTE CONTROL": this.sight = SightInventory.Type.REMOTE_CONTROL; break;
			default: {
				System.err.println(tempSight.toUpperCase() + " is not a valid sight for the projectile \"" + name + "\"");
				this.sight = SightInventory.Type.UNARMED;
			}
		}
		
		//effects
		String tempShock = extractPropertyVARCHAR("aftershock");
		switch (tempShock.toUpperCase()) {
			case "NONE": this.aftershock = Aftershock.Type.NONE; break;
			case "DUST": this.aftershock = Aftershock.Type.DUST; break;
			case "EXPLOSION": this.aftershock = Aftershock.Type.EXPLOSION; break;
			case "EARTHQUAKE": this.aftershock = Aftershock.Type.EARTHQUAKE; break;
			default: {
				System.err.println(tempShock.toUpperCase() + " is not a valid aftershock for the projectile \"" + name + "\"");
				this.aftershock = Aftershock.Type.NONE;
			}
		}
		
		String tempMayhem = extractPropertyVARCHAR("mayhem");
		switch (tempMayhem.toUpperCase()) {
			case "NONE": this.mayhem = DoT.Type.NONE; break;
			case "POISON": this.mayhem = DoT.Type.POISON; break;
			case "BURN": this.mayhem = DoT.Type.BURN; break;
			default: {
				System.err.println(tempMayhem.toUpperCase() + " is not a valid mayhem for the projectile \"" + name + "\"");
				this.mayhem = DoT.Type.NONE;
			}
		}
		
		//sound
		this.SFX = extractSound();
	}
	
	private Tune[] extractSound() {
		ArrayList<String> paths = SQLRetriever.retListVARCHAR(
			buildForeignQuery("path", "sound", "id", "proj_sound", "sound_id", false), "path");
		
		ArrayList<String> desc = SQLRetriever.retListVARCHAR(
			buildForeignQuery("description", "sound", "id", "proj_sound", "sound_id", false), "description");
		
		ArrayList<String> tempGenre = SQLRetriever.retListVARCHAR(
			buildForeignQuery("type", "sound", "id", "proj_sound", "sound_id", false), "type");
		
		ArrayList<Boolean> loopable = SQLRetriever.retListBOOLEAN(
			buildForeignQuery("loopable", "sound", "id", "proj_sound", "sound_id", false), "loopable");
		
		
		Sound.Clique[] clique = new Sound.Clique[desc.size()];
		List<Sound.Clique> originCliques = Arrays.asList(Sound.Clique.values()); //PROB
		for (int i = 0; i < desc.size(); i++) {
			for (Sound.Clique c : originCliques) {
				if (desc.get(i).equals(c.name)) {
					clique[i] = c;
				}
			}
		}
		
		Sound.Genre[] genre = new Sound.Genre[tempGenre.size()];
		for (int i = 0; i < genre.length; i++) {
			switch (tempGenre.get(i).toUpperCase()) {
				case "SFX": genre[i] = Sound.Genre.SFX; break;
				case "BGM": genre[i] = Sound.Genre.BGM; break;
				default: genre[i] = Sound.Genre.SFX;
			}
		}
		
		int[] counters = new int[Sound.Clique.values().length];
		for (int i = 0; i < counters.length; i++) counters[i] = 1;
		
		String tempName;
		Tune[] tempSFX = new Tune[paths.size()];
		for (int i = 0; i < tempSFX.length; i++) {
			tempName = clique[i].name.substring(2) + " " + counters[clique[i].getIndex()]++;
			tempSFX[i] = new Tune(tempName, paths.get(i), clique[i], genre[i], loopable.get(i));
		}
		
		return tempSFX;
	}
	
	private String buildForeignQuery(String property, String originTable, String originCol,
			   						 String connectorTable, String connectorCol, boolean involveVersion) {
		
		char acronyms = 'a';
		char originAcronyms = acronyms++;
		char connectorAcronyms = acronyms++;
		
		String temp = new String(
			  "SELECT " + originAcronyms + "." + property + " "
			+ "FROM " + connectorTable + " " + connectorAcronyms + " "
			+ "INNER JOIN " + originTable + " " + originAcronyms + " "
			+ "ON " + originAcronyms + "." + originCol + " = " + connectorAcronyms + "." + connectorCol + " "
			+ "WHERE " + connectorAcronyms + "." + "proj_name = '" + name + "'");
		
		String ending;
		if (!involveVersion) ending = ";";
		else ending = " AND " + connectorAcronyms + "." + "proj_version = " + version + ";";
		
		return temp + ending;
	}
	
	protected String condition() {
		return    "INNER JOIN proj_versions pn ON pn.proj_name = " + tableName + ".name " 
				+ "WHERE proj_name = '" + name + "' AND version = " + version + ";";
	}

	protected void extractPrimaryKey(PrimaryKey pk) {
		this.name = (String) pk.getKeyComponent(0);
		this.version = ((Integer) pk.getKeyComponent(1)).intValue();
	}
}