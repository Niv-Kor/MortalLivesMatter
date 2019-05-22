package morlivm.state.scene;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

import morlivm.main.Game;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Physics;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class Signature
{
	/*
	private final static Point LOGO_END_POINT = new Point(Game.WIDTH / 2 - LOGO.getWidth(null) / 2, 70);
	private final static Point LOGO_POINT = new Point(Game.WIDTH / 2 - LOGO.getWidth(null) / 2,
			  										  Game.HEIGHT / 2 - LOGO.getHeight(null) / 2 + 1);
	 */
	
	private BufferedImage logo;
	private Point logoPoint, logoEndPoint;
	private int transparency;
	private boolean running;
	private TimingDevice timingDevice;
	
	public Signature() {
		logo = ImageHandler.load("/Logo/SmallLogo.png");
		timingDevice = new TimingDevice();
		timingDevice.addTimer("start", 3.5);
		timingDevice.addTimer("trans", 0.01);
		transparency = 252;
		logoEndPoint = new Point(Game.WIDTH / 2 - logo.getWidth(null) / 2, 70);
		logoPoint = new Point(Game.WIDTH / 2 - logo.getWidth(null) / 2,
							  Game.HEIGHT / 2 - logo.getHeight(null) / 2 + 1);
	}
	
	public void update(double delta) {
		if (running && transparency == 0) {
			if (logoPoint.largerThan(logoEndPoint, Physics.Axis.Y, 0)) logoPoint.setY(logoPoint.getY() - 2);
			else running = false;
		}
	}
	
	public void signature(ExtendedGraphics2D g, String sig, Font f) {
		if (!running) return;
		Timer startTimer = timingDevice.getTimer("start");
		blacken(g);
		
		g.setFont(f);
		g.setColor(new Color(255, 255, 255, transparency));
		g.drawString(sig, Game.WIDTH / 2 - sig.length() / 2 * 16, Game.HEIGHT / 2 + 200);
		g.drawImage(logo,
				   (int) logoPoint.getX(),
				   (int) logoPoint.getY(),
				   logo.getWidth(null),
				   logo.getHeight(null), null);
		
		if (Game.getStability().getTopFPS() < 5) return;
		if (startTimer.getPointer() < startTimer.getSummedRoof()) startTimer.play();
		if (transparency > 0 && startTimer.reachRoof()) {
			if (timingDevice.getTimer("trans").progressedToRoof()) {
				transparency--;
				timingDevice.getTimer("trans").init();
			}
		}
	}
	
	public boolean done() {
		return !running;
	}
	
	private void blacken(ExtendedGraphics2D g) {
		g.setColor(new Color(0, 0, 0, transparency));
		g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
	}
	
	public void run(boolean flag) {
		running = flag;
	}
	
	public BufferedImage getLogo() { return logo; }
	public Point getLogoEndPoint() { return new Point(logoEndPoint); }
	public boolean isRunning() { return running; }
}