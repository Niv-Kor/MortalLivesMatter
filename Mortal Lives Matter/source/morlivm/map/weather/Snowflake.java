package morlivm.map.weather;
import java.awt.Color;
import java.awt.Dimension;

import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.RangedInt;

public class Snowflake extends Precipitate implements Graphable
{
	protected final static Color COLOR = new Color(228, 248, 255);
	protected final static RangedInt DIAMETER = new RangedInt(3, 10);
	
	public Snowflake(Climate manager, Wind wind) {
		super(manager, wind, DIAMETER);
	}
	
	protected void initPoints(Point p) {
		/*
		 * 0   9     10    1
		 *  --------------
		 *   \ |      | /
		 *8 __\|      |/__ 11
		 *   4 \      / 5
		 *      \    /
		 *       \  /
		 *        \
		 *       / \
		 *      /   \
		 * 15__/7   6\__ 12
		 *    /|     |\
		 *   / |     | \
		 * ---------------
		 *3   14     13   2
		 */
		
		this.points = new Point[16];
		points[0] = new Point(p);
		points[1] = new Point(points[0], length, 0, 0);
		points[2] = new Point(points[1], 0, length, 0);
		points[3] = new Point(points[0], 0, length, 0);
		points[4] = new Point(points[0], length / 3, length / 3, 0);
		points[5] = new Point(points[1], -length / 3, length / 3, 0);
		points[6] = new Point(points[2], -length / 3, -length / 3, 0);
		points[7] = new Point(points[3], length / 3, -length / 3, 0);
		points[8] = new Point(points[0], 0, length / 3, 0);
		points[9] = new Point(points[0], length / 3, 0, 0);
		points[10] = new Point(points[1], -length / 3, 0, 0);
		points[11] = new Point(points[1], 0, length / 3, 0);
		points[12] = new Point(points[2], 0, -length / 3, 0);
		points[13] = new Point(points[2], -length / 3, 0, 0);
		points[14] = new Point(points[3], length / 3, 0, 0);
		points[15] = new Point(points[3], 0, -length / 3, 0);
	}

	public void render(ExtendedGraphics2D g) {
		g.setColor(COLOR);
		
		//0 - 2
		g.drawLine((int) points[0].getX(), (int) points[0].getY(),
				   (int) points[2].getX(), (int) points[2].getY());
		
		//1 - 3
		g.drawLine((int) points[1].getX(), (int) points[1].getY(),
				   (int) points[3].getX(), (int) points[3].getY());
		
		//4 - 8
		g.drawLine((int) points[4].getX(), (int) points[4].getY(),
				   (int) points[8].getX(), (int) points[8].getY());
		
		//4 - 9
		g.drawLine((int) points[4].getX(), (int) points[4].getY(),
				   (int) points[9].getX(), (int) points[9].getY());

		//5 - 10
		g.drawLine((int) points[5].getX(), (int) points[5].getY(),
				   (int) points[10].getX(), (int) points[10].getY());
		
		//5 - 11
		g.drawLine((int) points[5].getX(), (int) points[5].getY(),
				   (int) points[11].getX(), (int) points[11].getY());
		
		//6 - 12
		g.drawLine((int) points[6].getX(), (int) points[6].getY(),
				   (int) points[12].getX(), (int) points[12].getY());
		
		//6 - 13
		g.drawLine((int) points[6].getX(), (int) points[6].getY(),
				   (int) points[13].getX(), (int) points[13].getY());
		
		//7 - 14
		g.drawLine((int) points[7].getX(), (int) points[7].getY(),
				   (int) points[14].getX(), (int) points[14].getY());
		
		//7 - 15
		g.drawLine((int) points[7].getX(), (int) points[7].getY(),
				   (int) points[15].getX(), (int) points[15].getY());
	}

	public Dimension getDimension() { return new Dimension(length, length); }
}