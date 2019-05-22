package morlivm.system.graphics;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import morlivm.main.Game;
import morlivm.system.UI.Point;
import morlivm.system.math.Percent;

public class ExtendedGraphics2D extends Graphics
{
	private Composite originComposite;
	private Graphics2D g;
	
	public ExtendedGraphics2D(Graphics2D g) {
		this.g = g;
	}
	
	public void drawOutlineString(String str, Point point, int stroke, Color inner, Color outline) {
		int x = (int) Math.round(point.getX());
		int y = (int) Math.round(point.getY());
		
		if (stroke == 0) {
			g.setColor(inner);
			g.drawString(str, x, y);
		}
		else {
			g.setColor(outline);
			g.drawString(str, x - stroke, y - stroke);
			g.drawString(str, x - stroke, y + stroke);
			g.drawString(str, x + stroke, y - stroke);
			g.drawString(str, x + stroke, y + stroke);
			g.setColor(inner);
			g.drawString(str, x, y);
		}
	}
	
	public void drawImage(Image image, Point point, Dimension dim, int transparency) {
		transparency = Percent.limit(0, 0xFF, transparency);
		Dimension d = (dim != null) ? dim : new Dimension(image.getWidth(null), image.getHeight(null));
		float trans = (float) (Percent.numOfNum(transparency, 0xFF) / 100);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, trans);
		originComposite = g.getComposite();
		g.setComposite(ac);
		g.drawImage(image, (int) Math.round(point.getX()), (int) Math.round(point.getY()), d.width, d.height, null);
		g.setComposite(originComposite);
	}
	
	public void castRectShadow(Point point, Dimension dim) {
		int x = (int) Math.round(point.getX());
		int y = (int) Math.round(point.getY());
		
		for (int i = 1, transparency = 70; i <= 13; i++, transparency -= 5) {
			g.setColor(new Color(30, 30, 30, transparency));
			g.drawLine(x + i, y + dim.height + i, x + dim.width + i, y + dim.height + i);
			g.drawLine(x + dim.width + i, y + i, x + dim.width + i, y + dim.height + i);
		}
	}

	public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		g.drawArc(arg0, arg1, arg2, arg3, arg4, arg5);
	}
	
	public void fillPoint(Point p) {
		Color fill = g.getColor();
		Color dot = (fill != Color.RED) ? Color.RED : Color.WHITE;
		Color outline = (fill != Color.BLACK) ? Color.BLACK : Color.WHITE;
		
		g.fillOval((int) p.getX() - 5, (int) p.getY() - 5, 10, 10);
		
		g.setColor(dot);
		g.fillOval((int) p.getX(), (int) p.getY(), 2, 2);
		
		g.setColor(outline);
		g.drawOval((int) p.getX() - 5, (int) p.getY() - 5, 10, 10);
		
		g.setColor(fill);
	}
	
	public void drawLine(double y) {
		g.drawLine(0, (int) y, Game.WIDTH, (int) y);
	}

	public void drawImage(Image arg0, Point arg1, Dimension arg2) {
		drawImage(arg0, arg1, arg2, 0xFF);
	}
	
	public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
		return g.drawImage(arg0, arg1, arg2, arg3);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3, ImageObserver arg4) {
		return g.drawImage(arg0, arg1, arg2, arg3, arg4);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, ImageObserver arg5) {
		return g.drawImage(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, Color arg5, ImageObserver arg6) {
		return g.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
							 int arg6, int arg7, int arg8, ImageObserver arg9) {
		return g.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
							 int arg6, int arg7, int arg8, Color arg9, ImageObserver arg10) {
		return g.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
	}

	public void drawLine(int arg0, int arg1, int arg2, int arg3) {
		g.drawLine(arg0, arg1, arg2, arg3);
	}

	public void drawOval(int arg0, int arg1, int arg2, int arg3) {
		g.drawOval(arg0, arg1, arg2, arg3);
	}

	public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
		g.drawPolygon(arg0, arg1, arg2);
	}

	public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
		g.drawPolyline(arg0, arg1, arg2);
	}

	public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		g.drawRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		g.fillArc(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void fillOval(int arg0, int arg1, int arg2, int arg3) {
		g.fillOval(arg0, arg1, arg2, arg3);
	}

	public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
		g.fillPolygon(arg0, arg1, arg2);
	}

	public void fillRect(int arg0, int arg1, int arg2, int arg3) {
		g.fillRect(arg0, arg1, arg2, arg3);
	}

	public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		g.fillRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
	}
	
	public boolean drawImage(Image arg0, AffineTransform arg1, ImageObserver arg2) {
		return g.drawImage(arg0, arg1, arg2);
	}

	public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2, int arg3) {
		g.drawImage(arg0, arg1, arg2, arg3);
	}
	
	public void clearRect(int arg0, int arg1, int arg2, int arg3) {
		g.clearRect(arg0, arg1, arg2, arg3);
	}

	public void clipRect(int arg0, int arg1, int arg2, int arg3) {
		g.clipRect(arg0, arg1, arg2, arg3);
	}

	public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		g.copyArea(arg0, arg1, arg2, arg3, arg4, arg5);
	}
	
	public void addRenderingHints(Map<?, ?> arg0) { g.addRenderingHints(arg0); }
	public void clip(Shape arg0) { g.clip(arg0); }
	public void draw(Shape arg0) { g.draw(arg0); }
	public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2) { g.drawGlyphVector(arg0, arg1, arg2); }
	public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1) { g.drawRenderableImage(arg0, arg1); }
	public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1) { g.drawRenderedImage(arg0, arg1); }
	public void drawString(String arg0, Point arg1) { g.drawString(arg0, (int) arg1.getX(), (int) arg1.getY()); }
	public void drawString(String arg0, int arg1, int arg2) { g.drawString(arg0, arg1, arg2); }
	public void drawString(String arg0, float arg1, float arg2) { g.drawString(arg0, arg1, arg2); }
	public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) { g.drawString(arg0, arg1, arg2); }
	public void drawString(AttributedCharacterIterator arg0, float arg1, float arg2) { g.drawString(arg0, arg1, arg2); }
	public void fill(Shape arg0) { g.fill(arg0); }
	public Color getBackground() { return g.getBackground(); }
	public Composite getComposite() { return g.getComposite(); }
	public GraphicsConfiguration getDeviceConfiguration() { return g.getDeviceConfiguration(); }
	public FontRenderContext getFontRenderContext() { return g.getFontRenderContext(); }
	public Paint getPaint() { return g.getPaint(); }
	public Object getRenderingHint(Key arg0) { return g.getRenderingHint(arg0); }
	public RenderingHints getRenderingHints() {	return g.getRenderingHints(); }
	public Stroke getStroke() {	return g.getStroke(); }
	public AffineTransform getTransform() { return g.getTransform(); }
	public boolean hit(Rectangle arg0, Shape arg1, boolean arg2) { return g.hit(arg0, arg1, arg2); }
	public void rotate(double arg0) { g.rotate(arg0); }
	public void rotate(double arg0, double arg1, double arg2) { g.rotate(arg0, arg1, arg2); }
	public void scale(double arg0, double arg1) { g.scale(arg0, arg1); }
	public void setBackground(Color arg0) { g.setBackground(arg0); }
	public void setComposite(Composite arg0) { g.setComposite(arg0); }
	public void setPaint(Paint arg0) { g.setPaint(arg0); }
	public void setRenderingHint(Key arg0, Object arg1) { g.setRenderingHint(arg0, arg1); }
	public void setRenderingHints(Map<?, ?> arg0) {	g.setRenderingHints(arg0); }
	public void setStroke(Stroke arg0) { g.setStroke(arg0); }
	public void setTransform(AffineTransform arg0) { g.setTransform(arg0); }
	public void shear(double arg0, double arg1) { g.shear(arg0, arg1); }
	public void transform(AffineTransform arg0) { g.transform(arg0); }
	public void translate(int arg0, int arg1) { g.translate(arg0, arg1); }
	public void translate(double arg0, double arg1) { g.translate(arg0, arg1); }
	public Graphics create() { return g.create(); }
	public Shape getClip() { return g.getClip(); }
	public Rectangle getClipBounds() { return g.getClipBounds(); }
	public Color getColor() { return g.getColor(); }
	public Font getFont() { return g.getFont(); }
	public FontMetrics getFontMetrics(Font arg0) { return g.getFontMetrics(); }
	public void setClip(Shape arg0) { g.setClip(arg0); }
	public void setClip(int arg0, int arg1, int arg2, int arg3) { g.setClip(arg0, arg1, arg2, arg3); }
	public void setColor(Color arg0) { g.setColor(arg0); }
	public void setFont(Font arg0) { g.setFont(arg0); }
	public void setPaintMode() { g.setPaintMode(); }
	public void setXORMode(Color arg0) { g.setXORMode(arg0); }
	public void dispose() { g.dispose(); }
}