package morlivm.control_panel;

public class Mode
{
	private String name;
	private boolean on;
	
	public Mode(String name) {
		this.name = new String(name);
	}
	
	public void activate(boolean flag) { on = flag; }
	public boolean isOn() { return on; }
	public String getName() { return name; }
}