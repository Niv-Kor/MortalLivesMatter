package morlivm.map.orientation;
import java.util.concurrent.Callable;
import morlivm.content.mortal.Mortal;
import morlivm.state.GameState;
import morlivm.system.graphics.GraphicsManager;

public abstract class Motion implements GraphicsManager
{
	protected Mortal mortal;
	protected Callable<Void> start, finish;
	protected boolean enabled, preperations, closure;
	protected GameState gameState;
	
	public void update(double delta) {
		if (isEnabled() && !preperations) start();
	}
	
	protected boolean start() {
		if (preperations) return false;
		
		if (start != null) {
			try	{ start.call(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		
		preperations = true;
		closure = false;
		return true;
	}
	
	protected boolean finish() {
		if (closure) return false;
		
		if (finish != null) {
			try	{ finish.call(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		
		preperations = false;
		closure = true;
		return true;
	}
	
	public void setRulerState(GameState gs) { gameState = gs; }
	public void setStartFunc(Callable<Void> func) { start = func; }
	public void setFinishFunc(Callable<Void> func) { finish = func; }
	public void enable(boolean flag) { enabled = flag; }
	public boolean isEnabled() { return enabled; }
	public boolean isActive() { return preperations; }
	public abstract void fail();
}