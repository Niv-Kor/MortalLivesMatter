package morlivm.state;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import morlivm.control_panel.Ammo;
import morlivm.database.DataManager;
import morlivm.database.MortalData;
import morlivm.database.ProjectileData;
import morlivm.main.Game;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.system.UI.Button;
import morlivm.system.UI.Cursor;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Octagon;
import morlivm.system.math.Physics;
import morlivm.warfare.damage_filter.Element;
import morlivm.warfare.damage_filter.Stats;

public class CharSelection extends State implements GraphicsManager
{
	public static class DisplayRect implements Graphable
	{
		private final static String DIRECTORY = "/states/OpeningPanel/";
		private final static Dimension RECT_DIM = new Dimension(233, 450);
		private final static Dimension CHAR_DIM = new Dimension(300, 300);
		
		private MortalData db;
		private Button display;
		private Point point;
		private Animation animation;
		private boolean active;
		private String powerStr;
		private Element element;
		
		public DisplayRect(String characterName, Point point) {
			this.db = DataManager.retMortal(characterName);
			
			String buttonImage = new String(DIRECTORY + characterName + "/Panel.png");
			this.point = new Point(point);
			this.display = new Button(buttonImage, null, null, new Point(point), RECT_DIM);
			this.element = new Element(db.elements[0]);
			this.powerStr = new String("power  " + db.stats.generalPower);
			element.activate(new Point(point.getX() + RECT_DIM.width / 2 - element.getDimension().width / 2,
			 		   				   point.getY() + 5 * RECT_DIM.height / 6 + 8));
			
			Pamphlet avatarSS = DataManager.retSheet("ss$m$" + characterName.toLowerCase() + "_display");
			this.animation = new Animation(avatarSS);
		}

		public void update(double delta) {
			display.setX(point.getX());
			display.setY(point.getY());
			display.update(delta);
			
			element.setX(display.getX() + RECT_DIM.width / 2 - element.getDimension().width / 2);
			element.setY(display.getY() + 5 * RECT_DIM.height / 6 + 8);
			element.update(delta);
			
			if (active) {
				if (animation.getRow() == 1 && animation.lastTick()) animation.setRow(2, true);
				animation.update(delta);
			}
			else {
				if (animation.getRow() != 3) animation.setRow(3, true);
				if (!animation.lastTick()) animation.update(delta);
			}
		}

		public void render(ExtendedGraphics2D g) {
			double x = display.getX();
			double y = display.getY();
			
			display.render(g);
			g.setFont(POWER_FONT);
			g.setColor(POWER_COLOR);
			g.drawString(powerStr, (int) (x + RECT_DIM.width / 2 - (powerStr.length() * 14) / 2),
						(int) (y + RECT_DIM.height - 110));
			
			element.render(g);
			g.drawImage(animation.getImage(), new Point((int) x - 30, (int) y - 20), CHAR_DIM);
		}
		
		public void activate(boolean flag) {
			active = flag;
			if (flag) animation.setRow(1, true);
		}
		
		public Button getFrame() { return display; }
		public Element getElement() { return element; }
		public boolean isActive() { return active; }
		public boolean isHovering() { return display.isHovering() && !active; }
		public boolean isAttended() { return display.attend(Button.Action.CLICK, Physics.Vector.LEFT); }
		public MortalData getData() { return db; }
		public void setDimension(Dimension d) {}
		public Dimension getDimension() { return RECT_DIM; }
		public Point getPoint() { return point; }
		public void setX(double x) { point.setX(x); }
		public void setY(double y) { point.setY(y); }
		public double getX() { return point.getX(); }
		public double getY() { return point.getY(); }
	}
	
