package morlivm.system.math;

public class RangedInt
{
	private int min, max;
	
	public RangedInt(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public RangedInt(RangedInt other) {
		this.min = other.min;
		this.max = other.max;
	}
	
	public int percent(double perc) {
		return (int) Math.round(Percent.percentOfNum(perc, max - min) + min);
	}
	
	public int getMin() { return min; }
	public int getMax() { return max; }
	public int generate() { return RNG.generate(min, max); }
	public boolean intersects(RangedInt other) { return other.min <= max && other.max >= min; }
	public String toString() { return "[" + getMin() + ", " + getMax() + "]"; }
}