package morlivm.warfare;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import morlivm.content.mortal.AeroEnemy;
import morlivm.content.mortal.EarthEnemy;
import morlivm.content.mortal.Mortal;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;

/** CURRENTLY NOT SHOWING NAME TAGS AT ALL **/
public class NameTag implements Graphable
{
	private final static Font FONT = new Font("Arial Rounded MT Bold", Font.ITALIC, 16);
	
	private Mortal mortal;
	private String name;
	private Point point;
	private boolean show;
	
	public NameTag(Mortal mortal, String name) {
		this.mortal = mortal;
		this.point = new Point();
		this.name = new String(name);
		show(false);
	}

	public void update(double delta) {
		updateCoordinates();
	}

	public void render(ExtendedGraphics2D g) {
		if (!show) return;
		
		g.setColor(new Color(0, 0, 0, 110));
		g.fillRect((int) getX() - FONT.getSize() / 2, (int) getY(),
				   (int) (name.length() * FONT.getSize() / 1.5),
				   (int) (FONT.getSize() * 1.2));
		
		g.fillRect((int) getX() - FONT.getSize() / 2, (int) getY(),
				   (int) (name.length() * FONT.getSize() / 1.5),
				   (int) (FONT.getSize() * 1.2));
		
		g.setFont(FONT);
		g.setColor(Color.WHITE);
		g.drawString(name, new Point(point, 0, FONT.getSize() - 2, 0));
	}
	
	private void updateCoordinates() {
		setX(mortal.getX() + mortal.getDimension().width / 2 - (name.length() * FONT.getSize() / 4));
		if (mortal instanceof EarthEnemy) setY(mortal.getZ() + 10);
		else if (mortal instanceof AeroEnemy) setY(mortal.getMass().getC().getY() + 10);
	}
	
	public void show(boolean flag) { show = false; } //TEMP - currently not showing name tags at all
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return new Dimension(FONT.getSize(), FONT.getSize()); }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}