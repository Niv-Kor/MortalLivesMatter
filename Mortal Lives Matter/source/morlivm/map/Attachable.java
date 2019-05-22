package morlivm.map;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Graphable;

public interface Attachable extends Graphable
{
	public Point getFixedPoint();
}