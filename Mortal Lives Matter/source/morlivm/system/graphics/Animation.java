package morlivm.system.graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.graphics.sheet.Sprite;
import morlivm.system.math.RNG;

public class Animation implements GraphicsManager
{
	private int counter, tick, ceiling, row;
	private BufferedImage image;
	private Sprite sprite;
	
	public Animation(Pamphlet pamphlet) {
		this.sprite = pamphlet.getSprite();
		this.ceiling = pamphlet.getSprite().getRunningSpeed();
		this.row = 1;
		reset();
	}
	
	public Animation(String staticImagePath) {
		this.sprite = new Sprite(staticImagePath);
		this.ceiling = 0;
		this.row = 1;
		reset();
	}
	
	public Animation(BufferedImage staticImage) {
		this.sprite = new Sprite(staticImage);
		this.ceiling = 0;
		this.row = 1;
		reset();
	}
	
	public Animation(Animation other) {
		this.sprite = new Sprite(other.sprite);
		this.ceiling = other.ceiling;
		this.row = other.row;
		reset();
	}
	
	public void update(double delta) {
		image = grabSprite();
	}
	
	public void render(ExtendedGraphics2D g) {}
	
	public BufferedImage grabSprite(int row, int col) { return sprite.grabSprite(col, row); }
	
	public BufferedImage grabSprite() {
		if (ceiling == 0) return sprite.grabSprite(tick, row);
		
		counter++; //counter counts to the ceiling and then changes tick
		if (counter == ceiling) {
			tick = (tick < getMax()) ? tick + 1 : 1;
			counter = 0;
		}
		
		return sprite.grabSprite(tick, row);
	}
	
	public void setRow(int r, boolean reset) {
		row = r;
		if (reset) reset();
	}
	
	public void reset() {
		tick = 1;
		counter = 0;
		image = grabSprite(row, tick);
	}
	
	public Sprite getSprite() { return sprite; }
	public BufferedImage getImage() { return image; }
	public int getRow() { return row; }
	private int getMax() { return sprite.getBoundaries()[row - 1]; }
	public void randomize() { tick = RNG.generate(1, getMax()); }
	public void setCeiling(int c) { ceiling = c; }
	public int getCeiling() { return ceiling; }
	public void setTick(int t) { tick = t; }
	public int getTick() { return tick; }
	public boolean getTick(int t) { return tick == t && counter == 1; } //so it will only work once per tick
	public boolean getTickHigherThan(int t) { return tick > t; }
	public boolean getTickLowerThan(int t) { return tick < t; }
	public boolean lastTick() { return getTick(getMax()); }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}