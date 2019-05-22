package morlivm.system.performance;

public class Timer
{
	public final static int INFINITE = -1;
	
	private int pointer, frameRoof;
	private double secondsRoof;
	private boolean enabled;
	private String name; 
	
	public Timer(String name, double roof) {
		this.name = new String(name);
		this.secondsRoof = roof;
		this.frameRoof = (roof != INFINITE) ? FPS.toFrames(roof) : -1;
		this.pointer = 0;
		this.enabled = true;
	}
	
	public Timer(String name, double roof, boolean enable) {
		this(name, roof);
		this.enabled = enable;
	}
	
	public void renew(double roof) {
		this.secondsRoof = roof;
		this.frameRoof = (roof != INFINITE) ? FPS.toFrames(roof) : -1;
		this.pointer = 0;
	}
	
	public void setRoof(double r) {
		frameRoof = FPS.toUnstableFrames(r);
		secondsRoof = r;
		if (r == 0) enable(false);
	}
	
	public boolean progressedToRoof() {
		if (frameRoof == INFINITE) return false;
		else {
			if (enabled) pointer++;
			return pointer >= frameRoof;
		}
	}
	
	public void enable(boolean flag) {
		renew(secondsRoof);
		enabled = flag;
	}
	
	public void play() { if (pointer < frameRoof || frameRoof == INFINITE) pointer++; }
	public void init() { pointer = 0; }
	public double getSecondsRoof() { return secondsRoof; }
	public int getPointer() { return pointer; }
	public int getSummedRoof() { return frameRoof; }
	public String getName() { return new String(name); }
	public boolean reach(int milestone) { return pointer == milestone; }
	public boolean reachRoof() { return (frameRoof == INFINITE) ? false : pointer >= frameRoof; }
	public boolean isEnabled() { return enabled; }
}