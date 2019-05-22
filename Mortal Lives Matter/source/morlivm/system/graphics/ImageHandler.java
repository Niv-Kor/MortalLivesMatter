package morlivm.system.graphics;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class ImageHandler
{
	private final static BufferedImage NULL = load("/sheets/null.png");
	public final static BufferedImage BLANK = load(blankPath());
	
	public static boolean test(String path) {
		try	{ ImageIO.read(ImageHandler.class.getResource(path)); }
		catch (Exception e) { return false;	}
		return true;
	}
	
	public static BufferedImage load(String path) {
		BufferedImage image = null;

		try	{ image = ImageIO.read(ImageHandler.class.getResource(path)); }
		catch (Exception e) {
			if (path != null) {
				System.err.println("Could not find the image directory " + path);
				return NULL;
			}
			else return BLANK;
		}
		
		return image;
	}
	
	public static BufferedImage copy(BufferedImage source) {
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return b;
	}
	
	public static boolean compare(BufferedImage imgA, BufferedImage imgB) {
		// The images must be the same size.
		if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight())
			return false;

		int width  = imgA.getWidth();
		int height = imgA.getHeight();

		//loop over every pixel.
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Compare the pixels for equality.
				if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static char[][] binarize(String path) {
		return binarize(load(path));
	}
	
	public static char[][] binarize(BufferedImage image) {
		char[][] binaryImg = new char[image.getHeight()][image.getWidth()];
		Color tempColor;
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				tempColor = new Color(image.getRGB(x, y));
				binaryImg[y][x] = (tempColor.getRed() > 0) ? '0' : '1';
			}
		}
		return binaryImg;
	}
	
	public static String blankPath() { return "/sheets/blank.png"; }
	public static boolean isBlank(BufferedImage image) { return image == BLANK; }
}