package morlivm.map.parallex;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics;

public class FarView extends GeoLayer
{
	private final static int FRAME_RATE = 10;
	private final static double TRANSPARENCY = 0.3;
	
	public FarView(Point point, GameState gs) {
		super(Structure.getDatabase().farView, point, gs);
	}
	
	public void update(double delta) {
		if (impactZone && entity.isWalkingHorizontally()
			&& !(entity.isWalking(Physics.Vector.LEFT, Physics.Axis.X) && entity.getX() >= Game.WIDTH / 2)) {
			setX(getX() - delta * entity.getSpeed() / FRAME_RATE * entity.getUserDirect(Physics.Axis.X).straight());
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
	}
	
	protected int transparency() {
		return (int) Percent.percentOfNum(TRANSPARENCY * 100, 0xFF);
	}
}