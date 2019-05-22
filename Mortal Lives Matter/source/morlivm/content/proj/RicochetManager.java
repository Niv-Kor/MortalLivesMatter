package morlivm.content.proj;
import java.util.ArrayList;
import java.util.List;

import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;

public class RicochetManager implements GraphicsManager
{
	private List<Graphable> aimList, slideList;
	
	public RicochetManager() {
		aimList = new ArrayList<Graphable>();
		slideList = new ArrayList<Graphable>();
	}
	
	public void update(double delta) {
		for (int i = 0; i < slideList.size(); i++) slideList.get(i).update(delta);
		for (int i = 0; i < aimList.size(); i++) aimList.get(i).update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		for (int i = 0; i < slideList.size(); i++) slideList.get(i).render(g);
		for (int i = 0; i < aimList.size(); i++) aimList.get(i).render(g);
	}
	
	public void add(Projectile p) {
		if (p.getDatabase().subType == Projectile.SubType.AIM) aimList.add(p);
		else if (p.getDatabase().subType == Projectile.SubType.STRAIGHT) slideList.add(p);
	}
	
	public void remove(Projectile p) {
		aimList.remove(p);
		slideList.remove(p);
	}
	
	public int getAmount() { return aimList.size() + slideList.size(); }
	
	public List<Graphable> getList() {
		List<Graphable> allList = new ArrayList<Graphable>();
		
		for (Graphable g : aimList) allList.add(g);
		for (Graphable g : slideList) allList.add(g);
		
		return allList;
	}
}