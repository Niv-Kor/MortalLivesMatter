package morlivm.content.proj;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.geom.Area;
import morlivm.content.mortal.Mortal;
import morlivm.content.mortal.Player;
import morlivm.database.ProjectileData;
import morlivm.main.Game;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.math.Physics.Vector;
import morlivm.warfare.target.AimedTarget;

public class Bullet extends Projectile
{
	private boolean transProj;
	
	public Bullet(Mortal owner, double strength, ProjectileData pdb,
				  int projHeight, GameState gs, Point deviation) {
		
		super(owner, strength, pdb, projHeight, gs, deviation);
	}
	
	protected void init(SubType subType) {
		Point endPoint = null;
		if (subType == SubType.AIM) endPoint = Game.getMouseInput().getPoint();
		else if (subType == SubType.STRAIGHT) endPoint = new Point(Game.WIDTH * 1.5 * getDirectX().straight(), 0);
		this.target = new AimedTarget(this, endPoint);
		this.pinpoint = new Collider(new Point(point, 0, dim.height / 2, 0), new Dimension(dim.width, dim.height / 2));
		
		this.sound = db.SFX;
		musicBox.put(sound);
		musicBox.export();
	}
	
	public boolean failureCheck() {
		/*
		 *_______________________ 
		 * 				 |		/
		 * 				 |	tr /|					
		 * 				 |	  / |
		 * 		fail 	 |	 /  |
		 *               |  /   |
		 * 				 | /    |
		 * 				 |/     |
		 * 				*|		|
		 * 		player	*|	gen	|
		 * 				*|		|
		 * 				 |\		|
		 * 				 | \	|
		 * 				 |	\	|
		 * 				 |	 \	|
		 * 				 |	tr\	|
		 *_______________|_____\|
		 */
		
		Collider cursor = Game.getMouseInput().getMouseCollider(3);
		Polygon genuinePoly, transPoly;
		boolean genuine;
		int[] x, y;
		int n;
		
		if (offensive.getDirectX() == Vector.RIGHT) {
			//right genuine polygon
			n = 4;
			x = new int[n];
			y = new int[n];
			
			x[0] = Game.WIDTH;
			y[0] = 0;
			
			x[1] = (int) Math.round(offensive.getHead().getB().getX() + 200);
			y[1] = (int) Math.round(offensive.getHead().getB().getY());
			
			x[2] = (int) Math.round(offensive.getLegs().getC().getX() + 200);
			y[2] = (int) Math.round(offensive.getLegs().getC().getY());
			
			x[3] = Game.WIDTH;
			y[3] = Game.HEIGHT;
			
			genuinePoly = new Polygon(x, y, n);
			
			//right trans polygon
			n = 6;
			x = new int[n];
			y = new int[n];
			
			x[0] = Game.WIDTH - 1;
			y[0] = -1;
			
			x[1] = (int) Math.round(offensive.getMidX());
			y[1] = -1;
			
			x[2] = (int) Math.round(offensive.getMidX());
			y[2] = Game.HEIGHT + 1;
			
			x[3] = Game.WIDTH - 1;
			y[3] = Game.HEIGHT + 1;
			
			x[4] = (int) Math.round(offensive.getLegs().getC().getX() + 199);
			y[4] = (int) Math.round(offensive.getLegs().getC().getY() + 1);
			
			x[5] = (int) Math.round(offensive.getHead().getB().getX() + 199);
			y[5] = (int) Math.round(offensive.getHead().getB().getY() - 1);
			
			transPoly = new Polygon(x, y, n);
		}
		else {
			//left genuine polygon
			n = 4;
			x = new int[n];
			y = new int[n];
			
			x[0] = 0;
			y[0] = 0;
			
			x[1] = (int) Math.round(offensive.getHead().getA().getX() - 200);
			y[1] = (int) Math.round(offensive.getHead().getA().getY());
			
			x[2] = (int) Math.round(offensive.getLegs().getD().getX() - 200);
			y[2] = (int) Math.round(offensive.getLegs().getD().getY());
			
			x[3] = 0;
			y[3] = Game.HEIGHT;
			
			genuinePoly = new Polygon(x, y, n);
			
			//left trans polygon
			n = 6;
			x = new int[n];
			y = new int[n];
			
			x[0] = 1;
			y[0] = -1;
			
			x[1] = (int) Math.round(offensive.getMidX());
			y[1] = -1;
			
			x[2] = (int) Math.round(offensive.getMidX());
			y[2] = Game.HEIGHT + 1;
			
			x[3] = 1;
			y[3] = Game.HEIGHT + 1;
			
			x[4] = (int) Math.round(offensive.getLegs().getC().getX() - 199);
			y[4] = (int) Math.round(offensive.getLegs().getC().getY() + 1);
			
			x[5] = (int) Math.round(offensive.getHead().getB().getX() - 199);
			y[5] = (int) Math.round(offensive.getHead().getB().getY() - 1);
			
			transPoly = new Polygon(x, y, n);
		}
		
		
		Area genuineArea = new Area(genuinePoly);
		Area transArea = new Area(transPoly);
		Area cursor1 = new Area(cursor.getPolygon());
		Area cursor2 = new Area(cursor1);
		
		cursor1.intersect(genuineArea); //cursor in the genuine area
		cursor2.intersect(transArea); //cursor in the transparent area
		
		if (!cursor1.isEmpty()) genuine = super.failureCheck(); //genuine
		else if (!cursor2.isEmpty()) { //transparent
			setX(getX() - 50 * getDirectX().straight()); //take back
			pinpoint = new Collider(new Point(point, 0, dim.height / 2, 0), new Dimension(2, 2));
			transProj = true;
			genuine = true;
		}
		else genuine = false; //fail
		
		if (genuine) playSound();
		return genuine;
	}

	public void update(double delta) {
		super.update(delta);
		if (!running) return;
		
		animation.update(delta);
		if (running && !hit(false)) { //reached end point
			if (offensive instanceof Player) ((Player) offensive).getSight().lock(false);
			if (hit(true)) { //hit target
				gameState.getAftershockManager().addShock(db.aftershock,
					new Point(target.getX() - db.aftershock.getDimension().width / 2,
					target.getY() - db.aftershock.getDimension().height / 2));
				
				running = false;
				musicBox.requestRemovalAll();
			}
			else moveTowardsTarget(delta);
		}
		else running = false;
	}

	public void render(ExtendedGraphics2D g) {
		if (running && !transProj) moveTowardsTarget(g);
		else if (!running) terminate(false);
	}
	
	protected void moveTowardsTarget(double delta) {
		setX(getX() + target.getCurrentDistX() / speed * getDirectX().straight());
		pinpoint.setX(pinpoint.getX() + target.getCurrentDistX() / speed * getDirectX().straight());
		
		if (db.subType == SubType.AIM) {
			setY(getY() - target.getCurrentDistY() / speed * getDirectY().straight());
			pinpoint.setY(pinpoint.getY() - target.getCurrentDistY() / speed * getDirectY().straight());
		}
	}
	
	protected void moveTowardsTarget(ExtendedGraphics2D g) {
		if (db.subType == SubType.AIM) target.aim(g);
		else if (db.subType == SubType.STRAIGHT) target.slide(g, false);
	}
	
	private boolean hit(boolean succeed) {
		if (!succeed) return reachedEndPoint();
		else return contactMngr.getAimedHit(offensive, pinpoint, strength, conditionType,
											true, !(offensive instanceof Player), sound[1]);
	}
	
	public void terminate(boolean cry) {
		super.terminate(cry);
		ricoMngr.remove(this);
	}
	
	public int getState() {	return -1; }
}