	private final static Font POWER_FONT = new Font("Aller Display", Font.ITALIC, 26);
	private final static Font SMALL_FONT = new Font("Aller Display", Font.PLAIN, 18);
	private final static Font VALUE_FONT = new Font("Arial Rounded MT Bold", Font.ITALIC, 16);
	private final static BufferedImage ARCHER_SYMBOL = ImageHandler.load("/States/OpeningPanel/ArcherSymbol.png");
	private final static BufferedImage THIEF_SYMBOL = ImageHandler.load("/States/OpeningPanel/ThiefSymbol.png");
	private final static Color POWER_COLOR = new Color(214, 214, 173);
	private final static Point CHAR_STARTING_POINT = new Point(70, 200);
	private final static Point STATS_STARTING_POINT = new Point(0, Game.HEIGHT - 220);
	private final static Color PARAMETER_COLOR = new Color(166, 219, 231);
	private final static int X_SHIFT = 280, Y_SHIFT = 30;
	private final static int STATS_AMOUNT = 10;
	
	private DisplayRect[] displayRects;
	private Button homeButton;
	private String[][][] values;
	private BufferedImage[] classSymbols, blackPattern, blankAmmo;
	private BufferedImage[][] ammo;
	private Collider leftWall, rightWall;
	private Collider extremeLeftWall, extremeRightWall;
	private Octagon[] octagons;
	
	public CharSelection(BufferedImage background, Game game) {
		super(background, game);
		
		this.homeButton = new Button(MainScreen.BUTTONS_PATH + "Home.png",
									 MainScreen.BUTTONS_PATH + "HomePtr.png", null,
									 new Point(Game.WIDTH / 2 - MainScreen.BUTTON_DIM.width / 2, 50),
									 MainScreen.BUTTON_DIM);
	}

	public LoadedSectionsQueue upload() {
		if (Game.getStateManager().isRecorded(this)) return null;
		
		Queue<MortalData> players = DataManager.retAllPlayers();
		MortalData[] midLevelSortage = new MortalData[players.size()];
		this.displayRects = new DisplayRect[players.size()];
		Point point = new Point(CHAR_STARTING_POINT);
		int space = 8;
		
		//initialize
		for (int i = 0; !players.isEmpty(); i++)
			midLevelSortage[i] = players.poll();
		
		sort(midLevelSortage, 0, midLevelSortage.length - 1);
		
		for (int i = 0; i < midLevelSortage.length; i++) {
			point.setX(CHAR_STARTING_POINT.getX() + (DisplayRect.RECT_DIM.width + space) * i);
			displayRects[i] = new DisplayRect(midLevelSortage[i].name, point);
		}
		
		this.leftWall = new Collider(new Point(35, (int) CHAR_STARTING_POINT.getY()),
								  new Dimension(35, DisplayRect.RECT_DIM.height));
		
		this.extremeLeftWall = new Collider(new Point(0, (int) CHAR_STARTING_POINT.getY()),
										 new Dimension(35, DisplayRect.RECT_DIM.height));
		
		this.rightWall = new Collider(new Point(Game.WIDTH - 70, (int) CHAR_STARTING_POINT.getY()),
								   new Dimension(35, DisplayRect.RECT_DIM.height));
		
		this.extremeRightWall = new Collider(new Point(Game.WIDTH - 35, (int) CHAR_STARTING_POINT.getY()),
										  new Dimension(35, DisplayRect.RECT_DIM.height));
		
		initStats();
		return null;
	}
	
