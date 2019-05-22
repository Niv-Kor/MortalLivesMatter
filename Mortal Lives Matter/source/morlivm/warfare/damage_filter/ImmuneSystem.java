package morlivm.warfare.damage_filter;
import java.util.ArrayList;
import java.util.List;

import morlivm.content.mortal.Mortal;
import morlivm.state.GameState;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.math.RNG;
import morlivm.warfare.DoT;

public class ImmuneSystem implements GraphicsManager
{
	private final static double INFECTION_MULTIPLIER = 0.8;
	
	private Mortal mortal;
	private DoT.Type[] affection;
	private DoT condition;
	private GameState gameState;
	
	public ImmuneSystem(Mortal mortal, DoT.Type[] affection, GameState gs) {
		this.mortal = mortal;
		this.affection = affection;
		this.gameState = gs;
	}
	
	public void update(double delta) {
		if (isInfected()) condition.update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		if (isInfected()) condition.render(g);
	}
	
	public boolean isImmune(DoT.Type type) {
		for (DoT.Type dot : affection) if (dot == type) return true;
		return false;
	}
	
	public void infect(DoT.Type type, double str) {
		if (type == null || isInfected(type) || isImmune(type)) return;
		
		if (RNG.unstableCondition(type.contagion))
			condition = new DoT(mortal, type, str, gameState);
	}
	
	public void infectOther(Mortal other) {
		if (!isInfected()) return;
		
		if (gameState.getContactManager().hit(mortal, other, other.getMass(), 0, false, null, null))
			other.getImmuneSystem().infect(condition.getType(), condition.getStrength() * INFECTION_MULTIPLIER);
	}
	
	public DoT.Type passRandomInfection() {
		if (affection.length == 0) return null;
		else return affection[RNG.generate(0, affection.length - 1)];
	}
	
	public void heal() { condition = null; }
	public DoT getCondition() { return condition; }
	public boolean isInfected() { return condition != null && mortal.isAlive(); }
	public boolean isInfected(DoT.Type type) { return condition != null && condition.getType() == type; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}