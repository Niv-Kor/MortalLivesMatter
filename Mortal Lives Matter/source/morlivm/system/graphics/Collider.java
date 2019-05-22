package morlivm.system.graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import morlivm.system.UI.Point;
import morlivm.system.math.Physics.Vector;

public class Collider implements Graphable
{
	private Dimension dim;
	private Polygon polygon;
	public Polygon[] masks;
	private Point point, initPoint;
	private boolean hidden;
	
	public Collider(BufferedImage image, Point point, boolean trace) {
		initByImagePreference(image, point, trace);
	}
	
	public Collider(BufferedImage image, boolean trace) {
		initByImagePreference(image, new Point(), trace);
	}
	
	public Collider(Point point, Dimension dim) {
		initRectangular(point, dim);;
	}
	
	public Collider(Collider other) {
		this.dim = new Dimension(other.dim);
		this.point = new Point(other.point);
		this.initPoint = new Point(other.initPoint);
		this.polygon = new Polygon(other.polygon.xpoints,
								   other.polygon.ypoints,
								   other.polygon.npoints);
		
		createMasks();
	}
	
	private void initByImagePreference(BufferedImage image, Point point, boolean trace) {
		if (trace) initPolygonial(image, point);
		else initRectangular(point, new Dimension(image.getWidth(), image.getHeight()));
	}
	
	private void initPolygonial(BufferedImage image, Point point) {
		BinaryImageTracer bit = new BinaryImageTracer(image);
		this.polygon = bit.getPolygon();
		this.initPoint = bit.getInitPoint();
		this.point = new Point(point);
		this.dim = new Dimension(image.getWidth(), image.getHeight());

		createMasks();
	}
	
	private void initRectangular(Point point, Dimension dim) {
		int np = 4;
		int[] xp = new int[np];
		int[] yp = new int[np];
		
		xp[0] = (int) Math.round(point.getX());
		yp[0] = (int) Math.round(point.getY());
		
		xp[1] = (int) Math.round(point.getX() + dim.width);
		yp[1] = (int) Math.round(point.getY());
		
		xp[2] = (int) Math.round(point.getX() + dim.width);
		yp[2] = (int) Math.round(point.getY() + dim.height);
		
		xp[3] = (int) Math.round(point.getX());
		yp[3] = (int) Math.round(point.getY() + dim.height);
		
		this.dim = new Dimension(dim);
		this.point = new Point(point);
		this.initPoint = new Point();
		this.polygon = new Polygon(xp, yp, np);
		
		createMasks();
	}
	
	public void update(double delta) {}
	
	public void render(ExtendedGraphics2D g) {
		if (!isHidden()) g.setColor(Color.GREEN);
		else g.setColor(Color.RED);
		
		g.drawPolygon(getPolygon());
	}
	
	public void copy(Collider other) {
		this.dim = new Dimension(other.dim);
		this.point = new Point(other.point);
		this.initPoint = new Point(other.initPoint);
		this.polygon = new Polygon(other.polygon.xpoints,
								   other.polygon.ypoints,
								   other.polygon.npoints);
		
		createMasks();
	}
	
	public void erase() {
		int[] x = new int[0], y = new int[0];
		this.polygon = new Polygon(x, y, 0);
	}
	
	private void createMasks() {
		int n = 8, w = Math.abs(getDimension().width), h = Math.abs(getDimension().height);
		int[] x = new int[n], y = new int[n];
		
		this.masks = new Polygon[4];
		
		//up
		x[0] = 0;
		y[0] = 0;
		
		x[1] = w;
		y[1] = 0;
		
		x[2] = w;
		y[2] = (int) Math.round(4 * h / 5);
		
		x[3] = (int) Math.round(14 * w / 15);
		y[3] = (int) Math.round(4 * h / 5);
		
		x[4] = (int) Math.round(14 * w / 15);
		y[4] = (int) Math.round(h / 2);
		
		x[5] = (int) Math.round(w / 5);
		y[5] = (int) Math.round(h / 2);
		
		x[6] = (int) Math.round(w / 5);
		y[6] = (int) Math.round(4 * h / 5);
		
		x[7] = 0;
		y[7] = (int) Math.round(4 * h / 5);
		
		masks[0] = new Polygon(x, y, n);
		
		//left
		x[0] = 0;
		y[0] = 0;
		
		x[1] = (int) Math.round(4 * w / 5);
		y[1] = 0;
		
		x[2] = (int) Math.round(4 * w / 5);
		y[2] = (int) Math.round(h / 15);
		
		x[3] = (int) Math.round(w / 2);
		y[3] = (int) Math.round(h / 15);
		
		x[4] = (int) Math.round(w / 2);
		y[4] = (int) Math.round(14 * h / 15);
		
		x[5] = (int) Math.round(4 * w / 5);
		y[5] = (int) Math.round(14 * h / 15);
		
		x[6] = (int) Math.round(4 * w / 5);
		y[6] = h;
		
		x[7] = 0;
		y[7] = h;
		
		masks[1] = new Polygon(x, y, n);
		
		//down
		x[0] = 0;
		y[0] = (int) Math.round(h / 5);
		
		x[1] = (int) Math.round(w / 15);
		y[1] = (int) Math.round(h / 5);
		
		x[2] = (int) Math.round(w / 15);
		y[2] = (int) Math.round(h / 2);
		
		x[3] = (int) Math.round(14 * w / 15);
		y[3] = (int) Math.round(h / 2);
		
		x[4] = (int) Math.round(14 * w / 15);
		y[4] = (int) Math.round(h / 5);
		
		x[5] = w;
		y[5] = (int) Math.round(h / 5);
		
		x[6] = w;
		y[6] = h;
		
		x[7] = 0;
		y[7] = h;
		
		masks[2] = new Polygon(x, y, n);
		
		//right
		x[0] = (int) Math.round(w / 5);
		y[0] = 0;
		
		x[1] = w;
		y[1] = 0;
		
		x[2] = w;
		y[2] = h;
		
		x[3] = (int) Math.round(w / 5);
		y[3] = h;
		
		x[4] = (int) Math.round(w / 5);
		y[4] = (int) Math.round(14 * h / 15);
		
		x[5] = (int) Math.round(w / 2);
		y[5] = (int) Math.round(14 * h / 15);
		
		x[6] = (int) Math.round(w / 2);
		y[6] = (int) Math.round(h / 15);
		
		x[7] = (int) Math.round(w / 5);
		y[7] = (int) Math.round(h / 15);
		
		masks[3] = new Polygon(x, y, n);
		updateCoordinates();
	}
	
