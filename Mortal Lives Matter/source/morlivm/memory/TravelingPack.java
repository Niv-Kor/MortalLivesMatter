package morlivm.memory;
import morlivm.content.mortal.Player;
import morlivm.control_panel.ControlPanel;

public class TravelingPack
{
	private static Player player;
	private static ControlPanel controlPanel;
	private static boolean packed;
	
	public static void pack(Player p, ControlPanel cp) {
		player = p;
		controlPanel = cp;
		packed = true;
	}
	
	public static void discard() {
		player = null;
		controlPanel = null;
		packed = true;
	}
	
	public static Player getPackedPlayer() { return player; }
	public static ControlPanel getPackedPanel() { return controlPanel; }
	public static boolean hasPackedPlayer() { return player != null && controlPanel != null; }
	public static boolean isPacked() { return packed; }
}