package morlivm.state;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import morlivm.main.Game;
import morlivm.main.Loader;
import morlivm.main.Structure;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.graphics.ScreenDarkener;
import morlivm.system.math.Percent;
import morlivm.system.math.RNG;
import morlivm.system.performance.Stability;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class Load extends State
{
	private final static Font FONT = new Font("Segoe UI", Font.BOLD, 18);	
	private final static String BG_DIRECTORY = "/states/loading/backgrounds/";
	private final static BufferedImage LOGO = ImageHandler.load("/Logo/SmallLogo.png");
	private final static Dimension BAR_DIM = new Dimension(1024, 45);
	
	private BufferedImage background, bar;
	private static BufferedImage[] barSet;
	private State loadedState;
	private String loadingStr;
	private int barCount;
	private int[][] polyRoundXY;
	private boolean tickBarForward, requestInitializing, initializedState;
	private double percentage, randLagP;
	private DecimalFormat decimalFormat;
	private TimingDevice timingDevice;
	private Stability stability;
	
	public Load(State loadedState, BufferedImage background, Game game) {
		super(null, null);
		
		this.loadedState = loadedState;
		this.stability = Game.getStability();
		this.background = ImageHandler.load(BG_DIRECTORY + Structure.getDatabase().mapName + ".png");
		this.percentage = 0;
		this.barCount = 0;
		this.bar = barSet[barCount];
		this.tickBarForward = true;
		this.loadingStr = new String("Loading");
		this.polyRoundXY = new int[2][11];
		this.randLagP = RNG.generateDouble(90, 95);
		this.decimalFormat = new DecimalFormat("#.#");
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("bar", 0.083);
		timingDevice.addTimer("loading string", 0.5);
		
		int barX = 90;
		int barY = 752;
		int barW = BAR_DIM.width - 15;
		int barH = BAR_DIM.height - 5;
		
		//x axis
		polyRoundXY[0][0] = barX;
		polyRoundXY[0][1] = barX + barW - 5;
		polyRoundXY[0][2] = barX + barW;
		polyRoundXY[0][3] = barX + barW;
		polyRoundXY[0][4] = barX + barW - 5;
		polyRoundXY[0][5] = barX;
		polyRoundXY[0][6] = barX + 2;
		polyRoundXY[0][7] = barX + 4;
		polyRoundXY[0][8] = barX + 4;
		polyRoundXY[0][9] = barX + 2;
		polyRoundXY[0][10] = barX;
		
		//y axis
		polyRoundXY[1][0] = barY;
		polyRoundXY[1][1] = barY;
		polyRoundXY[1][2] = barY + 10;
		polyRoundXY[1][3] = barY + barH - 10;
		polyRoundXY[1][4] = barY + barH;
		polyRoundXY[1][5] = barY + barH;
		polyRoundXY[1][6] = barY + barH - 8;
		polyRoundXY[1][7] = barY + barH - 16;
		polyRoundXY[1][8] = barY + barH - 26;
		polyRoundXY[1][9] = barY + barH - 34;
		polyRoundXY[1][10] = barY;
	}

	public static void init() {
		barSet = new BufferedImage[10];
		for (int i = 0; i < barSet.length; i++)
			barSet[i] = ImageHandler.load("/States/Loading/Bar/" + (i + 1) + ".png");
	}
	
	public LoadedSectionsQueue upload() { return null; }

	public void update(double delta) {
		timingDevice.setClocks();
		
		Timer barTmr = timingDevice.getTimer("bar");
		Timer loadingStrTmr = timingDevice.getTimer("loading string");
		int addition;
		int currentWidth = polyRoundXY[0][1] - polyRoundXY[0][0];
		int barW = bar.getWidth(null);
		boolean barNotFull = polyRoundXY[0][0] < polyRoundXY[0][1];
		boolean barFarFromFull = polyRoundXY[0][0] < polyRoundXY[0][1] - 5;
		
		//tick components
		if (barTmr.progressedToRoof()) {
			bar = barSet[changeBarImage()];
			barTmr.init();
		}
		
		if (loadingStrTmr.progressedToRoof()) {
			loadingStr = changeLoadingStr();
			loadingStrTmr.init();
		}
		
		//progress bar
		if (barNotFull) {
			if (stability.isStable()) {
				if (barFarFromFull) addition = 3;
				else addition = 1;
			}
			else if (stability.isStabilityWithinRange(5) && percentage < randLagP) addition = 1;
			else addition = 0;
		}
		else addition = 0;
		
		for (int i = 0; i < polyRoundXY[0].length; i++)
			if (i < 1 || i > 4) polyRoundXY[0][i] += addition;
		
		if (barNotFull) {
			percentage = Percent.limit(Double.parseDouble(decimalFormat.format
									  (Percent.numOfNum(barW - currentWidth, barW))));
		}
		else if (Loader.finished(loadedState)) ScreenDarkener.apply(Color.BLACK, loadedState, false);
		
		if (requestInitializing) {
			Loader.load(loadedState, false);
			requestInitializing = false;
			initializedState = true;
		}
	}

	public void render(ExtendedGraphics2D g) {
		g.drawImage(background, new Point(), null);
		g.drawImage(LOGO, new Point(300, 200), null);
		g.drawImage(bar, new Point(80, 750), null);
		
		//"Loading" + percentage
		g.setColor(Color.WHITE);
		g.setFont(FONT);
		g.drawString(loadingStr, Game.WIDTH / 2 - 45, 740);
		g.drawString(percentage + " %", Game.WIDTH / 2 - 30, 820);
		
		//bar coverage
		g.setColor(Color.BLACK);
		if (percentage == 0) g.fillRoundRect(83, 753, 20, 35, 20, 30);
		g.fillPolygon(polyRoundXY[0], polyRoundXY[1], 11);
		
		if (!requestInitializing && !initializedState) requestInitializing = true;
	}
	
	private int changeBarImage() {
		if (barCount < 9 && tickBarForward) return barCount++;
		else if (barCount > 0) {
			tickBarForward = false;
			return barCount--;
		}
		else {
			tickBarForward = true;
			return changeBarImage();
		}
	}
	
	private String changeLoadingStr() {
		//try sending the section's discription if it exists
		String temp = Loader.getDiscription(loadedState);
		if (!temp.equals("")) return temp;
		
		int diff = loadingStr.length() - "Loading".length();
		
		if (diff < 3) return new String(loadingStr.concat("."));
		else if (diff == 3) return new String("Loading");
		return loadingStr;
	}
	
	public State getLoadedState() { return loadedState; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}