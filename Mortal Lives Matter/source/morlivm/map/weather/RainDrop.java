package morlivm.map.weather;
import java.awt.Color;
import java.awt.Dimension;

import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.RangedInt;

public class RainDrop extends Precipitate implements Graphable
{
	private final static Color COLOR = new Color(153, 188, 200);
	private final static RangedInt DIAMETER = new RangedInt(30, 70);
	
	public RainDrop(Climate manager, Wind wind) {
		super(manager, wind, DIAMETER);
	}
	
	protected void initPoints(Point p) {
		this.points = new Point[2];
		points[0] = new Point(p);
		points[1] = new Point(points[0].getX() + vectorX, points[0].getY() + vectorY);
	}
	
	public void render(ExtendedGraphics2D g) {
		g.setColor(COLOR);
		g.drawLine((int) points[0].getX(), (int) points[0].getY(),
				   (int) points[1].getX(), (int) points[1].getY());
	}

	public Dimension getDimension() { return new Dimension((int) vectorX, (int) vectorY); }
}