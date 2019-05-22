package morlivm.map.orientation;
import java.util.ArrayList;
import java.util.List;
import morlivm.content.mortal.Mortal;
import morlivm.database.LevelData;
import morlivm.main.Structure;
import morlivm.map.Incline;
import morlivm.map.Magnet;
import morlivm.map.parallex.Ground;
import morlivm.state.GameState;
import morlivm.system.math.Physics.Vector;

public class Topology
{
	private static Ground ground;
	private static LevelData levelDB;
	private static List<Incline> iList;
	private static Mortal player;
	
	public static void init(GameState gs) {
		levelDB = Structure.getDatabase();
		ground = gs.getArena().getGround();
		iList = new ArrayList<Incline>();
		
		for (Incline i : levelDB.inclines) iList.add(i);
	}
	
	public static double bottomLim(double x) {
		return topLim(x) + groundDepth(x);
	}
	
	public static double topLim(double x) {
		double calc = levelDB.baseBottomLim - groundDepth(x) - overallHillHeight(x) + Magnet.getDifference(ground).getY();
		
		//add the difference between groundY of player to groundY of relevant x
		//player can move the ground vertically, but other mortals cannot be aware of the change
		if (player != null) calc += Math.abs(groundY(player.getMidX()) - groundY(x));
		
		return calc;
	}
	
	public static double vectorY(double x, Vector directX) {
		Incline inc = relevantIncline(x);
		return inc.getVectorY() * subDirectY(inc, x, directX).straight();
	}
	
	public static double overallHillHeight(double x) {
		return relevantIncline(x).hillHeight(x + Math.abs(ground.getX()));
	}
	
	private static Vector subDirectY(Incline inc, double x, Vector directX) {
		if (directX == Vector.NONE || inc.isPlain()) return Vector.NONE;
		
		if (inc.isAscented()) {
			switch(directX) {
				case LEFT: return Vector.DOWN;
				case RIGHT: return Vector.UP;
				default: return Vector.NONE;
			}
		}
		else {
			switch(directX) {
				case LEFT: return Vector.UP;
				case RIGHT: return Vector.DOWN;
				default: return Vector.NONE;
			}
		}
	}
	
	public static int groundDepth(double x) {
		return relevantIncline(x).getGroundDepth();
	}
	
	public static double groundY(double x) {
		return overallHillHeight(x) / 2;
	}
	
	public static Incline relevantIncline(double x) {
		x += Math.abs(ground.getX());
		for (Incline i : iList) if (i.withinDistance(x)) return i;
		return new Incline(x, Structure.getDatabase().rightWall, 0, 0, null, levelDB);
	}
	
	public static void setRulerEntity(Mortal m) {
		player = m;
	}
}