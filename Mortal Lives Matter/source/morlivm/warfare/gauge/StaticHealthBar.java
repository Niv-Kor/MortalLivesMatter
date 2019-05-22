package morlivm.warfare.gauge;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import morlivm.content.mortal.Mortal;
import morlivm.control_panel.ControlPanel;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;

public abstract class StaticHealthBar extends HealthBar
{
	public final static int POLY_SIZE = 5;
	
	protected Color normalColor = Color.WHITE;
	protected Color blinkColor = Color.BLACK;
	protected Color shadowColor = Color.GRAY;
	
	protected int[] remainder;
	protected Font font;
	private String display;
	private Timer timer;
	private int timerCounter;
	protected int[][][] origin, draft;
	protected int lowFramePoint, highFramePoint;
	protected ControlPanel controlPanel;
	
	public StaticHealthBar(Mortal mortal, int health, boolean killWhenOver) {
		super(mortal, health, killWhenOver);
		
		this.timerCounter = 0;
		this.origin = new int[2][2][POLY_SIZE];
		this.draft = new int[2][2][POLY_SIZE];
		this.remainder = new int[2];
	}
	
	protected void adjustColor() {
		shadow = shadowColor;
		color = normalColor; 
	}
	
	public void flicker() {
		flickering = true;
		shadow = blinkColor;
		color = blinkColor;
		if (timer == null) {
			timer = new Timer(100, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (timerCounter > 4) {
						timerCounter = 0;
						flickering = false;
						if (timer != null) timer.stop();
						timer = null;
						return;
					}
					timerCounter++;
		
					if (color == normalColor) {
						shadow = blinkColor;
						color = blinkColor;
					}
					else {
						shadow = shadowColor;
						color = normalColor;
					}
				}
			});
			timer.start();
			if (timer != null) timer.setRepeats(true);
		}
	}
	
	public void update(double delta) {
		if (!flickering) adjustColor();
		if (healthPoints < 0) healthPoints = 0;
		if (healthPoints > fullHealth) healthPoints = fullHealth;
		if (barWidth > highFramePoint) barWidth = highFramePoint;
		
		super.update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		display = toString();
		int x = (highFramePoint + lowFramePoint) / 2 - display.length() * font.getSize() / 4 + 20;
		int y = (draft[0][1][0] + draft[1][1][3]) / 2 + font.getSize() / 2 - 3;
		
		g.setColor(color);
		g.fillPolygon(draft[1][0], draft[1][1], POLY_SIZE);
		g.setColor(shadow);
		g.fillPolygon(draft[0][0], draft[0][1], POLY_SIZE);
		
		g.setFont(font);
		g.drawOutlineString(display, new Point(x, y), 1, Color.WHITE, new Color(53, 53, 53));
		
		super.render(g);
	}
	
	protected void fix() {
		if (drain) {
			if (draft[0][0][1] > draft[0][0][0]) draft[0][0][1] = (int) barWidth;
			if (draft[0][0][2] > draft[0][0][3]) draft[0][0][2] = (int) barWidth + remainder[0];
			if (draft[1][0][1] > draft[1][0][0]) draft[1][0][1] = (int) barWidth + remainder[0];
			if (draft[1][0][2] > draft[1][0][3]) draft[1][0][2] = (int) barWidth + remainder[0] + remainder[1];
		}
		else if (fill) {
			if (draft[0][0][2] >= draft[0][0][3] + remainder[0]) draft[0][0][1] = (int) barWidth;
			if (draft[1][0][2] >= draft[1][0][0] + remainder[1]) draft[0][0][2] = (int) barWidth + remainder[0];
			if (draft[1][0][2] >= draft[1][0][0] + remainder[1]) draft[1][0][1] = (int) barWidth + remainder[0];
			draft[1][0][2] = (int) barWidth + remainder[0] + remainder[1];
		}
		
		if (draft[0][0][1] < draft[0][0][0]) draft[0][0][1] = draft[0][0][0];
		if (draft[0][0][2] < draft[0][0][3]) draft[0][0][2] = draft[0][0][3];
		if (draft[1][0][1] < draft[1][0][0]) draft[1][0][1] = draft[1][0][0];
		if (draft[1][0][2] < draft[1][0][3]) draft[1][0][2] = draft[1][0][3];
	}

	public String toString() {
		return new String((int) healthPoints + " / " + (int) fullHealth);
	}
	
	public double getPoints() { return healthPoints; }
	public void connectRulerPanel(ControlPanel cp) { controlPanel = cp; }
	protected void adjustDirection() {}
	protected abstract void init();
}