	public void initStats() {
		BufferedImage blackPatternIMG = ImageHandler.load("/States/OpeningPanel/BlackPattern.png");
		BufferedImage blankAmmoIMG = ImageHandler.load(Ammo.getDirectory("blank"));
		NumberFormat formatter = new DecimalFormat("#0.0");
		
		Stats[] s = new Stats[displayRects.length]; //pdb.stats
		for (int i = 0; i < displayRects.length; i++)
			s[i] = displayRects[i].getData().stats;
		
		this.octagons = new Octagon[displayRects.length];
		for (int i = 0; i < displayRects.length; i++)
			octagons[i] = new Octagon(s[i], new Point(60, Game.HEIGHT - 180), 60);
				
		this.classSymbols = new BufferedImage[displayRects.length];
		for (int i = 0; i < displayRects.length; i++)
			classSymbols[i] = getClassSymbol(displayRects[i].getData().classType);
		
		//patterns
		this.blackPattern = new BufferedImage[STATS_AMOUNT / 2];
		for (int i = 0; i < blackPattern.length; i++)
			blackPattern[i] = ImageHandler.copy(blackPatternIMG);
		
		//ammo
		this.ammo = new BufferedImage[displayRects.length][5];
		this.blankAmmo = new BufferedImage[5];
		
		//init ammo cubes
		for (int i = 0; i < 5; i++) blankAmmo[i] = ImageHandler.copy(blankAmmoIMG);
		
		ProjectileData[] tempProj;
		for (int i = 0; i < displayRects.length; i++) {
			tempProj = displayRects[i].getData().projectiles;
			
			for (int j = 0, k = 0; j < 5; j++) {
				if (j + k < tempProj.length) {
					
					//bypass any kind of quest items
					if (tempProj[j + k].questItem) {
						if (j + ++k >= tempProj.length) {
							ammo[i][j] = ImageHandler.copy(blankAmmoIMG);
							continue;
						}
					}
					
					//the ammo is appropriate and should be shown
					ammo[i][j] = ImageHandler.load(Ammo.getDirectory(displayRects[i].getData().projectiles[j + k].name));
				}
				else ammo[i][j] = ImageHandler.copy(blankAmmoIMG);
			}
		}
		
		//values
		this.values = new String[displayRects.length][STATS_AMOUNT][2];
		for (int i = 0; i < displayRects.length; i++) {
			values[i][0][0] = new String("ATT: ");
			values[i][0][1] = new String("" + s[i].offense);
			
			values[i][1][0] = new String("DEF: ");
			values[i][1][1] = new String("" + s[i].defense);
			
			values[i][2][0] = new String("HEALTH: ");
			values[i][2][1] = new String("" + s[i].health);
			
			values[i][3][0] = new String("MANA: ");
			values[i][3][1] = new String("" + s[i].mana);
			
			values[i][4][0] = new String("STAMINA: ");
			values[i][4][1] = new String("" + s[i].stamina);
			
			values[i][5][0] = new String("AGILITY: ");
			values[i][5][1] = new String(formatter.format(s[i].agility / Physics.METER) + " m/s");
			
			values[i][6][0] = new String("ACCURACY: ");
			values[i][6][1] = new String("" + s[i].accuracy + " %");
			
			values[i][7][0] = new String("KB CHANCE: ");
			values[i][7][1] = new String("10 %");
			
			values[i][8][0] = new String("CRIT RATE: ");
			values[i][8][1] = new String("" + s[i].criticalRate + " %");
			
			values[i][9][0] = new String("CRIT DAMAGE: ");
			values[i][9][1] = new String("150 %");
		}
	}

	public void update(double delta) {
		for (int i = 0; i < displayRects.length; i++) {
			displayRects[i].update(delta);
			
			if (displayRects[i].isHovering())
				if (!displayRects[i].isActive()) activateRect(i);
			
			if (mouseInput.getHoverManager().isEmpty()) deactivateAll();
		}
		
		for (int i = 0; i < displayRects.length; i++)
			if (displayRects[i].isAttended()) start(displayRects[i].getData());
		
		if (homeButton.attend(Button.Action.CLICK, Physics.Vector.LEFT))
			game.returnToMainScreen(false);
		
		scroll();
		Game.getStateManager().getFloatingPattern().update(delta);
	}

	public void render(ExtendedGraphics2D g) {
		g.drawImage(background, 0, 0, Game.WIDTH, Game.HEIGHT, null);
		Game.getStateManager().getFloatingPattern().render(g);
		homeButton.render(g);
		
		int index = getActiveIndex();
		for (DisplayRect dr : displayRects) dr.render(g);
		renderStats(g, index, index != -1);
	}
	
