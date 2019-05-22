package morlivm.map.parallex;
import morlivm.main.Structure;
import morlivm.map.Habitat;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Percent;

public class Sky extends GeoLayer
{
	private final static double TRANSPARENCY = 1;
	
	public Sky(Point point, GameState gs) {
		super(Structure.getDatabase().sky, point, gs);
		Habitat.setConditions(ldb.habitatScenery, gameState);
	}
	
	public void update(double delta) {
		super.update(delta);
		Habitat.updateList(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		Habitat.renderList(g);
	}
	
	protected int transparency() {
		return (int) Percent.percentOfNum(TRANSPARENCY * 100, 0xFF);
	}
}