package morlivm.system.UI;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import morlivm.main.Game;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Physics;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Button implements Graphable
{
	public static enum Action {
		CLICK, PRESS;
	}
	
	private Point point;
	private Dimension dim;
	private BufferedImage current, def, savedDef, focus, alternative;
	private Animation animation;
	private Collider collider;
	private boolean animate, hovers, request, alt;
	private MusicBox musicBox;
	
	public Button(String defImg, String fcsImg, String altImg, Point point, Dimension dim) {
		this.animate = false;
		this.def = ImageHandler.load(defImg);
		this.focus = (fcsImg != null) ? ImageHandler.load(fcsImg) : null;
		this.alternative = (altImg != null) ? ImageHandler.load(altImg) : null;
		this.current = def;
		this.point = new Point(point);
		this.dim = new Dimension(dim);
		this.musicBox = new MusicBox();
		this.collider = new Collider(point, dim);
		
		musicBox.put(new Tune("hover", "/Sound/Main/SFX/ButtonHover.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.put(new Tune("click", "/Sound/Main/SFX/ButtonClick.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.export();
	}
	
	public Button(Animation animation, Point point, Dimension dim) {
		this.animate = true;
		this.animation = animation;
		this.current = animation.grabSprite();
		this.point = new Point(point);
		this.dim = new Dimension(dim);
		this.musicBox = new MusicBox();
		this.collider = new Collider(point, dim);
		
		musicBox.put(new Tune("hover", "/Sound/Main/SFX/ButtonHover.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.put(new Tune("click", "/Sound/Main/SFX/ButtonClick.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.export();
	}
	
	public void update(double delta) {}
	
	public void render(ExtendedGraphics2D g) {
		g.drawImage(current, (int) getX(), (int) getY(), dim.width, dim.height, null);
	}
	
	public void focus(boolean flag) {
		if (animate) {
			if (flag) {
				handleAnimationState();
				current = animation.grabSprite();
			}
		}
		else {
			if (focus == null || alt) return;
			
			if (!isFocused() && flag) {
				current = focus;
				musicBox.play("hover");
			}
			else if (isFocused() && !flag) {
				if (alternative != null && def == alternative) current = alternative;
				else current = def;
			}
		}
	}
	
	public void change(boolean flag) {
		if (alternative == null) return;
		
		if (!isChanged() && flag) {
			savedDef = ImageHandler.copy(def);
			def = alternative;
			current = alternative;
		}
		else if (isChanged() && !flag) {
			def = ImageHandler.copy(savedDef);
			current = def;
		}
		
		alt = flag;
	}
	
	public void setDefaultImage(BufferedImage img) {
		def = img;
		current = def;
	}
	
	public void setX(double x) {
		point.setX(x);
		collider.setX(x);
	}
	
	public void setY(double y) {
		point.setY(y);
		collider.setY(y);
	}
	
	public boolean attend(Action action, Physics.Vector side) {
		boolean act;
		
		switch (action) {
			case CLICK: act = Game.getMouseInput().click(side); break;
			case PRESS: act = Game.getMouseInput().press(side); break;
			default: act = false;
		}
		
		hovers = Game.getMouseInput().hover(toString(), collider, Cursor.POINTER, Cursor.CLICKER);
		
		if (hovers) {
			focus(true);
			if (act) request = true;
		}
		else focus(false);
		
		if (request) {
			request = false;
			if (action == Action.CLICK) musicBox.play("click");
			return true;
		}
		else return false;
	}
	
	private void handleAnimationState() {
		int[] bndry = animation.getSprite().getBoundaries();
		
		if (animation.lastTick()) animation.setRow(animation.getRow() + 1, true);
		if (animation.getRow() > bndry.length - 1) animation.setRow(1, true);
	}
	
	public boolean isHovering() { return hovers; }
	public boolean isChanged() { return alt; }
	public boolean isFocused() { return current == focus; } 
	public void setPointerImage(BufferedImage img) { focus = img; }
	public void setDimension(Dimension d) { dim = new Dimension(d); }
	public Dimension getDimension() { return dim; }
	public Point getPoint() { return point; }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public Image getImage() { return current; }
	public int getWidth() { return (int)dim.getWidth(); }
	public int getHeight() { return (int)dim.getHeight(); }
	public Collider getCollider() { return collider; }
}