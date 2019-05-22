package morlivm.warfare.gauge;
import java.awt.Color;
import java.awt.image.BufferedImage;

import morlivm.content.mortal.Boss;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.ImageHandler;

public class BossHP extends StaticHealthBar
{
	private BufferedImage barImg, hpCircleImg, avatar, elements;
	
	public BossHP(Boss boss, int health) {
		super(boss, health, true);
		
		this.font = EXTRA_LARGE_POINTS_FONT;
		this.barImg = ImageHandler.load("/Sprites/Boss/BossHP.png");
		this.hpCircleImg = ImageHandler.load("/Sprites/Boss/BossCircle1.png");
		this.avatar = ImageHandler.load("/Sprites/Boss/Avatars/LavaBoss.png");
		this.elements = ImageHandler.load("/Sprites/Boss/ElementSlots.png");
		this.shadowColor = new Color(227, 173, 10, 230);
		this.normalColor = new Color(255, 210, 0, 230);
		this.point = new Point(121, 34);
		adjustColor();
		init();
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		
		g.drawImage(barImg,
				   (int) point.getX(),
				   (int) point.getY(),
				   barImg.getWidth(null),
				   barImg.getHeight(null), null);
		
		g.drawImage(elements,
				   (int) point.getX() + barImg.getWidth(null) + hpCircleImg.getWidth(null) / 2 - 28,
				   (int) point.getY() + 12,
				   elements.getWidth(null),
				   elements.getHeight(null), null);
		
		g.drawImage(hpCircleImg,
				   (int) point.getX() + barImg.getWidth(null) - 12,
				   (int) point.getY() - 23,
				   hpCircleImg.getWidth(null),
				   hpCircleImg.getHeight(null), null);
		
		g.drawImage(avatar,
				   (int) point.getX() + barImg.getWidth(null) + 1,
				   (int) point.getY() - 11,
				   avatar.getWidth(null),
				   avatar.getHeight(null), null);
	}
	
	protected void init() {
		draft[0][0][0] = 138;
		draft[0][0][1] = 651;
		draft[0][0][2] = 649;
		draft[0][0][3] = 155;
		draft[0][0][4] = 138;
		
		draft[0][1][0] = 45;
		draft[0][1][1] = 45;
		draft[0][1][2] = 65;
		draft[0][1][3] = 65;
		draft[0][1][4] = 45;
		
		draft[1][0][0] = 155;
		draft[1][0][1] = 651;
		draft[1][0][2] = 649;
		draft[1][0][3] = 172;
		draft[1][0][4] = 155;
		
		draft[1][1][0] = 65;
		draft[1][1][1] = 65;
		draft[1][1][2] = 82;
		draft[1][1][3] = 82;
		draft[1][1][4] = 65;
		
		remainder[0] = 0;
		remainder[1] = 0;
		highFramePoint = draft[0][0][1];
		lowFramePoint = draft[0][0][0];
		frameWidth = highFramePoint - lowFramePoint;
		barWidth = draft[0][0][1];
	}
}