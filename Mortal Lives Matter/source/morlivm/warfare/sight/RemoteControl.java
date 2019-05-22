package morlivm.warfare.sight;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import morlivm.content.proj.Bomb;
import morlivm.database.DataManager;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Physics.Vector;

public class RemoteControl extends Sight implements Graphable
{
	private final static Dimension REMOTE_DIM = new Dimension(34, 64);
	private final static Dimension SIGNAL_DIM = new Dimension(34, 33);
	private final static Pamphlet SIGNAL_SS = DataManager.retSheet("ss$g$remote_control_signal");
	private final static BufferedImage[] SWITCH = {ImageHandler.load("/sheets/sight/remote_control/Off.png"),
												   ImageHandler.load("/sheets/sight/remote_control/On.png")};

	private Animation animation;
	private Point signalP;
	private boolean on;
	
	public RemoteControl(SightInventory sightInv, GameState gs) {
		super(sightInv, gs);
		
		this.currentSprite = SWITCH[0];
		this.currentDim = new Dimension(REMOTE_DIM);
		this.animation = new Animation(SIGNAL_SS);
		this.signalP = new Point();
		updateCoordinates(REMOTE_DIM);
	}

	public void update(double delta) {
		super.update(delta);
		
		if (gameState.getContactManager().explosiveOnTarget()) signal(true);
		else signal(false);
		
		updateCoordinates(REMOTE_DIM);
		if (on) {
			animation.update(delta);
			if (mouseInput.click(Vector.LEFT)) {
				((Bomb) target).trigger();
				signal(false);
			}
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		int transparency = on ? 0xFF : 180;
		
		g.drawImage(currentSprite, circularPoint, currentDim, transparency);
		if (on) g.drawImage(animation.getImage(), signalP, SIGNAL_DIM, transparency);
	}
	
	private void signal(boolean flag) {
		if (flag) {
			on = true;
			currentSprite = SWITCH[1];
		}
		else {
			on = false;
			currentSprite = SWITCH[0];
			animation.reset();
		}
	}
	
	protected void updateCoordinates(Dimension dim) {
		super.updateCoordinates(dim);
		signalP.setX(circularPoint.getX() - 2);
		signalP.setY(circularPoint.getY() - 15);
	}
	
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return REMOTE_DIM; }
	public Point getPoint() { return circularPoint; }
	public void setX(double x) { circularPoint.setX(x); }
	public void setY(double y) { circularPoint.setY(y); }
	public double getX() { return circularPoint.getX(); }
	public double getY() { return circularPoint.getY(); }
	public SightInventory.Type getSightType() { return SightInventory.Type.REMOTE_CONTROL; }
}