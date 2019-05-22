package morlivm.system.UI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import morlivm.main.Game;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.math.Physics;
import morlivm.user_input.MouseInput;

public class DraggingDevice implements GraphicsManager
{
	public static enum Route {
		HORIZONTAL,
		VERTICAL,
		FREE;
	}
	
	private Route route;
	private Graphable component;
	private MouseInput mouseInput;
	private Collider collider, target;
	private boolean touched, isDragging, dropped;
	private Point mouseDist, minLim, maxLim, alignment;
	private static LinkedList<DraggingDevice> dList = new LinkedList<DraggingDevice>();
	
	public DraggingDevice(Graphable component, Route route) {
		this.mouseInput = Game.getMouseInput();
		this.component = component;
		this.route = route;
		this.mouseDist = new Point();
		this.collider = new Collider(component.getPoint(), component.getDimension());
		this.minLim = new Point(-Game.WIDTH * 2, -Game.HEIGHT * 2);
		this.maxLim = new Point(Game.WIDTH * 2, Game.HEIGHT * 2);
		dList.add(this);
	}

	public void update(double delta) {
		if (component == null) {
			dList.remove(this);
			return;
		}
		
		collider.setX(component.getX());
		collider.setY(component.getY());
		hover();
		drag();
		align();
	}

	public void render(ExtendedGraphics2D g) {}
	
	private void hover() {
		if (!(isDragging || (!touched && mouseInput.drag(Physics.Vector.LEFT)))) {
			touched = mouseInput.hover("dragged " + component, collider, Cursor.POINTER, Cursor.POINTER);
			if (touched) saveDragDistance();
		}
	}
	
	private void drag() {
		if (touched && mouseInput.drag(Physics.Vector.LEFT)) {
			if (Cursor.getCursor() == Cursor.POINTER) Cursor.setCursor(Cursor.CLICKER);
			isDragging = true;
			mouseInput.prohibitHovers(true);
			calcDragDistance();
		}
		else {
			isDragging = false;
			mouseInput.prohibitHovers(false);
		}
	}
	
	private void align() {
		if (mouseInput.drag(Physics.Vector.LEFT)) return;
		if (target != null && collider.touch(target)) dropped = true;
		
		if (alignment != null) {
			component.setX(alignment.getX());
			component.setY(alignment.getY());
		}
		else {
			if (component.getY() < 0) component.setY(0);
			else if (component.getY() > Game.HEIGHT - 50) component.setY(Game.HEIGHT - 50);
			if (component.getX() < -component.getDimension().width / 3) component.setX(-component.getDimension().width / 3);
			else if (component.getX() + component.getDimension().width > Game.WIDTH + component.getDimension().width / 3)
				component.setX(Game.WIDTH + component.getDimension().width / 3 - component.getDimension().width);
		}
	}
	
	private void saveDragDistance() {
		if (route == Route.HORIZONTAL || route == Route.FREE)
			mouseDist.setX(mouseInput.getX() - component.getX());
		
		if (route == Route.VERTICAL || route == Route.FREE)
			mouseDist.setY(mouseInput.getY() - component.getY());
	}

	private void calcDragDistance() {
		if (route == Route.HORIZONTAL || route == Route.FREE)
			component.setX(mouseInput.getX() - mouseDist.getX());
		
		if (route == Route.VERTICAL || route == Route.FREE)
			component.setY(mouseInput.getY() - mouseDist.getY());
		
		//check limits
		if (component.getX() < minLim.getX()) component.setX(minLim.getX());
		if (component.getX() > maxLim.getX()) component.setX(maxLim.getX());
		if (component.getY() < minLim.getY()) component.setY(minLim.getY());
		if (component.getY() > maxLim.getY()) component.setY(maxLim.getY());
	}
	
	public void setLimit(Point min, Point max) {
		minLim = new Point(min);
		maxLim = new Point(max);
	}
	
	public static boolean isDraggingAny() {
		for (int i = 0; i < dList.size(); i++)
			if (dList.get(i).isDragging) return true;
		
		return false;
	}
	
	public void setAlignment(Point align) { alignment = new Point(align); }
	public void setDropTarget(Collider c) { target = c != null ? new Collider(c) : null; }
	public Route getRoute() { return route; }
	public boolean isDragging() { return isDragging; }
	public boolean dropped() { return dropped; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}