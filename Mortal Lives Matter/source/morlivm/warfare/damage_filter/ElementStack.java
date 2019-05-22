package morlivm.warfare.damage_filter;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import morlivm.content.mortal.Boss;
import morlivm.content.mortal.Enemy;
import morlivm.content.mortal.AeroEnemy;
import morlivm.content.mortal.EarthEnemy;
import morlivm.content.mortal.Mortal;
import morlivm.content.mortal.Player;
import morlivm.main.Game;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.math.RNG;

public class ElementStack implements GraphicsManager
{
	private final static int MIN = 1, MAX = 3;
	private final static Point INIT_CNTRL_PNL = new Point(413, Game.HEIGHT - 64);
	private final static Point INIT_BOSS_PNL = new Point(Game.WIDTH / 2 + 89, 118);
	
	private Stack<Element> elements;
	private Mortal mortal;
	private boolean show;
	
	public ElementStack(Mortal mortal, Element[] elements) {
		this.mortal = mortal;
		
		this.elements = new Stack<Element>();
		for (int i = 0; i < elements.length && i < 3; i++)
			this.elements.add(new Element(elements[i]));
			
		if (mortal instanceof Enemy) show(false);
		
		for (int i = 0; i < getAmount(); i++)
			this.elements.get(i).activate(new Point());
		
		updateCoordinates();
	}
	
	public void update(double delta) {
		for (Element e : elements) e.update(delta);
		updateCoordinates();
	}

	public void render(ExtendedGraphics2D g) {
		if (!show && mortal instanceof Enemy) return;
		for (Element e : elements) e.render(g);
	}
	
	private void updateCoordinates() {
		double multiplier;
		Element temp;
		
		if (mortal instanceof Enemy && !(mortal instanceof Boss)) {
			switch(getAmount()) {
				case 1: multiplier = 0.6; break;
				case 2: multiplier = 1.45; break;
				case 3: multiplier = 2.1; break;
				default: multiplier = 1;
			}
			
			for (int i = 0; i < getAmount(); i++) {
				temp = elements.get(i);
				
				temp.setX(mortal.getX() + mortal.getDimension().width / 2 - temp.getDimension().width
						  * multiplier + (temp.getDimension().width + 10) * i);
				
				if (mortal instanceof EarthEnemy) temp.setY(mortal.getZ() + 10);
				else if (mortal instanceof AeroEnemy) temp.setY(mortal.getMass().getC().getY() + 40);
			}
		}
		else if (mortal instanceof Player) {
			for (int i = 0; i < getAmount(); i++) {
				temp = elements.get(i);
				temp.setX(INIT_CNTRL_PNL.getX() + (temp.getDimension().width + 21) * i);
				temp.setY(INIT_CNTRL_PNL.getY());
			}
		}
		else if (mortal instanceof Boss) {
			elements.get(0).setX(INIT_BOSS_PNL.getX());
			elements.get(0).setY(INIT_BOSS_PNL.getY());
			
			if (getAmount() >= 2) {
				elements.get(1).setX(INIT_BOSS_PNL.getX() + 49);
				elements.get(1).setY(INIT_BOSS_PNL.getY() - 21);
			}
			if (getAmount() == 3) {
				elements.get(2).setX(INIT_BOSS_PNL.getX() + 69);
				elements.get(2).setY(INIT_BOSS_PNL.getY() - 68);
			}
		}
	}
	
	public void add(Element e) {
		if (getAmount() < MAX) elements.push(e);
	}
	
	public void addRandom() {
		List<Element.Type> list = Arrays.asList(Element.Type.values());
		add(new Element((Element.Type) RNG.select(list)));
	}
	
	public void remove() {
		if (getAmount() > MIN) elements.pop();
	}
	
	public double fight(ElementStack defensive) {
		double filter = 1;
		
		for(int i = 0; i < getAmount(); i++)
			for(int j = 0; j < defensive.getAmount(); j++)
					filter *= Element.fight(elements.get(i), defensive.getElement(j + 1));

		return filter;
	}
	
	public Element getElement(int index) {
		return index >= 1 && index <= 3 && elements.size() >= index ? elements.get(index - 1) : null;
	}
	
	public void show(boolean flag) { show = flag; }
	public int getAmount() { return elements.size(); }
	public List<Graphable> getList() { return null; }
}