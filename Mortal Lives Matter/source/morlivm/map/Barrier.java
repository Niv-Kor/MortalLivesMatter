package morlivm.map;
import java.awt.Dimension;
import morlivm.database.DataManager;
import morlivm.main.Structure;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.sheet.Pamphlet;

public class Barrier implements Attachable
{
	private Animation animation;
	private boolean opening;
	private Ground ground;
	private Point point, fixedPoint;
	private Boundary boundary;
	private Pamphlet pamphlet;
	
	public Barrier(Point point, Ground ground) {
		this.point = new Point(point);
		this.fixedPoint = new Point(point, 0, 0, Topology.groundY(point.getX()));
		this.ground = ground;
		this.pamphlet = DataManager.retSheet("ss$a$barrier_" + Structure.getDatabase().mapName);
		this.animation = new Animation(pamphlet);
		this.boundary = new Boundary(pamphlet, this);
		Boundary.add(boundary);
	}
	
	public void update(double delta) {
		ground.moveAlong(this, delta);
		
		if (opening) {
			animation.update(delta);
			if (isClear()) opening = false;
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		g.drawImage(animation.getImage(), point, pamphlet.getDimension());
	}
	
	public boolean isClear() {
		if (animation.getTickHigherThan(pamphlet.getSprite().getBoundaries()[0] / 2)) {
			Boundary.remove(boundary);
			return true;
		}
		return false;
	}
	
	public void closeGateway() {
		opening = false;
		Boundary.add(boundary);
		animation.setRow(2, true);
	}
	
	public void openGateway() { opening = true; }
	public boolean isClearing() { return opening; }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return pamphlet.getDimension(); }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); };
	public void setY(double y) { point.setY(y); };
	public double getX() { return point.getX(); };
	public double getY() { return point.getY(); }
	public Point getFixedPoint() { return fixedPoint; }
}