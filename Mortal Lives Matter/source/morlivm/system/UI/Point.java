package morlivm.system.UI;
import java.awt.geom.Point2D;

import morlivm.system.math.Physics.Axis;

public class Point extends Point2D
{
	private double x, y, z;
	
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
		this.z = 0;
	}
	
	public Point() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	public Point(Point other, double dx, double dy, double dz) {
		this.x = other.x + dx;
		this.y = other.y + dy;
		this.z = other.z + dz;
	}
	
	public Point(Point other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	public boolean equalsX(Point other) {
		return this.x == other.x;
	}
	
	public boolean equalsY(Point other) {
		return this.y == other.y;
	}
	
	public boolean equalsZ(Point other) {
		return this.z == other.z;
	}
	
	public boolean equals(Point other) {
		return this.x == other.x && this.y == other.y && this.z == other.z;
	}
	
	public boolean largerThan(Point other, Axis axis, double delta) {
		switch(axis) {
			case X: return this.x > other.x + delta;
			case Y: return this.y > other.y + delta;
			case Z: return this.z > other.z + delta;
			default: System.err.println("Invalid input."); return false;
		}
	}
	
	public boolean smallerThan(Point other, Axis axis, double delta) {
		switch(axis) {
			case X: return this.x < other.x - delta;
			case Y: return this.y < other.y - delta;
			case Z: return this.z < other.z - delta;
			default: System.err.println("Invalid input."); return false;
		}
	}
	
	public boolean withinRange(Point other, double epsilon) {
		return withinRange(other, Axis.X, epsilon)
			&& withinRange(other, Axis.Y, epsilon)
			&& withinRange(other, Axis.Z, epsilon);
	}
	
	public boolean withinRange(Point other, Axis axis, double epsilon) {
		switch(axis) {
			case X: return this.x <= other.x + epsilon && this.x >= other.x - epsilon;
			case Y: return this.y <= other.y + epsilon && this.y >= other.y - epsilon;
			case Z: return this.z <= other.z + epsilon && this.z >= other.z - epsilon;
			default: return false;
		}
	}
	
	public double distance(Point other, Axis axis) {
		switch(axis) {
			case X: return Math.abs(this.x - other.x);
			case Y: return Math.abs(this.y - other.y);
			case Z: return Math.abs(this.z - other.z);
			default: return 0;
		}
	}
	
	public double distance(Point other) {
		return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
	}
	
	public Point getMidBetween(Point other) {
		double x = (this.x > other.x) ? this.x : other.x;
		double y = (this.y > other.y) ? this.y : other.y;
		return new Point(x - distance(other, Axis.X) / 2, y - distance(other, Axis.Y) / 2);
	}
	
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() { return x; }
	public double getY() { return y; }
	public double getZ() { return z; }
	public void setX(double x) { this.x = x; }
	public void setY(double y) { this.y = y; }
	public void setZ(double z) { this.y = z; }
	public String toString() { return new String("(" + x + "," + y + "," + z + ")"); }
}