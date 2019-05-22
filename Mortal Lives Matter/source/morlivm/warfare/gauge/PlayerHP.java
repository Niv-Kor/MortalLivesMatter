package morlivm.warfare.gauge;
import java.awt.Color;
import java.awt.Dimension;

import morlivm.content.mortal.Player;
import morlivm.control_panel.ControlPanel;
import morlivm.control_panel.SaleAd;
import morlivm.system.UI.Point;

public class PlayerHP extends StaticHealthBar
{
	public PlayerHP(Player player, int health) {
		super(player, health, true);
		
		this.font = LARGE_POINTS_FONT;
		this.shadowColor = new Color(147, 28, 28, 170);
		this.normalColor = new Color(185, 12, 41, 170);
		this.blinkColor = new Color(214, 91, 111, 170);
		init();
	}
	
	protected void init() {
		draft[0][0][0] = 676;
		draft[0][0][1] = 1015;
		draft[0][0][2] = 993;
		draft[0][0][3] = 669;
		draft[0][0][4] = 676;
		
		draft[0][1][0] = 827;
		draft[0][1][1] = 827;
		draft[0][1][2] = 841;
		draft[0][1][3] = 841;
		draft[0][1][4] = 827;
		
		draft[1][0][0] = 669;
		draft[1][0][1] = 993;
		draft[1][0][2] = 973;
		draft[1][0][3] = 662;
		draft[1][0][4] = 669;
		
		draft[1][1][0] = 841;
		draft[1][1][1] = 841;
		draft[1][1][2] = 855;
		draft[1][1][3] = 855;
		draft[1][1][4] = 841;
		
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 2; j++)
				for (int k = 0; k < POLY_SIZE; k++)
					origin[i][j][k] = draft[i][j][k];
		
		remainder[0] = draft[0][0][2] - draft[0][0][1];
		remainder[1] = draft[1][0][2] - draft[1][0][1];
		highFramePoint = draft[0][0][1];
		lowFramePoint = draft[1][0][3];
		frameWidth = highFramePoint - lowFramePoint;
		barWidth = highFramePoint;
	}
	
	public boolean decrease(double p) {
		if (super.decrease(p)) {
			flicker();
			return true;
		}
		return false;
	}
	
	public void connectRulerPanel(ControlPanel cp) {
		super.connectRulerPanel(cp);
		buy = new SaleAd(SaleAd.Product.HP, this, new Point(origin[1][0][3], origin[0][1][0]), controlPanel);
	}
	
	public String getDeclineErrorMessage() { return "HP bar is already full."; }
	public String getAdDescription() { return "full HP"; }
	public Dimension getAdDimension() { return new Dimension(355, 30); }
}