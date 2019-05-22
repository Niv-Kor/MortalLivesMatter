package morlivm.database;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import morlivm.content.Entity;
import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.Player;
import morlivm.database.primary_key.MortalPK;
import morlivm.database.primary_key.PrimaryKey;
import morlivm.database.query.SQLRetriever;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.TableOriented;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.warfare.Arsenal;
import morlivm.warfare.Attack;
import morlivm.warfare.DoT;
import morlivm.warfare.damage_filter.Element;
import morlivm.warfare.damage_filter.Stats;

public class MortalData extends Data
{
	public static enum ClassType {
		UNDEFINED, ENEMY, ARCHER, ROGUE;
	}
	
	public static final String TABLE_NAME = "mortals";
	
	public int ID;
	public String name;
	public boolean playable, isBoss;
	public Entity.Genus genus;
	public Pamphlet spriteSheet;
	public Entity.Size size;
	public ClassType classType;
	public Arsenal arsenal;
	public ProjectileData[] projectiles;
	public Collider head, mass, legs;
	public Stats stats;
	public Element.Type[] elements;
	public DoT.Type[] immuneSystem;
	public Tune[] SFX;
	
	public MortalData(MortalPK pk) {
		super(pk);
		this.tableName = TABLE_NAME;
		this.name = extractPropertyVARCHAR("name");
		
		download();
	}
	
	public void download() {
		//main properties
		String className = extractPropertyVARCHAR("class");
		for (ClassType ct : ClassType.values())
			if (className.toUpperCase().equals(ct.name())) this.classType = ct;
		
		if (classType == null) {
			System.err.println("The class \"" + className + "\" does not exist.");
			classType = ClassType.UNDEFINED;
		}
		
		this.playable = extractPropertyBOOLEAN("playable");
		this.isBoss = extractPropertyBOOLEAN("is_boss");
		
		String genusType = extractPropertyVARCHAR("genus");
		switch (genusType.toUpperCase()) {
			case "EARTH": this.genus = Entity.Genus.EARTH; break;
			case "AERO": this.genus = Entity.Genus.AERO; break;
			case "AQUEOUS": this.genus = Entity.Genus.AQUEOUS; break;
			case "AMPHIBIC": this.genus = Entity.Genus.AMPHIBIAN; break;
			default: {
				System.err.println("Could not recognize the type " + genusType.toUpperCase() + ".");
				this.genus = Entity.Genus.EARTH;
			}
		}
		
		//size
		String sizeSymbol = extractPropertyVARCHAR("size");
		switch (sizeSymbol) {
			case "S": this.size = Entity.Size.SMALL; break;
			case "M": this.size = Entity.Size.MEDIUM; break;
			case "L": this.size = Entity.Size.LARGE; break;
			case "XL": this.size = Entity.Size.XLARGE; break;
			case "HUGE": this.size = Entity.Size.HUGE; break;
			default: {
				System.err.println("Could not recognize the size " + sizeSymbol.toUpperCase() + ".");
				this.size = Entity.Size.SMALL;
			}
		}
		
		//sprite sheet
		String spriteCode = extractPropertyVARCHAR("sprite_code");
		this.spriteSheet = DataManager.retSheet(spriteCode);
		
		//arsenal
		this.arsenal = extractArsenal();
		
		List<Attack> attacks = arsenal.getAll(Attack.Combat.PROJECTILE);
		this.projectiles = new ProjectileData[attacks.size()];
		ProjectileData pdb;
		boolean isDefault;
		
		//find the default proj and insert at the beginning
		for (int i = 0; i < attacks.size(); i++) {
			pdb = attacks.get(i).getProjDatabase();
			isDefault = SQLRetriever.retBOOLEAN(
				  "SELECT a.default "
				+ "FROM arsenal a "
				+ "WHERE mortal_id = " + ID + " "
				+ "AND proj_name = '" + pdb.name + "';" 
				, "default");
			
			if (isDefault) {
				projectiles[0] = pdb;
				attacks.remove(i);
				break;
			}
		}
		
		//insert rest
		int index = projectiles.length != attacks.size() ? 1 : 0; //check if projectiles has the default proj first
		for (int i = 0, j = index; i < attacks.size(); i++, j++)
			projectiles[j] = attacks.get(i).getProjDatabase();
		
		//body parts
		int x_st, x_en, y_st, y_en;
		
		x_st = extractPropertyINT("head_x_st");
		x_en = extractPropertyINT("head_x_en");
		y_st = extractPropertyINT("head_y_st");
		y_en = extractPropertyINT("head_y_en");
		this.head = new Collider(new Point(x_st, y_st), new Dimension(x_en - x_st, y_en - y_st));
		
		x_st = extractPropertyINT("mass_x_st");
		x_en = extractPropertyINT("mass_x_en");
		y_st = extractPropertyINT("mass_y_st");
		y_en = extractPropertyINT("mass_y_en");
		this.mass =  new Collider(new Point(x_st, y_st), new Dimension(x_en - x_st, y_en - y_st));
		
		x_st = extractPropertyINT("legs_x_st");
		x_en = extractPropertyINT("legs_x_en");
		y_st = extractPropertyINT("legs_y_st");
		y_en = extractPropertyINT("legs_y_en");
		this.legs =  new Collider(new Point(x_st, y_st), new Dimension(x_en - x_st, y_en - y_st));
		
		//stats
		int offence = extractPropertyINT("stats_offense");
		int defense = extractPropertyINT("stats_defense");
		int health = extractPropertyINT("stats_health");
		int stamina = extractPropertyINT("stats_stamina");
		int mana = extractPropertyINT("stats_mana");
		int accuracy = extractPropertyINT("stats_accuracy");
		int kbrate = extractPropertyINT("stats_kbrate");
		double agility = extractPropertyDECIMAL("stats_agility");
		boolean shiled = extractPropertyBOOLEAN("stats_shield");
		this.stats = new Stats(offence, defense, shiled, health, mana, stamina, agility, accuracy, kbrate);
		
		//elements
		List<String> tempElementList = new ArrayList<String>();
		String tempElementName;
		
		for (int i = 1; i <= 3; i++) {
			tempElementName = extractPropertyVARCHAR("element_" + i);
			if (tempElementName != null) tempElementList.add(tempElementName);
		}
		
		Element.Type tempElementType;
		this.elements = new Element.Type[tempElementList.size()];
		for (int i = 0; i < elements.length; i++) {
			tempElementName = tempElementList.get(i);
			
			switch(tempElementName.toUpperCase()) {
				case "DIVINE": tempElementType = Element.Type.DIVINE; break;
				case "LUNAR": tempElementType = Element.Type.LUNAR; break;
				case "NATURE": tempElementType = Element.Type.NATURE; break;
				default: tempElementType = Element.Type.NONE;
			}
			
			elements[i] = tempElementType;
		}
		
		//immune system
		this.immuneSystem = extractImmunities();
		
		//sound
		this.SFX = extractSound();
	}
	
