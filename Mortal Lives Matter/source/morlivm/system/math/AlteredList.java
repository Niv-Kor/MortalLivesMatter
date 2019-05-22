package morlivm.system.math;
import java.util.Iterator;
import java.util.LinkedList;

import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;

@SuppressWarnings("serial")
public class AlteredList<T> extends LinkedList<T>
{
	public AlteredList() {
		super();
	}
	
	public void update(double delta) {
		Iterator<T> iterator = iterator();
		
		try {
			while (iterator.hasNext()) ((Graphable) iterator.next()).update(delta);
		}
		catch (ClassCastException e) { popError(); }
	}
	
	public void render(ExtendedGraphics2D g) {
		Iterator<T> iterator = iterator();
		
		try {
			while (iterator.hasNext()) ((Graphable) iterator.next()).render(g);
		}
		catch (ClassCastException e) { popError(); }
	}
	
	private void popError() { System.err.println("AlteredList's generic type should be of Graphable interface."); }
}