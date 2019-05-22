package morlivm.map.parallex;
import java.awt.Color;
import java.awt.image.BufferedImage;

import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.main.testing.Tester;
import morlivm.map.Attachable;
import morlivm.map.Magnet;
import morlivm.map.orientation.Topology;
import morlivm.map.weather.WeatherManager;
import morlivm.map.weather.WeatherManager.ParallexLight;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics.Axis;

public class Cliff extends GeoLayer implements Attachable 
{
	public final static Point ORIGIN_POINT = new Point(0, 0);
	private final static double TRANSPARENCY = 1;
	private final static int FRAME_RATE = 1;
	
	private BufferedImage ceiling;
	
	public Cliff(Point point, GameState gs) {
		super(Structure.getDatabase().cliff, point, gs);
		if (ldb.ceiling != null) this.ceiling = Structure.getDatabase().ceiling;
	}
	
	public void update(double delta) {
		gameState.getArena().getGround().moveAlong(this, delta);
		if (!Magnet.isActive()) {
			
			setY(Topology.overallHillHeight(entity.getMidX()) / 2 + ORIGIN_POINT.getY());
		
			if (usingImapctZone())
			setY(getY() + delta * entity.getSpeed() / FRAME_RATE *
					 Topology.vectorY(entity.getMidX(), entity.getUserDirect(Axis.X).oppose()) * 2);
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		if (ceiling != null) g.drawImage(ceiling, (int) getX(), 0, getWidth(), getHeight(), null);
		
		//coordinates alignment
		if (getX() < getWidth() * -1 + Game.WIDTH) setX(getWidth() * -1 + Game.WIDTH);
		if (getX() > 0) setX(0);
		
		WeatherManager.maskOver(g, -160, ParallexLight.FRONT);
		
		int topLim = (int) Topology.topLim(entity.getMidX());
		int bottomLim = (int) Topology.bottomLim(entity.getMidX());
		
		if (Tester.graphifyGroundLimits) {
			g.setColor(Color.ORANGE);
			g.drawString("" + topLim, 10, topLim - 15);
			g.drawLine(topLim);
			g.setColor(new Color(255, 0, 236));
			g.drawString("" + bottomLim, 10, bottomLim - 15);
			g.drawLine(bottomLim);
			
			g.setColor(Color.WHITE);
			g.drawString("" + gameState.getArena().getGround().getY(), 10, 50);
		}
	}
	
	protected int transparency() {
		return (int) Percent.percentOfNum(TRANSPARENCY * 100, 0xFF);
	}

	public Point getFixedPoint() { return ORIGIN_POINT; }
}