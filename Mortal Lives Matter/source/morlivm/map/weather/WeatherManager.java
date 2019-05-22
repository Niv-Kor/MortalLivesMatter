package morlivm.map.weather;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import morlivm.database.LevelData;
import morlivm.main.Structure;
import morlivm.main.testing.RecordPrinter;
import morlivm.main.testing.Tester;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Percent;
import morlivm.system.math.RNG;
import morlivm.system.math.RangedInt;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class WeatherManager implements GraphicsManager
{
	private static enum LightMask implements Graphable {
		SUNRISE(1),
		AM(16),
		PM(1),
		STARS(1),
		RAINBOW(1);
		
		private final static String DIRECTORY = "/Maps/light masks/";
		
		private int imagePtr;
		private boolean shows, animated, reverse;
		private Point point;
		private TimingDevice timingDevice;
		private BufferedImage[] imageStack;
		private BufferedImage image;
		
		private LightMask(int frames) {
			if (frames > 1) {
				this.animated = true;
				this.imageStack = new BufferedImage[frames];
				for (int i = 0; i < frames; i++)
					imageStack[i] = ImageHandler.load(DIRECTORY + name().toLowerCase() + (i + 1) + ".png");
			}
			else {
				this.imageStack = new BufferedImage[1];
				imageStack[0] = ImageHandler.load(DIRECTORY + name().toLowerCase() + ".png");
			}
			
			this.point = new Point();
			this.imagePtr = 0;
			this.image = imageStack[imagePtr];
			this.timingDevice = new TimingDevice();
			timingDevice.addTimer("render", 0.05);
			timingDevice.addTimer("standby", 0.7);
		}
		
		public void update(double delta) {
			Timer render = timingDevice.getTimer("render");
			Timer standby = timingDevice.getTimer("standby");
			image = imageStack[imagePtr];
			
			if (animated && standby.progressedToRoof()) {
				if (render.progressedToRoof()) {
					if (!reverse && ++imagePtr == imageStack.length) {
						imagePtr--;
						reverse = true;
						standby.init();
					}
					else if (reverse && --imagePtr < 0) {
						imagePtr++;
						reverse = false;
						standby.init();
					}
					
					render.init();
				}
			}
		}

		public void render(ExtendedGraphics2D g) {}
		
		public void maskOver(ExtendedGraphics2D g, int delta) {
			if (maskTrans() == 0) return;
			g.drawImage(image, point, null, maskTrans() + delta);
		}
		
		private int maskTrans() {
			int hour = LocalTime.now().getHour();
			
			switch (name()) {
				case "AM": {
					if (hour > WeatherManager.SUNRISE && hour <= WeatherManager.SUNSET) {
						shows = true;
						return transPerc((int) getDaytimePercent());
					}
					else {
						shows = false;
						return transPerc(0);
					}
				}
				
				case "PM":				
				case "STARS": {
					if (hour > WeatherManager.NOON || hour <= WeatherManager.SUNRISE + 2) {
						shows = true;
						return transPerc((int) (100 - getDaytimePercent()));
					}
					else {
						shows = false;
						return transPerc(0);
					}
				}
				
				case "SUNRISE": {
					if (hour == WeatherManager.SUNRISE) {
						shows = true;
						return transPerc(100); 
					}
					else {
						shows = false;
						return transPerc(0);
					}
				}
				
				case "RAINBOW": {
					return shows ? transPerc((int) getDaytimePercent()) : transPerc(0);
				}
				
				default: return transPerc(0);
			}
		}
		
		private int transPerc(int perc) {
			if (perc == 100) return 0xFF;
			else if (perc == 0) return 0;
			return (int) (Percent.percentOfNum(perc, 0xFF));
		}
		
		public boolean isShowing() {
			maskTrans(); //to init "shows" if necessarry
			return shows;
		}
		
		public void setDimension(Dimension d) {}
		public Dimension getDimension() { return new Dimension(image.getWidth(), image.getHeight()); }
		public void show(boolean flag) { shows = flag; }
		public Point getPoint() { return new Point(); }
		public void setX(double x) { point.setX(x); }
		public void setY(double y) { point.setY(y); }
		public double getX() { return point.getX(); }
		public double getY() { return point.getY(); }
		public BufferedImage getImage() { return image; }
	}
	
	public static enum ParallexLight { FRONT, MID, BACK; }
	
	public final static String SOUND_DIRECTORY = "/sound/weather/";
	private final static int SUNRISE = 5, NOON = 15, SUNSET = 19, MIDNIGHT = 24;
	private final static int SNOW_DEG = -5;
	private final static int DEG_DEVIATION = 3;
	
	private static List<LightMask> lightMasks = Arrays.asList(LightMask.values());
	private List<Climate> climates;
	private Wind wind;
	private int degrees, precipitationsChance;
	private double humidity;
	private MusicBox musicBox;
	private LocalDateTime time;
	
	public WeatherManager() {
		this.time = LocalDateTime.now();
		this.climates = new ArrayList<Climate>();
		this.musicBox = new MusicBox();
		
		if (!Tester.controlWeather) {
			initDegrees();
			this.wind = new Wind(degrees, getDaytimePercent());
			tryPrecipitations();
		}
		else {
			this.wind = new Wind(Tester.wind);
			if (Tester.snow) climates.add(new Climate(Climate.Type.SNOW, wind));
			else if (Tester.rain) climates.add(new Climate(Climate.Type.RAIN, wind));
			if (Tester.rainbow) LightMask.RAINBOW.show(true);
			
			System.err.println("\n! UNDER WERATHER CONTROL !");
		}
		
		//wind sound
		String windSoundName = null;
		if (Math.abs(wind.getVector()) >= 0.85) windSoundName = "heavy_wind";
		else if (Math.abs(wind.getVector()) >= 0.45) windSoundName = "calm_wind";
		
		if (windSoundName != null) {
			musicBox.put(new Tune(windSoundName, SOUND_DIRECTORY + windSoundName + ".wav",
					  			  Sound.Clique.GENERAL, Sound.Genre.SFX, true));
			
			musicBox.export();
			musicBox.loop(windSoundName);
		}
		
		weatherReport();
	}
	
	private void initDegrees() {
		LevelData ldb = Structure.getDatabase();
		RangedInt rangedDeg = ldb.degrees;
		
		//change the natural humidity randomly
		double randomMultiplier = RNG.generateDouble(-1.1, 1.1);
		
		this.humidity = Percent.limit(getDaytimePercent() + ldb.naturalHumidity * randomMultiplier);
		this.degrees = rangedDeg.percent(getDaytimePercent() + DEG_DEVIATION * randomMultiplier);
	}
	
	private void tryPrecipitations() {
		//climates.add(new Sunshine(//percent humidity));
		precipitationsChance = (int) (Math.round(100 - humidity) / 1.5);
		
		if (RNG.unstableCondition(precipitationsChance)) {
			if (degrees <= SNOW_DEG) climates.add(new Climate(Climate.Type.SNOW, wind));
			else climates.add(new Climate(Climate.Type.RAIN, wind));
		}
		
		//show rainbow if sun + rain
		if (LightMask.AM.isShowing() && hasPrecipitation(Climate.Type.RAIN)) LightMask.RAINBOW.show(true);
		else LightMask.RAINBOW.show(false);
	}

	public void update(double delta) {
		for (Climate c : climates) c.update(delta);
		for (LightMask mask : lightMasks) mask.update(delta);
		
		int hour = time.getHour();
		time = LocalDateTime.now();
		
		//refresh
		if (hour != time.getHour()) {
			initDegrees();
			tryPrecipitations();
			weatherReport();
		}
	}

	public void render(ExtendedGraphics2D g) {
		for (Climate c : climates) c.render(g);
	}
	
	public static void maskOver(ExtendedGraphics2D g, int delta, ParallexLight pos) {
		switch(pos) {
			case FRONT: {
				LightMask.PM.maskOver(g, delta);
				LightMask.AM.maskOver(g, delta);
				break;
			}
			case MID: {
				LightMask.PM.maskOver(g, delta);
				LightMask.SUNRISE.maskOver(g, delta);
				LightMask.RAINBOW.maskOver(g, delta);
				break;
			}
			case BACK: {
				LightMask.STARS.maskOver(g, delta);
				break;
			}
		}
	}
	
	public boolean hasPrecipitation(Climate.Type prec) {
		for (Climate c : climates) if (c.getType() == prec) return true;
		return false;
	}
	
	public int getAmount(Climate.Type prec) {
		for (Climate c : climates) if (c.getType() == prec) return c.getAmount();
		return 0;
	}
	
	public static BufferedImage getDaylightMask() {
		int hour = LocalTime.now().getHour();
		
		if (hour == SUNRISE) return LightMask.SUNRISE.getImage();
		else if (hour > SUNRISE && hour <= NOON) return LightMask.AM.getImage();
		else return LightMask.PM.getImage();
	}
	
	public static double getDaytimePercent() {
		int hour = LocalTime.now().getHour();
		
		if (hour >= 0 && hour < SUNRISE) return 0; //complete darkness
		else if (hour <= NOON) return Percent.numOfNum(hour, NOON); //am
		else return 100 - Percent.numOfNum(hour - NOON, MIDNIGHT - NOON); //pm
	}
	
	public void weatherReport() {
		String hrStr = (time.getHour() < 10) ? "0" + time.getHour() : "" + time.getHour();
		String mnStr = (time.getMinute() < 10) ? "0" + time.getMinute() : "" + time.getMinute();
		double dayIntense = Percent.numOfNum(LightMask.AM.maskTrans(), 0xFF);
		double nightIntense = 100 - getDaytimePercent();
		
		RecordPrinter.headline("Weather Report");
		RecordPrinter.write("time\t\t\t" + hrStr + ":" + mnStr);
		RecordPrinter.write("degrees\t\t\t" + degrees + "°c");
		RecordPrinter.write("humidity\t\t" + new DecimalFormat("#0.0").format(humidity) + "%");
		RecordPrinter.write("wind\t\t\t" + new DecimalFormat("#0.000").format(wind.getVector()));
		RecordPrinter.write("precipitation chance\t" + precipitationsChance + "%");
		RecordPrinter.write("sun intensity\t\t" + (int) (dayIntense - nightIntense) + "%");
		RecordPrinter.separator();
		RecordPrinter.write("*rain\t\t\t" + hasPrecipitation(Climate.Type.RAIN));
		RecordPrinter.write("*snow\t\t\t" + hasPrecipitation(Climate.Type.SNOW));
		RecordPrinter.write("*blizzard\t\t" + (hasPrecipitation(Climate.Type.RAIN) && hasPrecipitation(Climate.Type.SNOW)));
		RecordPrinter.write("*rainbow\t\t" + LightMask.RAINBOW.shows);
		System.out.println(RecordPrinter.print());
	}
	
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}