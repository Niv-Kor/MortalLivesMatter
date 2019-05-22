package morlivm.system.math;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumeralHandler
{
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();
	 
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static int countDigits(int num) {
		if (num == 0) return 1;
		
		int counter = 0;
		
		while (num > 0) {
			counter++;
			num /= 10;
		}
		
		return counter;
	}
	
	public static Dimension max(Dimension a, Dimension b) {
		double aSize = a.width * a.height;
		double bSize = b.width * b.height;
		
		return (aSize > bSize) ? a : b;
	}
}