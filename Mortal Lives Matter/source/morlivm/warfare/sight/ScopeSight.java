package morlivm.warfare.sight;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import morlivm.content.Entity;
import morlivm.database.DataManager;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.RNG;
import morlivm.system.performance.Timer;

public class ScopeSight extends Sight
{
	private final static Font FONT = new Font("Arial Rounded MT Bold", Font.BOLD, 16);
	public final static Pamphlet SS = DataManager.retSheet("ss$g$scope_sight");
	private final static int MID_VALUE = 35;
	
	private int damageValue, lineLength, assessor;
	private int verDist, horDist, slowingDevice;
	private Point verP, horP;
	private Point topCircP, bottomCircP, leftCircP, rightCircP;
	private boolean verFlag, horFlag, locked;
	private Entity preTarget;
	private Animation animation;
	
	public ScopeSight(SightInventory sightInv, GameState gs) {
		super(sightInv, gs);
		
		timingDevice.addTimer("vertical", 0.03);
		timingDevice.addTimer("horizontal", 0.03);
		this.animation = new Animation(SS);
		this.currentSprite = animation.grabSprite();
		this.currentDim = UnarmedSight.DIM;
		
		int rng = RNG.generate(0, SS.getDimension().width / 2);
		this.verDist = rng;
		this.horDist = rng;
		this.verFlag = true;
		this.horFlag = true;
		updateCoordinates(currentDim);
		this.lineLength = 60;
		this.verP = new Point(topCircP.getX() - lineLength / 2, topCircP.getY() + 20);
		this.horP = new Point(leftCircP.getX() + 20, leftCircP.getY() - lineLength / 2);
	}
	
