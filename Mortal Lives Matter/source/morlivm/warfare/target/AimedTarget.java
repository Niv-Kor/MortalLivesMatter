package morlivm.warfare.target;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import morlivm.content.Entity;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Physics.Vector;

public class AimedTarget extends Target
{
	private double originDX, originDY, dx, dy;
	private double angle, distance;
	private Vector directX, directY;
	private AffineTransform trans, backup;
	
	public AimedTarget(Entity entity, Point point) {
		super(entity, point);
		
		this.directX = entity.getDirectX();
		this.directY = entity.getDirectY();
		this.dx = Math.abs(entity.getX() - point.getX());
		this.dy = Math.abs(entity.getY() - point.getY());
		this.originDX = dx;
		this.originDY = dy;
		this.angle = Math.atan(Math.tan(dy / dx) * directY.straight() * -1 * directX.straight());
		this.distance = Math.pow(dx * dx + dy * dy, 0.5);
	}
	
	public AimedTarget(Entity entity, Graphable component) {
		super(entity, component.getPoint());
		
		this.directX = entity.getDirectX();
		this.directY = entity.getDirectY();
		this.dx = Math.abs(entity.getX() - point.getX());
		this.dy = Math.abs(entity.getY() - point.getY());
		this.originDX = dx;
		this.originDY = dy;
		this.angle = Math.atan(Math.tan(dy / dx) * directY.straight() * -1 * directX.straight());
		this.distance = Math.pow(dx * dx + dy * dy, 0.5);
	}
	
	public void aim(ExtendedGraphics2D g) {
		backup = g.getTransform();
		trans =  new AffineTransform();
		trans.rotate(angle, entity.getX(), entity.getY());
		g.transform(trans);
		g.drawImage(entity.getImage(),
				   (int) entity.getX(),
				   (int) entity.getY(),
				   entity.getDimension().width,
				   entity.getDimension().height, null);
		
		g.transform(backup);
	}
	
	public void slide(ExtendedGraphics2D g, boolean move) {
		if (move) {
			updateLocation();
			int directX = (point.getX() > entity.getX()) ? 1 : -1;
			entity.setY(entity.getY() - dy / entity.getSpeed());
			entity.setX(entity.getX() + dx / entity.getSpeed() * directX);
		}
		
		g.drawImage(entity.getImage(), entity.getPoint(), entity.getDimension());
	}
	
	public boolean reach() {
		Collider proj = new Collider(entity.getImage(), new Point(entity.getX(), entity.getY()), false);
		Collider target = new Collider(point, new Dimension(20, 20));
		
		if (proj.touch(target)) return true;
		else return false;
	}
	
	public void updateLocation() {
		dx = Math.abs(entity.getX() - point.getX());
		dy = Math.abs(entity.getY() - point.getY());
	}
	
	public void spiralAim(ExtendedGraphics2D g, Image img, double x, double y, int width, int height) {
		spiralAim(g).drawImage(img, (int)x, (int)y, width, height, null);
		g.transform(backup);
	}
	
	private ExtendedGraphics2D spiralAim(ExtendedGraphics2D g) {
		backup = g.getTransform();
		trans.rotate(angle, point.getX(), point.getY());
		g.transform(trans);
		return g;
	} 
	
	public void setDistX(double distX) {
		this.dx = distX;
		this.distance = Math.pow(dx * dx + dy * dy, 0.5);
		this.angle = Math.atan(Math.tan(dy / dx) * directY.straight() * -1 * directX.straight());
	}
	
	public void setDistY(double distY) {
		this.dy = distY;
		this.distance = Math.pow(dx * dx + dy * dy, 0.5);
		this.angle = Math.atan(Math.tan(dy / dx) * directY.straight() * -1 * directX.straight());
	}
	
	public double getOriginDistX() { return originDX; }
	public double getOriginDistY() { return originDY; }
	public double getCurrentDistX() { return dx; }
	public double getCurrentDistY() { return dy; }
	public double getAngle() { return angle; }
	public void setAngle(double angle) { this.angle = angle; }
	public double getDistance() { return distance; }
	public void follow(double delta) {}
}