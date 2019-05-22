package morlivm.system.graphics.sheet;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ImageHandler;

public class Mask extends Sheet
{
	private Collider collider;
	
	public Mask(String path) {
		this.path = new String(path);
		BufferedImage image = ImageHandler.load(path);
		this.collider = new Collider(image, !ImageHandler.isBlank(image));
		this.dim = collider.getDimension();
	}
	
	public Mask(Mask other) {
		this.path = new String(other.path);
		this.dim = new Dimension(other.dim);
		this.collider = new Collider(other.collider);
	}
	
	public Collider getCollider() { return collider; }
}