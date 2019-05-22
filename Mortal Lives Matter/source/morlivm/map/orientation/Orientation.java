package morlivm.map.orientation;
import java.awt.Dimension;
import java.util.List;
import morlivm.content.mortal.Mortal;
import morlivm.map.Boundary;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.math.Physics.Axis;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RNG;

public class Orientation
{
	private List<Boundary> bList;
	private Mortal mortal;
	private double priorYGeneration;
	private Vector lastBlockedDirect;
	
	public Orientation(Mortal mortal) {
		this.mortal = mortal;
		this.bList = Boundary.getList();
		this.priorYGeneration = mortal.getDirectY().generate();
		this.lastBlockedDirect = Vector.NONE;
	}
	
	public boolean canMove() {
		return canMove(Axis.X) && canMove(Axis.Y);
	}
	
	public boolean canMove(Axis axis) {
		if (axis == Axis.Z) return true;
		
		Vector colliderDirect;
		colliderDirect = (axis == Axis.X) ? mortal.getDirectX() : mortal.getDirectY();
		if (colliderDirect == Vector.NONE) return true;
		
		//copy the legs collider of mortal and expand it by delta
		int delta = 15;
		Collider legs = mortal.getLegs();
		Dimension oldDim = legs.getDimension(), newDim;
		Point oldP = legs.getPoint(), newPoint;
		
		switch(colliderDirect) {
			case UP: {
				newDim = new Dimension(oldDim.width + delta * 2, oldDim.height / 2 + delta);
				newPoint = new Point(oldP, -delta, -delta, 0);
				break;
			}
			case LEFT: {
				newDim = new Dimension(oldDim.width / 2 + delta, oldDim.height + delta * 2);
				newPoint = new Point(oldP, -delta, -delta, 0);
				break;
			}
			case DOWN: {
				newDim = new Dimension(oldDim.width + delta * 2, oldDim.height / 2 + delta);
				newPoint = new Point(oldP, -delta, delta / 2, 0);
				break;
			}
			case RIGHT: {
				newDim = new Dimension(oldDim.width / 2 + delta, oldDim.height + delta * 2);
				newPoint = new Point(oldP, delta / 2, -delta, 0);
				break;
			}
			default: return true;
		}
		
		Collider expandedLegs = new Collider(newPoint, newDim);
		Point bndryMidP, legsMidP;
		boolean walking;
		
		walking = (axis == Axis.X) ? mortal.isWalkingHorizontally() : mortal.isWalkingVertically();
		
		//allow if mortal is not walking at all
		if (!walking) return true;

		//allow if mortal is trying to walk the other direction
		else if (colliderDirect == lastBlockedDirect.oppose()) {
			lastBlockedDirect = Vector.NONE;
			return true;
		}
		
		legsMidP = new Point(expandedLegs.getX() + expandedLegs.getDimension().width / 2,
				  			 expandedLegs.getY() + expandedLegs.getDimension().height / 2);
		
		for (Boundary b : bList) {
			bndryMidP = new Point(b.getX() + b.getDimension().width / 2,
								  b.getY() + b.getDimension().height / 2);
			
			//continue checking only if the boundary is at a decent distance from the mortal
			if (bndryMidP.withinRange(legsMidP, Math.max(b.getDimension().width, b.getDimension().height))) {
				if (walking && b.intersects(expandedLegs, colliderDirect)) {
					//save last blocked direction, but only once, to avoid digging up inside the boundary
					if (lastBlockedDirect == Vector.NONE) lastBlockedDirect = colliderDirect;
					return false;
				}
			}
		}
		
		return true;
	}
	
	public double generateSoftY() {
		Vector vector = mortal.getDirectY();
		int currentVector = vector.straight(); 
		
		if (currentVector == 1) {
			if (priorYGeneration < 0) priorYGeneration = vector.generate();
			else if (priorYGeneration == 0) priorYGeneration = softGeneration(currentVector);
		}
		else if (currentVector == -1) {
			if (priorYGeneration > 0) priorYGeneration = vector.generate();
			else if (priorYGeneration == 0) priorYGeneration = softGeneration(currentVector);
		}
		else priorYGeneration = 0;
		
		//no need to change the generation
		return priorYGeneration;
	}
	
	private double softGeneration(int vector) {
		if (vector < 0) priorYGeneration = RNG.generateDouble(-0.3, -0.05);
		else if (vector > 0) priorYGeneration = RNG.generateDouble(0.05, 0.3);
		else priorYGeneration = 0;
		
		return priorYGeneration;
	}
}