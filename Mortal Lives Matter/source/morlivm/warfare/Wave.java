package morlivm.warfare;
import java.util.Queue;

import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.SpawnManager;
import morlivm.database.LevelData;
import morlivm.database.LevelData.WavePattern;
import morlivm.database.MortalData;
import morlivm.main.Structure;
import morlivm.map.parallex.Ground;
import morlivm.system.performance.TimingDevice;

public class Wave
{
	private final static double DELAY = 2;
	
	private SpawnManager spawnMngr;
	private TimingDevice timingDevice;
	private LevelData ldb;
	private WavePattern pattern;
	private Ground ground;
	
	public Wave(SpawnManager spawnMngr, Ground ground) {
		this.spawnMngr = spawnMngr;
		this.ground = ground;
		this.ldb = Structure.getDatabase();
		this.pattern = ldb.waves[0];
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("delay", DELAY);
	}
	
	public void spawn() {
		Queue<MortalData> queue;
		timingDevice.setClocks();
		
		if (spawnMngr.count(Enemy.class) == 0) {
			if (timingDevice.getTimer("delay").progressedToRoof()) {
				if (pattern.getCounter() == 0) {
					ground.clearNextWave();
					pattern = ldb.waves[getSection() - 1];
				}
				else if (pattern.useOne()) {
					queue = pattern.generate();
					
					while(!queue.isEmpty())
						spawnMngr.create(queue.poll(), false);
				}
				timingDevice.getTimer("delay").init();
			}
		}
	}
	
	public void spawnMinions() {
		Queue<MortalData> queue = pattern.generateMinions();
			
		while(!queue.isEmpty())
			spawnMngr.create(queue.poll(), true);
	}
	
	public int getSection() {
		for (int i = 0; i < ldb.waves.length; i++)
			if (ldb.waves[i].getCounter() > 0) return i + 1;
		
		return ldb.waves.length; //top one
	}
}