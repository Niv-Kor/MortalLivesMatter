package morlivm.warfare;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import morlivm.map.AttachManager;
import morlivm.map.Attachable;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.RNG;
import morlivm.warfare.FloatingDamage.DamageType;

public class DamageManager implements GraphicsManager, AttachManager
{
	private List<FloatingDamage> dList;
	private BufferedImage[] critPatterns, elementSignal;
	
	public DamageManager() {
		this.dList = new ArrayList<FloatingDamage>();
		
		this.critPatterns = new BufferedImage[3];
		critPatterns[0] = ImageHandler.load("/sheets/general/critical_damage/Claw1.png");
		critPatterns[1] = ImageHandler.load("/sheets/general/critical_damage/Claw2.png");
		critPatterns[2] = ImageHandler.load("/sheets/general/critical_damage/Claw3.png");
		
		this.elementSignal = new BufferedImage[3];
		elementSignal[0] = ImageHandler.load("/sheets/general/element_damage_signal/Divine.png");
		elementSignal[1] = ImageHandler.load("/sheets/general/element_damage_signal/Lunar.png");
		elementSignal[2] = ImageHandler.load("/sheets/general/element_damage_signal/Nature.png");
	}
	
	public void update(double delta) {
		for (int i = 0; i < dList.size(); i++)
			dList.get(i).update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		for (int i = 0; i < dList.size(); i++)
			dList.get(i).render(g);
	}
	
	public void addDamage(FloatingDamage.DamageType type, int num, boolean crit, Point point, int directX) {
		point.setY(point.getY() - dList.size() * 20);
		dList.add(new FloatingDamage(type, num, crit, point, directX, this));
	}
	
	public Image getDamageCritPattern() {
		return ImageHandler.copy(critPatterns[RNG.generate(0, critPatterns.length - 1)]);
	}
	
	public Image getElementSignal(FloatingDamage.DamageType type) {
		if (type != DamageType.DIVINE && type != DamageType.LUNAR && type != DamageType.NATURE) return null;
		return getSignalCopy(type);
	}
	
	private BufferedImage getSignalCopy(FloatingDamage.DamageType type) {
		switch(type) {
		case DIVINE: return ImageHandler.copy(elementSignal[0]);
		case LUNAR: return ImageHandler.copy(elementSignal[1]);
		case NATURE: return ImageHandler.copy(elementSignal[2]);
		default: return null;
		}
	}
	
	public List<Attachable> getMagnetizedComponents() { return new ArrayList<Attachable>(dList); }
	public void removeDamage(FloatingDamage floatingDamage) { dList.remove(floatingDamage); }
	public List<? extends Graphable> getList() { return dList; }
}