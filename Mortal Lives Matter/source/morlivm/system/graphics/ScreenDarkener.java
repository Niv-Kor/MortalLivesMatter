package morlivm.system.graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import morlivm.main.Game;
import morlivm.main.Loader;
import morlivm.memory.Loadable;
import morlivm.state.State;
import morlivm.system.UI.Point;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class ScreenDarkener
{
	private final static int MIN = 0, MAX = 252;
	private final static int PULSE = 14;
	
	private static Color color;
	private static BufferedImage image;
	private static Loadable requestedComponent;
	private static int transparency;
	private static TimingDevice timingDevice;
	private static boolean darken, requestFlag, stable, initialize;
	
	public static void init() {
		timingDevice = new TimingDevice();
		timingDevice.addTimer("trans", 0.01);
		reset();
	}
	
	public static void update(double delta) {
		if (color == null && image == null) return;
		timingDevice.setClocks();
		
		Timer trans = timingDevice.getTimer("trans");
		
		if (trans.progressedToRoof()) {
			if (darken) {
				if (transparency < MAX) transparency += PULSE;
				else if (!initialize || (initialize && Loader.finished(requestedComponent))) {
					darken = false;
					if (requestedComponent != null) {
						requestedComponent.execute();
						if (requestedComponent instanceof State)
							Game.getStateManager().requestState((State) requestedComponent);
					}
				}
			}
			else {
				if (!requestFlag && requestedComponent != null) requestFlag = true;
				if (Game.getStability().isStable() || stable) {
					stable = true;
					if (transparency > MIN) transparency -= PULSE;
					else reset();
				}
			}
			trans.init();
		}
	}
	
	public static void render(ExtendedGraphics2D g) {
		if (color != null) {
			g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), transparency));
			g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
		}
		else if (image != null) {
			g.drawImage(image, new Point(), new Dimension(Game.WIDTH, Game.HEIGHT), transparency);
		}
	}
	
	public static void apply(Color c, Loadable component, boolean init) {
		color = c;
		requestedComponent = component;
		initialize = init;
		if (init && component != null) Loader.load(component, false);
		Game.getKeyInput().enable(false);
	}
	
	public static void apply(BufferedImage im, Loadable component, boolean init) {
		image = im;
		requestedComponent = component;
		initialize = init;
		if (init && component != null) Loader.load(component, false);
		Game.getKeyInput().enable(false);
	}
	
	public static void reset() {
		transparency = 0;
		initialize = false;
		stable = false;
		color = null;
		image = null;
		darken = true;
		requestFlag = false;
		requestedComponent = null;
		Game.getKeyInput().enable(true);
	}
}