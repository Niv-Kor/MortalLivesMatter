package morlivm.map;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class Earthquake
{
	private final static double MOVEMENT = 0.05, COOLDOWN = 2;
	
	private boolean moveable, quakes, cools;
	private boolean[] quakeStages;
	private int[] richterScale;
	private TimingDevice timingDevice;
	private Magnet magnet1, magnet2;
	private GameState gameState;
	
	public Earthquake(GameState gs) {
		this.gameState = gs;
		this.quakes = false;
		this.cools = false;
		this.moveable = true;
		this.magnet1 = new Magnet();
		this.magnet2 = new Magnet();
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("quake", MOVEMENT);
		timingDevice.addTimer("cooldown", COOLDOWN);
	}
	
	public void update(double delta) {
		timingDevice.setClocks();
		
		int nextQuake, direct, richter = 0;
		Timer quake = timingDevice.getTimer("quake");
		Timer cooldown = timingDevice.getTimer("cooldown");
		
		if (!quakes) {
			if (cools && cooldown.progressedToRoof()) {
				cools = false;
				moveable = true;
			}
			return;
		}
		else if (quake.progressedToRoof() && (!magnet1.isEmpty() || !magnet2.isEmpty())) {
			nextQuake = getNextQuakeStage();
			if (nextQuake != -1) {
				richter = richterScale[nextQuake / 2];
				direct = (nextQuake % 2 == 0) ? 1 : -1;
				
				magnet1.translate(new Point(richter * direct, richter * direct));
				magnet2.translate(new Point((richter + 2) * direct, (richter + 2) * direct));
				
				if (nextQuake < 0) {
					//TODO attachY()
				}
				
				quake.init();
			}
			else { //done quaking
				quakes = false;
				cools = true;
				magnet1.attach();
				magnet2.attach();
				magnet1.close();
				magnet2.close();
				return;
			}
		}
	}
	
	public void set(int richter, boolean moveability) {
		if (quakes) return;
		if (richter % 2 == 1) richter++;
		
		quakes = true;
		moveable = moveability;
		timingDevice.getTimer("cooldown").renew(COOLDOWN);
		timingDevice.getTimer("quake").renew(MOVEMENT);
		quakeStages = new boolean[richter * 2];
		richterScale = new int[richter];
		
		for (int i = 0, k = richter * 2 + 2; i < richterScale.length; i++, k -= 2)
			richterScale[i] = k;
		
		//magnetizing components
		magnet1.add(gameState.getArena(), true);
		magnet1.add(gameState.getSpawnManager(), false);
		magnet2.add(gameState.getArena().getCliff(), true);
	}
	
	private int getNextQuakeStage() {
		for (int i = 0; i < quakeStages.length; i++) {
			if (quakeStages[i] == false) {
				quakeStages[i] = true;
				return i;
			}
		}
		return -1;
	}
	
	public boolean getMoveability() { return moveable || !isQuaking(); }
	public boolean isQuaking() { return quakes || cools; }
}