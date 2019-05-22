package morlivm.system.UI;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import morlivm.main.Game;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.math.Physics;
import morlivm.system.math.RNG;
import morlivm.system.math.RangedInt;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class FloatingPattern implements GraphicsManager
{
	private static class Floater implements Graphable
	{
		private final static RangedInt SQUR_DIM = new RangedInt(5, 40);
		private final static RangedInt SPEED = new RangedInt(1, 3);
		private final static RangedInt TRANS = new RangedInt(0, 200);
		
		private Dimension dim;
		private Point point;
		private double speed;
		private Color color;
		private FloatingPattern floatingPattern;
		
		public Floater(Color color, FloatingPattern floatingPattern) {
			int d = SQUR_DIM.generate();
			this.dim = new Dimension(d, d);
			this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), TRANS.generate());
			
			int deviation = RNG.generate(1, dim.width);
			this.point = new Point(RNG.generateDouble(-deviation , Game.WIDTH - deviation), Game.HEIGHT + dim.height);
			
			this.floatingPattern = floatingPattern;
			this.speed = SPEED.generate();
		}

		public void update(double delta) {
			setY(getY() - speed);
			if (point.smallerThan(new Point(), Physics.Axis.Y, dim.height)) floatingPattern.remove(this);
		}

		public void render(ExtendedGraphics2D g) {
			g.setColor(color);
			g.fillOval((int) getX(), (int) getY(), dim.width, dim.height);
		}
		
		public void setDimension(Dimension d) { dim = new Dimension(d); }
		public Dimension getDimension() { return dim; }
		public Point getPoint() { return point; }
		public void setX(double x) { point.setX(x); }
		public void setY(double y) { point.setY(y); }
		public double getX() { return point.getX(); }
		public double getY() { return point.getY(); }
	}
	
	private final static double SPAWN_RATE = 0.4;
	
	private Color[] colors;
	private List<Floater> fList;
	private TimingDevice timingDevice;
	
	public FloatingPattern(Color[] colors) {
		this.colors = colors;
		this.fList = new ArrayList<Floater>();
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("spawn", SPAWN_RATE);
	}

	public void update(double delta) {
		Timer spawn = timingDevice.getTimer("spawn");
		
		if (spawn.progressedToRoof()) {
			add(new Floater(colors[RNG.generate(0, colors.length - 1)] ,this));
			spawn.init();
		}
		
		for (int i = 0; i < fList.size(); i++)
			fList.get(i).update(delta);
	}

	public void render(ExtendedGraphics2D g) {
		for (int i = 0; i < fList.size(); i++)
			fList.get(i).render(g);
	}

	public void add(Floater f) { fList.add(f); }
	public void remove(Floater f) { fList.remove(f); }
	public List<? extends Graphable> getList() { return fList; }
}