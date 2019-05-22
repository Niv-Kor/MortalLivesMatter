package morlivm.memory;
import java.awt.Color;
import java.util.Stack;

import morlivm.content.Entity;
import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.Mortal;
import morlivm.content.mortal.Player;
import morlivm.content.mortal.SpawnManager;
import morlivm.control_panel.Megaphone;
import morlivm.database.MortalData;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.map.parallex.Cliff;
import morlivm.map.parallex.Ground;
import morlivm.state.GameState;
import morlivm.state.Load;
import morlivm.state.State;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ScreenDarkener;
import morlivm.system.math.Index;

public class DeathControl implements StaticSaveable
{
	private final static int CLIFF = 0;
	private final static int GROUND = 1;
	private final static int CLOSE_VIEW = 2;
	private final static int FAR_VIEW = 3;
	private final static int SKY = 4;
	
	private static GameState lastSavedLevel;
	private static Point lastSavedPos;
	private static Point[] lastSavedMapPos;
	private static Index gameStructure;
	private static Player player;
	private static MortalData playerDB;
	private static Stack<MortalData> remainEnemies;
	private static int closedBarriers;
	
	public static void init(Game game, MortalData pdb) {
		playerDB = pdb;
		if (Structure.getLevel() == 0) save(false);
	}
	
	public static void save(boolean saveSpot) {
		State state = Game.getStateManager().getCurrentState();
		lastSavedLevel = (state instanceof GameState) ? (GameState) state : (GameState) ((Load) state).getLoadedState();
		closedBarriers = lastSavedLevel.getArena().getGround().getClosedBarriers();
		gameStructure = Structure.getIndex();
		
		if (saveSpot) {
			lastSavedPos = new Point(lastSavedLevel.getPlayer().getX(), lastSavedLevel.getPlayer().getY());
			lastSavedMapPos = lastSavedLevel.getArena().getCurrentPosition();
		}
		else {
			lastSavedPos = new Point(Game.WIDTH / 5 - 90, 0);
			lastSavedMapPos = new Point[5];
			lastSavedMapPos[CLIFF] = new Point(Cliff.ORIGIN_POINT);
			lastSavedMapPos[GROUND] = new Point(Ground.originPoint());
			lastSavedMapPos[CLOSE_VIEW] = new Point(0, 270);
			lastSavedMapPos[FAR_VIEW] = new Point(0, 80);
			lastSavedMapPos[SKY] = new Point(0, 0);
		}
		
		//save enemies amount and identity
		remainEnemies = new Stack<MortalData>();
		SpawnManager spawnMngr = lastSavedLevel.getSpawnManager();
		Stack<Entity> enemies = spawnMngr.getAll(Enemy.class);
		
		MortalData m;
		while (!enemies.isEmpty()) {
			m = (MortalData) ((Mortal) enemies.pop()).getDatabase();
			remainEnemies.push(m);
		}
		
		player = new Player(playerDB, lastSavedPos, lastSavedLevel);
	}
	
	public static void load() {
		ScreenDarkener.apply(Color.BLACK, new Loadable() {
			public LoadedSectionsQueue upload() {
				State gameState = Game.getStateManager().getCurrentState();
				if (gameState instanceof GameState)
					((GameState) gameState).getSpawnManager().dismiss();
				
				lastSavedLevel.requestAfterDeath();
				lastSavedLevel.getArena().getGround().setClosedBarriers(closedBarriers);
				
				Structure.setStruct(gameStructure.row, gameStructure.col);
				SpawnManager spawnMngr = lastSavedLevel.getSpawnManager();
				
				spawnMngr.dismiss();
				lastSavedLevel.connectRulerEntity(player);
				while (!remainEnemies.isEmpty())
					spawnMngr.create(remainEnemies.pop(), false);
				
				lastSavedLevel.getArena().setPosition(lastSavedMapPos);
				
				return null;
			}

			public void execute() {
				Game.getStateManager().requestSmoothState(lastSavedLevel, true);
				Megaphone.announce("Returned to the last savepoint.");
			}

			public String getLoadedUnitCode() {	return toString(); }
			public boolean uploadTest() { return lastSavedLevel.getSpawnManager() != null; }
		}, true);
	}
}