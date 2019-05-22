package morlivm.system.math;

public class RangedDouble
{
	private double min, max;
	
	public RangedDouble(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	public RangedDouble(RangedDouble other) {
		this.min = other.min;
		this.max = other.max;
	}
	
	public double percent(double perc) {
		return Percent.percentOfNum(perc, max - min) + min;
	}
	
	public double getMin() { return min; }
	public double getMax() { return max; }
	public double generate() { return RNG.generateDouble(min, max); }
	public boolean intersects(RangedDouble other) { return other.min <= max && other.max >= min; }
	public String toString() { return "[" + NumeralHandler.round(getMin(), 2) + ", " + NumeralHandler.round(getMax(), 2) + "]"; }
}