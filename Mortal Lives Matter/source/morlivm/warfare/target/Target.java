package morlivm.warfare.target;
import java.awt.Dimension;

import morlivm.content.Entity;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;

public abstract class Target implements Graphable
{
	protected Entity entity;
	protected Point point;
	
	public Target(Entity entity, Point point) {
		this.entity = entity;
		this.point = point;
	}
	
	public void update(double delta) {}
	public void render(ExtendedGraphics2D g) {}
	public abstract void follow(double delta);
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return null; }
	public Point getPoint() { return point; }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public double getZ() { return point.getZ(); }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public void setZ(double z) { point.setZ(z); }
}