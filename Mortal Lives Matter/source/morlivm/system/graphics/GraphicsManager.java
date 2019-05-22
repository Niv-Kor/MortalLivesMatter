package morlivm.system.graphics;
import java.util.List;

public interface GraphicsManager
{
	public void update(double delta);
	public void render(ExtendedGraphics2D g);
	public List<? extends Graphable> getList();
}