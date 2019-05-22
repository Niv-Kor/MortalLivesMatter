package morlivm.system.performance;
import morlivm.main.Game;

public class FPS
{
	private static int FPS;
	
	public static void update(double delta) {
		FPS = Game.getStability().getCurrentStability();
	}
	
	public static int toFrames(double sec) {
		return (int) Math.round(sec * Game.getStability().getNormalizedFPS());
	}
	
	public static int toUnstableFrames(double sec) {
		return (int) Math.round(sec * FPS);
	}
}