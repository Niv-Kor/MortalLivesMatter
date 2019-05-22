package morlivm.state;
import java.awt.image.BufferedImage;

import morlivm.main.Game;
import morlivm.memory.Loadable;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Tune;
import morlivm.user_input.MouseInput;

public abstract class State implements GraphicsManager, Loadable
{
	protected BufferedImage background;
	protected MouseInput mouseInput;
	protected Game game;
	
	public State(BufferedImage background, Game game) {
		this.background = background;
		this.game = game;
		this.mouseInput = Game.getMouseInput();
	}
	
	public boolean uploadTest() { return true; }
	public abstract LoadedSectionsQueue upload();
	public void execute() {}
	public String getLoadedUnitCode() { return toString(); }
	public abstract void update(double delta);
	public abstract void render(ExtendedGraphics2D g);
	public void initMusicBox(Tune[] t) { Game.getStateManager().initMusicBox(t); }
	public MusicBox getMusicBox() { return Game.getStateManager().getMusicBox(); }
	public Game getGame() { return game; }
}