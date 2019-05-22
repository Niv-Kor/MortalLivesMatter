package morlivm.control_panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.io.UnsupportedEncodingException;

import morlivm.database.ProjectileData;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.NumeralHandler;
import morlivm.system.math.RangedInt;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class Ammo implements Graphable, Purchasable
{
	public final static String PATH = "/control_panel/ammo/ico/";
	private final static Font INFINITY_FONT = new Font("Ariel", Font.PLAIN, 16);
	private final static Font NUMERAL_FONT = new Font("Ariel", Font.BOLD, 12);
	public final static Dimension DIM = new Dimension(50, 50);
	public final static int MAX_AMOUNT = 999;
	public final static int PURCHASED_AMOUNT = 20;
	
	private int amount, pumpSize;
	private Image image;
	private Point point, amountPoint;
	private RangedInt collectable;
	private boolean infinite, questItem, digitsFlag;
	private boolean resetPump, selected;
	private SaleAd saleAd;
	private String display;
	private TimingDevice timingDevice;
	private AmmoManager ammoMngr;
	private ProjectileData pdb;
	
	public Ammo(ProjectileData pdb, Point point, AmmoManager ammoManager) {
		this.pdb = pdb;
		this.image = ImageHandler.load(PATH + pdb.name + ".png");
		this.point = new Point(point);
		this.ammoMngr = ammoManager;
		this.collectable = new RangedInt(pdb.collectableAmount);
		this.infinite = collectable.getMin() == 0 && collectable.getMax() == 0;
		this.amountPoint = new Point(point.getX() + DIM.width - 17, point.getY() + DIM.height - 2);
		this.questItem = pdb.questItem;
		this.timingDevice = new TimingDevice();
		this.saleAd = new SaleAd(SaleAd.Product.AMMO, this, this.point, ammoManager.getControlPanel());
		
		saleAd.allow(!infinite);
		
		if (!infinite) {
			this.amount = collectable.generate();
			this.display = new String("" + amount);
		}
		else {
			this.amount = -1;
			try { display = new String(String.valueOf(Character.toString('\u221E')).getBytes("UTF-8"), "UTF-8"); }
			catch (UnsupportedEncodingException ex) { display = "?"; }
		}
	}
	
	public void update(double delta) {
		saleAd.update(delta);
		
		if (!infinite) display = new String("" + amount);
		if (amount == 0) terminate();
	}
	
	public void render(ExtendedGraphics2D g) {
		Color color;
		
		if (selected) {
			g.setColor(new Color(255, 234, 0));
			g.fillRoundRect((int) getX() - 2, (int) getY() - 2,
							DIM.width + 4, DIM.height + 4, 20, 20);
			
			g.setColor(new Color(255, 216, 0));
			g.fillRoundRect((int) getX() - 2,
							(int) getY() - 2 + DIM.height / 2,
							DIM.width + 4, DIM.height + 4 - DIM.height / 2, 20, 20);
		}
		
		g.drawImage(image, point, DIM);
		pump(g);
		
		color = (amount >= 10 || infinite || questItem) ? Color.WHITE : Color.RED;
		g.setColor(color);
		if (infinite) g.setFont(INFINITY_FONT);
		else g.setFont(NUMERAL_FONT);
		g.drawString(display, (int) amountPoint.getX(), (int) amountPoint.getY());
		
		saleAd.render(g);
	}
	
	public void useOne() {
		if (!infinite) amount--;
		timingDevice.addTimer("pump", 0.05);
		resetPump = true;
		pumpSize = 0;
	}
	
	public void increase() {
		if (infinite) return;
		amount += collectable.generate();
		fixAmountDisplay();
		if (amount > MAX_AMOUNT) amount = MAX_AMOUNT;
	}
	
	public void increase(int a) {
		if (infinite) return;
		amount += a;
		fixAmountDisplay();
		if (amount > MAX_AMOUNT) amount = MAX_AMOUNT;
	}
	
	public void terminate() {
		ammoMngr.reset();
		ammoMngr.remove(this);
	}
	
	public void setPoint(Point p) {
		point = new Point(p);
		amountPoint = new Point(getX() + DIM.width - 17, getY() + DIM.height - 2);
		saleAd.setPoint(p);
	}
	
	private void fixAmountDisplay() {
		if (NumeralHandler.countDigits(amount) == 3 && !digitsFlag) {
			amountPoint.setX(amountPoint.getX() - 5);
			digitsFlag = true;
		}
	
		if (NumeralHandler.countDigits(amount) == 2 && digitsFlag) {
			amountPoint.setX(amountPoint.getX() + 5);
			digitsFlag = false;
		}
	}
	
	private void pump(ExtendedGraphics2D g) {
		Timer pump = timingDevice.getTimer("pump");
		
		if (!(resetPump && pumpSize > 0)) {
			if (pump != null && pump.progressedToRoof()) {
				if (pumpSize++ < 10) {
					g.setColor(new Color(255, 234, 0));
					g.fillRoundRect((int) getX() - 2 - pumpSize, (int) getY() - 2 - pumpSize,
									DIM.width + 4 + pumpSize * 2, DIM.height + 4 + pumpSize * 2, 22, 22);
					
					g.setColor(new Color(255, 216, 0));
					g.fillRoundRect((int) getX() - 2 - pumpSize,
									(int) getY() - 2 - pumpSize + DIM.height / 2,
									DIM.width + 4 + pumpSize * 2,
									DIM.height + 4 + pumpSize * 2 - DIM.height / 2, 22, 22);
					
					g.drawImage(image, (int) getX() - pumpSize, (int) getY() - pumpSize,
							   DIM.width + pumpSize * 2, DIM.height + pumpSize * 2, null);
					
					amountPoint.setX(getX() + DIM.width - 17 + pumpSize);
					amountPoint.setY(getY() + DIM.height - 2 + pumpSize);
				}
				else {
					timingDevice.removeTimer("pump");
					amountPoint.setX(getX() + DIM.width - 17);
					amountPoint.setY(getY() + DIM.height - 2);
					fixAmountDisplay();
				}
			}
		}
		resetPump = false;
	}
	
	public void setX(double x) {
		point.setX(x);
		saleAd.setX(x);
	}
	
	public void setY(double y) {
		point.setY(y);
		saleAd.setY(y);
	}
	
	public static String getDirectory(String icoName) { return PATH + icoName + ".png"; }
	public ProjectileData getProjData() { return pdb; }
	public boolean checkPurchaseRelevance() { return getAmount() < MAX_AMOUNT; }
	public String getDeclineErrorMessage() { return "Reached the maximum amount of bullets."; }
	public String getAdDescription() { return PURCHASED_AMOUNT + " bullets"; }
	public void purchase() { increase(PURCHASED_AMOUNT); }
	public void select(boolean flag) { selected = flag; }
	public Dimension getAdDimension() { return DIM; }
	public int getAmount() { return amount; }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return DIM; }
	public Point getPoint() { return point; }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}