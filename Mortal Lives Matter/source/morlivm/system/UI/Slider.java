package morlivm.system.UI;
import java.awt.Dimension;

import morlivm.main.Game;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics;
import morlivm.system.performance.TimingDevice;

public class Slider implements Graphable
{
	private final static Dimension DIM = new Dimension(22, 22);
	
	private DraggingDevice.Route route;
	private Button ball;
	private double range, savedLevel;
	private Point initP;
	private DraggingDevice draggingDevice;
	private TimingDevice timingDevice;
	
	public Slider(DraggingDevice.Route route, Point point, int range) {
		this.initP = new Point(point);
		this.route = route;
		this.range = range;
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("delay", 0.033);
		this.ball = new Button("/sheets/UI Components/Slider/DefBall.png", null,
							   "/sheets/UI Components/Slider/VerPtrBall.png", initP, DIM);
		
		this.draggingDevice = new DraggingDevice(this, route);
		
		if (route == DraggingDevice.Route.HORIZONTAL)
			draggingDevice.setLimit(initP, new Point(initP, range, 0, 0));
		
		else if (route == DraggingDevice.Route.VERTICAL)
			draggingDevice.setLimit(initP, new Point(initP, 0, range, 0));
	}
	
	public void update(double delta) {
		if (!ball.isChanged() && ball.attend(Button.Action.PRESS, Physics.Vector.LEFT)) ball.change(true);
		else if (!Game.getMouseInput().press(Physics.Vector.LEFT)) ball.change(false);
		
		draggingDevice.update(delta);
		timingDevice.setClocks();
	}
	
	public void render(ExtendedGraphics2D g) {
		ball.render(g);
	}
	
	public void setLevel(double percentage) {
		if (route == DraggingDevice.Route.HORIZONTAL) ball.setX(initP.getX() + range * percentage / 100.0);
		if (route == DraggingDevice.Route.VERTICAL) ball.setY(initP.getY() + range * percentage / 100.0);
	}
	
	public double getLevel() {
		double result = 0;
		if (route == DraggingDevice.Route.HORIZONTAL) result = Percent.numOfNum(ball.getX() - initP.getX(), range);
		if (route == DraggingDevice.Route.VERTICAL) result = Percent.numOfNum(ball.getY() - initP.getY(), range);
		return Percent.limit(result);
	}
	
	public double getSavedLevel() { return savedLevel; }
	public void preserveLevel() { savedLevel = getLevel(); }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return DIM; }
	public Point getPoint() { return ball.getPoint(); }
	public void setX(double x) { ball.setX(x); }
	public void setY(double y) { ball.setY(y); }
	public double getX() { return ball.getX(); }
	public double getY() { return ball.getY(); }
}