package morlivm.main.testing;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import morlivm.database.DataManager.Map;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Percent;

public class Tester
{
	private static class TimedPrint
	{
		private String str;
		private int setSeconds, countdown;
		
		public TimedPrint(String str, int seconds) {
			this.str = new String(str);
			this.countdown = setSeconds = Percent.limit(0, seconds, seconds) * Game.STABLE_FPS;
		}
		
		public void update() {
			if (countdown-- <= 0) {
				System.out.println(str);
				countdown = setSeconds;
			}
		}
		
		public String toString() { return str; }
	}
	
	private final static Font FONT = new Font("Arial", Font.BOLD, 14);
	
	public static List<TimedPrint> queue = new ArrayList<TimedPrint>();
	
	public static boolean allowSpawn;
	public static boolean openBarriers;
	public static boolean openPortal;
	public static boolean graphifyCursor;
	public static boolean graphifyGroundLimits;
	public static boolean graphifyBoudaries;
	public static int fpsDisplay;
	
	//weather control
	public static boolean controlWeather;
	public static double wind;
	public static boolean rain, snow, rainbow;
	
	public static void apply() {
		Structure.setStruct(Map.FOREST, 2);
		allowSpawn = true; //def true
		openBarriers = true; //def false
		openPortal = false; //def false
		graphifyCursor = false; //def false
		graphifyGroundLimits = false; //def false
		graphifyBoudaries = true; //def false
		fpsDisplay = 1; //def 1 second
		
		//weather control
		controlWeather = false; //def false
		wind = 1;
		rain = true;
		snow = false;
		rainbow = false;
	}
	
	public static void update(double delta) {
		for (TimedPrint tp : queue) tp.update();
	}
	
	public static void render(ExtendedGraphics2D g) {}
	
	public static void printAt(String str, int seconds) {
		for (TimedPrint tp : queue)
			if (tp.toString().equals(str)) return;
		
		queue.add(new TimedPrint(str, seconds));
	}
	
	public static void graphify(ExtendedGraphics2D g, Graphable component) {
		graphify(g, component, Color.GREEN);
	}
	
	public static void graphify(ExtendedGraphics2D g, Graphable component, Color c) {
		int x = (int) component.getX();
		int y = (int) component.getY();
		int width = component.getDimension().width;
		int height = component.getDimension().height;
		Collider col = new Collider(new Point(x, y), new Dimension(width, height));
		
		g.setColor(c);
		g.drawRect((int) col.getX(), (int) col.getY(), col.getDimension().width, col.getDimension().height);
		g.setColor(Color.RED);
		g.setFont(FONT);
		g.drawString("A", (int) col.getA().getX(), (int) col.getA().getY());
		g.drawString("B", (int) col.getB().getX(), (int) col.getB().getY());
		g.drawString("C", (int) col.getC().getX(), (int) col.getC().getY());
		g.drawString("D", (int) col.getD().getX(), (int) col.getD().getY());
	}
}