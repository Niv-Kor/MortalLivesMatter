package morlivm.warfare.gauge;
import java.awt.Color;
import java.awt.Dimension;

import morlivm.content.mortal.Player;
import morlivm.control_panel.ControlPanel;
import morlivm.control_panel.SaleAd;
import morlivm.system.UI.Point;

public class PlayerStamina extends StaticHealthBar
{
	public PlayerStamina(Player player, int health) {
		super(player, health, false);
		
		this.font = LARGE_POINTS_FONT;
		this.shadowColor = new Color(28, 120, 178, 170);
		this.normalColor = new Color(0, 150, 255, 170);
		init();
	}
	
	protected void init() {
		draft[0][0][0] = 668;
		draft[0][0][1] = 873;
		draft[0][0][2] = 891;
		draft[0][0][3] = 671;
		draft[0][0][4] = 668;
		
		draft[0][1][0] = 790;
		draft[0][1][1] = 790;
		draft[0][1][2] = 805;
		draft[0][1][3] = 805;
		draft[0][1][4] = 790;
		
		draft[1][0][0] = 671;
		draft[1][0][1] = 890;
		draft[1][0][2] = 906;
		draft[1][0][3] = 675;
		draft[1][0][4] = 671;
		
		draft[1][1][0] = 805;
		draft[1][1][1] = 805;
		draft[1][1][2] = 820;
		draft[1][1][3] = 820;
		draft[1][1][4] = 805;
		
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 2; j++)
				for (int k = 0; k < POLY_SIZE; k++)
					origin[i][j][k] = draft[i][j][k];
		
		remainder[0] = draft[1][0][2] - draft[1][0][1];
		remainder[1] = draft[0][0][2] - draft[0][0][1];
		highFramePoint = draft[0][0][1];
		lowFramePoint = draft[0][0][0] - remainder[0] - remainder[1];
		frameWidth = highFramePoint - lowFramePoint;
		barWidth = highFramePoint;
	}
	
	public boolean decrease(double p) {
		if (healthPoints - p < 0) return false;
		else return super.decrease(p);
	}
	
	public void connectRulerPanel(ControlPanel cp) {
		super.connectRulerPanel(cp);
		buy = new SaleAd(SaleAd.Product.STAMINA, this, new Point(origin[0][0][0], origin[0][1][0]), controlPanel);
	}
	
	public String getDeclineErrorMessage() { return "Stamina bar is already full."; }
	public String getAdDescription() { return "full stamina"; }
	public Dimension getAdDimension() { return new Dimension(240, 33); }
}