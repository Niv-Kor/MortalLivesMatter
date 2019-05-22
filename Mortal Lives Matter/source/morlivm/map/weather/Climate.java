package morlivm.map.weather;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Climate implements GraphicsManager
{
	public static enum Type {
		RAIN(RainDrop.class, true),
		SNOW(Snowflake.class, false);
		
		public Class<?> dropType;
		public MusicBox musicBox;
		
		private Type(Class<?> prec, boolean makeSound) {
			this.dropType = prec;
			this.musicBox = new MusicBox();
			
			if (makeSound) {
				musicBox.put(new Tune(name(), WeatherManager.SOUND_DIRECTORY + name() + ".wav",
									  Sound.Clique.GENERAL, Sound.Genre.SFX, true));
				musicBox.export();
			}
		}
		
		public void loopSound() { if (musicBox.size() > 0) musicBox.loop(name()); }
	}
	
	protected List<Precipitate> drops;
	protected TimingDevice timingDevice;
	protected MusicBox musicBox;
	protected Wind wind;
	protected Type type;
	protected int diameter;
	
	public Climate(Type type, Wind wind) {
		this.type = type;
		this.wind = wind;
		this.diameter = 30;
		this.musicBox = new MusicBox();
		this.timingDevice = new TimingDevice();
		this.drops = new ArrayList<Precipitate>();
		timingDevice.addTimer("drop", wind.getTiming());
		type.loopSound();
	}
	
	public void update(double delta) {
		Timer drop = timingDevice.getTimer("drop");
		Precipitate p = null;
		
		
		if (getAmount() < Precipitate.MAX && drop.progressedToRoof()) {
			for (int i = 0; i < wind.getUnits(); i++) {
				try {
					p = type.dropType.asSubclass(Precipitate.class).
						getConstructor(Climate.class, Wind.class).
						newInstance(this, wind);
				}
				catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					
					e.printStackTrace();
				}
				
				if (p != null) drops.add(p);
			}
			
			drop.init();
		}
		
		for (int i = 0; i < drops.size(); i++)
			drops.get(i).update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		for (int i = 0; i < drops.size(); i++)
			drops.get(i).render(g);
	}
	
	public int getAmount() { return drops.size(); }
	public void remove(Precipitate p) { drops.remove(p); }
	public Type getType() { return type; }
	public List<? extends Graphable> getList() { return drops; }
}