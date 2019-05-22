package morlivm.main.testing;

public class RecordPrinter
{
	private final static int SEPARATOR_LENGTH = 30;
	
	private static String str = new String("");
	
	public static void headline(String h) {
		refresh();
		appand("\n" + h + ":");
		separator();
	}
	
	public static void separator() {
		String separator = "";
		for (int i = 0; i < SEPARATOR_LENGTH; i++)
			separator = separator.concat("-");
		
		appand(separator);
	}
	
	public static String print() {
		String temp = new String(str);
		refresh();
		return temp;
	}
	
	public static void write(String s) { appand(s); }
	public static void refresh() { str = new String(""); }
	private static void appand(String s) { str = str.concat(s + "\n"); }
}