	private Tune[] extractSound() {
		ArrayList<String> paths = SQLRetriever.retListVARCHAR(
			buildForeignQuery("path", "sound", "id", "mortal_sound", "sound_id"), "path");
		
		ArrayList<String> desc = SQLRetriever.retListVARCHAR(
			buildForeignQuery("description", "sound", "id", "mortal_sound", "sound_id"), "description");
		
		ArrayList<String> tempGenre = SQLRetriever.retListVARCHAR(
			buildForeignQuery("type", "sound", "id", "mortal_sound", "sound_id"), "type");
		
		ArrayList<Boolean> loopable = SQLRetriever.retListBOOLEAN(
			buildForeignQuery("loopable", "sound", "id", "mortal_sound", "sound_id"), "loopable");
		
		Sound.Clique[] clique = new Sound.Clique[desc.size()];
		List<Sound.Clique> originCliques = Arrays.asList(Sound.Clique.values());
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
				default: {
					System.err.println("The genre " + tempGenre.get(i).toUpperCase() + " does not exist");
					genre[i] = Sound.Genre.SFX;
				}
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
				 					 String connectorTable, String connectorCol) {
		
		char acronyms = 'a';
		char originAcronyms = acronyms++;
		char connectorAcronyms = acronyms++;
		
		return new String(
		  "SELECT " + originAcronyms + "." + property + " "
		+ "FROM " + connectorTable + " " + connectorAcronyms + " "
		+ "INNER JOIN " + originTable + " " + originAcronyms + " "
		+ "ON " + originAcronyms + "." + originCol + " = " + connectorAcronyms + "." + connectorCol + " "
		+ "WHERE " + connectorAcronyms + "." + "mortal_id = " + ID + ";");
	}
	
