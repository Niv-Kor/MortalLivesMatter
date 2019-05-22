package morlivm.user_input;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import morlivm.main.Game;
import morlivm.main.testing.Tester;
import morlivm.system.UI.Cursor;
import morlivm.system.UI.Hover;
import morlivm.system.UI.HoverManager;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Physics;
import morlivm.system.performance.TimingDevice;

public class MouseInput implements Graphable, MouseMotionListener, MouseListener, MouseWheelListener
{
	public final static int DOWN = 0, UP = 1;
	public final static int LEFT = 0, RIGHT = 1;
	
	private Point point;
	private HoverManager hoverManager;
	private int[] grab;
	private boolean onTarget, prohibitHovers;
	private boolean[] click, press, release, drag, scroll;
	private TimingDevice timingDevice;
	private MouseEvent event;
	
	public MouseInput(Game game) {
		this.hoverManager = new HoverManager(this);
		this.point = new Point();
		this.timingDevice = new TimingDevice();
		this.click = new boolean[2];
		this.press = new boolean[2];
		this.release = new boolean[2];
		this.drag = new boolean[2];
		this.grab = new int[2];
		this.scroll = new boolean[2];
	}
	
	public void init() {
		timingDevice.addTimer("shake cursor", 0.5);
	}
	
	public void mouseMoved(MouseEvent e) {
		event = e;
		updateCoordinates(e);
	}
	
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			drag[LEFT] = true;
			if (grab[LEFT] == 1) grab[LEFT] = 2;
		}
		else if (SwingUtilities.isRightMouseButton(e)) {
			drag[RIGHT] = true;
			if (grab[RIGHT] == 1) grab[RIGHT] = 2;
		}
		
		event = e;
		updateCoordinates(e);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() > 0) {
			scroll[DOWN] = true;
			scroll[UP] = false;
		}
		else if (e.getWheelRotation() < 0) {
			scroll[DOWN] = false;
			scroll[UP] = true;
		}
		else {
			scroll[DOWN] = false;
			scroll[UP] = false;
		}
	}
	
	public void update(double delta) {
		Game.getKeyInput().requestKeyFocus(); //avoid taking keyboard's control
		if (click[LEFT]) click[LEFT] = false;
		if (click[RIGHT]) click[RIGHT] = false;
		
		if (!drag[LEFT]) grab[LEFT] = 0;
		if (!drag[RIGHT]) grab[RIGHT] = 0;
		
		if (release[LEFT]) release[LEFT] = false;
		if (release[RIGHT]) release[RIGHT] = false;
		
		if (press[LEFT] || press[RIGHT]) hoverManager.hideTargets(true);
		
		if (event != null) updateCoordinates(event);
		scroll[DOWN] = false;
		scroll[UP] = false;
		hoverManager.attemptRelease();
	}
	
	public void render(ExtendedGraphics2D g) {
		if (Tester.graphifyCursor)
			Tester.graphify(g, getMouseCollider(Cursor.CURSOR_PIX_DIM), Color.CYAN);
	}
	
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			press[LEFT] = true;
			click[LEFT] = true;
			if (grab[LEFT] == 0) grab[LEFT] = 1;
		}
		else if (SwingUtilities.isRightMouseButton(e)) {
			press[RIGHT] = true;
			click[RIGHT] = true;
			if (grab[LEFT] == 0) grab[LEFT] = 1;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			release[LEFT] = true;
			press[LEFT] = false;
			drag[LEFT] = false;
		}
		else if (SwingUtilities.isRightMouseButton(e)) {
			release[RIGHT] = true;
			press[RIGHT] = false;
			drag[RIGHT] = false;
		}
		hoverManager.hideTargets(false);
	}
	
	public boolean hover(String name, Collider target, int ptr, int clicker) {
		if (prohibitHovers) return false;
		return hoverManager.hover(new Hover(name, target, ptr, clicker));
	}
	
	public Collider getMouseCollider(int hotSpotRange) {
		if (hotSpotRange < 3) hotSpotRange = 3;
		if (hotSpotRange > Cursor.CURSOR_PIX_DIM) hotSpotRange = Cursor.CURSOR_PIX_DIM;
		
		Dimension hotSpot = new Dimension(hotSpotRange, hotSpotRange);
		Point deviation = new Point(getX() + 10, getY() + 10);
		
		return new Collider(deviation, hotSpot);
	}
	
	private void updateCoordinates(MouseEvent e) {
		double deviation = Cursor.CURSOR_PIX_DIM / 4;
		point.setX(e.getX() - deviation);
		point.setY(e.getY() - deviation);
	}
	
	public boolean scroll() { return scroll[DOWN] || scroll[UP] ; }
	public boolean scroll(Physics.Vector d) { return getBooleanAdjustment(scroll, d); }
	
	public boolean click() { return click[LEFT] || click[RIGHT]; }
	public boolean click(Physics.Vector d) { return getBooleanAdjustment(click, d); }
	
	public boolean press() { return press[LEFT] || press[RIGHT]; }
	public boolean press(Physics.Vector d) { return getBooleanAdjustment(press, d); }
	
	public boolean release() { return release[LEFT] || release[RIGHT]; }
	public boolean release(Physics.Vector d) { return getBooleanAdjustment(release, d);	}
	
	public boolean drag() { return drag[LEFT] || drag[RIGHT]; }
	public boolean drag(Physics.Vector d) { return getBooleanAdjustment(drag, d); }
	
	public boolean grab() { return grab[LEFT] == 2 || grab[RIGHT] == 2; }
	public boolean grab(Physics.Vector d) { return getIntAdjustment(grab, d) == 2; }
	
	private boolean getBooleanAdjustment(boolean[] arr, Physics.Vector d) {
		int index = d == Physics.Vector.LEFT || d == Physics.Vector.DOWN ? 0 : 1;
		return arr[index];
	}
	
	private int getIntAdjustment(int[] arr, Physics.Vector d) {
		int index = d == Physics.Vector.LEFT || d == Physics.Vector.DOWN ? 0 : 1;
		return arr[index];
	}
	
	public boolean voidHover() { return hoverManager.isEmpty(); }
	public void prohibitHovers(boolean flag) { prohibitHovers = flag; }
	public boolean prohibitedHovers() { return prohibitHovers; }
	public HoverManager getHoverManager() { return hoverManager; }
	public void setFocusOnTarget(boolean flag) { onTarget = flag; }
	public boolean isOnTarget() { return onTarget; }
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return null; }
	public Point getPoint() { return point; }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public void setX(double x) {}
	public void setY(double y) {}
}