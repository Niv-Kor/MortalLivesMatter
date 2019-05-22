package morlivm.system.graphics;
import java.awt.Dimension;

import morlivm.system.UI.Point;

public interface Graphable
{
	public void update(double delta);
	public void render(ExtendedGraphics2D g);
	public void setDimension(Dimension d);
	public Dimension getDimension();
	public Point getPoint();
	public void setX(double x);
	public void setY(double y);
	public double getX();
	public double getY();
}