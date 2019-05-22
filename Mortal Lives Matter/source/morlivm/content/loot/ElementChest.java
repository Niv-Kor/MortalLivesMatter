package morlivm.content.loot;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import morlivm.control_panel.Megaphone;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.ImageHandler;

public class ElementChest extends Loot
{
	private final static String PATH = "/Sprites/Elements/ElementChest/";
	private final static Dimension DIM = new Dimension(75, 75);
	
	private BufferedImage bubble;
	private boolean pop;
	
	public ElementChest(Point point, GameState gs) {
		super(point, DIM, gs);
		
		this.grantEffectWhenStep = false;
		this.animation = new Animation(PATH + "ElementChest.png");
		this.bubble = ImageHandler.load(PATH + "Pointer.png");
	}
	
	public ElementChest() {
		super();
	}
	
	public void update(double delta) {
		super.update(delta);
		if (step && !pop) Megaphone.announce("Drag a key into the lock to open the chest.");
		if (pop) {
			grantEffect();
			terminate();
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		if (step && !pop) g.drawImage(bubble, new Point(point, -45, -bubble.getHeight() + 5, 0), null, 220);
	}
	
	public void grantEffect() {
		gameState.getPlayer().getElementStack().addRandom();
	}
	
	public Collider getBubbleCollider() {
		Point bubbleP = new Point(point, -45, -bubble.getHeight() + 5, 0);
		Point bubbleHole = new Point(bubbleP, 13, 9, 0);
		Dimension dim = new Dimension(146, 147);
		return new Collider(bubbleHole, dim);
	}
	
	public void popBubble() { pop = true; }
}