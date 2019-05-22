package morlivm.main;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import morlivm.database.query.SQLRetriever;
import morlivm.main.testing.Tester;
import morlivm.memory.Memory;
import morlivm.state.GameState;
import morlivm.state.Load;
import morlivm.state.MainScreen;
import morlivm.state.Pause;
import morlivm.state.StateManager;
import morlivm.system.UI.Cursor;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.graphics.ScreenDarkener;
import morlivm.system.performance.FPS;
import morlivm.system.performance.Stability;
import morlivm.system.sound.Sound;
import morlivm.user_input.KeyInput;
import morlivm.user_input.KeyProcessor;
import morlivm.user_input.MouseInput;

@SuppressWarnings("serial")
public class Game extends Canvas implements Runnable
{
	private final static Dimension DIM = new Dimension(1200, 900);
	public final static int WIDTH = DIM.width, HEIGHT = DIM.height;
	public final static int STABLE_FPS = 60;

	private JFrame frame;
	private Dimension size;
	private Thread thread;
	private BufferStrategy buffer;
	private MainScreen mainScreen;
	private static KeyInput keyInput;
	private static MouseInput mouseInput;
	private static StateManager stateMngr;
	private static Stability stability;
	private volatile boolean running;

	public Game() {
		size = new Dimension(WIDTH, HEIGHT);
		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);
		setSize(size);
	}

	public void init() {
		BufferedImage gameIcon = ImageHandler.load("/Logo/Icon.png");
		
		frame = new JFrame("Mortal Lives Matter");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setIconImage(gameIcon);
		frame.add(this);
		frame.pack();
		frame.setLocationRelativeTo(null);
		keyInput = new KeyInput();
		mouseInput = new MouseInput(this);
		frame.add(keyInput);
		frame.setVisible(true);
		this.addMouseMotionListener(mouseInput);
		this.addMouseWheelListener(mouseInput);
		this.addMouseListener(mouseInput);
		frame.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {	keyInput.requestFocus(); }
			public void focusLost(FocusEvent e) {}
		});

		createBufferStrategy(3);
		buffer = getBufferStrategy();
		SQLRetriever.init();
		stability = new Stability();
		try	{ Memory.init(); } catch (IOException e1) {	e1.printStackTrace(); }
		Load.init();
		KeyProcessor.load();
		Cursor.init(this);
		Sound.init();
		Pause.init();
		Structure.init();
		mouseInput.init();
		stateMngr = new StateManager();
		stateMngr.init();
		ScreenDarkener.init();
		
		//manually upload mainScreen
		mainScreen = new MainScreen(this);
		mainScreen.upload();
		mainScreen.execute();
		Loader.appandHistory(mainScreen);
		
		stateMngr.requestState(mainScreen);
		Tester.apply();
	}

	public synchronized void start() {
		if (running)
			return;
		thread = new Thread(this);
		running = true;
		thread.start();
	}

	public synchronized void stop() {
		if (!running)
			return;
		running = false;
		thread.interrupt();
		frame.dispose();
	}

	public void update(double delta) {
		if (stateMngr.getCurrentState() instanceof GameState)
			if (KeyProcessor.isPressed(KeyProcessor.Key.PAUSE)) Pause.pause(!Pause.isRunning());
		
		if (!Pause.isRunning()) {
			stateMngr.update(delta);
		}
		
		KeyProcessor.update(delta);
		ScreenDarkener.update(delta);
		FPS.update(delta);
		Sound.update(delta);
		Pause.update(delta);
		Cursor.update(delta);
		mouseInput.update(delta);
		Tester.update(delta);
	}
	
	public void render() {
		ExtendedGraphics2D g = new ExtendedGraphics2D((Graphics2D) buffer.getDrawGraphics());
		stateMngr.render(g);
		mouseInput.render(g);
		ScreenDarkener.render(g);
		mouseInput.render(g);

		g.dispose();
		buffer.show();
	}

	public void run() {
		init();

		double delta = 0, frameTime = 1.0 / 60.00;
		int counter = 0;
		long lastTime = System.nanoTime(), timer = System.currentTimeMillis();
		
		while (running) {
			long now = System.nanoTime();
			long passedTime = now - lastTime;
			lastTime = now;

			delta += passedTime / 1000000000.0;

			if (delta > frameTime) {
				stateMngr.processStateChanges();
				update(frameTime);
				render();
				stability.raiseFPS();
				stability.raiseUPS();
				delta -= frameTime;
			}
			
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				if (counter++ == Tester.fpsDisplay) {
					System.out.println("fps: " + stability.getFPS() + ", ups: " + stability.getUPS());
					counter = 0;
				}
				stability.reset();
			}
			
			try	{ Thread.sleep(8); }
			catch (Exception e) {}
		}
	}
	
	public void returnToMainScreen(boolean newInstance) {
		if (newInstance) mainScreen = new MainScreen(this);
		stateMngr.requestSmoothState(mainScreen, false);
	}
	
	public void add(Component c) { frame.add(c); }
	public static StateManager getStateManager() { return stateMngr; }
	public static KeyInput getKeyInput() { return keyInput; }
	public static MouseInput getMouseInput() { return mouseInput; }
	public static Stability getStability() { return stability; }
	public JFrame getFrame() { return frame; }
	
	public static void main(String[] args) {
		Game game = new Game();
		Loader loader = new Loader();
		game.start();
		loader.start();
	}
}