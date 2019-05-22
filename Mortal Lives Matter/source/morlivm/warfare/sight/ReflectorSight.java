package morlivm.warfare.sight;
import java.awt.Dimension;

import morlivm.content.Entity;
import morlivm.database.DataManager;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.performance.Timer;

public class ReflectorSight extends Sight implements Graphable
{
	private final static Pamphlet SS = DataManager.retSheet("ss$g$reflector_sight");
	private final static int FRAMES = SS.getSprite().getCols();
	
	private int state, preTick, pumpSize;
	private Dimension currentDim;
	private boolean rotate, showDefSight, defSightFlag, pumping, resetPump;
	
	public ReflectorSight(SightInventory sightInv, GameState gs) {
		super(sightInv, gs);
		
		this.state = 0;
		this.preTick = -1;
		this.animation = new Animation(SS);
		this.currentSprite = animation.grabSprite();
		this.currentDim = SS.getDimension();
		updateCoordinates(currentDim);
	}
	
	public void update(double delta) {
		super.update(delta);
		
		updateCoordinates(currentDim);
		if (animation.getTickLowerThan(FRAMES) && rotate) {
			if (preTick != -1) {
				animation.setTick(10 - preTick);
				preTick = -1;
			}
			if (!showDefSight) currentSprite = animation.grabSprite();
			if (animation.getTick(FRAMES)) rotate = false;
		}
		if (!mouseInput.isOnTarget()) setTarget(null);
		alternateSight();
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		if (!show) return;
		
		g.drawImage(currentSprite, (int) getX() - pumpSize, (int) getY() - pumpSize,
					currentDim.width + pumpSize * 2, currentDim.height + pumpSize * 2, null);
		
		pump(g);
	}
	
	public void setTarget(Entity t) {
		if (target != t) {
			rotate = true;
			if (animation.getTickLowerThan(FRAMES) && state != 0) preTick = animation.getTick();
			animation.reset();
			if (state == 0) state++;
			else state = (state == 1) ? 2 : 1;
			
			animation.setRow(state, true);
		}
		target = t;
	}
	
	private void pump(ExtendedGraphics2D g) {
		Timer pump = timingDevice.getTimer("pump");
		
		if (!(resetPump && pumpSize > 0)) {
			if (pumping && pump != null && pump.progressedToRoof()) {
				if (pumpSize < 10) pumpSize++;
				else {
					timingDevice.removeTimer("pump");
					pumping = false;
					pumpSize = 0;
				}
			}
		}
		resetPump = false;
	}
	
	private void alternateSight() {
		showDefSight = player.meleeAttacking();
		
		if (showDefSight) {
			currentSprite = sightInv.getDefSightImg();
			currentDim = UnarmedSight.DIM;
			defSightFlag = false;
		}
		else {
			currentDim = SS.getDimension();
			if (!defSightFlag) {
				animation.reset();
				currentSprite = animation.grabSprite();
			}
			defSightFlag = true;
		}
		updateCoordinates(currentDim);
	}
	
	public void pump() {
		timingDevice.addTimer("pump", 0.05);
		pumping = true;
		resetPump = true;
		pumpSize = 0;
	}
	
	public int getDexFilter() { return 100 - (int) distance * 2; }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return SS.getDimension(); }
	public Point getPoint() { return circularPoint; }
	public void setX(double x) { circularPoint.setX(x); }
	public void setY(double y) { circularPoint.setY(y); }
	public double getX() { return circularPoint.getX(); }
	public double getY() { return circularPoint.getY(); }
	public SightInventory.Type getSightType() { return SightInventory.Type.REFLECTOR; }
}