package morlivm.state;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import morlivm.main.Game;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.state.scene.Signature;
import morlivm.system.UI.Button;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.graphics.ScreenDarkener;
import morlivm.system.math.Physics;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class MainScreen extends State
{
	private static enum StateButton {
		CHAR_SELECTION("Start", CharSelection.class),
		SETTINGS("Settings", Settings.class);
		
		public String name;
		public Button button;
		public State state;
		public boolean request;
		private Class<?> c;
		
		public Class<?> getStateClass() { return c; }
		
		private StateButton(String name, Class<?> c) {
			this.name = new String(name);
			this.c = c;
		}
	}
	
	private final static Font SIG_FONT = new Font("Arial Rounded MT Bold", Font.PLAIN, 28);
	public final static String BUTTONS_PATH = "/States/OpeningPanel/Buttons/";
	public final static Dimension BUTTON_DIM = new Dimension(262, 72);
	
	private List<StateButton> buttons;
	private BufferedImage logo;
	private Point logoEndPoint;
	private String sig;
	private Signature scene;
	
	public MainScreen(Game game) {
		super(null, game);
		this.scene = new Signature();
		scene.run(true);
	}

	public LoadedSectionsQueue upload() {
		if (Game.getStateManager().isRecorded(this)) return null;
		
		this.buttons = Arrays.asList(StateButton.values());
		this.logo = ImageHandler.copy(scene.getLogo());
		this.background = ImageHandler.load("/States/OpeningPanel/Background.jpg");
		this.logoEndPoint = scene.getLogoEndPoint();
		this.sig = new String("Made by Niv Kor, 2018");
		
		StateButton temp;
		for (int i = 0; i < buttons.size(); i++) {
			temp = buttons.get(i);
			temp.button = new Button(BUTTONS_PATH + temp.name + ".png",
									 BUTTONS_PATH + temp.name + "Ptr.png", null,
									 new Point(Game.WIDTH / 2 - BUTTON_DIM.width / 2,
									 Game.HEIGHT / 2 + 50 + (BUTTON_DIM.height + 10) * i),
									 BUTTON_DIM);
			
			try {
				temp.state = temp.getStateClass().asSubclass(State.class).
							 getConstructor(BufferedImage.class, Game.class).
							 newInstance(background, game);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				
				e.printStackTrace();
				System.err.println("ERROR: The state " + temp.getClass() + " could not be loaded.");
			}
		}
		
		getMusicBox().put(new Tune("introBGM", "/Sound/Main/BGM/Intro.wav", Sound.Clique.GENERAL, Sound.Genre.BGM, true));
		getMusicBox().export();
		
		return null;
	}
	
	public void execute() {
		getMusicBox().loop("introBGM");
	}

	public void update(double delta) {
		if (!scene.done()) {
			scene.update(delta);
			return;
		}
		
		for (StateButton b : buttons) {
			b.button.update(delta);
			
			if (b.button.attend(Button.Action.CLICK, Physics.Vector.LEFT)) {
				b.request = true;
				ScreenDarkener.apply(Color.BLACK, b.state, true);
			}
		}
		Game.getStateManager().getFloatingPattern().update(delta);
	}

	public void render(ExtendedGraphics2D g) {
		g.drawImage(background, 0, 0, Game.WIDTH, Game.HEIGHT, null);
		Game.getStateManager().getFloatingPattern().render(g);
		
		if (!scene.done()) {
			scene.signature(g, sig, SIG_FONT);
		}
		else {
			g.drawImage(logo,
					   (int) logoEndPoint.getX(),
					   (int) logoEndPoint.getY(),
					    logo.getWidth(null),
					    logo.getHeight(null), null);
			
			g.setColor(Color.GREEN);
			
			for (int i = 0; i < buttons.size(); i++) buttons.get(i).button.render(g);
		}
	}
	
	public State getRequestedState() {
		for (StateButton b : buttons) {
			if (b.request) {
				b.request = false;
				return b.state;
			}
		}
		return null;
	}
	
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}