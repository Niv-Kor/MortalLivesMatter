package morlivm.system.math;

public class Percent
{
	public final static int MIN = 0, MAX = 100;
	
	public static double numOfNum(double numerator, double denominator) {
		return numerator / denominator * 100;
	}
	
	public static double percentOfNum(double percent, double num) {
		return num / 100 * percent;
	}
	
	public static double limit(double num) {
		if (num > MAX) return MAX;
		else if (num < MIN) return MIN;
		else return num;
	}
	
	public static int limit(int bottomLim, int topLim, int num) {
		if (num > topLim) return topLim;
		else if (num < bottomLim) return bottomLim;
		else return num;
	}
	
	public static double limitDouble(double bottomLim, double topLim, double num) {
		if (num > topLim) return topLim;
		else if (num < bottomLim) return bottomLim;
		else return num;
	}
}