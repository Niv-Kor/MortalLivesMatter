package morlivm.warfare.damage_filter;
import java.awt.Dimension;

import morlivm.content.Entity;
import morlivm.map.Attachable;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Physics.Vector;

public class BodyPart implements Attachable
{
	private Dimension dim;
	private double rightX, leftX;
	private Point point;
	private Collider mass;
	private int spriteWidth;
	
	public BodyPart(Point point, Dimension dim, Entity entity) {
		this.dim = new Dimension(dim);
		this.mass = new Collider(point, dim);
		this.point = new Point(point);
		this.rightX = point.getX();
		this.leftX = entity.getDimension().width - (rightX + dim.width);
		this.spriteWidth = entity.getDimension().width;
	}
	
	public BodyPart(Collider col, Entity entity) {
		this.dim = new Dimension(col.getDimension());
		this.mass = new Collider(col);
		this.point = new Point(col.getPoint());
		this.rightX = point.getX();
		this.leftX = entity.getDimension().width - (rightX + dim.width);
		this.spriteWidth = entity.getDimension().width;
	}
	
	public BodyPart(Point point, Dimension dim) {
		this.dim = new Dimension(dim);
		this.point = new Point(point);
		this.mass = new Collider(point, dim);
	}
	
	public BodyPart(BodyPart other, Entity entity) {
		this.dim = new Dimension(other.dim);
		this.point = new Point(other.point);
		this.mass = new Collider(other.mass);
		this.rightX = point.getX();
		this.leftX = entity.getDimension().width - (rightX + dim.width);
		this.spriteWidth = entity.getDimension().width;
	}
	
	public BodyPart(BodyPart other) {
		this.dim = new Dimension(other.dim);
		this.point = new Point(other.point);
		this.mass = new Collider(other.mass);
		this.rightX = other.rightX;
		this.leftX = other.leftX;
		this.spriteWidth = other.spriteWidth;
	}
	
	public void update(double delta) {}
	public void render(ExtendedGraphics2D g) {}
	
	public void connectRulerEntity(Entity e) {
		this.rightX = point.getX();
		this.leftX = e.getDimension().width - (rightX + dim.width);
		this.spriteWidth = e.getDimension().width;
	}
	
	public void fixPart(Vector direct, Point p, boolean lockX, boolean lockY) {
		int directMultiplier = (direct == Vector.RIGHT) ? 0 : 1;
		if (direct == Vector.RIGHT) point.setX(rightX);
		else point.setX(leftX);
		
		if (!lockX) setX(p.getX() + point.getX() - spriteWidth * directMultiplier);
		if (!lockY) setY(p.getY() + point.getY());
	}
	
	public double getX() { return mass.getX(); }
	public double getY() { return mass.getY(); }
	public void setX(double x) { mass.setX(x); }
	public void setY(double y) { mass.setY(y); }
	public Collider getCollider() { return mass; }
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return point; }
	public Point getFixedPoint() { return getPoint(); }
}