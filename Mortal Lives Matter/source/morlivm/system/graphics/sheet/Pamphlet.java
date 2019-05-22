package morlivm.system.graphics.sheet;
import java.awt.Dimension;

import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.NumeralHandler;

public class Pamphlet
{
	private String code;
	private Sprite sprite;
	private Mask mask;
	
	public Pamphlet(String code, Sprite sprite, Mask mask) {
		this.code = new String(code);
		this.sprite = (sprite != null) ? sprite : new Sprite(ImageHandler.BLANK);
		this.mask = (mask != null) ? mask : new Mask(ImageHandler.blankPath());
	}
	
	public Pamphlet(Pamphlet other) {
		this.code = new String(other.code);
		this.sprite = new Sprite(other.sprite);
		this.mask = new Mask(other.getMask());
	}
	
	public Dimension getDimension() { return (NumeralHandler.max(sprite.getDimension(), mask.getDimension())); }
	public String getCode() { return code; }
	public Sprite getSprite() { return sprite; }
	public Mask getMask() { return mask; }
}