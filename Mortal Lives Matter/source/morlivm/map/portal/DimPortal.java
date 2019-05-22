package morlivm.map.portal;
import java.awt.Dimension;

import morlivm.content.mortal.Player;
import morlivm.control_panel.Megaphone;
import morlivm.database.DataManager;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Percent;
import morlivm.user_input.KeyProcessor;

public class DimPortal extends Portal
{
	private final static Pamphlet SS = DataManager.retSheet("ss$g$dimensional_portal");
	
	private int state;
	private boolean ready;
	
	public DimPortal(GameState gs, Player player, Point point) {
		super(gs, player);
		
		this.animation = new Animation(SS);
		this.state = 1;
		this.dim = new Dimension(SS.getDimension());
		this.originP = new Point(point);
		this.point = new Point(ground.getX() + point.getX(), point.getY());
		this.dim = SS.getDimension();
	}

	public void update(double delta) {
		super.update(delta);
		if (!open || terminate) return;
		
		ground.setEarthquake(2, false);
		vanish();
		changeSprite();
		updateCoordinates();
	}
	
	private void vanish() {
		if (transparency < 0xFF) return;
		
		Collider body = player.getMass();
		Point playerP = new Point(body.getX() + body.getDimension().width / 2, body.getY());
		Point portalP = new Point(getX() + dim.width / 2, getY() + dim.height / 2);
		double dist = playerP.distance(portalP);
		double distPercent;
		int trans = 0;
		
		if (dist <= dim.width / 2) {
			distPercent = Percent.numOfNum(dist, dim.width / 2);
			trans = (int) Percent.percentOfNum((100 - distPercent), 200);
			Megaphone.announcePerm("Press ENTER to move into the portal.");
			ready = true;
		}
		else {
			Megaphone.dismiss("Press ENTER to move into the portal.");
			ready = false;
		}
			
		player.setTransparency(0xFF - trans);
	}
	
	protected void enter() {
		if (ready && KeyProcessor.isDown(KeyProcessor.Key.PORTAL)) {
			player.setTransparency(0xFF);
			gameState.levelUp();
			terminate();
		}
	}
	
	private void changeSprite() {
		if (animation.lastTick()) {
			if (state < SS.getSprite().getRows() - 1) state++;
			else state = 1;
		}
		
		animation.setRow(state, false);
	}
}