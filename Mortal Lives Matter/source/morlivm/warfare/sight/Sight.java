package morlivm.warfare.sight;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import morlivm.content.Entity;
import morlivm.content.mortal.Player;
import morlivm.control_panel.Notifier;
import morlivm.main.Game;
import morlivm.state.GameState;
import morlivm.system.UI.Cursor;
import morlivm.system.UI.DraggingDevice;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.math.Physics;
import morlivm.system.performance.TimingDevice;
import morlivm.user_input.MouseInput;

public class Sight implements GraphicsManager
{
	protected double distance;
	protected boolean show;
	protected Point circularPoint;
	protected Dimension currentDim;
	protected Player player;
	protected Entity target;
	protected BufferedImage currentSprite;
	protected Animation animation;
	protected TimingDevice timingDevice;
	protected MouseInput mouseInput;
	protected SightInventory sightInv;
	protected GameState gameState;
	
	public Sight(SightInventory sightInv, GameState gs) {
		this.sightInv = sightInv;
		this.gameState = gs;
		this.player = gs.getPlayer();
		this.mouseInput = Game.getMouseInput();
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("vertical", 0.03);
		timingDevice.addTimer("horizontal", 0.03);
		updateDistance();
	}
	
	public void update(double delta) {
		updateDistance();
		timingDevice.setClocks();
		show = mouseInput.getHoverManager().isEmpty() && !Notifier.isHovering() && !DraggingDevice.isDraggingAny();
	}
	
	public void render(ExtendedGraphics2D g) {}
	
	protected void updateDistance() {
		if (target == null) {
			distance = 0;
			return;
		}
		distance = Math.abs((target.getX() + target.getDimension().width / 2)
				   - (player.getX() + player.getDimension().width / 2)) / Physics.METER;
	}
	
	protected void updateCoordinates(Dimension dim) {
		circularPoint = new Point(mouseInput.getX() - dim.width / 2 + Cursor.CURSOR_PIX_DIM / 2,
				  				  mouseInput.getY() - dim.height / 2 + Cursor.CURSOR_PIX_DIM / 2);
	}
	
	public void pump() {}
	public void lock(boolean flag) {}
	public void setTarget(Entity t) { target = t; }
	public int getDexFilter() { return 100; }
	public SightInventory.Type getSightType() { return SightInventory.Type.UNARMED; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}