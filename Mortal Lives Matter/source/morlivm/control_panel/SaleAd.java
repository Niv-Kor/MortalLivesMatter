package morlivm.control_panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.system.UI.Button;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Physics;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class SaleAd implements Graphable
{
	public static enum Product {
		AMMO(500),
		STAMINA(3500),
		HP(5000),
		SAVEPOINT(800);
		
		public int price;
		public String ICOpath;
		
		private Product(int price) {
			this.price = price;
			this.ICOpath = new String(PATH + name() + ".png");
		}
	}
	
	private final static String PATH = "/control_panel/purchase/";
	private final static Font PRICE_TAG_FONT = new Font("Ariel", Font.BOLD, 12);
	
	private Product product;
	private Purchasable component;
	private Dimension dim;
	private boolean allow, show;
	private Point cursorP;
	private Button button;
	private Backpack currency;
	private MusicBox musicBox;
	
	public SaleAd(Product product, Purchasable component, Point point, ControlPanel cp) {
		this.product = product;
		this.component = component;
		this.dim = new Dimension(component.getAdDimension());
		this.currency = cp.getCurrency();
		this.allow = true;
		this.button = new Button(null, product.ICOpath, null, point, dim);
		this.musicBox = new MusicBox();
		musicBox.put(new Tune("decline", "/Sound/Main/SFX/Decline.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.export();
	}

	public void update(double delta) {
		if (!allow) return;
		
		if (button.attend(Button.Action.CLICK, Physics.Vector.LEFT))
			purchase();
		
		show = button.isFocused();
	}

	public void render(ExtendedGraphics2D g) {
		if (!allow) return;
		button.render(g);
		
		Point p;
		
		if (show) {
			updateCursorCoord();
			p = new Point(cursorP);
			
			g.setFont(PRICE_TAG_FONT);
			g.drawOutlineString(component.getAdDescription(), p, 1, Color.WHITE, Color.BLACK);
			
			p.setY(cursorP.getY() + 15);
			g.drawOutlineString(product.price + " M", p, 1, Color.YELLOW, Color.BLACK);
		}
	}
	
	public void setPoint(Point p) {
		button.setX(p.getX());
		button.setY(p.getY());
	}
	
	public void purchase() {
		if (!check()) {
			musicBox.play("decline");
			return;
		}
		
		component.purchase();
	}
	
	private boolean check() {
		if (Structure.atBossMap()) {
			Megaphone.announce("Unable to do that during a boss fight.");
			return false;
		}
		
		if (!component.checkPurchaseRelevance()) {
			Megaphone.announce(component.getDeclineErrorMessage());
			return false;
		}
		
		if (!currency.spend(product.price)) {
			Megaphone.announce("Insufficient credit.");
			return false;
		}
		
		return true;
	}
	
	private void updateCursorCoord() {
		cursorP = new Point(Game.getMouseInput().getX() - 50, Game.getMouseInput().getY() - 10);
	}
	
	public Product getProduct() { return product; }
	public void allow(boolean flag) { allow = flag; }
	public void show(boolean flag) { show = flag; }
	public boolean isShowing() { return show; }
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return button.getPoint(); }
	public void setX(double x) { button.setX(x); }
	public void setY(double y) { button.setY(y); }
	public double getX() { return button.getX(); }
	public double getY() { return button.getY(); }
}