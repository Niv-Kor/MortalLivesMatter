package morlivm.state;
import java.awt.Dimension;
import java.awt.Image;

import morlivm.main.Game;
import morlivm.system.UI.Button;
import morlivm.system.UI.DraggingDevice;
import morlivm.system.UI.Point;
import morlivm.system.UI.Slider;
import morlivm.system.UI.Toggle;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Physics;
import morlivm.system.sound.Sound;

public class Pause
{
	private static Image screen, settingsUI;
	private static Slider sliderBGM, sliderSFX;
	private static boolean running;
	private static Button xButton, BGMMute, SFXMute;
	private static Toggle mouseModeSwitch;
	
	public static void init() {
		screen = ImageHandler.load("/states/Pause/Screen.png");
		settingsUI = ImageHandler.load("/states/Pause/UI.png");
		
		sliderBGM = new Slider(DraggingDevice.Route.HORIZONTAL, new Point(555, 350), 160);
		sliderSFX = new Slider(DraggingDevice.Route.HORIZONTAL, new Point(555, 393), 160);
		
		sliderBGM.setLevel(Sound.getVolumePercent(Sound.Genre.BGM));
		sliderSFX.setLevel(Sound.getVolumePercent(Sound.Genre.SFX));
		
		xButton = new Button("/states/Pause/DefxButton.png",
							 "/states/Pause/PtrxButton.png", null,
							 new Point(302, 220), new Dimension(30, 30));
		
		BGMMute = new Button(null, null, "/states/Pause/Mute.png",
							 new Point(460, 345), new Dimension(80,30));
		
		SFXMute = new Button(null, null, "/states/Pause/Mute.png",
				 			 new Point(460, 388), new Dimension(80,30));
		
		mouseModeSwitch = new Toggle("mouse mode", new Point(620, 520));
	}
	
	public static void update(double delta) {
		if (!running) return;
		sliderBGM.update(delta);
		sliderSFX.update(delta);
		mouseModeSwitch.update(delta);
		
		Sound.setCollectiveVolume(Sound.Genre.BGM, (float) (sliderBGM.getLevel() / 100));
		Sound.setCollectiveVolume(Sound.Genre.SFX, (float) (sliderSFX.getLevel() / 100));
		
		if (BGMMute.attend(Button.Action.CLICK, Physics.Vector.LEFT)) {
			if (!BGMMute.isChanged()) {
				BGMMute.change(true);
				sliderBGM.preserveLevel();
				sliderBGM.setLevel(0);
			}
			else {
				BGMMute.change(false);
				sliderBGM.setLevel(sliderBGM.getSavedLevel());
			}
		}

		if (SFXMute.attend(Button.Action.CLICK, Physics.Vector.LEFT)) {
			if (!SFXMute.isChanged()) {
				SFXMute.change(true);
				sliderSFX.preserveLevel();
				sliderSFX.setLevel(0);
			}
			else {
				SFXMute.change(false);
				sliderSFX.setLevel(sliderSFX.getSavedLevel());
			}
		}
		
		if (sliderBGM.getLevel() > 0) BGMMute.change(false);
		if (sliderSFX.getLevel() > 0) SFXMute.change(false);
		Sound.save();
		
		if (xButton.attend(Button.Action.CLICK, Physics.Vector.LEFT)) pause(false);
	}
	
	public static void display(ExtendedGraphics2D g) {
		if (!running) return;
		
		g.drawImage(screen, 0, 0, Game.WIDTH, Game.HEIGHT, null);
		g.drawImage(settingsUI, Game.WIDTH / 2 - settingsUI.getWidth(null) / 2,
					Game.HEIGHT / 2 - settingsUI.getHeight(null) / 2,
					settingsUI.getWidth(null), settingsUI.getHeight(null), null);
		
		xButton.render(g);
		BGMMute.render(g);
		SFXMute.render(g);
		sliderBGM.render(g);
		sliderSFX.render(g);
		mouseModeSwitch.render(g);
	}
	
	public static boolean isRunning() { return running; }
	public static void pause(boolean flag) { running = flag; }
}