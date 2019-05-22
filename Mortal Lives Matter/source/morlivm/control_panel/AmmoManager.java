package morlivm.control_panel;
import java.awt.Image;
import java.util.List;

import morlivm.content.mortal.Player;
import morlivm.database.MortalData;
import morlivm.database.ProjectileData;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.graphics.ImageHandler;
import morlivm.user_input.KeyProcessor;
import morlivm.warfare.Arsenal;
import morlivm.warfare.Attack;
import morlivm.warfare.sight.SightInventory;

public class AmmoManager implements GraphicsManager
{
	private final static int SLOTS = 5;
	private final static int SLOT_INIT_X = Game.WIDTH / 2 - 140;
	private final static int SLOT_INIT_Y = 27;

	private Ammo[] slots;
	private boolean[] activity;
	private Image[] numeralOrders;
	private Arsenal arsenal;
	private ProjectileData[] projectiles;
	private Magazine magazine;
	private SightInventory sightInv;
	private Player player;
	private Point point;
	private Image UI;
	private ControlPanel controlPanel;
	
	public AmmoManager(Player player, SightInventory sightInv, ControlPanel cp) {
		this.player = player;
		this.sightInv = sightInv;
		this.controlPanel = cp;
		this.arsenal = ((MortalData) player.getDatabase()).arsenal;
		this.projectiles = ((MortalData) player.getDatabase()).projectiles;
		this.sightInv.equip(arsenal.generate(Attack.Combat.PROJECTILE, Player.State.ATTACK_1).getProjDatabase().sight);
		this.slots = new Ammo[SLOTS];
		this.activity = new boolean[SLOTS];
		this.numeralOrders = new Image[5];
		this.initNumeralOrders();
		this.UI = ImageHandler.load("/control_panel/ammo/template.png");
		this.point = new Point(Game.WIDTH / 2 - UI.getWidth(null) / 2, 0);
		
		add(projectiles[0]); //primary weapon
		relocate(Structure.atBossMap());
		activate(0);
	}
	
	private void initNumeralOrders() {
		String num;
		
		for (int i = 0; i < SLOTS; i++) {
			num = "" + (i + 1);
			numeralOrders[i] = ImageHandler.load("/control_panel/ammo/" + num + ".png");
		}
	}
	
	public void update(double delta) {
		KeyProcessor.Key k = null;
		
		for (int i = 0; i < SLOTS; i++) {
			if (!isOccupied(i)) continue;
			
			else {
				switch(i + 1) {
					case 1: k = KeyProcessor.Key.AMMO_1; break;
					case 2: k = KeyProcessor.Key.AMMO_2; break;
					case 3: k = KeyProcessor.Key.AMMO_3; break;
					case 4: k = KeyProcessor.Key.AMMO_4; break;
					case 5: k = KeyProcessor.Key.AMMO_5; break;
				}
				
				if (KeyProcessor.isDown(k) && isOccupied(i)) {
					activate(i);
					magazine.changeWeapon(projectiles[player.selectAmmo(slots[i])].magazine);
					sightInv.equip(projectiles[player.selectAmmo(slots[i])].sight);
				}
			}
		}
		
		for (int i = 0; i < SLOTS; i++)
			if (isOccupied(i)) slots[i].update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		g.drawImage(UI, (int) point.getX(), (int) point.getY(), UI.getWidth(null), UI.getHeight(null), null);
		
		for (int i = 0; i < SLOTS; i++) {
			if (isActive(i)) {
				g.drawImage(numeralOrders[i],
						   (int) point.getX(),
						   (int) point.getY(),
						   UI.getWidth(null),
						   UI.getHeight(null), null);
			}
			
			if (isOccupied(i)) slots[i].render(g);
		}
	}
	
	public void add(ProjectileData projDataBase) {
		if (exists(projDataBase.name)) {
			getAmmo(projDataBase.name).increase();
			return;
		}
		
		int unusedSlot = getUnusedSlot();
		
		if (getUnusedSlot() == -1) return;
		else slots[unusedSlot] = new Ammo(projDataBase, locateSlot(unusedSlot), this);
	}
	
	public void remove(Ammo ammo) {
		for (int i = 0; i < SLOTS; i++)
			if (slots[i] == ammo) slots[i] = null;
	}
	
	private boolean exists(String ammoType) {
		for (int i = 0; i < SLOTS; i++)
			if (isOccupied(i) && slots[i].getProjData().name.equals(ammoType)) return true;
		
		return false;
	}
	
	private int getUnusedSlot() {
		for (int i = 0; i < SLOTS; i++)
			if (slots[i] == null) return i;
		return -1;
	}
	
	private Point locateSlot(int slot) {
		int location = (Structure.atBossMap()) ? Game.WIDTH - UI.getWidth(null) + 100 : SLOT_INIT_X;
		return new Point(location + (Ammo.DIM.width + 7) * slot, SLOT_INIT_Y);
	}

	public boolean isEmpty() {
		for (int i = 0; i < SLOTS; i++)
			if (slots[i] != null) return false;
		return true;
	}
	
	private Ammo getAmmo(String type) {
		for (int i = 0; i < SLOTS; i++) {
			if (slots[i] == null) continue;
			if (slots[i].getProjData().name.equals(type)) return slots[i];
		}
		return null;
	}
	
	public void activate(int slot) {
		for (int i = 0; i < SLOTS; i++) {
			activity[i] = (i == slot);
			if (slots[i] != null) slots[i].select(activity[i]);
		}
		player.setActiveProj(getActiveAmmo().getProjData());
	}
	
	public int getActiveSlot() {
		for (int i = 0; i < SLOTS; i++)
			if (isActive(i)) return i;
		return -1; //formal return statement
	}
	
	public void relocate(boolean boss) {
		int divisor = (boss) ? 1 : 2;
		point.setX(Game.WIDTH / divisor - UI.getWidth(null) / divisor);
		for (int i = 0; i < slots.length; i++)
			if (slots[i] != null) slots[i].setPoint(locateSlot(i));
	}
	
	public void reset() {
		activate(0);
		magazine.changeWeapon(projectiles[0].magazine);
		sightInv.equip(projectiles[0].sight);
	}
	
	public void insertMagazine(Magazine m) { magazine = m; }
	public Ammo getActiveAmmo() { return slots[getActiveSlot()]; }
	public void use() { slots[getActiveSlot()].useOne(); }
	private boolean isOccupied(int slot) { return slots[slot] != null; }
	public boolean isActive(int slot) { return activity[slot]; }
	public ControlPanel getControlPanel() { return controlPanel; }
	public List<Graphable> getList() { return null; }
}