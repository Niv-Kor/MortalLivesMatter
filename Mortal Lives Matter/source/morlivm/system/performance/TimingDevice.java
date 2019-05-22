package morlivm.system.performance;
import java.util.LinkedList;

public class TimingDevice
{
	private LinkedList<Timer> tList;
	
	public TimingDevice() {
		tList = new LinkedList<Timer>();
	}
	
	public void setClocks() {
		Timer t;
		
		for (int i = 0; i < tList.size(); i++) {
			t = tList.get(i);
			t.setRoof(t.getSecondsRoof());
		}
	}
	
	public Timer getTimer(String name) {
		Timer t;
		
		for (int i = 0; i < tList.size(); i++) {
			t = tList.get(i);
			if (t.getName().equals(name)) return t;
		}
		return null;
	}
	
	public void addTimer(String name, double roof, boolean enable) {
		if (!exists(name)) tList.add(new Timer(name, roof, enable));
	}
	
	private boolean exists(String name) { return getTimer(name) != null; }
	public void addTimer(Timer t) { if (!exists(t.getName())) tList.add(t); }
	public void addTimer(String name, double roof) { if (!exists(name)) tList.add(new Timer(name, roof)); }
	public void removeTimer(String name) { tList.remove(getTimer(name)); }
}