	private Arsenal extractArsenal() {
		String query = "SELECT * "
					 + "FROM attacks a "
					 + "WHERE a.mortal_id = " + ID + ";";
		
		Arsenal arsenal = new Arsenal(this);
		ProjectileData[] projectiles = extractProjectiles();
		ArrayList<Integer> originStates = SQLRetriever.retListINT(query, "index");
		TableOriented[] states = convertStates(originStates);
		ArrayList<Integer> launchHeight = SQLRetriever.retListINT(query, "launch_height");
		ArrayList<Integer> climax = SQLRetriever.retListINT(query, "climax");
		ArrayList<Double> strength = SQLRetriever.retListDECIMAL(query, "strength");
		ArrayList<Double> speed = SQLRetriever.retListDECIMAL(query, "speed");
		ArrayList<Boolean> isMelee = SQLRetriever.retListBOOLEAN(query, "melee");
		ArrayList<Boolean> isDefault = SQLRetriever.retListBOOLEAN(query, "default");
		
		for (int i = 0; i < launchHeight.size(); i++) {
			//get all projectiles that match this state from arsenal
			if (!isMelee.get(i)) {
				List<String> projNamesPerState = SQLRetriever.retListVARCHAR(
					"SELECT proj_name "
				  + "FROM arsenal "
				  + "WHERE proj_state = " + originStates.get(i) + ";"
				  , "proj_name");
				
				//find a match in the projectiles array
				for (int j = 0; j < projNamesPerState.size(); j++) {
					for (int k = 0; k < projectiles.length; k++) {
						if (projectiles[k] != null && projNamesPerState.get(j).equals(projectiles[k].name)) {
							arsenal.learnMove(new Attack(states[i], climax.get(i), speed.get(i),
														 strength.get(i), launchHeight.get(i),
														 isDefault.get(i)), projectiles[k]);
							
							projectiles[k] = null;
						}
					}
				}
			}
			else { //the state does not invlove any projectiles
				arsenal.learnMove(new Attack(states[i], climax.get(i), speed.get(i),
											 strength.get(i), launchHeight.get(i), isDefault.get(i)));
			}
		}
		
		return arsenal;
	}

	private ProjectileData[] extractProjectiles() {
		ArrayList<String> tempProjNames = SQLRetriever.retListVARCHAR(
			  "SELECT a.proj_name "
			+ "FROM arsenal a "
			+ "INNER JOIN mortals m ON m.id = a.mortal_id "
			+ "WHERE m.id = " + ID + ";"
			, "proj_name");
		
		ArrayList<Integer> tempProjVers = SQLRetriever.retListINT(
			  "SELECT a.proj_version "
			+ "FROM arsenal a "
			+ "INNER JOIN mortals m ON m.id = a.mortal_id "
			+ "WHERE m.id = " + ID + ";"
			, "proj_version");
		
		ProjectileData[] projData = new ProjectileData[tempProjNames.size()];
		for (int i = 0; i < tempProjNames.size(); i++)
			projData[i] = DataManager.retProj(tempProjNames.get(i), tempProjVers.get(i));
		
		return projData;
	}
	
	private TableOriented[] convertStates(ArrayList<Integer> states) {
		int[] temp = new int[states.size()];
		for (int i = 0; i < temp.length; i++)
			temp[i] = states.get(i);
		
		return convertStates(temp);
	}
	
	private TableOriented[] convertStates(int[] states) {
		TableOriented[] projStates = null;
		
		if (playable) {
			projStates = new Player.State[states.length];
			for (int i = 0; i < projStates.length; i++) {
				switch (states[i]) {
					case 1: projStates[i] = Player.State.ATTACK_1; break;
					case 2: projStates[i] = Player.State.ATTACK_2; break;
					case 3: projStates[i] = Player.State.ATTACK_3; break;
					case 4: projStates[i] = Player.State.ATTACK_4; break;
					default: {
						System.err.println("Unexpected projectile state of " + states[i]);
						projStates[i] = Player.State.ATTACK_1;
					}
				}
			}
		}
		else {
			projStates = new Enemy.State[states.length];
			for (int i = 0; i < projStates.length; i++) {
				switch (states[i]) {
					case 1: projStates[i] = Enemy.State.ATTACK_1; break;
					case 2: projStates[i] = Enemy.State.ATTACK_2; break;
					case 3: projStates[i] = Enemy.State.ATTACK_3; break;
					case 4: projStates[i] = Enemy.State.ATTACK_4; break;
					default: {
						System.err.println("Unexpected projectile state of " + states[i]);
						projStates[i] = Enemy.State.ATTACK_1;
					}
				}
			}
		}
		
		return projStates;
	}
	
	private DoT.Type[] extractImmunities() {
		String tempImmunityName;
		ArrayList<String> tempImmunityList = SQLRetriever.retListVARCHAR(
				  "SELECT i.immunity "
				+ "FROM immune_system i "
				+ "INNER JOIN mortals m ON m.id = i.mortal_id "
				+ "WHERE m.id = " + ID + ";"
				, "immunity");
		
		DoT.Type[] immuneSys = new DoT.Type[tempImmunityList.size()];
		for (int i = 0; i < immuneSys.length; i++) {
			tempImmunityName = tempImmunityList.get(i);
			
			switch(tempImmunityName.toUpperCase()) {
				case "POISON": immuneSys[i] = DoT.Type.POISON; break;
				case "BURN": immuneSys[i] = DoT.Type.BURN; break;
			}
		}
		
		return immuneSys;
	}
	
	protected String condition() {
		return "WHERE id = " + ID + ";";
	}

	protected void extractPrimaryKey(PrimaryKey pk) {
		this.ID = ((Integer) pk.getKeyComponent(0)).intValue();
	}
}