package morlivm.map.weather;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RNG;
import morlivm.system.math.RangedDouble;
import morlivm.system.math.RangedInt;
import morlivm.system.performance.FPS;

public class Wind {
	private final static RangedInt DEGREES = new RangedInt(-35, 42);
	private final static RangedDouble TIMING = new RangedDouble(0.012, 0.11);
	private final static RangedDouble SPEED = new RangedDouble(0.4, 1);
	private final static RangedInt ANGLE = new RangedInt(15, 60);
	private final static RangedInt UNITS = new RangedInt(2, 20);
	
	private Physics.Vector direction;
	private double vector, timing, angle;
	private int speed, units;
	
	public Wind(int degrees, double dayLightPercent) {
		this.vector = (Percent.numOfNum(degrees - DEGREES.getMin(), DEGREES.getMax() - DEGREES.getMin()) - 50) / 50;
		
		//if it's darker outside than randomly add wind
		if (RNG.unstableCondition((int) (100 - dayLightPercent))) {
			if (vector > 0) vector += RNG.generateDouble(vector, 1);
			else vector += RNG.generateDouble(-1, vector);
		}

		init(vector);
	}
	
	public Wind(double vector) {
		init(vector);
	}
	
	public void init(double vector) {
		this.vector = Percent.limitDouble(-1, 1, vector); //-1 -> 1
		double intensity = 100 * Math.abs(vector); //0 -> 100
		
		//direction the wind blows
		direction = vector < 0 ? Vector.LEFT : Vector.RIGHT;
		
		this.timing = TIMING.percent(100 - intensity);
		this.speed = FPS.toFrames(SPEED.percent(intensity));
		this.angle = Math.toRadians(ANGLE.percent(100 - intensity));
		this.units = UNITS.percent(intensity);
	}
	
	public Vector getDirection() { return direction; }
	public double getVector() { return vector; }
	public double getTiming() { return timing; }
	public double getAngle() { return angle; }
	public int getSpeed() { return speed; }
	public int getUnits() { return units; }
}