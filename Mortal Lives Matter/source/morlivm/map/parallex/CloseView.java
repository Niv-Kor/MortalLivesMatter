package morlivm.map.parallex;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.map.weather.WeatherManager;
import morlivm.map.weather.WeatherManager.ParallexLight;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics;

public class CloseView extends GeoLayer
{
	private final static int FRAME_SPEED = 3;
	private final static double TRANSPARENCY = 0.6;
	
	public CloseView(Point point, GameState gs) {
		super(Structure.getDatabase().closeView, point, gs);
	}
	
	public void update(double delta) {
		if (impactZone && entity.isWalkingHorizontally()
			&& !(entity.isWalking(Physics.Vector.LEFT, Physics.Axis.X) && entity.getX() >= Game.WIDTH / 2)) {
			setX(getX() - delta * entity.getSpeed() / FRAME_SPEED * entity.getUserDirect(Physics.Axis.X).straight());
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		WeatherManager.maskOver(g, -80, ParallexLight.MID);
		WeatherManager.maskOver(g, -100, ParallexLight.BACK);
	}

	protected int transparency() {
		return (int) Percent.percentOfNum(TRANSPARENCY * 100, 0xFF);
	}
}