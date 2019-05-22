package morlivm.map;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import morlivm.content.mortal.Mortal;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RNG;

public class StainManager implements GraphicsManager, AttachManager
{
	public static class BloodStain implements Attachable
	{
		private final static String PATH = "/sheets/general/blood/stains/";
		private final static Dimension DIM = new Dimension(80, 50);
		private final static int VARIATIONS = 8;
		
		private Point point, fixedPoint;
		private Ground ground;
		private Dimension dim;
		private BufferedImage sprite;
		
		public BloodStain(Mortal mortal, GameState gs) {
			this.ground = gs.getArena().getGround();
			this.dim = new Dimension(DIM);
			Collider legs = mortal.getLegs();
			Point backPoint = (mortal.getDirectX() == Vector.RIGHT) ? legs.getA() : legs.getB();
			if (backPoint.equals(legs.getA())) dim.width *= -1;
			this.point = new Point(backPoint);
			this.fixedPoint = new Point(point, 0, 0, Topology.groundY(point.getX()));
			this.sprite = ImageHandler.load(PATH + RNG.generate(0, VARIATIONS - 1) + ".png");
		}
	
		public void update(double delta) {
			ground.moveAlong(this, delta);
		}
	
		public void render(ExtendedGraphics2D g) {
			g.drawImage(sprite, point, dim);
		}
		
		public void setDimension(Dimension d) {}
		public Dimension getDimension() { return dim; }
		public Point getPoint() { return point; }
		public void setX(double x) { point.setX(x); }
		public void setY(double y) { point.setY(y); }
		public double getX() { return point.getX(); }
		public double getY() { return point.getY(); }
		public Point getFixedPoint() { return fixedPoint; }
	}
	
	private List<BloodStain> bList;
	private GameState gameState;
	
	public StainManager(GameState gs) {
		this.bList = new ArrayList<BloodStain>();
		this.gameState = gs;
	}

	public void update(double delta) {
		for (BloodStain b : bList) b.update(delta);
	}

	public void render(ExtendedGraphics2D g) {
		for (BloodStain b : bList) b.render(g);
	}
	
	public List<Attachable> getMagnetizedComponents() {
		return new ArrayList<Attachable>(bList);
	}
	
	public void spatter(Mortal mortal) { bList.add(new BloodStain(mortal, gameState)); }
	public List<? extends Graphable> getList() { return bList; }
}