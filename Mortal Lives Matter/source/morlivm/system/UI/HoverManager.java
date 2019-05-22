package morlivm.system.UI;
import java.util.ArrayList;
import java.util.List;

import morlivm.system.graphics.Collider;
import morlivm.user_input.MouseInput;

public class HoverManager
{
	public List<Hover> hList;
	private MouseInput mouseInput;
	
	public HoverManager(MouseInput mouseInput) {
		this.mouseInput = mouseInput;
		this.hList = new ArrayList<Hover>();
	}
	
	public boolean hover(Hover hover) {
		Collider mouse = mouseInput.getMouseCollider(3);
		
		if (mouse.touch(hover.getTarget())) {
			if (hover.getDefCursor() != -1) {
				if (!mouseInput.press()) Cursor.setCursor(hover.getDefCursor());
				else Cursor.setCursor(hover.getPtrCursor());
			}
			if (!hoverExists(hover)) add(hover);

			return true;
		}
		else {
			dismiss(hover);
			return false;
		}
	}
	
	public boolean hoverExists(Hover hover) {
		Hover temp;
		if (!isEmpty()) {
			for (int i = 0; i < hList.size(); i++) {
				temp = hList.get(i);
				if (temp.equals(hover)) return true;
			}
		}
		return false;
	}
	
	public boolean hoverExists(String name) {
		Hover temp;
		if (!isEmpty()) {
			for (int i = 0; i < hList.size(); i++) {
				temp = hList.get(i);
				if (temp.getName().equals(name)) return true;
			}
		}
		return false;
	}
	
	public void attemptRelease() {
		if (!isEmpty())
			for (int i = 0; i < hList.size(); i++) hover(hList.get(i));
	}
	
	public void hideTargets(boolean flag) {
		if (!isEmpty())
			for (int i = 0; i < hList.size(); i++) hList.get(i).getTarget().hide(flag);
	}
	
	public void clear() {
		if (!isEmpty())
			for (int i = 0; i < hList.size(); i++) dismiss(hList.get(i));
	}
	
	public void print() {
		for (int i = 0; i < hList.size(); i++){
			System.out.println("hover #" + i + ": " + hList.get(i).getName());
		}
	}
	
	private void add(Hover hover) { hList.add(hover); }
	private void dismiss(Hover hover) { hList.remove(hover); }
	public boolean isEmpty() { return hList.size() == 0; }
}