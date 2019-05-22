package morlivm.system.UI;
import java.awt.Dimension;
import java.awt.Image;

import morlivm.control_panel.Mode;
import morlivm.control_panel.ModeManager;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Physics;

public class Toggle implements Graphable
{
	private final static Dimension BASE_DIM = new Dimension(66, 27);
	private final static Dimension COVER_DIM = new Dimension(42, 27);
	private final static String BASE = new String("/sheets/UI Components/Toggle/Base.png");
	private final static String COVER = new String("/sheets/UI Components/Toggle/Cover.png");
	
	private Button rect;
	private Image cover;
	private Point coverPoint, startingPoint, endPoint;
	private Mode mode;
	private boolean slide;
	
	public Toggle(String modeName, Point point) {
		this.coverPoint = new Point(point);
		this.startingPoint = new Point(point);
		this.endPoint = new Point(point.getX() + BASE_DIM.width / 2, point.getY());
		this.cover = ImageHandler.load(COVER);
		this.rect = new Button(BASE, null, null, point, BASE_DIM);
		mode = new Mode(modeName);
		ModeManager.add(mode);
	}

	public void update(double delta) {
		if (rect.attend(Button.Action.CLICK, Physics.Vector.LEFT)) {
			mode.activate(!mode.isOn());
			slide = true;
		}
		
		if (slide) {
			if (mode.isOn()) {
				if (coverPoint.smallerThan(endPoint, Physics.Axis.X, 0))
					coverPoint.setX(coverPoint.getX() + 1);
				else slide = false;
			}
			else {
				if (coverPoint.largerThan(startingPoint, Physics.Axis.X, 0))
					coverPoint.setX(coverPoint.getX() - 1);
				else slide = false;
			}
		}
	}

	public void render(ExtendedGraphics2D g) {
		rect.render(g);
		g.drawImage(cover, (int) coverPoint.getX(), (int) coverPoint.getY(), COVER_DIM.width, COVER_DIM.height, null);
	}
	
	public void setX(double x) {
		if (!mode.isOn()) coverPoint.setX(x);
		else coverPoint.setX(x + BASE_DIM.width / 2);
		startingPoint.setX(x);
		endPoint.setX(x + BASE_DIM.width / 2);
	}
	
	public void setY(double y) {
		coverPoint.setY(y);
		startingPoint.setY(y);
		endPoint.setY(y);
	}
	
	public boolean isOn() { return mode.isOn(); } 
	public void set(boolean flag) { mode.activate(flag); }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return BASE_DIM; }
	public Point getPoint() { return startingPoint; }
	public double getX() { return 0; }
	public double getY() { return 0; }
}