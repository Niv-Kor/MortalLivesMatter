package morlivm.warfare.aftershock;
import morlivm.database.DataManager;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.sheet.Pamphlet;

public class Blood extends Aftershock
{
	public final static Pamphlet SS = DataManager.retSheet("ss$g$blood");

	public Blood(Animation spriteTicker, Point point, GameState gs) {
		super(spriteTicker, point, gs);
	}
}