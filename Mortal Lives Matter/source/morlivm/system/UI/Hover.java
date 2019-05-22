package morlivm.system.UI;
import morlivm.system.graphics.Collider;

public class Hover
{
	private String name;
	private Collider target;
	private int ptrcursor, clickerCursor;
	
	public Hover(String name, Collider target, int ptr, int clicker) {
		this.name = new String(name);
		this.target = new Collider(target);
		this.ptrcursor = ptr;
		this.clickerCursor = clicker;
	}
	
	public boolean equals(Hover other) { return name.equals(other.name); }
	public String getName() { return name; }
	public Collider getTarget() { return target; }
	public int getDefCursor() { return ptrcursor; }
	public int getPtrCursor() { return clickerCursor; }
}