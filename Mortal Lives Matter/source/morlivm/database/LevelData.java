package morlivm.database;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import morlivm.database.primary_key.LevelPK;
import morlivm.database.primary_key.PrimaryKey;
import morlivm.database.query.SQLRetriever;
import morlivm.map.Boundary;
import morlivm.map.Habitat;
import morlivm.map.Incline;
import morlivm.map.parallex.Ground;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.RNG;
import morlivm.system.math.RangedInt;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class LevelData extends Data
{
	public static class WavePattern
	{
		private RangedInt amount;
		private List<MortalData> enemies, minions;
		private int counter;
		
		public WavePattern(RangedInt amount, RangedInt times) {
			this.amount = new RangedInt(amount);
			this.counter = times.generate();
			this.enemies = new LinkedList<MortalData>();
			this.minions = new LinkedList<MortalData>();
		}
		
		public void insertEnemies(List<Integer> enemyID) {
			for (Integer id : enemyID)
				enemies.add(DataManager.retMortal(id.intValue()));
		}
		
		public void insertMinions(List<Integer> enemyID) {
			for (Integer id : enemyID)
				minions.add(DataManager.retMortal(id.intValue()));
		}
		
		public Queue<MortalData> generate() {
			Queue<MortalData> queue = new LinkedList<MortalData>();
			int calcAmount = amount.generate();
			
			for (int i = 0; i < calcAmount; i++)
				queue.add(enemies.get(RNG.generate(0, enemies.size() - 1)));
			
			return queue;
		}
		
		public Queue<MortalData> generateMinions() {
			Queue<MortalData> queue = new LinkedList<MortalData>();
			int calcAmount = amount.generate();
			
			for (int i = 0; i < calcAmount; i++)
				queue.add(minions.get(RNG.generate(0, minions.size() - 1)));
			
			return queue;
		}
		
		public int getCounter() { return counter; }
		public boolean hasMinions() { return minions.size() > 0; }
		public boolean useOne() { return --counter >= 0; }
	}
	
	public static final String TABLE_NAME = "levels";
	
	public String mapName;
	public int level;
	public int naturalHumidity;
	public int baseBottomLim, rightWall;
	public double gliding;
	public BufferedImage ground, cliff, ceiling, closeView, farView, sky;
	public WavePattern[] waves;
	public RangedInt degrees;
	public Habitat.Scenery habitatScenery;
	public List<Incline> inclines;
	public Queue<MortalData> bosses;
	public Tune[] SFX;
	
	private List<Pamphlet> partialObstacles;
	private List<Boundary> completeObstacles;
	
	public LevelData(LevelPK pk) {
		super(pk);
		this.tableName = TABLE_NAME;
		download();
	}

	protected void download() {
		//geo images and general properties
		this.ground = ImageHandler.load("/maps/" + mapName + "/" + level + "/ground.png");
		this.cliff = ImageHandler.load("/maps/" + mapName + "/" + level + "/cliff.png");
		
		String ceilingPath = "/maps/" + mapName + "/" + level + "/ceiling.png";
		if (ImageHandler.test(ceilingPath)) this.ceiling = ImageHandler.load(ceilingPath);
		
		this.closeView = ImageHandler.load("/maps/" + mapName + "/" + level + "/close view.png");
		this.farView = ImageHandler.load("/maps/" + mapName + "/" + level + "/far view.png");
		this.sky = ImageHandler.load("/maps/" + mapName + "/" + level + "/sky.png");
		
		this.baseBottomLim = extractPropertyINT("base_bottom_lim");
		this.rightWall = extractPropertyINT("right_wall");
		this.gliding = extractPropertyDECIMAL("gliding");
		this.partialObstacles = extractObstacles();
		this.inclines = extractInclines();
		this.waves = extractWaves();
		
		//habitat
		String scenery = extractPropertyVARCHAR("habitat");
		
		List<Habitat.Scenery> sceneList = Arrays.asList(Habitat.Scenery.values());
		for (Habitat.Scenery s : sceneList) {
			if (s.name().equals(scenery.toUpperCase())) {
				this.habitatScenery = s;
				break;
			}
		}

		if (habitatScenery == null)
			System.err.println("Could not recognize the habitat scenery \"" + scenery + "\".");
		
		//climate
		int minDeg = extractPropertyINT("min_deg");
		int maxDeg = extractPropertyINT("max_deg");
		this.degrees = new RangedInt(minDeg, maxDeg);
		this.naturalHumidity = extractPropertyINT("natural_humidity");
		
		//sound
		this.SFX = extractSound();
	}
	
	private List<Incline> extractInclines() {
		List<Incline> inclines = new ArrayList<Incline>();
		String query = "SELECT * "
					 + "FROM inclines i "
					 + "WHERE i.map_name = '" + mapName + "' "
					 + "AND level = " + level + " "
					 + "ORDER BY i.x ASC;";
		
		List<Integer> x = SQLRetriever.retListINT(query, "x");
		List<Double> angle = SQLRetriever.retListDECIMAL(query, "angle");
		List<Integer> depth = SQLRetriever.retListINT(query, "ground_depth");
		
		Incline prev;
		for (int i = 0, dist; i < x.size(); i++) {
			//previous incline
			if (i == 0) prev = null;
			else prev = inclines.get(i - 1);
			
			//distance
			if (i == x.size() - 1) dist = (rightWall + 100) - x.get(i).intValue();
			else dist = x.get(i + 1).intValue() - x.get(i);
			
			inclines.add(new Incline(x.get(i), dist, angle.get(i), depth.get(i), prev, this));
		}
		
		return inclines;
	}
	
	private Tune[] extractSound() {
		List<String> paths = SQLRetriever.retListVARCHAR(
			buildForeignQuery("path", "sound", "id", "map_sound", "sound_id"), "path");
		
		List<String> desc = SQLRetriever.retListVARCHAR(
			buildForeignQuery("description", "sound", "id", "map_sound", "sound_id"), "description");
		
		List<String> tempGenre = SQLRetriever.retListVARCHAR(
			buildForeignQuery("type", "sound", "id", "map_sound", "sound_id"), "type");
		
		List<Boolean> loopable = SQLRetriever.retListBOOLEAN(
			buildForeignQuery("loopable", "sound", "id", "map_sound", "sound_id"), "loopable");
		
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
			+ "WHERE " + connectorAcronyms + "." + "map_name = '" + mapName + "' "
			+ "AND " + connectorAcronyms + "." + "level = " + level + ";");
	}
	
	private List<Pamphlet> extractObstacles() {
		List<Pamphlet> obs = new ArrayList<Pamphlet>();
		
		List<String> obsCodes = SQLRetriever.retListVARCHAR(
			  "SELECT o.sheet_code "
			+ "FROM obstacles o "
			+ "WHERE map_name = '" + mapName + "' "
			+ "AND level = " + level + ";"
			, "sheet_code");
		
		for (String code : obsCodes)
			obs.add(DataManager.retSheet(code));
		
		return obs;
	}
	
	private WavePattern[] extractWaves() {
		int wavesAmount, minAmount, maxAmount, minTimes, maxTimes;
		boolean isMinion, isBoss;
		bosses = new LinkedList<MortalData>();
		List<Integer> possibleEnemies;
		List<Integer> regEnemies = new LinkedList<Integer>();
		List<Integer> minions = new LinkedList<Integer>();
		String query;
		
		wavesAmount = SQLRetriever.retINT(
			  "SELECT MAX(w.index) AS 'waves_amount' "
			+ "FROM waves w "
			+ "WHERE w.map_name = '" + mapName + "' "
			+ "AND w.level = " + level + ";"
			, "waves_amount");
		
		WavePattern[] waves = new WavePattern[wavesAmount];
		for (int i = 0; i < wavesAmount; i++) {
			query = "SELECT * "
				  + "FROM waves w "
				  + "WHERE w.map_name = '" + mapName + "' "
				  + "AND w.level = " + level + " "
				  + "AND w.index = " + (i + 1) + ";";
			
			minAmount = SQLRetriever.retINT(query, "min_amount");
			maxAmount = SQLRetriever.retINT(query, "max_amount");
			minTimes = SQLRetriever.retINT(query, "min_times");
			maxTimes = SQLRetriever.retINT(query, "max_times");
			
			//level does not have a wave at i
			if (minAmount == SQLRetriever.NaN || maxAmount == SQLRetriever.NaN) continue;
			
			waves[i] = new WavePattern(new RangedInt(minAmount, maxAmount), new RangedInt(minTimes, maxTimes));
			
			//extract spawn
			possibleEnemies = SQLRetriever.retListINT(
				  "SELECT s.mortal_id "
				+ "FROM spawns s "
				+ "WHERE s.map_name = '" + mapName + "' "
				+ "AND s.level = " + level + " "
				+ "AND s.index = " + (i + 1) + ";" 
				, "mortal_id");
			
			for (Integer id : possibleEnemies) {
				//check if the monster is a minion
				isMinion = SQLRetriever.retBOOLEAN(
					  "SELECT s.is_minion "
					+ "FROM spawns s "
					+ "WHERE s.map_name = '" + mapName + "' "
					+ "AND s.level = " + level + " "
					+ "AND s.index = " + (i + 1) + " " 
					+ "AND s.mortal_id = " + id + ";"
					, "is_minion");
						
				//check if the monster is a boss monster
				isBoss = SQLRetriever.retBOOLEAN(
					  "SELECT m.is_boss "
					+ "FROM mortals m "
					+ "WHERE id = " + id + ";"
					, "is_boss");
				
				if (isBoss) bosses.add(DataManager.retMortal(id));
				
				if (isMinion) minions.add(id);
				else if (!isBoss) regEnemies.add(id);
			}
			
			waves[i].insertEnemies(regEnemies);
			waves[i].insertMinions(minions);
		}
		
		return waves;
	}
	
	protected String condition() {
		return "WHERE map_name = '" + mapName + "' AND levels.index = " + level + ";";
	}
	
	public List<Boundary> getObstacles(Ground ground) {
		if (completeObstacles == null) {
			completeObstacles = new ArrayList<Boundary>();
			
			String query = "SELECT * "
						 + "FROM obstacles o "
						 + "WHERE map_name = '" + mapName + "' "
						 + "AND level = " + level + ";";
			
			List<Integer> x = SQLRetriever.retListINT(query, "x");
			List<Integer> y = SQLRetriever.retListINT(query, "y");
			
			for (int i = 0; i < partialObstacles.size(); i++)
				completeObstacles.add(new Boundary(partialObstacles.get(i), new Point(x.get(i), y.get(i)), ground));
		}
		
		return completeObstacles;
	}
	
	protected void extractPrimaryKey(PrimaryKey pk) {
		this.mapName = (String) pk.getKeyComponent(0);
		this.level = ((Integer) pk.getKeyComponent(1)).intValue();
	}
}