	private void activateRect(int rect) {
		if (displayRects[rect].isActive()) return;
		
		for (int i = 0; i < displayRects.length; i++)
			displayRects[i].activate(i == rect);
	}
	
	private void scroll() {
		int delta = 0;
		int space = DisplayRect.RECT_DIM.width / 3;
		
		if (displayRects[0].getFrame().getCollider().getX() < space) {
			if (mouseInput.hover("left wall", leftWall, Cursor.DEFAULT, Cursor.DEFAULT)) delta = 2;
			else if (mouseInput.hover("radical left wall", extremeLeftWall, Cursor.DEFAULT, Cursor.DEFAULT)) delta = 6;
		}
		
		if (displayRects[displayRects.length - 1].getFrame().getCollider().getB().getX() > Game.WIDTH - space) {
			if (mouseInput.hover("right wall", rightWall, Cursor.DEFAULT, Cursor.DEFAULT)) delta = -2;
			else if (mouseInput.hover("radical right wall", extremeRightWall, Cursor.DEFAULT, Cursor.DEFAULT)) delta = -6;
		}
		
		//shift
		if (delta != 0)
			for (int i = 0; i < displayRects.length; i++)
				displayRects[i].setX(displayRects[i].getX() + delta);
	}
	
	private void sort(MortalData[] md, int lowerIndex, int higherIndex) {
        int i = lowerIndex;
        int j = higherIndex;

        double pivot = md[lowerIndex + (higherIndex - lowerIndex) / 2].stats.generalPower;

        while (i <= j) {
        	while (md[i].stats.generalPower > pivot) i++;
            while (md[j].stats.generalPower < pivot) j--;
            if (i <= j) swap(md, i++, j--);
        } 
        if (lowerIndex < j) sort(md, lowerIndex, j);
        if (i < higherIndex) sort(md, i, higherIndex);
    }
 
    private void swap(MortalData[] md, int i, int j) {
    	MortalData temp = md[i];
    	md[i] = md[j];
    	md[j] = temp;
    }
	
	private void start(MortalData playerData) {
		getMusicBox().stop("introBGM");
		DataManager.setPlayer(playerData);
		Game.getStateManager().requestSmoothState(new GameState(playerData, game), true);
	}
	
	public Point getCharHeadPoint(String name) {
		for (int i = 0; i < displayRects.length; i++)
			if (displayRects[i].getData().name.equals(name))
				return new Point(displayRects[i].getX() + displayRects[i].getDimension().width / 2,
								 displayRects[i].getY() - 20);
		
		return null;
	}
	
	public BufferedImage getClassSymbol(MortalData.ClassType classType) {
		switch(classType) {
			case ARCHER: return ARCHER_SYMBOL;
			case ROGUE: return THIEF_SYMBOL;
			default: return null;
		}
	}
	
