package morlivm.warfare.damage_filter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import morlivm.database.DataManager;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.graphics.sheet.Pamphlet;

public class Element implements Graphable
{
	public static enum Type {
		NONE(null),
		DIVINE(new Color(241, 234, 184)),
		LUNAR(new Color(163, 196, 224)),
		NATURE(new Color(187, 234, 154));
		
		private final static String DIRECTORY = "/sheets/general/element/";
		private final static String SHEET_CODE = "ss$g$element_";
		
		public Color color;
		public Pamphlet pamphlet;
		public BufferedImage powerComparison;

		private Type(Color color) {
			this.color = color;
			this.pamphlet = DataManager.retSheet(SHEET_CODE + name().toLowerCase());
			this.powerComparison = ImageHandler.load(DIRECTORY + "power_comparison/" + name() + ".png");
		}
	}
	
	public final static double IMPACT = 0.2;
	
	private Type type, master, slave;
	private Point point;
	private Animation animation;
	
	public Element(Type type) {
		this.type = type;
		this.master = getMasterType(type);
		this.slave = getSlaveType(type);
	}
	
	public Element(Element other) {
		this.type = other.type;
		this.master = other.master;
		this.slave = other.slave;
		this.point = other.point != null ? new Point(other.point) : null;
		this.animation = (other.animation != null) ? new Animation(other.animation) : null;
	}
	
	public void update(double delta) {
		animation.update(delta);
	}

	public void render(ExtendedGraphics2D g) {
		g.drawImage(animation.getImage(), point, type.pamphlet.getDimension());
	}
	
	public void activate(Point p) {
		this.point = new Point(p);
		this.animation = new Animation(type.pamphlet);
		animation.randomize();
	}
	
	public static double fight(Element offensive, Element defensive) {
		if (defensive.getType() == offensive.getMasterType()) return 1 - IMPACT;
		if (defensive.getType() == offensive.getType()) return 1;
		if (defensive.getType() == offensive.getSlaveType()) return 1 + IMPACT;
		return 1; //foraml return statement
	}
	
	public Type getType() { return type; }
	public Type getMasterType() { return master; }
	public Type getSlaveType() { return slave; }
	
	public static Type getMasterType(Type type) {
		switch(type) {
			case DIVINE: return Type.NATURE;
			case LUNAR:	return Type.DIVINE;
			case NATURE: return Type.LUNAR;
			default: return null;
		}
	}
	
	public static Type getSlaveType(Type type) {
		switch(type) {
			case DIVINE: return Type.LUNAR;
			case LUNAR:	return Type.NATURE;
			case NATURE: return Type.DIVINE;
			default: return null;
		}
	}
	
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return type.pamphlet.getDimension(); }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}