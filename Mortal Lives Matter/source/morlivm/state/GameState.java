package morlivm.state;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import morlivm.content.Entity;
import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.Player;
import morlivm.content.mortal.SpawnManager;
import morlivm.content.proj.RicochetManager;
import morlivm.control_panel.ControlPanel;
import morlivm.control_panel.Megaphone;
import morlivm.control_panel.Notifier;
import morlivm.database.LevelData;
import morlivm.database.MortalData;
import morlivm.main.Game;
import morlivm.main.Loader;
import morlivm.main.Structure;
import morlivm.map.Boundary;
import morlivm.map.Landmark;
import morlivm.map.StainManager;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Arena;
import morlivm.map.weather.WeatherManager;
import morlivm.memory.DeathControl;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.memory.TravelingPack;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.performance.TimingDevice;
import morlivm.system.sound.Sound;
import morlivm.warfare.ContactManager;
import morlivm.warfare.DamageManager;
import morlivm.warfare.EpidemicManager;
import morlivm.warfare.aftershock.AftershockManager;

public class GameState extends State
{
	private Player player;
	private MortalData pdb;
	private LevelData ldb;
	private WeatherManager weatherMngr;
	private Arena arena;
	private ControlPanel controlPanel;
	private SpawnManager spawnMngr;
	private ContactManager contactMngr;
	private RicochetManager ricoMngr;
	private DamageManager damageMngr;
	private StainManager stainMngr;
	private AftershockManager aftershockMngr;
	private EpidemicManager epidemicMngr;
	private TimingDevice timingDevice;
	private boolean terminated;
	
	public GameState(MortalData playerData, Game game) {
		super(null, game);
		this.pdb = playerData;
		this.ldb = Structure.getDatabase();
	}
	
	public void execute() {
		getMusicBox().loop(Sound.Clique.A_BGM);
		getMusicBox().loop(Sound.Clique.A_ATMOSPHERE);
		SpawnManager.activate(true);
	}
	
	public LoadedSectionsQueue upload() {
		LoadedSectionsQueue sections = new LoadedSectionsQueue();
		GameState current = this;
		
		sections.enqueue("Initializing...", new Callable<Void>() {
			public Void call() throws Exception {
				timingDevice = new TimingDevice();
				timingDevice.addTimer("loading", 6.66);
				initMusicBox(ldb.SFX);
				
				return null;
			}
		});
		
		if (terminated) terminated = false;
		if (Game.getStateManager().isRecorded(this)) return null;
		
		sections.enqueue("Loading arena...", new Callable<Void>() {
			public Void call() throws Exception {
				current.damageMngr = new DamageManager();
				current.aftershockMngr = new AftershockManager(current);
				Notifier.init();
				Megaphone.init();
				Landmark.init();
				
				current.weatherMngr = new WeatherManager();
				current.arena = new Arena(current);
				Topology.init(current);
				current.stainMngr = new StainManager(current);
				current.spawnMngr = new SpawnManager(current);
				current.contactMngr = new ContactManager(current);
				current.ricoMngr = new RicochetManager();
				current.epidemicMngr = new EpidemicManager(current);
				
				return null;
			}
		});
		
		sections.enqueue("Drawing boundaries...", new Callable<Void>() {
			public Void call() throws Exception {
				List<Boundary> obstacles = ldb.getObstacles(arena.getGround());
				for (Boundary obs : obstacles) {
					obs.setY(obs.getY() + Topology.groundY(obs.getX()));
					Boundary.add(obs);
				}
				
				return null;
			}
		});
		
		sections.enqueue("Retrieving player information...", new Callable<Void>() {
			public Void call() throws Exception {
				if (TravelingPack.isPacked()) unpack();
				else {
					current.player = new Player(pdb, new Point(Game.WIDTH / 5 - 90, 0), current);
					current.controlPanel = new ControlPanel(player, current);
				}
				
				arena.connectRulerEntity(player);
				arena.getGround().init();
				
				return null;
			}
		});
		
		sections.enqueue("Initializing...", new Callable<Void>() {
			public Void call() throws Exception {
				contactMngr.connectRulerEntity(player);
				spawnMngr.spawn(player);
				if (Structure.atBossMap()) spawnMngr.create(ldb.bosses.poll(), false);
				
				DeathControl.init(game, (MortalData) player.getDatabase());
				Topology.setRulerEntity(player);
				
				return null;
			}
		});
		
		return sections;
	}