	public void update(double delta) {
		super.update(delta);
		
		Timer vertical = timingDevice.getTimer("vertical");
		Timer horizontal = timingDevice.getTimer("horizontal");
		int direction, limit;
		boolean[] verLimits = {verP.getY() < bottomCircP.getY() - 20, verP.getY() > topCircP.getY() + 20};
		boolean[] horLimits = {horP.getX() < rightCircP.getX() - 20, horP.getX() > leftCircP.getX() + 20};
		verP = new Point(topCircP);
		
		if (!mouseInput.isOnTarget() || player.meleeAttacking()) setTarget(null);
		updateCoordinates(currentDim);
		
		if (target != null) {
			if (!locked) {
				currentSprite = animation.grabSprite();
				
				if (vertical.progressedToRoof()) {
					direction = verFlag ? 3 : -3;
					limit = verFlag ? 0 : 1;
					if (verLimits[limit]) {
						verP.setY(verP.getY() + direction / slowingDevice);
						verDist += direction;
					}
					else verFlag = !verFlag;
					vertical.init();
				}
				
				if (horizontal.progressedToRoof()) {
					direction = horFlag ? 3 : -3;
					limit = horFlag ? 0 : 1;
					if (horLimits[limit]) {
						horP.setX(horP.getX() + direction / slowingDevice);
						horDist += direction;
					}
					else horFlag = !horFlag;
					horizontal.init();
				}
				
				damageValue = (horDist <= MID_VALUE) ? horDist : MID_VALUE * 2 - horDist;
				if (damageValue < 0) damageValue = 0;
				assessor = horDist - MID_VALUE;
			}
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		if (!show) return;
		
		if (target != null) {
			g.setColor(new Color(180, 238, 255, 100));
			g.fillOval((int) circularPoint.getX(),
					   (int) circularPoint.getY(),
					   currentDim.width, currentDim.height);
			
			g.setColor(new Color(255, 255, 255, 80));
			g.fillOval((int) circularPoint.getX() + 10,
					   (int) circularPoint.getY() + 10,
					   currentDim.width / 2, currentDim.height / 2);
			
			g.drawImage(currentSprite, circularPoint, currentDim);
			
			g.setStroke(new BasicStroke(3));
			g.setColor(new Color(255, 255, 255, 200));
			g.drawLine((int) verP.getX(), (int) verP.getY(), (int) verP.getX() + lineLength, (int) verP.getY());
			g.drawLine((int) horP.getX(), (int) horP.getY(), (int) horP.getX(), (int) horP.getY() + lineLength);
			
			g.setColor(Color.RED);
			g.drawLine((int) leftCircP.getX() + 20 + MID_VALUE - 3,
					   (int) leftCircP.getY(),
					   (int) leftCircP.getX() + 20 + MID_VALUE + 3,
					   (int) leftCircP.getY());
			
			g.drawLine((int) topCircP.getX(),
					   (int) topCircP.getY() + 20 + MID_VALUE - 3,
					   (int) topCircP.getX(),
					   (int) topCircP.getY() + 20 + MID_VALUE + 3);
			
			g.setFont(FONT);
			Color innerC = new Color(91, 255, 87, 255);
			Color outlineC = new Color(115, 115, 115);
			Point p = new Point(circularPoint.getX() - 30, leftCircP.getY() + 5);
			String assessorStr = new String("" + assessor);
			g.drawOutlineString(assessorStr, p, 1, innerC, outlineC);
			
			NumberFormat formatter = new DecimalFormat("#0.0");
			String distanceStr = new String("D" + formatter.format(distance));
			p.setX(bottomCircP.getX() - distanceStr.length() * FONT.getSize() / 2 + 10);
			p.setY(bottomCircP.getY() + 20);
			g.drawOutlineString(distanceStr, p, 1, innerC, outlineC);
		}
		else {
			g.drawImage(currentSprite,
					   (int) circularPoint.getX(),
					   (int) circularPoint.getY(),
					   currentDim.width, currentDim.height, null);
		}
	}
	
	protected void updateCoordinates(Dimension dim) {
		super.updateCoordinates(dim);
		
		topCircP = new Point(circularPoint.getX() + dim.width / 2, circularPoint.getY());
		bottomCircP = new Point(circularPoint.getX() + dim.width / 2, circularPoint.getY() + dim.height);
		leftCircP = new Point(circularPoint.getX(), circularPoint.getY() + dim.height / 2);
		rightCircP = new Point(circularPoint.getX() + dim.width, circularPoint.getY() + dim.height / 2);
		verP = new Point(topCircP.getX() - lineLength / 2, topCircP.getY() + 20 + verDist);
		horP = new Point(leftCircP.getX() + 20 + horDist, leftCircP.getY() - lineLength / 2);
	}
	
	protected void updateDistance() {
		super.updateDistance();
		double time;
		
		if (distance > 8) {
			slowingDevice = 1;
			time = 0.03;
		}
		else {
			slowingDevice = 3;
			if (distance > 5) time = 0.05;
			else if (distance > 3) time = 0.07;
			else {
				time = 0.07;
				horDist = MID_VALUE;
				verDist = MID_VALUE;
			}
		}
		
		if (time != timingDevice.getTimer("horizontal").getSecondsRoof()) {
			timingDevice.getTimer("horizontal").setRoof(time);
			timingDevice.getTimer("vertical").setRoof(time);
		}
	}
	
	public void setTarget(Entity t) {
		super.setTarget(t);
		
		if (t != null && t != preTarget) {
			currentDim = SS.getDimension();
			int rng = RNG.generate(0, currentDim.width / 2);
			this.verDist = rng;
			this.horDist = rng;
		}
		else if (t == null) setDefSight();
		preTarget = t;
	}
	
	private void setDefSight() {
		currentSprite = sightInv.getDefSightImg();
		currentDim = UnarmedSight.DIM;
		lock(false);
	}
		
	public void lock(boolean flag) { locked = flag; }
	public int getDexFilter() { return (int) (100 + damageValue + distance * 5); }
	public SightInventory.Type getSightType() { return SightInventory.Type.SCOPE; }
}