package morlivm.warfare.gauge;
import java.awt.Color;

import morlivm.content.mortal.Mortal;
import morlivm.system.graphics.ExtendedGraphics2D;

public class DynamicHealthBar extends HealthBar
{
	public final static int FRAME_WIDTH = 66;
	
	private boolean showOutline;
	
	public DynamicHealthBar(Mortal mortal, int health, boolean killWhenOver, boolean showOutline, Color color) {
		super(mortal, health, killWhenOver);
		
		this.barWidth = FRAME_WIDTH;
		this.frameWidth = FRAME_WIDTH;
		this.showOutline = showOutline;
		this.color = color;
		adjustDirection();
		show(false);
	}
	
	public void update(double delta) {
		super.update(delta);
		adjustDirection();
	}
	
	public void render(ExtendedGraphics2D g) {
		if (over() || !show) return;
		
		if (showOutline) {
			g.setColor(new Color(0, 0, 0, 120));
			g.fillRoundRect((int) getX(), (int) getY(), FRAME_WIDTH + 4, 19, 3 ,3);
		}
		
		g.setColor(color);
		g.fillRoundRect((int) getX() + 2, (int) getY() + 2, (int) barWidth, 15, 2, 2);
	}
	
	protected void fix() {
		if (barWidth > FRAME_WIDTH) barWidth = FRAME_WIDTH;
		if (barWidth < 0) barWidth = 0;
	}
	
	private void adjustDirection() {
		directX = mortal.getDirectX();
		
		setX(mortal.getHead().getX());
		setY(mortal.getHead().getY() - 50);
	}
	
	public void show(boolean flag) { show = flag; }
}