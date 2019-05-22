package morlivm.control_panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;

import morlivm.main.Game;
import morlivm.memory.Memory;
import morlivm.memory.Saveable;
import morlivm.system.UI.Cursor;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.NumeralHandler;
import morlivm.system.performance.TimingDevice;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Backpack implements Graphable, Saveable
{
	private final static Font CREDIT_FONT = new Font("Candara", Font.BOLD, 18);
	private final static Font TRANSACTION_FONT = new Font("Arial Rounded MT Bold", Font.BOLD, 30);
	private final static int INIT_ROD_X = 83, INIT_TIP_X = INIT_ROD_X - 3;
	
	private long credit;
	private int earn, spend;
	private int writtenIndex = -1;
	private Point earnOriginP, earnDraftP, spendOriginP, spendDraftP;
	private Point backpackP, rodP, tipP;
	private BufferedImage backpack, rod, tip;
	private Collider collider;
	private boolean opening, fullyClosed, fullyOpen, writeCredit;
	private int initRodWidth, rodWidth;
	private TimingDevice timingDevice;
	private MusicBox musicBox;
	private int[] digits, trans;
	
	public Backpack() {
		load();
		
		this.backpack = ImageHandler.load("/control_panel/Inventory/Backpack.png");
		this.rod = ImageHandler.load("/control_panel/Inventory/Rod.png");
		this.tip = ImageHandler.load("/control_panel/Inventory/Tip.png");
		
		this.initRodWidth = rod.getWidth(null);
		this.rodWidth = initRodWidth;
		
		this.backpackP = new Point(30, 30);
		this.rodP = new Point(INIT_ROD_X, 50);
		this.tipP = new Point(INIT_TIP_X, 50);
		
		this.trans = new int[2];
		this.digits = new int[0];
		this.spendOriginP = new Point(backpackP.getX() + backpack.getWidth(null) / 2
									  - NumeralHandler.countDigits(earn) * TRANSACTION_FONT.getSize() / 2,
									  backpackP.getY() + backpack.getHeight(null) - 10);
		
		this.earnOriginP = new Point(backpackP.getX() + backpack.getWidth(null) - 10,
									 backpackP.getY() + backpack.getHeight(null) / 2 + TRANSACTION_FONT.getSize() / 2);
		
		this.collider = new Collider(backpack, backpackP, false);
		this.timingDevice = new TimingDevice();
		this.musicBox = new MusicBox();
		musicBox.put(new Tune("spend", "/Sound/Main/SFX/Buy.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		timingDevice.addTimer("credit writer", 0.033);
		musicBox.export();
	}
	
	public void update(double delta) {
		if (Game.getMouseInput().hover("coin", collider, Cursor.POINTER, Cursor.CLICKER)) opening = true;
		else opening = false;
		
		if (opening) {
			if (fullyClosed) countCredit();
			
			if (tipP.getX() < rodP.getX() + rod.getWidth(null))
				tipP.setX(tipP.getX() + 10);
			else if (tipP.getX() + tip.getWidth(null) < backpackP.getX() + 260) {
				tipP.setX(tipP.getX() + 10);
				rodWidth += 10;
			}
			fullyClosed = false;
			if (tipP.getX() + tip.getWidth(null) >= backpackP.getX() + 260) fullyOpen = true;
		}
		else {
			if (tipP.getX() > INIT_TIP_X) tipP.setX(tipP.getX() - 10);
			if (rodWidth > initRodWidth) rodWidth -= 10;
			
			fullyOpen = false;
			writeCredit = false;
			writtenIndex = -1;
			if (tipP.getX() == INIT_TIP_X && rodWidth == initRodWidth) fullyClosed = true;
		}
		
		if (fullyOpen) writeCredit = true;
		timingDevice.setClocks();
	}
	
	public void render(ExtendedGraphics2D g) {
		int digitX = (int) backpackP.getX() + 95;
		String transaction;
		
		g.drawImage(rod, (int) rodP.getX(), (int) rodP.getY(), rodWidth, rod.getHeight(null), null);
		g.drawImage(tip, (int) tipP.getX(), (int) tipP.getY(), tip.getWidth(null), tip.getHeight(null), null);
		g.drawImage(backpack, (int) backpackP.getX(), (int) backpackP.getY(), backpack.getWidth(null), backpack.getHeight(null), null);
		
		if (writeCredit) {
			timingDevice.getTimer("credit writer").play();
			if (writtenIndex < digits.length - 1 && timingDevice.getTimer("credit writer").reachRoof()) {
				writtenIndex++;
				timingDevice.getTimer("credit writer").init();
			}
			if (writtenIndex != -1) {
				g.setFont(CREDIT_FONT);
				g.setColor(Color.BLACK);
				for (int i = 0; i <= writtenIndex; i++)
					g.drawString("" + digits[i], digitX + i * 18, 75);
			}
		}
		
		if (spend > 0 && trans[0] > 0) {
			transaction = new String("-" + spend);
			
			g.setFont(TRANSACTION_FONT);
			g.drawOutlineString(transaction, spendDraftP, 1,
								new Color(255, 68, 68, trans[0]), new Color(191, 0, 0, trans[0]));
			
			spendDraftP.setY(spendDraftP.getY() + 2);
			trans[0] -= 5;
			if (trans[0] <= 0) spend = 0;
		}
		if (earn > 0 && trans[1] > 0) {
			transaction = new String("+" + earn);
			
			g.setFont(TRANSACTION_FONT);
			g.drawOutlineString(transaction, earnDraftP, 1,
								new Color(112, 253, 35, trans[1]), new Color(58, 176, 0, trans[1]));
			
			earnDraftP.setX(earnDraftP.getX() + 2);
			trans[1] -= 5;
			if (trans[1] <= 0) earn = 0;
		}
	}
	
	private void countCredit() {
		int zeros;
		String zerosString, display;
		
		zeros = 8 - countDigits(credit);
		zerosString = new String();
		for (int i = 0; i < zeros; i++)
			zerosString = new String(zerosString.concat("0"));
		if (credit == 0) display = new String(zerosString);
		else display = new String(zerosString + credit);
		
		digits = stringToArray(display, digits);
	}
	
	private int[] stringToArray(String str, int[] digits) {
		Long longString = Long.parseLong(str);
		int length = str.length() - 1;
		digits = new int[length + 1];
		
		while (longString > 0) {
			digits[length--] = (int) (longString % 10);
			longString /= 10;
		}
		return digits;
	}
	
	private int countDigits(long num) {
		int counter = 0;
		while(num > 0) {
			counter++;
			num /= 10;
		}
		return counter;
	}
	
	public boolean spend(int s) {
		if (credit - s >= 0) {
			credit -= s;
			spend = s;
			spendDraftP = new Point(spendOriginP);
			trans[0] = 255;
			musicBox.play("spend");
			save();
			return true;
		}
		else return false;
	}
	
	public void earn(int e) {
		credit += e;
		earn = e;
		earnDraftP = new Point(earnOriginP);
		trans[1] = 255;
		save();
	}
	
	public Point getCoinCoord() { return backpackP; }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return null; }
	public Point getPoint() { return backpackP; }
	public void setX(double x) { backpackP.setX(x); }
	public void setY(double y) { backpackP.setY(y); }
	public double getX() { return backpackP.getX(); }
	public double getY() { return backpackP.getY(); }
	public void save() { Memory.save(credit, Memory.Element.BACKPACK); }
	public void load() { credit = Memory.loadLong(Memory.Element.BACKPACK);	}
}