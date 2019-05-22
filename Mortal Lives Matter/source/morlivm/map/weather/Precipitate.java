package morlivm.map.weather;
import java.awt.Dimension;

import morlivm.main.Game;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Physics;
import morlivm.system.math.RNG;
import morlivm.system.math.RangedInt;

public abstract class Precipitate implements Graphable
{
	public final static int MAX = 600;
	
	protected Point startP, endP;
	protected Point[] points;
	protected int speed, length;
	protected double vectorX, vectorY;
	protected Climate mngr;
	
	public Precipitate(Climate manager, Wind wind, RangedInt diam) {
		this.mngr = manager;
		this.speed = wind.getSpeed();
		this.length = diam.generate();
		this.startP = generateStartingPoint(wind.getVector());
		
		//right vector
		if (wind.getDirection().straight() > 0) endP = new Point(Game.WIDTH, Game.HEIGHT);
		//left or straight vector
		else endP = new Point(0, Game.HEIGHT);
		
		double angle = wind.getAngle();
		double angleC = Math.toRadians(90 - Math.toDegrees(angle));
		this.vectorY = length * Math.abs(Math.sin(angle));
		this.vectorX = length * Math.sin(angleC) * wind.getDirection().straight();
		
		initPoints(startP);
	}
	
	public void update(double delta) {
		setX(getX() + delta * speed * vectorX);
		setY(getY() + delta * speed * vectorY);

		//terminate
		boolean exceedWall;
		boolean exceedGround = getPoint().largerThan(endP, Physics.Axis.Y, 0);
		
		if (vectorX > 0) exceedWall = getPoint().largerThan(endP, Physics.Axis.X, 0);
		else exceedWall = getPoint().smallerThan(endP, Physics.Axis.X, 0);
		
		if (exceedWall || exceedGround)	{
			if (mngr.getAmount() > MAX) {
				setX(startP.getX());
				setY(startP.getY());
			}
			else mngr.remove(this);
		}
	}
	
	public void setDimension(Dimension d) {
		vectorX = d.width;
		vectorY = d.height;
	}
	
	private Point generateStartingPoint(double vector) {
		/*
		 *        ----------------
		 * 	      |	     A  	 |		
		 *  ______|______________|______
		 * |  	  |  			 |      |
		 * |  B   |	   screen	 |	C   |
		 * |	  |				 |	    |
		 * |______|______________|______|
		 */
		
		boolean rectA = vector == 0 || RNG.unstableCondition(50);
		
		if (rectA) { //A
			return RNG.generatePixel(new Point(0, -Game.HEIGHT / 2),
									 new Dimension(Game.WIDTH, Game.HEIGHT / 2));
		}
		else {
			if (vector > 0) { //B
				return RNG.generatePixel(new Point(-Game.WIDTH, -Game.HEIGHT / 2),
										 new Dimension((int) (Game.WIDTH * Math.abs(vector)),
										(int) (Game.HEIGHT * 1.5)));
			}
			else { //C
				return RNG.generatePixel(new Point(Game.WIDTH, -Game.HEIGHT / 2),
										 new Dimension((int) (Game.WIDTH * Math.abs(vector)),
										(int) (Game.HEIGHT * 1.5)));
			}
		}
	}
	
	protected abstract void initPoints(Point p);
	public Point getPoint() { return new Point(points[0]); }
	public double getX() { return points[0].getX(); }
	public double getY() { return points[0].getY();	}
	public void setX(double x) { initPoints(new Point(x, getY())); }
	public void setY(double y) { initPoints(new Point(getX(), y)); }
}