	public boolean touch(Collider shape) {
		if (isHidden()) return false;
		
		Area a = new Area(getPolygon());
		Area b = new Area(shape.getPolygon());
		
		a.intersect(b);
		return !a.isEmpty();
	}
	
	public boolean touch(Collider col, Vector direction) {
		if (isHidden()) return false;
		
		Area a, b, c, d;
		Polygon opposeMaskA, opposeMaskB;
		
		//move mask to the right spot
		opposeMaskA = getMask(direction);
		opposeMaskB = col.getMask(direction.oppose());
		
		a = new Area(getPolygon());
		b = new Area(col.getPolygon());
		c = new Area(opposeMaskA);
		d = new Area(opposeMaskB);
		
		a.intersect(b);
		
		if (!a.isEmpty()) {
			//intersect with both a and b's opposite direction masks
			a.intersect(c);
			a.intersect(d);
			
			//empty means they touched the correct side
			return a.isEmpty();
		}
		else return false;
	}
	
	protected void updateMaskCoordinates() {
		int w = Math.abs(getDimension().width);
		int h = Math.abs(getDimension().height);
		int x, y;
		
		for (int i = 0; i < masks.length; i++) {
			x = masks[i].xpoints[0];
			y = masks[i].ypoints[0];
			masks[i].translate(-x, -y);
			
			switch (i) {
				case 0: { //up
					x = 0;
					y = 0;
					break;
				}
				case 1: { //left
					x = 0;
					y = 0;
					break;
				}
				case 2: { //down
					x = 0;
					y = (int) Math.round(h / 5);
					break;
				}
				case 3: { //right
					x = (int) Math.round(w / 5);
					y = 0;
					break;
				}
			}
			
			masks[i].translate((int) getX() + x, (int) getY() + y);
		}
	}
	
	protected Polygon getMask(Vector direction) {
		int opposeMaskIndex;
		
		switch (direction) {
			case UP: opposeMaskIndex = 0; break;
			case LEFT: opposeMaskIndex = 1; break;
			case DOWN: opposeMaskIndex = 2; break;
			case RIGHT: opposeMaskIndex = 3; break;
			default: {
				int[] x = new int[0];
				int[] y = new int[0];
				return new Polygon(x, y, 0);
			}
		}
		
		return masks[opposeMaskIndex];
	}
	
	public void setX(double x) {
		if (polygon.npoints == 0) return;
		
		point.setX(x);
		updateCoordinates();
	}

	public void setY(double y) {
		if (polygon.npoints == 0) return;
		
		point.setY(y);
		updateCoordinates();
	}
	
	private void updateCoordinates() {
		//primary point
		if (polygon.npoints > 0) {
			int px = polygon.xpoints[0];
			int py = polygon.ypoints[0];
			
			polygon.translate(-px, -py);
			polygon.translate((int) (getX() + initPoint.getX()),
							  (int) (getY() + initPoint.getY()));
		}
		
		//masks
		int w = Math.abs(getDimension().width);
		int h = Math.abs(getDimension().height);
		int x, y;
		
		for (int i = 0; i < masks.length; i++) {
			x = masks[i].xpoints[0];
			y = masks[i].ypoints[0];
			masks[i].translate(-x, -y);
			
			switch (i) {
				case 0: { //up
					x = 0;
					y = 0;
					break;
				}
				case 1: { //left
					x = 0;
					y = 0;
					break;
				}
				case 2: { //down
					x = 0;
					y = (int) Math.round(h / 5);
					break;
				}
				case 3: { //right
					x = (int) Math.round(w / 5);
					y = 0;
					break;
				}
			}
			
			masks[i].translate((int) getX() + x, (int) getY() + y);
		}
	}
	
	public void hide(boolean flag) { hidden = flag; }
	public Point getA() { return new Point(getX(), getY()); }
	public Point getB() { return new Point(getX() + dim.width, getY()); }
	public Point getC() { return new Point(getX() + dim.width, getY() + dim.height); }
	public Point getD() { return new Point(getX(), getY() + dim.height); }
	public Point getE() { return new Point(getA().getMidBetween(getB())); }
	public Point getF() { return new Point(getB().getMidBetween(getC())); }
	public Point getG() { return new Point(getC().getMidBetween(getD())); }
	public Point getH() { return new Point(getD().getMidBetween(getA())); }
	public boolean isHidden() { return hidden; }
	public void setDimension(Dimension d) {}
	public Point getPoint() { return point; }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public Dimension getDimension() { return dim; }
	public Polygon getPolygon() { return polygon; }
}