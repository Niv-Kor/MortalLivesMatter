package morlivm.map;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import morlivm.main.testing.Tester;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Physics.Vector;

public class Boundary implements Attachable, AttachManager
{
	private Ground ground;
	private Collider collider;
	private Point point, fixedPoint;
	private Graphable component;
	private static List<Boundary> bList = new ArrayList<Boundary>();
	
	public Boundary(BufferedImage blueprint, Point point, Ground ground) {
		this.collider = new Collider(blueprint, point, true);
		this.point = new Point(point);
		this.fixedPoint = new Point(point, 0, 0, Topology.groundY(point.getX()));
		this.ground = ground;
	}
	
	public Boundary(BufferedImage blueprint, Graphable component) {
		this.component = component;
		this.point = new Point(component.getPoint());
		this.collider = new Collider(blueprint, component.getPoint(), true);
	}
	
	public Boundary(Pamphlet sheet, Graphable component) {
		this.component = component;
		this.point = new Point(component.getPoint());
		this.collider = new Collider(sheet.getMask().getCollider());
		collider.setX(component.getX());
		collider.setY(component.getY());
	}
	
	public Boundary(Pamphlet sheet, Point point, Ground ground) {
		this.collider = new Collider(sheet.getMask().getCollider());
		collider.setX(point.getX());
		collider.setY(point.getY());
		this.point = new Point(point);
		this.fixedPoint = new Point(point, 0, 0, Topology.groundY(point.getX()));
		this.ground = ground;
	}
	
	public Boundary(Boundary other) {
		if (other != null) this.collider = new Collider(other.collider);
	}
	
	public static void updateBoundaryList(double delta) {
		for (Boundary b : bList) b.update(delta);
	}
	
	public static void renderBoundaryList(ExtendedGraphics2D g) {
		for (Boundary b : bList) b.render(g);
	}
	
	public void update(double delta) {
		if (component != null) {
			setX(component.getX());
			setY(component.getY());
		}
		else {
			ground.moveHeightwise(this, delta);
			setX(fixedPoint.getX() + ground.getX());
		}
		
		collider.setX(getX());
		collider.setY(getY());
	}
	
	public void render(ExtendedGraphics2D g) {
		if (Tester.graphifyBoudaries) collider.render(g);
	}
	
	public static boolean exists(Boundary boundary) {
		for (Boundary b : bList) if (b == boundary) return true;
		return false;
	}
	
	public boolean intersects(Collider c) { return collider.touch(c); }
	public boolean intersects(Collider c, Vector direction) { return collider.touch(c, direction); }
	public static void add(Boundary b) { if (!exists(b)) bList.add(b); }
	public static void remove(Boundary b) { bList.remove(b); }
	public static List<Boundary> getList() { return bList; }
	public static void clear() { bList.clear(); }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return collider.getDimension(); }
	public Point getFixedPoint() { return fixedPoint; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public List<Attachable> getMagnetizedComponents() { return new ArrayList<Attachable>(bList); }
}