	public void update(double delta) {
		if (terminated) return;
		timingDevice.setClocks();
		
		arena.update(delta);
		weatherMngr.update(delta);
		stainMngr.update(delta);
		spawnMngr.update(delta);
		epidemicMngr.update(delta);
		damageMngr.update(delta);
		Boundary.updateBoundaryList(delta);
		aftershockMngr.update(delta);
		controlPanel.update(delta);
		Landmark.update(delta);
		ricoMngr.update(delta);
		Megaphone.update(delta);
		Notifier.update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		arena.getSky().render(g);
		arena.getFarView().render(g);
		arena.getCloseView().render(g);
		arena.getGround().render(g);
		stainMngr.render(g);
		spawnMngr.render(g);
		aftershockMngr.render(g);
		arena.getCliff().render(g);
		renderEnemyComponents(g); //display enemy components over the cliff
		damageMngr.render(g);
		weatherMngr.render(g);
		controlPanel.render(g);
		Megaphone.render(g);
		Notifier.render(g);
		Pause.display(g);
		Boundary.renderBoundaryList(g);
		Landmark.render(g);
		ricoMngr.render(g);
	}
	
	private void renderEnemyComponents(ExtendedGraphics2D g) {
		List<? extends Graphable> list = spawnMngr.getList();
		Entity ent;
		Enemy e;
		
		for (int i = 0; i < list.size(); i++) {
			ent = (Entity) list.get(i);
			if (ent instanceof Enemy && ent.isAlive()) {
				e = (Enemy) list.get(i);
				e.getHP().render(g);
				e.getShield().render(g);
				e.getNameTag().render(g);
				e.getElementStack().render(g);
			}
		}
	}
	
	public void levelUp() {
		Structure.levelUp();
		terminated = true;
		getMusicBox().stop();
		TravelingPack.pack(player, controlPanel);
		Boundary.clear();
		SpawnManager.activate(false);
		Game.getStateManager().requestSmoothState(new GameState(pdb, game), true);
	}
	
	public void requestAfterDeath() {
		terminated = true;
		SpawnManager.activate(false);
		Boundary.clear();
		Loader.clearFromHistory(this);
		Game.getStateManager().getMusicBox().stop();
	}
	
	private void unpack() {
		player = TravelingPack.getPackedPlayer();
		controlPanel = TravelingPack.getPackedPanel();
		controlPanel.relocate();
		player.setX(Game.WIDTH / 5 - 90);
		player.setY(Topology.topLim(player.getX()) + 100 - player.getDimension().height);
		player.unpack(this);
	}
	
	public void connectRulerEntity(Player p) {
		player = p;
		controlPanel = new ControlPanel(player, this);
		arena.connectRulerEntity(player);
		contactMngr.connectRulerEntity(player);
		spawnMngr.spawn(player);
		DeathControl.init(game, (MortalData) player.getDatabase());
	}
	
	public Arena getArena() { return arena; }
	public Player getPlayer() { return player; }
	public ContactManager getContactManager() { return contactMngr; }
	public RicochetManager getRicochetManager() { return ricoMngr; }
	public SpawnManager getSpawnManager() { return spawnMngr; }
	public DamageManager getDamageManager() { return damageMngr; }
	public StainManager getStainManager() { return stainMngr; }
	public AftershockManager getAftershockManager() { return aftershockMngr; }
	public EpidemicManager getEpidemicManager() { return epidemicMngr; }
	public WeatherManager getWeatherManager() { return weatherMngr; }
	public ControlPanel getControlPanel() { return controlPanel; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}