package morlivm.map.portal;
import morlivm.content.mortal.Player;
import morlivm.database.DataManager;
import morlivm.database.LevelData;
import morlivm.main.Structure;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Physics;
import morlivm.system.performance.Timer;

public class LevelPortal extends Portal
{
	private final static Pamphlet SS = DataManager.retSheet("ss$a$portal");
	
	private boolean glowFlag;
	private Point midP, endP;
	
	public LevelPortal(GameState gs, Player player) {
		super(gs, player);
		
		LevelData ldb = Structure.getDatabase();
		this.originP = new Point(ldb.rightWall, 0);
		this.endP = new Point(ldb.rightWall - 2 * SS.getDimension().width / 3, 0);
		this.midP = new Point(endP, SS.getDimension().width / 6, 0, 0);
		
		this.glowFlag = true;
		this.point = new Point(ground.getX() + originP.getX(), originP.getY());
		this.animation = new Animation(SS);
		this.dim = SS.getDimension();
		timingDevice.addTimer("movement", 0.01);
	}

	public void update(double delta) {
		if (!open) return;
		super.update(delta);
		
		Timer movement = timingDevice.getTimer("movement");
		int direction = glowFlag ? -1 : 1;
		
		if (movement.progressedToRoof()) {
			originP.setX(originP.getX() + 2 * direction);
			updateCoordinates();
			
			if (glowFlag && originP.smallerThan(endP, Physics.Axis.X, 0)
			|| !glowFlag && originP.largerThan(midP, Physics.Axis.X, 0)) glowFlag = !glowFlag;
			
			movement.init();
		}
	}
	
	protected void enter() {
		Collider body = player.getMass();
		Point playerP = new Point(body.getX() + body.getDimension().width / 2, body.getY());
		Point portalP = new Point(getX() + 3 * dim.width / 4, getY() + dim.height / 2);
		double dist = playerP.distance(portalP);
		
		if (open && dist <= dim.width / 4) {
			gameState.levelUp();
			terminate();
		}
	}
}