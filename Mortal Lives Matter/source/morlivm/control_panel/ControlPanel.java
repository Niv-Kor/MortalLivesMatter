package morlivm.control_panel;
import java.awt.Dimension;
import java.awt.Image;

import morlivm.content.mortal.Player;
import morlivm.database.MortalData;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.state.GameState;
import morlivm.state.Pause;
import morlivm.system.UI.Button;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Physics;
import morlivm.warfare.Attack;
import morlivm.warfare.gauge.PlayerHP;
import morlivm.warfare.gauge.PlayerStamina;
import morlivm.warfare.sight.Sight;
import morlivm.warfare.sight.SightInventory;

public class ControlPanel implements Graphable
{
	private final static Dimension DIM = new Dimension(879, 136);
	
	private Player player;
	private Dimension dimension;
	private Point point;
	private Image base;
	private Button pause;
	private AmmoManager ammoMngr;
	private Magazine magazine;
	private Backpack currency;
	private KeyCounter keyCounter;
	private SightInventory sightInv;
	private PlayerHP hp;
	private PlayerStamina stamina;
	
	public ControlPanel(Player player, GameState gs) {
		this.player = player;
		this.dimension = DIM;
		this.point = new Point(180, Game.HEIGHT - dimension.getHeight() - 20, 0);
		this.base = ImageHandler.load("/control_panel/template.png");
		this.sightInv = new SightInventory(gs);
		this.ammoMngr = new AmmoManager(player, sightInv, this);
		sightInv.connectAmmoRuler(ammoMngr);
		
		this.currency = new Backpack();
		this.keyCounter = new KeyCounter(gs);
		this.hp = (PlayerHP) player.getHP();
		this.stamina = (PlayerStamina) player.getStamina();
		hp.connectRulerPanel(this);
		stamina.connectRulerPanel(this);
		
		int mag = ((MortalData) player.getDatabase()).arsenal.getAll(Attack.Combat.PROJECTILE).get(0).getProjDatabase().magazine;
		this.magazine = new Magazine(mag, ammoMngr);
		ammoMngr.insertMagazine(magazine);
		
		this.pause = new Button("/states/Pause/DefButton.png",
								"/states/Pause/PtrButton.png", null,
								new Point(140, Game.HEIGHT - 98),
								new Dimension(40, 40));
	}
	
	public void update(double delta) {
		hp.update(delta);
		stamina.update(delta);
		player.getElementStack().update(delta);
		currency.update(delta);
		keyCounter.update(delta);
		ammoMngr.update(delta);
		magazine.update(delta);
		sightInv.update(delta);
		
		if (pause.attend(Button.Action.CLICK, Physics.Vector.LEFT)) Pause.pause(true);
	}
	
	public void render(ExtendedGraphics2D g) {
		g.drawImage(base,
				   (int) point.getX(),
				   (int) point.getY(),
				   (int) dimension.getWidth(),
				   (int) dimension.getHeight(), null);

		pause.render(g);
		stamina.render(g);
		hp.render(g);
		player.getElementStack().render(g);
		currency.render(g);
		keyCounter.render(g);
		ammoMngr.render(g);
		magazine.render(g);
		sightInv.render(g);
	}
	
	public void relocate() {
		ammoMngr.relocate(Structure.atBossMap());
		magazine.relocate(Structure.atBossMap());
	}
	
	public PlayerHP getHPBar() { return hp; }
	public PlayerStamina getStaminaBar() { return stamina; }
	public Backpack getCurrency() { return currency; }
	public KeyCounter getKeyCounter() { return keyCounter; }
	public Sight getSight() { return sightInv.getSight(); }
	public AmmoManager getAmmoManager() { return ammoMngr; }
	public Magazine getMagazine() { return magazine; }
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return DIM; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}