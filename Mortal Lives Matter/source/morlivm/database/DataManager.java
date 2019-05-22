package morlivm.database;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import morlivm.database.primary_key.LevelPK;
import morlivm.database.primary_key.MortalPK;
import morlivm.database.primary_key.PrimaryKey;
import morlivm.database.primary_key.ProjectilePK;
import morlivm.database.query.SQLRetriever;
import morlivm.system.graphics.sheet.Mask;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.graphics.sheet.Sprite;
import morlivm.system.math.Index;
import morlivm.system.math.RNG;

public class DataManager
{
	public static enum Map {
		FOREST(0),
		DESERT(1),
		ARCTIC(2);
		
		private int index;
		
		private Map(int index) {
			this.index = index;
		}
		
		public static Map translateIndex(int index) {
			switch (index) {
			case 0: return FOREST;
			case 1: return DESERT;
			case 2: return ARCTIC;
			default:
				System.err.println("Could not convert the index " + index + " to a valid map");
				return null;
		}
		}
		
		public int getIndex() { return index; }
		public String formalName() { return super.name().toLowerCase();	}
	}
	
	private static Hashtable<String, Data> knownData;
	private static Hashtable<String, Pamphlet> knownSheets;
	private static MortalData chosenPlayer;
	
	public static void init() {
		knownData = new Hashtable<String, Data>();
		knownSheets = new Hashtable<String, Pamphlet>();
	}
	
	public static MortalData retMortal(String name) {
		int extractedID = SQLRetriever.retINT(SQLRetriever.buildQuery("id", "mortals", "name", name), "id");
		return retMortal(extractedID);
	}
	
	public static MortalData retMortal(int id) {
		PrimaryKey pk = new MortalPK(Integer.valueOf(id));
		MortalData data = (MortalData) knownData.get(pk.getKey());
		
		if (data == null) {
			try {
				data = MortalData.class.getConstructor(MortalPK.class).newInstance((MortalPK) pk);
				knownData.put(pk.getKey(), data);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				System.err.println("Cannot load a new instance of " + MortalData.class.getName());
				e.printStackTrace();
			}
		}
		
		return data;
	}
	
	public static ProjectileData retProj(String name, int ver) {
		PrimaryKey pk = new ProjectilePK(name, Integer.valueOf(ver));
		ProjectileData data = (ProjectileData) knownData.get(pk.getKey());
		
		if (data == null) {
			try {
				data = ProjectileData.class.getConstructor(ProjectilePK.class).newInstance((ProjectilePK) pk);
				knownData.put(pk.getKey(), data);
			} 
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				System.err.println("Cannot load a new instance of " + ProjectileData.class.getName()
								 + " with " + name + ", " + ver);
				e.printStackTrace();
			}
		}
		
		return data;
	}
	
	public static LevelData retLevel(Index ind) {
		String name = Map.translateIndex(ind.row).formalName();
		return retLevel(name, ind.col + 1);
	}
	
	public static LevelData retLevel(String mapName, int level) {
		PrimaryKey pk = new LevelPK(mapName, Integer.valueOf(level));
		LevelData data = (LevelData) knownData.get(pk.getKey());
		
		if (data == null) {
			try {
				data = LevelData.class.getConstructor(LevelPK.class).newInstance((LevelPK) pk);
				knownData.put(pk.getKey(), data);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				System.err.println("Cannot load a new instance of " + LevelData.class.getName());
			}
		}
		
		return data;
	}
	
	public static Pamphlet retSheet(String code) {
		if (code == null) return null;
		
		Pamphlet sheet = knownSheets.get(code);
		
		if (sheet == null) {
			sheet = extractSheet(code);
			knownSheets.put(code, sheet);
		}
		
		return sheet;
	}
	
	private static Pamphlet extractSheet(String code) {
		String path;
		Sprite sprite = null;
		Mask mask = null;
		
		int width = SQLRetriever.retINT(
			  "SELECT s.width "
			+ "FROM sheets s "
			+ "WHERE code = '" + code + "';"
			, "width");
		
		int height = SQLRetriever.retINT(
			  "SELECT s.height "
			+ "FROM sheets s "
			+ "WHERE code = '" + code + "';"
			, "height");
		
		boolean hasSprite = SQLRetriever.retBOOLEAN(
		 	  "SELECT EXISTS("
			+ "SELECT sp.code "
			+ "FROM sprites sp "
			+ "WHERE code = '" + code + "') "
			+ "AS 'exists';"
			, "exists");
		
		boolean hasMask = SQLRetriever.retBOOLEAN(
		 	  "SELECT EXISTS("
			+ "SELECT m.code "
			+ "FROM masks m "
			+ "WHERE code = '" + code + "') "
			+ "AS 'exists';"
			, "exists");
		
		//sprite
		if (hasSprite) {
			path = SQLRetriever.retVARCHAR(
				  "SELECT sp.path "
				+ "FROM sprites sp "
				+ "WHERE code = '" + code + "';"
				, "path");
			
			double speed = SQLRetriever.retDECIMAL(
				  "SELECT sp.speed "
				+ "FROM sprites sp "
				+ "WHERE code = '" + code + "';"
				, "speed");
			
			sprite = new Sprite(path, new Dimension(width, height), speed);
		}
		
		//mask
		if (hasMask) {
			path = SQLRetriever.retVARCHAR(
				  "SELECT m.path "
				+ "FROM masks m "
				+ "WHERE code = '" + code + "';"
				, "path");
			
			mask = new Mask(path);
		}
		
		return new Pamphlet(code, sprite, mask);
	}
	
	public static Queue<MortalData> retAllPlayers() {
		ArrayList<String> playerNames = SQLRetriever.retListVARCHAR(
			  "SELECT m.name "
			+ "FROM mortals m "
			+ "WHERE m.playable = true;"
			, "name");
		
		Queue<MortalData> players = new LinkedList<MortalData>();
		for (String s : playerNames) players.add(retMortal(s));
		
		return players;
	}
	
	public static MortalData retChosenPlayer() {
		if (chosenPlayer != null) return chosenPlayer;
		else {
			Queue<MortalData> all = retAllPlayers();
			
			if (all.size() > 0) {
				int i, rng = RNG.generate(0, all.size() - 1);
				for (i = all.size() - 1; i > rng; i--) all.poll();
				return all.poll();
			}
			else {
				System.err.println("There are no playable mortals.");
				return null;
			}
		}
	}
	
	public static void setPlayer(MortalData playerData) { chosenPlayer = playerData; }
}