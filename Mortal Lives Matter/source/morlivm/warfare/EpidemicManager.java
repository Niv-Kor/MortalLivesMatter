package morlivm.warfare;
import java.util.ArrayList;
import java.util.List;

import morlivm.content.Entity;
import morlivm.content.mortal.Enemy;
import morlivm.state.GameState;
import morlivm.system.graphics.Graphable;
import morlivm.system.performance.TimingDevice;
import morlivm.warfare.damage_filter.ImmuneSystem;

public class EpidemicManager
{
	private List<Entity> iList;
	private List<? extends Graphable> eList;
	private TimingDevice timingDevice;
	
	public EpidemicManager(GameState gs) {
		this.iList = new ArrayList<Entity>();
		this.eList = gs.getSpawnManager().getList();
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("delay", 1);
	}
	
	public void update(double delta) {
		if (iList.size() == 0) return;
		
		timingDevice.setClocks();
		
		if (timingDevice.getTimer("delay").reachRoof()) {
			infect();
			timingDevice.getTimer("delay").init();
		}
	}
	
	private void infect() {
		Enemy enemy, infected;
		ImmuneSystem sys;
		
		for (Graphable temp1 : eList) {
			if (temp1 instanceof Enemy) enemy = (Enemy) temp1;
			else continue;
			
			sys = enemy.getImmuneSystem();
			
			if (!sys.isInfected() || !enemy.isAlive()) continue;
			for (Graphable temp2 : iList) {
				if (temp2 instanceof Enemy) infected = (Enemy) temp2;
				else continue;
				
				if (!(infected instanceof Enemy) || infected == enemy) continue;
				else sys.infectOther(infected);
			}
		}
	}
	
	public void add(Entity e) { iList.add(e); }
	public void remove(Entity e) { iList.remove(e); }
	public List<? extends Graphable> getList() { return iList; }
}