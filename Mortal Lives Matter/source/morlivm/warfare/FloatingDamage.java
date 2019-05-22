package morlivm.warfare;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import morlivm.map.Attachable;
import morlivm.map.orientation.Topology;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.warfare.damage_filter.Element;

public class FloatingDamage implements Attachable
{
	public static enum DamageType {MISS, PLAYER, ENEMY, DIVINE, LUNAR, NATURE};
	
	private final static Font NONE_CRIT_FONT = new Font("Bahnschrift", Font.BOLD, 24);
	private final static Font CRIT_FONT = new Font("Old English Text MT", Font.BOLD, 32);
	private final static Font MISS_FONT = new Font("Poplar Std", Font.PLAIN, 30);
	private final static Color[] NONE_CRIT = {new Color(255, 220, 54), Color.WHITE};
	private final static Color CRIT = Color.BLACK;
	private final static Color BLANK = Color.decode("0x4dd3ff");
	
	private DamageType type;
	private int directX, transparency;
	private Point originP, point;
	private String display;
	private Color color, outline;
	private Font font;
	private boolean crit;
	private Image critPattern, elementSignal;
	private DamageManager damageMngr;
	
	public FloatingDamage(DamageType type, int num, boolean crit,Point point, int directX, DamageManager dm) {
		this.type = type;
		this.originP = new Point(point, 0, 0, Topology.groundY(point.getX()));
		this.point = new Point(point);
		this.directX = directX;
		this.transparency = 255;
		this.crit = crit;
		this.damageMngr = dm;
		this.critPattern = damageMngr.getDamageCritPattern();
		
		if (type == DamageType.MISS) {
			this.color = BLANK;
			this.outline = new Color(53, 53, 53);
			this.font = MISS_FONT;
			this.display = createMissString();
		}
		else {
			this.outline = (crit) ? Color.WHITE : new Color(53, 53, 53);
			this.font = (crit || type == DamageType.PLAYER) ? CRIT_FONT : NONE_CRIT_FONT;
			this.display = new String("-" + num);
			if (type != DamageType.DIVINE && type != DamageType.LUNAR && type != DamageType.NATURE)
				this.color = (crit) ? CRIT : getNoneCritColor(type);
			else {
				this.color = (crit) ? CRIT : getNoneCritColor(DamageType.ENEMY);
				this.elementSignal = damageMngr.getElementSignal(type);
			}
		}
	}

	public void update(double delta) {
		shift();
		adjustColor();
		if (transparency == 0) damageMngr.removeDamage(this);
	}

	public void render(ExtendedGraphics2D g) {
		Point p = new Point(point);
		
		g.setFont(font);
		if (!crit && elementSignal != null) {
			p.setX(getX() + display.length() * font.getSize() / 3 - 5);
			p.setY(getY() - 40);
			g.drawImage(elementSignal, p, null, transparency);
		}
		else if (crit && type != DamageType.MISS) {
			p.setX(getX() - 15);
			p.setY(getY() - 45);
			g.drawImage(critPattern, p, null, transparency);
		}
		
		if (type == DamageType.MISS) g.drawOutlineString(display, new Point(point), 2, color, outline);
		else g.drawOutlineString(display, new Point(point), 1, color, outline);
	}
	
	private void shift() {
		setX(getX() + 1 * directX);
		setY(getY() - 1);
	}
	
	private void adjustColor() {
		color = new Color(color.getRed(), color.getGreen(), color.getBlue(), transparency);
		outline = new Color(outline.getRed(), outline.getGreen(), outline.getBlue(), transparency);
		if (transparency > 0) transparency -= 5;
	}
	
	private Color getNoneCritColor(DamageType type) {
		switch(type) {
			case PLAYER: return NONE_CRIT[0];
			case ENEMY: return NONE_CRIT[1];
			default: return Color.BLACK; // formal return statement;
		}
	}
	
	private String createMissString() {
		String temp1 = "MISS", temp2 = "";
		
		for (int i = 0; i < temp1.length(); i++)
			temp2 = new String(temp2.concat(temp1.charAt(i) + " "));
		return new String(temp2.substring(0, temp1.length() * 2 - 1));
	}
	
	public static DamageType getElementDamageType(Element.Type type) {
		switch(type) {
			case DIVINE: return DamageType.DIVINE;
			case LUNAR: return DamageType.LUNAR;
			case NATURE: return DamageType.NATURE;
			default: return null; //formal return statement
		}
	}
	
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return null; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public void resetY() { point.setY(originP.getY()); }
	public Point getFixedPoint() { return originP; }
}