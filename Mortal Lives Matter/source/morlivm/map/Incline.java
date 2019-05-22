package morlivm.map;
import morlivm.database.LevelData;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics.Vector;

public class Incline
{
	private double mapX, angle, vector;
	private int distance, groundDepth;
	private boolean ascent, descent;
	private LevelData ldb;
	private Incline prev;
	
	public Incline(double x, int dist, double angle, int depth, Incline prev, LevelData levelDB) {
		this.mapX = x;
		this.distance = dist;
		this.groundDepth = depth;
		this.angle = Math.abs(angle);
		this.ascent = angle > 0;
		this.descent = angle < 0;
		this.prev = prev;
		this.ldb = levelDB;
		this.vector = !isPlain() ? Percent.percentOfNum(Percent.numOfNum(Math.abs(angle), 90), 1) : 0;
	}
	
	public double topLim(double x) {
		return ldb.baseBottomLim - groundDepth - currentHillHeight(x);
	}
	
	public boolean withinDistance(double x) {
		return x >= mapX && x <= mapX + distance;
	}
	
	public boolean isInclinedTowards(Vector v) {
		return (v == Vector.LEFT && isAscented() || v == Vector.RIGHT && isDescented());
	}
	
	public double hillHeight(double x) {
		double unrelatedHeight;
		
		if (isPlain()) {
			unrelatedHeight = 0;
			
			if (prev != null) return unrelatedHeight + prev.hillHeight(prev.mapX + prev.distance);
			else return unrelatedHeight;
		}
		else {
			unrelatedHeight = currentHillHeight(x);
			
			if (prev != null) return unrelatedHeight + prev.hillHeight(prev.mapX + prev.distance);
			else return unrelatedHeight;
		}
	}
	
	public double currentHillHeight(double x) {
		double spot = Percent.limitDouble(1, x - mapX, x - mapX);
		if (isDescented()) spot *= -1;
		
		return spot * Math.tan(getRadAngle());
	}
	
	public boolean isAscented() { return ascent; }
	public boolean isDescented() { return descent; }
	public boolean isPlain() { return angle == 0; }
	public double getMapX() { return mapX; }
	public int getDist() { return distance; }
	public double getVectorY() { return vector; }
	public double getDegAngle() { return angle; }
	public double getRadAngle() { return Math.toRadians(angle); }
	public int getGroundDepth() { return groundDepth; }
	public Incline getPrevious() { return prev; }
}