package morlivm.map;
import java.util.ArrayList;
import java.util.List;

import morlivm.system.UI.Point;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Physics.Axis;

public class Magnet
{
	public static class MagnetizedComponent
	{
		private Attachable attachable;
		private Point backupP;
		private boolean precision;
		
		public MagnetizedComponent(Attachable a, boolean precision) {
			this.attachable = a;
			this.backupP = new Point(a.getPoint());
			this.precision = precision;
		}
		
		public Point difference() {
			double x = attachable.getPoint().distance(backupP, Axis.X);
			double y = attachable.getPoint().distance(backupP, Axis.Y);
			
			return new Point(x, y, 0);
		}
		
		public void translateX(double x) { attachable.setX(attachable.getX() + x); }
		public void translateY(double y) { attachable.setY(attachable.getY() + y); }
		public void translate(Point p) { translateX(p.getX()); translateY(p.getY()); }
		public void attachX() { if (precision) attachable.setX(backupP.getX()); }
		public void attachY() { if (precision) attachable.setY(backupP.getY()); }
		public void attach() { if (precision) { attachX(); attachY(); } }
		public Point getBackupPoint() { return backupP; }
		public Graphable getGraphableComponent() { return attachable; }
	}
	
	private List<MagnetizedComponent> list = new ArrayList<MagnetizedComponent>();
	private static List<Magnet> magnets = new ArrayList<Magnet>();
	
	public Magnet() {
		magnets.add(this);
	}
	
	public MagnetizedComponent getComponent(Graphable g) {
		for (MagnetizedComponent c : list)
			if (c.getGraphableComponent() == g) return c;
		
		return null;
	}
	
	public static boolean isActive() {
		for (Magnet m : magnets)
			if (!m.isEmpty()) return true;
		
		return false;
	}
	
	public static Point getDifference(Attachable a) {
		MagnetizedComponent mc = getMagnetizedComponent(a);
		return (mc != null) ? mc.difference() : new Point();
	}
	
	public static MagnetizedComponent getMagnetizedComponent(Attachable a) {
		for (Magnet m : magnets)
			for (MagnetizedComponent mc : m.list)
				if (mc.getGraphableComponent() == a) return mc;
		
		return null;
	}
	
	public void add(AttachManager attachManager, boolean precision) {
		List<Attachable> temp = attachManager.getMagnetizedComponents();
		for (Attachable a : temp) add(a, precision);
	}
	
	public void remove(AttachManager attachManager) {
		List<Attachable> temp = attachManager.getMagnetizedComponents();
		for (Attachable a : temp) remove(a);
	}
	
	public static void transfer(Magnet from, Magnet to) {
		to.close();
		
		MagnetizedComponent mc;
		for (int i = 0; i < from.list.size(); i++) {
			mc = from.list.get(i);
			from.list.remove(mc);
			to.list.add(mc);
		}
	}
	
	public String toString() {
		String str = "\nMAGNET LIST:\n";
		str.concat(str + "-------------------------\n");
		
		for (MagnetizedComponent mc : list)
			str.concat(str + mc.getGraphableComponent() + "\n");
		
		return str;
	}
	
	public void close() { list.clear(); }
	public void translateX(double x) { for (MagnetizedComponent c : list) c.translateX(x); }
	public void translateY(double y) { for (MagnetizedComponent c : list) c.translateY(y); }
	public void translate(Point p) { for (MagnetizedComponent c : list) c.translate(p); }
	public void attachX() { for (MagnetizedComponent c : list) c.attachX(); }
	public void attachY() { for (MagnetizedComponent c : list) c.attachY(); }
	public void attach() { for (MagnetizedComponent c : list) c.attach(); }
	public void add(Attachable attachable, boolean precision) { list.add(new MagnetizedComponent(attachable, precision)); }
	public void remove(Attachable attachable) { list.remove(getComponent(attachable)); }
	public boolean isEmpty() { return list.isEmpty(); }
}