package morlivm.warfare.aftershock;
import java.util.ArrayList;
import java.util.List;

import morlivm.map.AttachManager;
import morlivm.map.Attachable;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.warfare.aftershock.Aftershock.Type;

public class AftershockManager implements GraphicsManager, AttachManager
{
	private List<Aftershock> aList;
	private GameState gameState;
	
	public AftershockManager(GameState gs) {
		this.aList = new ArrayList<Aftershock>();
		this.gameState = gs;
	}
	
	public void update(double delta) {
		for (int i = 0; i < aList.size(); i++)
			aList.get(i).update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		for (int i = 0; i < aList.size(); i++)
			aList.get(i).render(g);
	}
	
	public void addShock(Aftershock a) { aList.add(a); }
	
	public void addShock(Aftershock.Type type, Point point) {
		if (type == Type.NONE) return;
		else addShock(type.createInstance(point, gameState));
	}
	
	public List<Attachable> getMagnetizedComponents() {
		return new ArrayList<Attachable>(aList);
	}
	
	public void removeShock(Aftershock aftershock) { aList.remove(aftershock); }
	public List<? extends Graphable> getList() { return aList; }
}