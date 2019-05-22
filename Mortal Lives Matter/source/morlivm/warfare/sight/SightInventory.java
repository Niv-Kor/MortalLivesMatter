package morlivm.warfare.sight;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import morlivm.content.proj.Projectile;
import morlivm.control_panel.AmmoManager;
import morlivm.state.GameState;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.graphics.ImageHandler;
import morlivm.warfare.ContactManager;

public class SightInventory implements GraphicsManager
{
	public static enum Type {
		NONE(-1),
		UNARMED(0),
		REFLECTOR(1),
		SCOPE(2),
		REMOTE_CONTROL(3);
		
		public int num;
		private Type(int num) { this.num = num; }
	}
	
	private Sight currentSight;
	private Type preSight;
	private BufferedImage defImg;
	private Sight[] sights;
	private ContactManager contactMngr;
	private AmmoManager ammoMngr;
	
	public SightInventory(GameState gs) {
		this.contactMngr = gs.getContactManager();
		this.defImg = ImageHandler.load(UnarmedSight.PATH);
		this.sights = new Sight[4];
		sights[Type.UNARMED.num] = new UnarmedSight(this, gs);
		sights[Type.REFLECTOR.num] = new ReflectorSight(this, gs);
		sights[Type.SCOPE.num] = new ScopeSight(this, gs);
		sights[Type.REMOTE_CONTROL.num] = new RemoteControl(this, gs);
		
		equip(Type.UNARMED);
		this.preSight = Type.UNARMED;
	}
	
	public void update(double delta) {
		checkRemoteRequest();
		currentSight.update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		currentSight.render(g);
	}
	
	private void checkRemoteRequest() {
		boolean remote = ammoMngr.getActiveAmmo().getProjData().trigger == Projectile.Trigger.IGNITION;
		
		if (!remote) {
			if (!(currentSight instanceof RemoteControl)) preSight = currentSight.getSightType();
			if (contactMngr.explosiveOnTarget()) equip(Type.REMOTE_CONTROL);
			else equip(preSight);
		}
	}
	
	public void connectAmmoRuler(AmmoManager am) { ammoMngr = am; }
	public void equip(Type type) { currentSight = sights[type.num]; }
	public BufferedImage getDefSightImg() { return defImg; }
	public Sight getSight() { return currentSight; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}