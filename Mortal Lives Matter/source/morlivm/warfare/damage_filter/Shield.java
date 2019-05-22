package morlivm.warfare.damage_filter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.util.Arrays;
import java.util.List;

import morlivm.content.Entity;
import morlivm.content.mortal.Mortal;
import morlivm.database.MortalData;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Physics;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.warfare.gauge.DynamicHealthBar;
import morlivm.warfare.gauge.HealthBar;

public class Shield implements Graphable
{
	private final static String PATH = "/sheets/general/shield/";
	
	public static enum Size {
		SMALL(Entity.Size.SMALL),
		MEDIUM(Entity.Size.MEDIUM),
		LARGE(Entity.Size.LARGE),
		XLARGE(Entity.Size.XLARGE),
		HUGE(Entity.Size.HUGE);
		
		private Entity.Size parallelSize;
		public Dimension dimension;
		public String imgPath;
		
		private Size(Entity.Size size) {
			this.parallelSize = size;
			this.dimension = new Dimension((int) (size.dimension.width * 1.42), size.dimension.height);
			this.imgPath = PATH + name() + ".png";
		}
		
		public Entity.Size getEntitySize() { return parallelSize; }
	}
	
	private final static int FULL_TRANS = 180;
	private final static Color COLOR = new Color(16, 226, 226);
	
	private Mortal mortal;
	private int fadeRate, directX, deltaX, transparency;
	private Point point;
	private Dimension dim;
	private boolean show;
	private Image dome;
	private DynamicHealthBar extraHP;
	private TimingDevice timingDevice;
	private MusicBox musicBox;
	
	public Shield(Mortal mortal, int mass) {
		this.mortal = mortal;
		Size size = getSize(((MortalData) mortal.getDatabase()).size);
		this.dim = size.dimension;
		this.deltaX = (dim.width - Math.abs(mortal.getDimension().width)) / 2;
		this.point = new Point(mortal.getX() - deltaX * directX, mortal.getY());
		this.dome = ImageHandler.load(size.imgPath);
		this.extraHP = new DynamicHealthBar(mortal, mass, false, false, COLOR);
		this.timingDevice = new TimingDevice();
		this.musicBox = new MusicBox();
		this.fadeRate = 10;
		
		musicBox.put(new Tune("guard", "/Sound/Main/SFX/ShieldGuard.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.put(new Tune("break", "/Sound/Main/SFX/ShieldBreak.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.export();
	}
	
	public void update(double delta) {
		if (!show && extraHP.over()) return;
		Timer glance = timingDevice.getTimer("glance");
		
		updateCoordinates();
		extraHP.update(delta);
		
		if (glance != null) {
			if (glance.progressedToRoof()) {
				if (transparency > 0) {
					transparency -= fadeRate;
					glance.init();
				}
				else {
					show = false;
					timingDevice.removeTimer("glance");
				}
			}
		}
	}

	public void render(ExtendedGraphics2D g) {
		extraHP.render(g);
		if (show) g.drawImage(dome, point, dim, transparency);
	}
	
	public void updateCoordinates() {
		Physics.Vector eDirectX = mortal.getDirectX();
		double entX;
		if (eDirectX == Physics.Vector.RIGHT) entX = mortal.getX();
		else entX = mortal.getX() - Math.abs(mortal.getDimension().width);
		
		setX(entX - deltaX);
		setY(mortal.getY());
	}
	
	public boolean crackShield(double percentage) {
		if (extraHP.over()) return true;
		
		show = true;
		extraHP.decrease(percentage);
		transparency = FULL_TRANS;
		timingDevice.addTimer("glance", 0.05);
		
		if (extraHP.over()) {
			fadeRate = 25;
			transparency = 0xFF;
			musicBox.play("break");
			return true;
		}
		else {
			musicBox.play("guard");
			return false;
		}
	}
	
	private Size getSize(Entity.Size s) {
		List<Size> list = Arrays.asList(Size.values());
		
		for (int i = 0; i < list.size(); i++)
			if (s == list.get(i).getEntitySize()) return list.get(i);
		
		return null;
	}
	
	public boolean isBroken() { return extraHP.over(); }
	public HealthBar getExtraHP() { return extraHP; }
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}