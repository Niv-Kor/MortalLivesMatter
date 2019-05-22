package morlivm.system.graphics.sheet;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import morlivm.system.graphics.ImageHandler;
import morlivm.system.performance.FPS;

public class Sprite extends Sheet
{
	private final static int MIN_BOUNDARY_SAMPLES = 5;
	private final static int MAX_ALLOWED_SAMPLES = 10;
	
	private BufferedImage[][] sprites;
	private int[] boundaries;
	private int speed;
	
	public Sprite(String path, Dimension grid, double speed) {
		this.path = new String(path);
		this.dim = new Dimension(grid);
		this.speed = FPS.toFrames(speed);
		
		BufferedImage spriteImage = ImageHandler.load(path);
		int width = grid.width;
		int height = grid.height;
		int rows = (int) Math.round(spriteImage.getHeight() / height);
		int cols = (int) Math.round(spriteImage.getWidth() / width);
		
		this.sprites = new BufferedImage[rows][cols];
		this.boundaries = assembleBoundaries(spriteImage, rows, cols, MIN_BOUNDARY_SAMPLES);
		
		for (int r = 1; r < sprites.length + 1; r++) {
			for (int c = 1; c < sprites[r - 1].length + 1; c++) {
				if (boundaries == null || boundaries[r - 1] >= c) {
					sprites[r - 1][c - 1] = spriteImage.getSubimage
					(c * width - width, r * height - height, width - 1, height -1);
				}
			}
		}
	}
	
	public Sprite(String path) {
		this.path = new String(path);
		initOneImageSprite(ImageHandler.load(path));
	}
	
	public Sprite(BufferedImage image) {
		this.path = new String("UNKNOWN PATH");
		initOneImageSprite(image);
	}
	
	public Sprite(Sprite other) {
		this.path = new String(other.path);
		this.boundaries = other.boundaries;
		this.dim = new Dimension(other.dim);
		this.speed = other.speed;
		this.sprites = new BufferedImage[other.sprites.length][other.sprites[0].length];
		
		for (int i = 0; i < other.sprites.length; i++)
			for (int j = 0; j < other.sprites[i].length; j++)
				if (other.sprites[i][j] != null)
					this.sprites[i][j] = ImageHandler.copy(other.sprites[i][j]);
	}
	
	private void initOneImageSprite(BufferedImage image) {
		this.dim = new Dimension(image.getWidth(), image.getHeight());
		this.sprites = new BufferedImage[1][1];
		sprites[0][0] = image;
		this.boundaries = new int[0];
		this.speed = 0;
	}
	
	private int[] assembleBoundaries(BufferedImage image, int rows, int cols, int samples) {
		int[] boundaries = new int[rows];
		
		int divisionY = (int) (dim.height / samples);
		int divisionX = (int) (dim.width / samples);
		int inGridX, inGridY;
		boolean found;
		
		for (int i = 0; i < rows; i++) { //rows
			found = false;
			for (int j = cols - 1; j >= 0; j--) { //columns
				for (int sampleRow = 1; sampleRow < samples; sampleRow++) { //y of grid
					for (int sampleCol = 1; sampleCol < samples; sampleCol++) { //x of grid
						inGridY = dim.height * i + divisionY * sampleRow;
						inGridX = dim.width * j + divisionX * sampleCol;
						
						//check if point on grid detected an alpha != 0
						if (((image.getRGB(inGridX, inGridY) >> 24) & 0xFF) != 0) {
							boundaries[i] += j + 1;
							found = true;
							break; //all the way to the next row
						}
					}
					if (found) break; //next row
				}
				if (found) break; //next row
			}
		}
		
		//check if boundaries are empty and run again, more precisely, if so
		int gridSum = 0;
		for (int i = 0; i < rows; i++) gridSum += boundaries[i];
		
		if (gridSum == 0) {
			if (samples < MAX_ALLOWED_SAMPLES) return assembleBoundaries(image, rows, cols, samples++);
			else System.err.println("The sprite sheet " + path + " seems empty.");
		}

		return boundaries;
	}
	
	public BufferedImage grabSprite(int col, int row) { return sprites[row - 1][col - 1]; }
	public int[] getBoundaries() { return boundaries; }
	public int getRows() { return sprites.length; }
	public int getCols() { return sprites[0].length; }
	public int getRunningSpeed() { return speed; }
}