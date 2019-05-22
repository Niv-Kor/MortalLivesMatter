package morlivm.map.parallex;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import morlivm.content.Entity;
import morlivm.content.mortal.Player;
import morlivm.database.LevelData;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Physics.Axis;
import morlivm.system.math.Physics.Vector;
import morlivm.system.sound.MusicBox;

public abstract class GeoLayer implements Graphable
{
	protected Image image;
	protected Point point;
	protected Dimension dim;
	protected boolean impactZone;
	protected Player entity;
	protected MusicBox musicBox;
	protected LevelData ldb;
	protected GameState gameState;
	
	public GeoLayer(BufferedImage geoImage, Point point, GameState gs) {
		this.point = new Point(point);
		this.dim = new Dimension(geoImage.getWidth(), geoImage.getHeight());
		this.gameState = gs;
		this.musicBox = new MusicBox();
		this.image = geoImage;
		this.ldb = Structure.getDatabase();
	}
	
	public void update(double delta) {}
	
	public void render(ExtendedGraphics2D g) {
		align();
		g.drawImage(image, point, dim, transparency());
	}
	
	protected void align() {
		if (getX() > 0) setX(0);
	}
	
	protected boolean usingImapctZone() {
		return impactZone && entity.isWalkingHorizontally() &&
			   !(entity.isWalking(Vector.LEFT, Axis.X) && entity.getX() >= Game.WIDTH / 2);
	}
	
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public void resetComponentsRelativeToX() {}
	public Image getImage() { return image; }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public int getWidth() { return dim.width; }
	public int getHeight() { return dim.height; }
	public void connectRulerEntity(Entity e) { this.entity = (Player) e; }
	public void setImpactZone(boolean flag) { impactZone = flag; }
	public boolean getImpactZone() { return impactZone; }
	public Entity getEntity() { return entity; }
	public void terminate() { musicBox.requestRemovalAll(); }
	protected abstract int transparency();
}