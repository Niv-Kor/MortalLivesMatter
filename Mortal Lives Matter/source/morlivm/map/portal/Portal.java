package morlivm.map.portal;
import java.awt.Dimension;

import morlivm.content.mortal.Player;
import morlivm.map.Attachable;
import morlivm.map.parallex.Ground;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.performance.TimingDevice;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Portal implements Attachable
{
	protected boolean open, terminate;
	protected int state, transparency;
	protected Dimension dim;
	protected Point originP, point;
	protected Animation animation;
	protected Ground ground;
	protected Player player;
	protected TimingDevice timingDevice;
	protected MusicBox musicBox;
	protected GameState gameState;
	
	public Portal(GameState gs, Player player) {
		this.gameState = gs;
		this.player = player;
		this.ground = gs.getArena().getGround();
		this.open = false;
		this.transparency = 0;
		this.timingDevice = new TimingDevice();
		this.musicBox = new MusicBox();
		this.state = 1;
		this.dim = new Dimension(0, 0);
		musicBox.put(new Tune("halo", "/sound/Main/SFX/Portal.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, true));
		musicBox.export();
	}

	public void update(double delta) {
		if (!open || terminate) return;
		timingDevice.setClocks();
		enter();
		
		if (transparency < 0xFF) {
			transparency += 5;
			if (transparency > 0xFF) transparency = 0xFF;
		}
		
		animation.update(delta);
	}

	public void render(ExtendedGraphics2D g) {
		if (!open) return;
		
		g.drawImage(animation.getImage(), point, null, transparency);
	}
	
	public void open() {
		open = true;
		musicBox.loop("halo");
	}
	
	protected void enter() {}
	
	protected void updateCoordinates() {
		setX(ground.getX() + originP.getX());
		setY(originP.getY());
	}
	
	protected void terminate() {
		terminate = true;
		musicBox.requestRemovalAll();
	}
	
	public boolean isOpen() { return open; }
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public Point getFixedPoint() { return originP; }
}