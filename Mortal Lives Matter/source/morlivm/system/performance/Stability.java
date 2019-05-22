package morlivm.system.performance;
import morlivm.main.Game;

public class Stability
{
	private int fps, ups;
	private int topFPS, normalized;
	
	public Stability() {
		this.fps = 0;
		this.ups = 0;
		this.normalized = Game.STABLE_FPS;
		this.topFPS = normalized;
	}
	
	public void reset() {
		topFPS = fps;
		fps = 0;
		ups = 0;
	}
	
	public boolean isStabilityWithinRange(int epsilon) {
		return topFPS <= normalized + epsilon && topFPS >= normalized - epsilon;
	}
	
	public boolean isStable() { return (topFPS == normalized) ? true : false; }
	public int getFPS() { return fps; }
	public int getUPS() { return ups; }
	public int raiseFPS() { return fps++; }
	public int raiseUPS() { return ups++; }
	public int getTopFPS() { return topFPS; }
	public int getCurrentStability() { return topFPS; }
	public int getNormalizedFPS() { return normalized; }
}
