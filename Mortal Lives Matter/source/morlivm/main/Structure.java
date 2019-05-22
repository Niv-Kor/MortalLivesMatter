package morlivm.main;
import morlivm.database.DataManager;
import morlivm.database.DataManager.Map;
import morlivm.database.LevelData;
import morlivm.system.math.Index;

public class Structure
{
	private final static int LEVELS_SET = 3;
	
	private static int map, level;
	
	public static void init() {
		level = 0;
		map = 0;
	}
	
	public static void levelUp() {
		if (++level > LEVELS_SET) {
			level = 0;
			map++;
		}
	}
	
	public static void setStruct(int m, int l) {
		map = m;
		level = l;
	}
	
	public static void setStruct(Map m, int l) {
		setStruct(m.getIndex(), l - 1);
	}
	
	public static boolean isSetTo(int m, int l) {
		return map == m && level == l;
	}
	
	public static int getLevel() { return level; }
	public static int getMap() { return map; }
	public static boolean atBossMap() { return level == LEVELS_SET; }
	public static Index getIndex() { return new Index(map, level); }
	public static LevelData getDatabase() { return DataManager.retLevel(getIndex()); }
}