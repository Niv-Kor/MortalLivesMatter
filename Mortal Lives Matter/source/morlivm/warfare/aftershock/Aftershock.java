package morlivm.warfare.aftershock;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;

import morlivm.database.DataManager;
import morlivm.map.Attachable;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.sound.MusicBox;

public class Aftershock implements Attachable
{
	public static enum Type {
		NONE(null, null),
		BLOOD(Blood.class, "ss$g$blood"),
		DUST(Dust.class, "ss$g$dust"),
		EXPLOSION(Explosion.class, "ss$g$explosion"),
		EARTHQUAKE(Quake.class, null);
		
		public Class<?> instanceOf;
		private Pamphlet pamphlet;
		private Animation animation;
		
		private Type(Class<?> c, String spriteCode) {
			this.instanceOf = c;
			this.pamphlet = DataManager.retSheet(spriteCode);
			this.animation = (pamphlet != null) ? new Animation(pamphlet) : null;
		}
		
		public Aftershock createInstance(Point p, GameState gs) {
			try	{
				return instanceOf.asSubclass(Aftershock.class).
					   getConstructor(Animation.class, Point.class, GameState.class).
					   newInstance(animation, p, gs);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
				   InvocationTargetException | NoSuchMethodException | SecurityException e) {
				
				e.printStackTrace();
				System.err.println("Aftershock constructor is invalid.");
			}
			
			return null;
		}
		
		public Dimension getDimension() { return (pamphlet != null) ? pamphlet.getDimension() : new Dimension(0, 0); }
	}
	
	protected Point point, originP;
	protected boolean init;
	protected Dimension dim;
	protected Animation animation;
	protected MusicBox musicBox;
	protected AftershockManager aftershockMngr;
	protected GameState gameState;
	
	public Aftershock(Animation animation, Point point, GameState gs) {
		this.gameState = gs;
		this.point = new Point(point);
		this.originP = new Point(point, 0, 0, gs.getArena().getGround().getY());
		this.aftershockMngr = gs.getAftershockManager();
		this.musicBox = new MusicBox();
		this.animation = (animation != null) ? new Animation(animation) : null;
		this.dim = (animation != null) ? animation.getSprite().getDimension() : new Dimension(0, 0);
	}
	
	public void update(double delta) {
		if (!init) init();
		animation.update(delta);
		if (animation.lastTick()) terminate();
	}
	
	public void render(ExtendedGraphics2D g) {
		g.drawImage(animation.getImage(), point, dim);
	}
	
	protected void terminate() { aftershockMngr.removeShock(this); }
	protected void init() {	init = true; }
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public Point getFixedPoint() { return originP; }
}