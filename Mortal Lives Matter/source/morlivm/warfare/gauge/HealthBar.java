package morlivm.warfare.gauge;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import morlivm.content.mortal.Mortal;
import morlivm.control_panel.Purchasable;
import morlivm.control_panel.SaleAd;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics;

public abstract class HealthBar implements Graphable, Purchasable
{
	protected final static Font SMALL_POINTS_FONT = new Font("Arial Rounded MT Bold", Font.BOLD, 13);
	protected final static Font LARGE_POINTS_FONT = new Font("Bahnschrift", Font.ITALIC, 18);
	protected final static Font EXTRA_LARGE_POINTS_FONT = new Font("OCR A Std", Font.PLAIN, 18);
	protected final static Color BAR_COLOR = Color.RED;
	
	protected double fullHealth, healthPoints, targetHealth, barWidth;
	protected boolean fill, drain, tookEffect;
	protected int frameWidth;
	protected Physics.Vector directX;
	protected Point point;
	protected Color color, shadow;
	protected Mortal mortal;
	protected SaleAd buy;
	protected boolean flickering, killWhenOver, show;
	
	public HealthBar(Mortal mortal, double health, boolean killWhenOver) {
		this.mortal = mortal;
		this.fullHealth = health;
		this.healthPoints = fullHealth;
		this.targetHealth = healthPoints;
		this.killWhenOver = killWhenOver;
		this.point = new Point();
		this.color = BAR_COLOR;
	}
	
	public void update(double delta) {
		if (over()) takeEffectWhenOver();
		if (buy != null) buy.update(delta);
		
		double largePercent = Percent.percentOfNum(8, fullHealth);
		double smallPercent = Percent.percentOfNum(2, fullHealth);
		
		if (drain) {
			if (healthPoints > targetHealth + largePercent)
				for (int i = 0; i < largePercent; i++) decrement();
			
			else if (healthPoints > targetHealth + smallPercent) 
				for (int i = 0; i < smallPercent; i++) decrement();
			
			else if (healthPoints > targetHealth) decrement();
			else drain = false;
		}
		else if (fill) {
			if (healthPoints < targetHealth - largePercent)
				for (int i = 0; i < largePercent; i++) increment();
			
			else if (healthPoints < targetHealth - smallPercent)
				for (int i = 0; i < smallPercent; i++) increment();
					
			else if (healthPoints < targetHealth) increment();
			else fill = false;
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		if (buy != null) buy.render(g);
	}
	
	public boolean increase(double p) {
		if (isFull()) return false;
		else {
			if (healthPoints + p > fullHealth) p = fullHealth - healthPoints;
			if (targetHealth <= healthPoints + p) targetHealth = healthPoints + p;
			fill = true;
			return true;
		}
	}
	
	public boolean decrease(double p) {
		if (over()) return false;
		else {
			if (healthPoints - p < 0) p = healthPoints;
			if (targetHealth >= healthPoints - p) targetHealth = healthPoints - p;
			drain = true;
			return true;
		}
	}
	
	private void increment() {
		if (isFull()) return;
		
		healthPoints++;
		barWidth += Percent.percentOfNum(Percent.numOfNum(1, fullHealth), frameWidth);
		fix();
		
	}
	
	private void decrement() {
		if (over()) return;
		
		healthPoints--;
		barWidth -= Percent.percentOfNum(Percent.numOfNum(1, fullHealth), frameWidth);
		fix();
	}
	
	protected void takeEffectWhenOver() {
		if (tookEffect) return;
		
		if (killWhenOver) mortal.terminate(true);
		tookEffect = true;
	}
	
	public boolean checkPurchaseRelevance() { return !isFull() && mortal.isAlive(); }
	public String getDeclineErrorMessage() { return null; }
	public String getAdDescription() { return "full bar"; }
	public Dimension getAdDimension() { return new Dimension(0, 0); }
	public void purchase() { recover(); }
	public void recover() { increase(fullHealth); }
	public boolean isFull() { return healthPoints == fullHealth; }
	public boolean over() { return healthPoints < 1; }
	protected abstract void fix();
	public double getHealthPoints() { return healthPoints; }
	public void show(boolean flag) { show = flag; }
	public boolean isShowing() { return show; }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return null; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}