	private void renderStats(ExtendedGraphics2D g, int index, boolean show) {
		double extraLength;
		Element.Type type;
		int valueSpace = 160;
		int x = (int) STATS_STARTING_POINT.getX();
		int y = (int) STATS_STARTING_POINT.getY() + Y_SHIFT * 2;
		Element activeElement;
		Point charHead;
		
		//class symbol
		if (show) {
			charHead = getCharHeadPoint(displayRects[index].getData().name);
			g.drawImage(classSymbols[index],
						(int) charHead.getX() - classSymbols[index].getWidth(null) / 2, (int) charHead.getY(),
					    classSymbols[index].getWidth(null), classSymbols[index].getHeight(null), null);
		}
		
		//stats list
		g.setFont(SMALL_FONT);
		for (int i = 0, xx = X_SHIFT, yy = 0; i < STATS_AMOUNT; i++, yy += Y_SHIFT) {
			if (i == STATS_AMOUNT / 2) {
				xx += X_SHIFT;
				yy = 0;
			}
			else if (i < blackPattern.length) {
				extraLength = (i == 0 || i == 3) ? 1.7 : 1;
				g.drawImage(blackPattern[i], new Point(x + xx - 100,
						    STATS_STARTING_POINT.getY() - 20 + yy),
						    new Dimension((int) (blackPattern[i].getWidth(null) * extraLength),
						    blackPattern[i].getHeight(null)), 200);
			}
			
			for (int k = 0; k == 0 || (k == 1 && show); k++) {
				if (k == 0) {
					g.setFont(SMALL_FONT);
					g.setColor(PARAMETER_COLOR);
				}
				else {
					g.setFont(VALUE_FONT);
					g.setColor(Color.WHITE);
				}
				int h = show ? index : 0;
				g.drawOutlineString(values[h][i][k],
									new Point((int) STATS_STARTING_POINT.getX() + xx + valueSpace * k,
								   (int) STATS_STARTING_POINT.getY() + yy), 1, Color.WHITE, Color.BLACK);
			}
		}
		
		x = Game.WIDTH - 380;
		y = (int) STATS_STARTING_POINT.getY() + 14;
		g.setFont(SMALL_FONT);
		g.setColor(PARAMETER_COLOR);
		
		g.drawOutlineString("AMMO:", new Point(x + 110, y - 13), 1, Color.WHITE, Color.BLACK);
		g.drawOutlineString("ELEMENT:", new Point(x + 10, y + 76), 1, Color.WHITE, Color.BLACK);
		if (show) {
			DisplayRect activeRect = getActiveRect();
			
			g.drawOutlineString(activeRect.getElement().getType().name(), new Point(x + 110, y + 76),
	  						    1, activeRect.getElement().getType().color, Color.BLACK);
			
			for (int i = 0, xx = 0; i < ammo[index].length; i++, x += 60)
				g.drawImage(ammo[index][i], x + xx, y, 50, 50, null);
		}
		else { //show blank ammo cubes
			for (int i = 0, xx = 0; i < blankAmmo.length; i++, x += 60)
				g.drawImage(blankAmmo[i], x + xx, y, 50, 50, null);
		}
		
		//element power comparison
		x = Game.WIDTH - 440;
		DisplayRect activeRect = getActiveRect();
		activeElement = activeRect != null ? activeRect.getElement() : null;
		type = activeRect != null ? activeElement.getType() : Element.Type.NONE;
		g.drawImage(type.powerComparison, new Point(x, y + 75), null);
		
		x = Game.WIDTH - 270;
		y = (int) STATS_STARTING_POINT.getY() + 140;
		
		g.drawOutlineString("strong against: ", new Point(x, y), 1, Color.WHITE, Color.BLACK);
		g.drawOutlineString("weak against: ", new Point(x, y + 30), 1, Color.WHITE, Color.BLACK);
		
		if (show) {
		g.drawOutlineString(activeElement.getSlaveType().name(),
						    new Point(x + 140, y), 1,
						    activeElement.getSlaveType().color, Color.BLACK);
		
		g.drawOutlineString(activeElement.getMasterType().name(),
						    new Point(x + 140, y + 30), 1,
						    activeElement.getMasterType().color, Color.BLACK);
		}
		
		int h = show ? index : 0;
		if (show) octagons[h].show(true);
		else octagons[h].show(false);
		octagons[h].render(g);
	}
	
	private DisplayRect getActiveRect() {
		for (DisplayRect dr : displayRects)
			if (dr.isActive()) return dr;
		
		return null;
	}
	
	private int getActiveIndex() {
		for (int i = 0; i < displayRects.length; i++)
			if (displayRects[i].isActive()) return i;
		
		return -1;
	}
	
	private void deactivateAll() {
		for (int i = 0; i < displayRects.length; i++)
			displayRects[i].activate(false);
	}
	
	public boolean uploadTest() {
		return values[displayRects.length - 1][9][1].equals("150 %");
	}
	
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}