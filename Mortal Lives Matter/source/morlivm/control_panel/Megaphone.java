package morlivm.control_panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedList;

import morlivm.main.Game;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class Megaphone
{
	private static class Message implements Graphable
	{
		private final static Font FONT = new Font("Arial", Font.BOLD, 16);
		private final static Point START_P = new Point(Game.WIDTH / 2, 200);
		public final static Color DEF_COLOR = Color.WHITE, HIGHLIGHT_COLOR = Color.RED;
		
		private TimingDevice timingDevice;
		private int transparency;
		private Point point;
		private String msg;
		private Color color;
		private boolean perm, vanishMsg;
		
		public Message(String msg, boolean perm) {
			this.msg = new String(msg);
			this.point = new Point(START_P);
			this.transparency = 0xFF;
			this.perm = perm;
			this.color = perm ? HIGHLIGHT_COLOR : DEF_COLOR;
			this.timingDevice = new TimingDevice();
			point.setX(point.getX() - (msg.length() * FONT.getSize() / 4) + 10);
			timingDevice.addTimer("vanish", 1);
			timingDevice.addTimer("fade", 0.03);
		}

		public void update(double delta) {
			Timer fade;
			timingDevice.setClocks();
			
			if (!vanishMsg) {
				if (timingDevice.getTimer("vanish").progressedToRoof())
					vanishMsg = true;
				return;
			}
			
			fade = timingDevice.getTimer("fade");
			if (!perm && fade.progressedToRoof()) {
				if (transparency > 0) transparency -= 5;
				else Megaphone.dismiss(msg);
				fade.init();
			}
		}
		
		public void render(ExtendedGraphics2D g) {
			g.setFont(FONT);
			Color outline = perm ? new Color(0, 0, 0, transparency) : new Color(120, 120, 120, transparency);
			g.drawOutlineString(msg, point, 1, new Color(color.getRed(), color.getGreen(),
								color.getBlue(), transparency), outline);
		}
		
		public void lower(int times) {
			point.setY(point.getY() + (times) * (FONT.getSize() + 5));
		}
		
		public boolean transparentEnough() { return transparency <= 50; }
		public String getText() { return msg; }
		public void setDimension(Dimension d) {}
		public Dimension getDimension() { return new Dimension(FONT.getSize(), FONT.getSize()); }
		public Point getPoint() { return point; }
		public void setX(double x) { point.setX(x); }
		public void setY(double y) { point.setY(y); }
		public double getX() { return point.getX(); }
		public double getY() { return point.getY(); }
	}
	
	private final static int MAX = 5;
	
	private static LinkedList<Message> mList;
	
	public static void init() {
		mList = new LinkedList<Message>();
	}
	
	public static void update(double delta) {
		for (Message m : mList) m.update(delta);
	}
	
	public static void render(ExtendedGraphics2D g) {
		for (Message m : mList) m.render(g);
	}
	
	public static void announce(String s) {
		if (exists(s)) return;
		Message tmpM = new Message(s, false);
		
		if (mList.size() > MAX) mList.removeLast(); 
		
		if (mList.size() > 0) {
			for (int i = 0; i < mList.size(); i++) {
				if (mList.get(i).transparentEnough()) {
					tmpM.lower(i);
					mList.add(i, tmpM);
					return;
				}
				else if (i == mList.size() - 1) {
					tmpM.lower(i + 1);
					break;
				}
			}
		}
		mList.add(tmpM);
	}
	
	public static void announcePerm(String msr) {
		if (exists(msr)) return;
		Message tmpM = new Message(msr, true);
		
		if (mList.size() > MAX) mList.removeLast();
		mList.addFirst(tmpM);
	}
	
	private static boolean exists(String msg) {
		for (Message m : mList)	if (m.getText().equals(msg)) return true;
		return false;
	}
	
	public static Message getMessage(String msg) {
		for (Message m : mList) if (m.getText().equals(msg)) return m;
		return null;
	}
	
	public static void dismiss(String s) { mList.remove(getMessage(s)); }
}