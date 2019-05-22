package morlivm.system.math;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Polygon;

import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.warfare.damage_filter.Stats;

public class Octagon implements Graphable
{
	private final static Font FONT = new Font("Showcard Gothic", Font.PLAIN, 10);
	private final static int VALUES = 8;
	private final static double INT_DEVIATION = 1.21;
	
	private int[][] stats;
	private int radius;
	private boolean show;
	private Point point;
	private Polygon scaler;
	private String[] statsName;
	private Polygon[] staticOcta;
	
	public Octagon(Stats s, Point point, int radius) {
		this.radius = radius;
		this.point = point;
		this.staticOcta = new Polygon[radius];
		initStats(s);
		
		Point temp;
		for (int i = 0; i < radius; i++) {
			temp = new Point(getX() + i * INT_DEVIATION, getY() + i * INT_DEVIATION);
			createOcta(i, temp, radius - i);
		}
		
		createScaler();
		scale();
	}
	
	public void update(double delta) {}

	public void render(ExtendedGraphics2D g) {
		g.setColor(new Color(0, 0, 0, 150));
		g.fillPolygon(staticOcta[0]);
		g.setColor(new Color(255, 255, 255, 80));
		g.fillPolygon(staticOcta[20]);
		g.setColor(new Color(255, 255, 255, 120));
		g.fillPolygon(staticOcta[40]);
		if (show) {
			g.setColor(new Color(241, 176, 0, 190));
			g.fillPolygon(scaler);
			g.setColor(new Color(241, 176, 0, 190).darker());
			g.drawPolygon(scaler);
		}
		
		g.setColor(new Color(255, 255, 255, 100));
		for (int i = 1; i <= radius; i += 20)
			g.drawPolygon(staticOcta[i - 1]);
		
		for (int i = 0; i < VALUES / 2; i++)
			g.drawLine(staticOcta[i].xpoints[i], staticOcta[i].ypoints[i],
					   staticOcta[i].xpoints[i + 4], staticOcta[i].ypoints[i + 4]);
		
		Point p;
		g.setFont(FONT);
		for (int i = 0; i < VALUES; i++) {
			p = getTextPoint(i, FONT.getSize(), statsName[i].length());
			g.drawOutlineString(statsName[i], p, 2, Color.WHITE, Color.BLACK);
		}
	}
	
	private void createOcta(int index, Point point, double length) {
		double pythagoras = length / Math.sqrt(2);
		double x = point.getX(), y = point.getY();
		Point[] octaPoints = new Point[VALUES + 1];
		
		octaPoints[0] = new Point(x + pythagoras, y);
		octaPoints[1] = new Point(x + pythagoras + length, y);
		octaPoints[2] = new Point(x + pythagoras * 2 + length, y + pythagoras);
		octaPoints[3] = new Point(x + pythagoras * 2 + length, y + pythagoras + length);
		octaPoints[4] = new Point(x + pythagoras + length, y + pythagoras * 2 + length);
		octaPoints[5] = new Point(x + pythagoras, y + pythagoras * 2 + length);
		octaPoints[6] = new Point(x, y + pythagoras + length);
		octaPoints[7] = new Point(x, y + pythagoras);
		octaPoints[8] = new Point(x + pythagoras, y);
		
		int[] staticX = new int[VALUES + 1], staticY = new int[VALUES + 1];
		for (int i = 0; i < VALUES + 1; i++) {
			staticX[i] = (int) octaPoints[i].getX();
			staticY[i] = (int) octaPoints[i].getY();
		}
		this.staticOcta[index] = new Polygon(staticX, staticY, VALUES + 1);
	}
	
	private void createScaler() {
		int[] staticX = new int[VALUES + 1], staticY = new int[VALUES + 1];
		for (int i = 0; i < VALUES + 1; i++) {
			staticX[i] = (int) (getX() + (radius + radius / Math.sqrt(2)) / 2);
			staticY[i] = (int) (getX() + (radius + radius / Math.sqrt(2)) / 2);
		}
		this.scaler = new Polygon(staticX, staticY, VALUES + 1);
	}
	
	private void initStats(Stats s) {
		this.stats = new int[VALUES + 1][2];
		this.statsName = new String[VALUES];
		
		stats[0][0] = s.offense;
		stats[0][1] = 150;
		statsName[0] = "ATT";
		
		stats[1][0] = s.defense;
		stats[1][1] = 150;
		statsName[1] = "DEF";
		
		stats[2][0] = s.health;
		stats[2][1] = 2500;
		statsName[2] = "HP";
		
		stats[3][0] = s.mana;
		stats[3][1] = 1000;
		statsName[3] = "MP";
		
		stats[4][0] = s.stamina;
		stats[4][1] = 2500;
		statsName[4] = "STAMINA";
		
		stats[5][0] = s.agility;
		stats[5][1] = 250;
		statsName[5] = "AGILITY";
		
		stats[6][0] = s.accuracy;
		stats[6][1] = 100;
		statsName[6] = "ACC";
		
		stats[7][0] = s.criticalRate;
		stats[7][1] = 20;
		statsName[7] = "CRIT";
		
		stats[8][0] = stats[0][0];
		stats[8][1] = stats[0][1];
	}
	
	private void scale() {
		for (int i = 0, j; i < VALUES + 1; i++) {
			j = radius - 1 - (int) (Percent.numOfNum(stats[i][0], stats[i][1]) * radius / 100);
			if (j < 0) j = 0;
			else if (j > radius - 1) j = radius - 1;
			
			scaler.xpoints[i] = staticOcta[j].xpoints[i];
			scaler.ypoints[i] = staticOcta[j].ypoints[i];
		}
	}
	
	private Point getTextPoint(int index, int strSize, int strLngth) {
		Point delta = new Point();
		
		switch(index) {
			case 0:
			case 1: delta = new Point(-5, -strSize / 2); break;
			case 2:
			case 3: delta = new Point(5, strSize / 2); break;
			case 4:
			case 5: delta = new Point(-strSize * strLngth / 4, strSize); break;
			case 6:
			case 7: delta = new Point(-strSize * 3, strSize / 2); break;
		}
		
		return new Point(staticOcta[0].xpoints[index] + delta.getX(),
						 staticOcta[0].ypoints[index] + delta.getY());
	}
	
	public void show(boolean flag) { show = flag; }
	public int getSqurSize() { return radius; }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return new Dimension(radius * 2, radius * 2); }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}