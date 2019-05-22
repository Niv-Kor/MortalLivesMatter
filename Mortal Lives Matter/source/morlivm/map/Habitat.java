package morlivm.map;
import java.awt.Dimension;
import java.util.LinkedList;
import morlivm.content.mortal.Player;
import morlivm.database.DataManager;
import morlivm.main.Game;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.map.weather.WeatherManager;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics;
import morlivm.system.math.RNG;
import morlivm.system.math.RangedDouble;
import morlivm.system.math.RangedInt;
import morlivm.system.performance.FPS;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class Habitat implements Graphable
{
	public static enum Scenery {
		NONE(null, 0, false),
		BIRDS("ss$a$birds", 3, false);
		
		private final static RangedDouble DENSITY = new RangedDouble(2.1, 10);
		
		public Pamphlet pamphlet;
		public Dimension[] dim;
		private boolean nocturnal;
		public int[] speed;
		
		private Scenery(String spriteCode, double velosity, boolean nocturnal) {
			if(spriteCode != null) {
				this.pamphlet = DataManager.retSheet(spriteCode);
				this.nocturnal = nocturnal;
				
				this.speed = new int[DIVERSITY];
				this.dim = new Dimension[DIVERSITY];
				
				for (int i = 0, p = 100; i < DIVERSITY; i++, p -= 100 / DIVERSITY) {
					int width = (int) Percent.percentOfNum(p, pamphlet.getDimension().width);
					int height = (int) Percent.percentOfNum(p, pamphlet.getDimension().height);
					dim[i] = new Dimension(width, height);
					speed[i] = (int) Percent.percentOfNum(p, FPS.toFrames(velosity));
				}
			}
		}
		
		public double getDensityTimer() {
			double percent = WeatherManager.getDaytimePercent();
			if (nocturnal) percent = 100 - percent;
			return DENSITY.percent(100 - percent);
		}
	}
	
	private final static int DIVERSITY = 4;
	private final static int INFLUENCED_SPEED_DIVISOR = 3;
	private final static RangedInt GROUP = new RangedInt(1, 5);
	
	private static LinkedList<Habitat> hList;
	private static TimingDevice timingDevice;
	private static Scenery collectiveScenery;
	private static GameState gameState;
	private int subType, transparency;
	private int speed;
	private Physics.Vector directX;
	private Point point;
	private Dimension dim;
	private Ground ground;
	private Player player;
	private Animation animation;

	public Habitat(Scenery scenery, Physics.Vector directX, GameState gs) {
		this.subType = RNG.generate(0, DIVERSITY - 1); //SUBTYPE IS SIZE/SPEED;
		this.transparency = (int) Percent.percentOfNum(100 - (subType * (100 / DIVERSITY)), 200);
		this.player = gs.getPlayer();
		this.ground = gs.getArena().getGround();
		
		int spriteSpeed = (subType == 0) ? 1 : 5;
		this.animation = new Animation(scenery.pamphlet);
		animation.setCeiling(spriteSpeed);
		animation.randomize();
		
		this.dim = new Dimension(scenery.dim[subType]);
		this.speed = scenery.speed[subType] + FPS.toFrames(RNG.generateDouble(0.3, 1.3));
		this.directX = directX;
		dim.width *= directX.straight();
		
		double deltaX = RNG.generate(0, 100);
		double deltaY = RNG.generate(0, 20);
		double x = (directX == Physics.Vector.RIGHT) ? - Math.abs(dim.width) * 2 : Game.WIDTH + Math.abs(dim.width) * 2;
		this.point = new Point(x + deltaX,
							   RNG.generate(dim.height / 2,
							  (int) Topology.topLim(x + deltaX)) - dim.height / 2 + deltaY);
	}
	
	public static void setConditions(Scenery s, GameState gs) {
		if (s == Scenery.NONE) return;
		
		hList = new LinkedList<Habitat>();
		gameState = gs;
		collectiveScenery = s;
		timingDevice = new TimingDevice();
		timingDevice.addTimer("spawn", s.getDensityTimer());
	}
	
	public static void updateList(double delta) {
		if (timingDevice == null || timingDevice.getTimer("spawn") == null ||
		    collectiveScenery == null || gameState == null)
			return;
		
		Timer spawn = timingDevice.getTimer("spawn");
		Physics.Vector direction;
		int amount;
		
		if (spawn.progressedToRoof()) {
			amount = GROUP.generate();
			direction = (RNG.generate(0, 1) == 0) ? Physics.Vector.LEFT : Physics.Vector.RIGHT;
			for (int i = 0; i < amount; i++) {
				Habitat.add(new Habitat(collectiveScenery, direction, gameState));
			}
			spawn.init();
		}
		
		for (int i = 0; i < hList.size(); i++)
			hList.get(i).update(delta);
	}
	
	public static void renderList(ExtendedGraphics2D g) {
		for (int i = 0; i < hList.size(); i++)
			hList.get(i).render(g);
	}
	
	public void update(double delta) {
		animation.update(delta);
		
		//move according to player
		//player doesn't move
		if ((!player.isWalkingHorizontally() && !player.isJumpingForward()) || !ground.getImpactZone())
			setX(getX() + delta * speed * directX.straight());
		//player moves
		else if ((player.isWalkingHorizontally() || player.isJumpingForward()) && ground.getImpactZone()) {
			if (player.getUserDirect(Physics.Axis.X) == Physics.Vector.RIGHT) {
				if (directX == Physics.Vector.RIGHT)
					setX(getX() + delta * (speed - player.getSpeed() / INFLUENCED_SPEED_DIVISOR));
				else setX(getX() - delta * (player.getSpeed() / INFLUENCED_SPEED_DIVISOR + speed));
			}
			else if (player.getUserDirect(Physics.Axis.X) == Physics.Vector.LEFT) {
				if (directX == Physics.Vector.RIGHT)
					setX(getX() + delta * (player.getSpeed() / INFLUENCED_SPEED_DIVISOR + speed));
				else setX(getX() - delta * (speed - player.getSpeed() / INFLUENCED_SPEED_DIVISOR));
			}
		}
		
		double edge = getX() + dim.width;
		if (directX == Physics.Vector.LEFT && edge < dim.width
		 || directX == Physics.Vector.RIGHT && edge > Game.WIDTH + dim.width)
			hList.remove(this);
	}
	
	public void render(ExtendedGraphics2D g) {
		g.drawImage(animation.getImage(), point, dim, transparency);
	}
	
	public static void add(Habitat h) { hList.add(h); }
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}