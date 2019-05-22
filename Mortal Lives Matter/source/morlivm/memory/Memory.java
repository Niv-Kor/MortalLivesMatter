package morlivm.memory;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class Memory
{
	public static enum Element {
		BACKPACK(0),
		BGM(1),
		SFX(2),
		KEYS(3),
		KEYSETS(4);
		
		public int num;
		public String data;
		
		public boolean compareData(String newData) {
			return newData.equals(data);
		}
		
		public void write(String data) {
			this.data = new String(data);
		}
		
		private Element(int num) {
			this.num = num;
		}
	}
	
	public final static String DIRECTORY = "res/Memory/";
	
	private static File[] files;
	private static RandomAccessFile randAccFile;
	
	public static void init() throws IOException {
		List<Element> enumList = Arrays.asList(Element.class.getEnumConstants());
		files = new File[Element.values().length];
		
		for (int i = 0; i < enumList.size(); i++)
			files[i] = new File(DIRECTORY + enumList.get(i).name() + ".txt");
		
		loadData(enumList);
	}
	
	private static void loadData(List<Element> list) {
		for (int i = 0; i < list.size(); i++)
			list.get(i).data = read(list.get(i));
	}
	
	private static boolean overwrite(String data, Element element) {
		if (element.compareData(data)) return false;
		else {
			try {
				randAccFile = new RandomAccessFile(files[element.num].getPath(), "rw");
				randAccFile.writeBytes(data);
				randAccFile.close();
				element.write(data);
				return true;
			} catch(IOException e) {
				System.out.println("ERROR: Data type " + element.name() + " could not be saved");
				return false;
			}
		}
	}
	
	private static String read(Element element) {
		String data;
		
		try {
			randAccFile = new RandomAccessFile(files[element.num].getPath(), "rw");
			data = randAccFile.readLine();
			randAccFile.close();
			return data;
		} catch(IOException e) {
			System.out.println("ERROR: Data type " + element.name() + " could not be loaded");
			return null;
		}
	}
	
	public static void save(String data, Element element) {
		overwrite(data, element);
	}
	
	public static void save(int data, Element element) {
		String str = processInt(data);
		overwrite(str, element);
	}
	
	public static void save(long data, Element element) {
		String str = processLong(data);
		overwrite(str, element);
	}
	
	public static void save(double data, Element element) {
		String str = processDouble(data);
		overwrite(str, element);
	}
	
	public static void save(float data, Element element) {
		String str = processFloat(data);
		overwrite(str, element);
	}
	
	public static String loadString(Element element) {
		return read(element);
	}
	
	public static int loadInt(Element element) {
		return restoreInt(read(element));
	}
	
	public static long loadLong(Element element) {
		return restoreLong(read(element));
	}
	
	public static double loadDouble(Element element) {
		return restoreDouble(read(element));
	}
	
	public static float loadFloat(Element element) {
		return restoreFloat(read(element));
	}
	
	public static String processInt(int num) {
		return Integer.toBinaryString(num);
	}
	
	public static String processLong(long num) {
		return Long.toBinaryString(num);
	}
	
	public static String processDouble(double num) {
		return Double.toHexString(num);
	}
	
	public static String processFloat(Float num) {
		return Float.toHexString(num);
	}
	
	public static int restoreInt(String str) {
		if (str == null) return 0;
		
		int value = 0;
		
		for (int i = 0, j = str.length() - 1; i < str.length(); i++, j--)
			if (str.charAt(i) == '1') value += Math.pow(2, j);
		
		return value;
	}
	
	public static long restoreLong(String str) {
		if (str == null) return 0;
		
		long value = 0;
		
		for (int i = 0, j = str.length() - 1; i < str.length(); i++, j--)
			if (str.charAt(i) == '1') value += Math.pow(2, j);
		
		return value;
	}
	
	public static double restoreDouble(String str) {
		if (str == null) return 0;
		else return Double.parseDouble(str);
	}
	
	public static float restoreFloat(String str) {
		if (str == null) return 0;
		else {
			try { return Float.valueOf(str); }
			catch (NumberFormatException e) { return 0; }
		}
	}
}