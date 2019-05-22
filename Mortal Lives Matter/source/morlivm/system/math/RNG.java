package morlivm.system.math;
import java.awt.Dimension;
import java.util.List;
import java.util.Random;

import morlivm.system.UI.Point;

public class RNG
{
	private static Random rng = new Random();
	
	public static int generate(int a, int b) {
		if (a == b) return a;
		if (b < a) { //swap
			int c = a;
			a = b;
			b = c;
		}
		
		//supports involvement of negative numbers
		int aTag = a - a, bTag = b - a;
		int result = rng.nextInt((bTag + 1) - aTag) + aTag;
		return -result + b;
	}
	
	public static double generateDouble(double a, double b) {
		if (a == b) return a;
		if (b < a) { //swap
			double c = a;
			a = b;
			b = c;
		}
		
		//doublize
		a /= 1.0;
		b /= 1.0;
		
		int aTag = (int) a;
		int bTag = (int) b;
		
		
		double deltaA = a - aTag, deltaB = b - bTag;
		double extraA = 0, extraB = 0;
		
		if (deltaA != 0) {
			do extraA = rng.nextDouble();
			while (extraA < Math.abs(deltaA));
		}
		
		if (deltaB != 0) {
			do extraB = rng.nextDouble();
			while (extraB > Math.abs(deltaB));
		}
		
		int result = generate(aTag, bTag);
		return result + extraA + extraB; 
	}
	
	public static Point generatePixel(Point point, Dimension dim) {
		RangedInt x = new RangedInt((int) point.getX(), (int) point.getX() + dim.width);
		RangedInt y = new RangedInt((int) point.getY(), (int) point.getY() + dim.height);
		return new Point(x.generate(), y.generate());
	}
	
	public static boolean unstableCondition() {
		return unstableCondition(generate(0, 100));
	}
	
	public static boolean unstableCondition(int percent) {
		if (percent <= 0) return false;
		else if (percent >= 100) return true;
		else return (double) percent / 100 >= rng.nextDouble();
	}
	
	public static int generateEpsilonPercentage(double epsilon, double num) {
		return RNG.generate((int) Percent.percentOfNum(100 - epsilon, num),
							(int) Percent.percentOfNum(100 + epsilon, num));
	}
	
	public static Object select(Object[] arr) { return arr[zeroToRange(arr.length)]; }
	public static <T> T select(List<T> list) { return list.get(zeroToRange(list.size())); }
	public static int select(int[] arr) { return arr[zeroToRange(arr.length)]; }
	public static double select(double[] arr) { return arr[zeroToRange(arr.length)]; }
	public static char select(char[] arr) { return arr[zeroToRange(arr.length)]; }
	public static float select(float[] arr) { return arr[zeroToRange(arr.length)]; }
	private static int zeroToRange(int arrLength) { return generate(0, arrLength - 1); }
}