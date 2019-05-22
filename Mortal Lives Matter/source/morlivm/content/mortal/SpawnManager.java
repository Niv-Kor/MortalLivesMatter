package morlivm.content.mortal;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import morlivm.content.Entity;
import morlivm.content.loot.Ammunition;
import morlivm.content.loot.Coin;
import morlivm.content.loot.Gem;
import morlivm.content.loot.Key;
import morlivm.content.loot.Loot;
import morlivm.content.proj.Bomb;
import morlivm.database.MortalData;
import morlivm.main.Loader;
import morlivm.main.Structure;
import morlivm.main.testing.Tester;
import morlivm.map.AttachManager;
import morlivm.map.Attachable;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.memory.Loadable;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.math.Physics;
import morlivm.system.math.RNG;
import morlivm.warfare.Wave;

public class SpawnManager implements GraphicsManager, AttachManager
{
	private List<Entity> eList;
	private Queue<Loadable> pendingSpawn;
	private Queue<Entity> pendingRemoval;
	private Wave wave;
	private GameState gameState;
	private static boolean activated;
	
	public SpawnManager(GameState gs) {
		this.gameState = gs;
		this.eList = new ArrayList<Entity>();
		this.pendingSpawn = new LinkedList<Loadable>();
		this.pendingRemoval = new LinkedList<Entity>();
		this.wave = new Wave(this, gameState.getArena().getGround());
	}
	
