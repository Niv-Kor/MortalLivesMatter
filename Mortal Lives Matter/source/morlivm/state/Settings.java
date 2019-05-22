package morlivm.state;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import morlivm.main.Game;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.system.UI.Button;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.math.Physics;

public class Settings extends State implements GraphicsManager
{
	private Button homeButton;
	private KeySettings keySettings;
	
	public Settings(BufferedImage background, Game game) {
		super(background, game);
		
		this.keySettings = new KeySettings();
		this.homeButton = new Button(MainScreen.BUTTONS_PATH + "Home.png",
									 MainScreen.BUTTONS_PATH + "HomePtr.png", null,
									 new Point(Game.WIDTH / 2 - MainScreen.BUTTON_DIM.width / 2, 50),
									 MainScreen.BUTTON_DIM);
	}
	
	public LoadedSectionsQueue upload() {
		if (Game.getStateManager().isRecorded(this)) return null;
		keySettings.init();
		return null;
	}
	
	public void update(double delta) {
		if (homeButton.attend(Button.Action.CLICK, Physics.Vector.LEFT)) {
			keySettings.cancelSet();
			game.returnToMainScreen(false);
		}
		
		Game.getStateManager().getFloatingPattern().update(delta);
		keySettings.update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		g.drawImage(background, 0, 0, Game.WIDTH, Game.HEIGHT, null);
		Game.getStateManager().getFloatingPattern().render(g);
		homeButton.render(g);
		keySettings.render(g);
	}
	
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}