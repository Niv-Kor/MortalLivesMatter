package morlivm.map.parallex;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import morlivm.content.mortal.Mortal;
import morlivm.map.AttachManager;
import morlivm.map.Attachable;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;

public class Arena implements GraphicsManager, AttachManager
{
	private final static int LAYERS = 5;
	
	private GeoLayer[] maps;
	private BufferedImage mask, stars;
	
	public Arena(GameState gs) {
		this.maps = new GeoLayer[LAYERS];
		maps[0] = new Cliff(Cliff.ORIGIN_POINT, gs);
		maps[1] = new Ground(Ground.originPoint(), gs);
		maps[2] = new CloseView(new Point(0, 0), gs);
		maps[3] = new FarView(new Point(0, 0), gs);
		maps[4] = new Sky(new Point(0, 0), gs);
	}
	
	public void update(double delta) {
		for (int i = 0; i < LAYERS; i++)
			maps[i].update(delta);
	}

	public void render(ExtendedGraphics2D g) {}
	
	public void setImpactZone(boolean flag) {
		for (int i = 0; i < LAYERS; i++)
			maps[i].setImpactZone(flag);
	}
	
	public void connectRulerEntity(Mortal mortal) {
		for (int i = 0; i < LAYERS; i++)
			maps[i].connectRulerEntity(mortal);
	}
	
	public Point[] getCurrentPosition() {
		Point[] pos = new Point[LAYERS];
		
		for (int i = 0; i < LAYERS; i++)
			pos[i] = new Point(maps[i].getX(), maps[i].getY());
		
		return pos;
	}
	
	public void setPosition(Point[] pos) {
		if (pos.length != LAYERS) return;
		
		for (int i = 0; i < LAYERS; i++) {
			maps[i].setX(pos[i].getX());
			maps[i].setY(pos[i].getY());
			maps[i].resetComponentsRelativeToX();
		}
	}
	
	public List<Attachable> getMagnetizedComponents() {
		List<Attachable> list = new ArrayList<Attachable>();
		list.addAll(getGround().getMagnetizedComponents());
		list.add(getCliff());
		list.add(getGround());
		
		return list;
	}
	
	public BufferedImage getMask() { return mask; }
	public BufferedImage getStars() { return stars; }
	public Cliff getCliff() { return (Cliff) maps[0]; }
	public Ground getGround() { return (Ground) maps[1]; }
	public CloseView getCloseView() { return (CloseView) maps[2]; }
	public FarView getFarView() { return (FarView) maps[3]; }
	public Sky getSky() { return (Sky) maps[4]; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
	public Point getPurePoint() { return new Point(); }
}