	public void update(double delta) {
		if (eList.size() > 1) sort();
		
		while (!pendingSpawn.isEmpty() && Loader.finished(pendingSpawn.peek()))
			spawn((Entity) pendingSpawn.poll());
		
		while (!pendingRemoval.isEmpty())
			eList.remove(pendingRemoval.poll());
		
		for (int i = 0; i < eList.size(); i++)
			eList.get(i).update(delta);
		
		if(activated) {
			gameState.getContactManager().enemyOnTarget();
			if (Tester.allowSpawn) wave.spawn();
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		for (int i = 0; i < eList.size(); i++)
			eList.get(i).render(g);
	}
	
	public Enemy create(MortalData mdb, boolean minion) {
		if (mdb == null) return null;
		
		Enemy temp = null;
		
		if (mdb.isBoss)	temp = createBoss(mdb);
		else if (minion) temp = createMinion(mdb);
		else {
			switch (mdb.genus) {
				case EARTH: temp = createEarthEnemy(mdb); break;
				case AERO: temp = createAeroEnemy(mdb); break;
				case AQUEOUS:
				case AMPHIBIAN: 
			}
		}
		
		if (temp != null) {
			Loader.load(temp, true);
			pendingSpawn.add(temp);
		}
		return temp;
	}
	
	private Enemy createEarthEnemy(MortalData mdb) {
		Ground ground = gameState.getArena().getGround();
		int deviation = (int) (mdb.size.dimension.height - mdb.head.getY());
		int tempX = RNG.generate(ground.getMinSpawnArea(), ground.getMaxSpawnArea());
		int minY = (int) Topology.topLim(tempX) - deviation;
		int maxY = (int) Topology.bottomLim(tempX) - deviation;
		int tempY = RNG.generate(minY, maxY);
		
		EarthEnemy temp = new EarthEnemy(mdb, new Point(tempX, tempY), gameState);
		return temp;
	}
	
	private Enemy createAeroEnemy(MortalData mdb) {
		Ground ground = gameState.getArena().getGround();
		int tempX = RNG.generate(ground.getMinSpawnArea(), ground.getMaxSpawnArea());
		
		AeroEnemy temp = new AeroEnemy(mdb, new Point(tempX, 0), gameState);
		return temp;
	}
	
	private Enemy createBoss(MortalData mdb) {
		/*
		Point tempPoint = mdb.spawnSpot;
		
		Boss temp = new Boss(mdb, new Point(tempPoint), gameState);
		*/ //TEMP
		
		
		Boss temp = new Boss(mdb, new Point(), gameState); //ALTERNATIVE
		return temp;
	}
	
	private Enemy createMinion(MortalData mdb) {
		Boss boss = (Boss) get(Boss.class, 0);
		if (boss == null) return null;
		
		int[] xRange = new int[2];
		Point minRange = new Point(boss.getX() - Math.abs(boss.getDimension().width), 0);
		Point maxRange = new Point(boss.getX() + Math.abs(boss.getDimension().width), 0);
		Point rightWall = new Point(Structure.getDatabase().rightWall, 0);
		xRange[0] = minRange.smallerThan(new Point(), Physics.Axis.X, 0) ? 0 : (int) minRange.getX();
		xRange[1] = maxRange.largerThan(rightWall, Physics.Axis.X, 0) ? (int) rightWall.getX() : (int) maxRange.getX();
		int deviation = xRange[0] >= mdb.size.dimension.width ? mdb.size.dimension.width : 0;
		int tempX = RNG.generate(xRange[0], xRange[1]) - deviation;
		
		Minion temp = new Minion(mdb, new Point(tempX, 0), gameState);
		return temp;
	}
	
	public void createDropLoot(Point point) {
		int type = RNG.generate(0, 3);
		
		switch(type) {
			case 0: {
				if (RNG.unstableCondition(Gem.DROP_CHANCE))
					spawn(new Gem(point, gameState));
				else createDropLoot(point);
				break;
			}
			case 2: {
				if (RNG.unstableCondition(Ammunition.DROP_CHANCE))
					spawn(new Ammunition(new Point(point), gameState));
				else createDropLoot(point);
				break;
			}
			case 3: {
				if (RNG.unstableCondition(Key.DROP_CHANCE))
					spawn(new Key(new Point(point), gameState));
				else createDropLoot(point);
				break;
			}
		}
	}
	
	public void createSpecialLoot(Point point, Loot.QuestItem type, int chance) {	
		if (RNG.unstableCondition(chance)) {
			try	{
				spawn(type.relatedClass.asSubclass(Loot.class).
					  getConstructor(Point.class, GameState.class).
					  newInstance(point, gameState));
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				
				e.printStackTrace();
				System.err.println("Could not create the special loot " + type.name());
			}
		}
	}
	
	public void createCoinLoot(Point point) {
		spawn(new Coin(new Point(point), gameState));
	}
	
	public int count(Class<?> c) {
		return getAll(c).size();
	}
	
	public Entity get(Class<?> c) {
		return get(c, RNG.generate(0, count(c) - 1));
	}
	
	public Entity get(Class<?> c, int index) {
		List<Entity> raffle = new LinkedList<Entity>();
		for (Entity e : eList)
			if ((e.getClass() == c || e.getClass().getSuperclass() == c) && e.isAlive())
				raffle.add( e);
		
		return index < raffle.size() ? raffle.get(index) : null;
	}
	
	public Stack<Entity> getAll(Class<?> c) {
		Stack<Entity> entStack = new Stack<Entity>();
		
		int i = 0;
		Entity e = get(c, i++);
		while (e != null) {
			entStack.push(e);
			e = get(c, i++);
		}
		
		return entStack;
	}
	
	private void sort() {
		Entity[] tmpArr = eList.toArray(new Entity[eList.size()]);
		eList.clear();
		Arrays.sort(tmpArr, new Comparator<Entity>() {
	        public int compare(Entity o1, Entity o2) {
	            if (o1 == null && o2 == null) return 0;
	            if (o1 == null) return 1;
	            if (o2 == null) return -1;
	            return o1.compareTo(o2);
	        }
		});
		
		for (Entity e : tmpArr) if (!e.isAlive()) eList.add(e);
		for (Entity e : tmpArr) if (e.isAlive()) eList.add(e);
	}
	
    public boolean checkMineAvailability(Point center, double epsilon) {
    	Entity e;
    	Point p;
    	
    	for (int i = 0; i < eList.size(); i++) {
    		e = eList.get(i);
    		if (!(e instanceof Bomb)) continue;
    		else {
    			p = new Point(e.getX(), e.getY());
    			return !(p.withinRange(center, epsilon));
    		}
    	}
    	return true;
    }
    
    public void dismiss() {
    	for (int i = 0; i < eList.size(); i++)
			eList.get(i).terminate(false);
    	eList.clear();
    }
    
    public Wave getWave() { return wave; }
    public void spawn(Entity e) { eList.add(e); }
	public void remove(Entity e) { if (!(e instanceof Boss)) pendingRemoval.add(e); }
	public void spawnMinions() { wave.spawnMinions(); }
	public static boolean isActivated() { return activated; }
	public static void activate(boolean flag) { activated = flag; }
	public List<Entity> getList() { return eList; }
	public List<Attachable> getMagnetizedComponents() { return new ArrayList<Attachable>(eList); }
}