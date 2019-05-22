package morlivm.control_panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;

import morlivm.content.loot.ElementChest;
import morlivm.content.loot.Key;
import morlivm.main.Structure;
import morlivm.memory.Memory;
import morlivm.memory.Saveable;
import morlivm.state.GameState;
import morlivm.system.UI.DraggingDevice;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;

public class KeyCounter implements Graphable, Saveable
{
	private final static Font FONT = new Font("Arial Rounded MT Bold", Font.ITALIC, 16);
	private final static Dimension DIM = new Dimension(70, 70);
	private final static BufferedImage FULL_IMG = ImageHandler.load(Key.PATH + "ico.png");
	private final static BufferedImage VOID_IMG = ImageHandler.load(Key.PATH + "void_ico.png");
	private final static int MIN = 0, MAX = 99;
	
	private int amount;
	private Point point, originP, strP;
	private DraggingDevice draggingDevice;
	private GameState gameState;
	
	public KeyCounter(GameState gs) {
		load();
		
		this.gameState = gs;
		this.point = new Point(33, 145);
		this.originP = new Point(point);
		this.strP = new Point(point, DIM.width - 15, DIM.height - 5, 0);
		this.draggingDevice = new DraggingDevice(this, DraggingDevice.Route.FREE);
		draggingDevice.setAlignment(point);
	}
	
	public void update(double delta) {
		if (getAmount() == 0) return;
		draggingDevice.update(delta);
		
		ElementChest chest = null;
		
		if (Structure.atBossMap()) 
			chest = (ElementChest) gameState.getSpawnManager().get(ElementChest.class, 0);
		
		if (chest != null && chest.isSteppedOn()) {
			Collider bubbleC = chest.getBubbleCollider();
			if (bubbleC != null) draggingDevice.setDropTarget(bubbleC);
			if (draggingDevice.dropped()) {
				chest.popBubble();
				use();
			}
		}
		else draggingDevice.setDropTarget(null);
	}
	
	public void render(ExtendedGraphics2D g) {
		g.setFont(FONT);
		g.drawOutlineString("x" + amount, strP, 1, Color.WHITE, Color.BLACK);
		
		g.drawImage(VOID_IMG, originP, DIM);
		if (amount > 0) g.drawImage(FULL_IMG, point, DIM);
	}
	
	public void add() {
		if (amount < MAX) amount++;
		save();
	}
	
	public void use() {
		if (amount > MIN) amount--;
		save();
	}
	
	public DraggingDevice getDraggingDevice() { return draggingDevice; }
	public void save() { Memory.save(amount, Memory.Element.KEYS); }
	public void load() { amount = Memory.loadInt(Memory.Element.KEYS); }
	public int getAmount() { return amount; }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return DIM; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}