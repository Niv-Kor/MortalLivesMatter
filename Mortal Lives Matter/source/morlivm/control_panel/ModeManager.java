package morlivm.control_panel;
import java.util.LinkedList;

public class ModeManager
{
	private static LinkedList<Mode> mList = new LinkedList<Mode>();
	
	public static Mode get(String name) {
		for (Mode m : mList) if (m.getName().equals(name)) return m;
		return null;
	}
	
	public static boolean isOn(String name) {
		Mode mode = get(name);
		return (mode != null) ? mode.isOn() : false;
	}
	
	public static void activate(String name, boolean flag) { get(name).activate(flag); }
	public static void add(Mode mode) { mList.add(mode); }
	public static void remove(Mode mode) { mList.remove(mode); }
}