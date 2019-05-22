package morlivm.system.sound;
import morlivm.system.performance.FPS;
import morlivm.system.performance.Timer;
import morlivm.system.performance.TimingDevice;

public class Tune
{
	public final static Tune MUTE = new Tune("mute", "/sound/silece.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, true);
	private final static int LIMIT = FPS.toFrames(0.35);
	
	private Sound.Genre genre;
	private Sound.Clique clique;
	private String path, name;
	private boolean loopable, midterm;
	private TimingDevice timingDevice;
	
	public Tune(String name, String path, Sound.Clique clique, Sound.Genre genre, boolean loops) {
		this.name = new String(name);
		this.path = new String(path);
		this.clique = clique;
		this.genre = genre;
		this.loopable = (genre == Sound.Genre.BGM) ? true : loops;
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("duration", Timer.INFINITE);
	}
	
	public Tune(Tune other) {
		this.name = new String(other.name);
		this.path = new String(other.path);
		this.clique = other.clique;
		this.genre = other.genre;
		this.loopable = other.loopable;
		this.timingDevice = other.timingDevice;
	}
	
	public void time() {
		timingDevice.setClocks();
		Timer duration = timingDevice.getTimer("duration");
		
		duration.play();
		if (duration.reach(LIMIT)) midterm = true;
	}
	
	public void activate() {
		timingDevice.getTimer("duration").init();
		midterm = false;
	}
	
	public boolean isUseable() { return timingDevice.getTimer("duration").getPointer() == 0 || midterm; } 
	public String getName() { return name; }
	public String getPath() { return path; }
	public Sound.Clique getClique() { return clique; }
	public Sound.Genre getGenre() { return genre; }
	public boolean isLoopable() { return loopable; }
}