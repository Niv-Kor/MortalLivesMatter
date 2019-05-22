package morlivm.control_panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.performance.TimingDevice;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;
import morlivm.user_input.KeyProcessor;

public class Magazine implements GraphicsManager
{
	private final static Font FONT = new Font("ariel", Font.BOLD, 12);
	private final static Dimension DIM = new Dimension(8, 25);
	private final static Color WARINIG_COLOR = new Color(255, 223, 72);
	private final static Color WARNING_OUTLINE = new Color(176, 112, 0, 200);
	private final static double RELOADING_TIME = 1;
	private final static int INIT_Y = 110;
	private final static int SPACE = 5;
	
	private int slots, bullets, initX;
	private AmmoManager ammoMngr;
	private Ammo ammo;
	private boolean reloading;
	private MusicBox musicBox;
	private TimingDevice timingDevice;
	
	public Magazine(int slots, AmmoManager ammoMngr) {
		this.slots = slots;
		this.ammoMngr = ammoMngr;
		this.ammo = ammoMngr.getActiveAmmo();
		this.timingDevice = new TimingDevice();
		timingDevice.addTimer("reload", RELOADING_TIME);
		this.musicBox = new MusicBox();
		manageBullets();
		
		relocate(Structure.atBossMap());
		musicBox.put(new Tune("reload", "/Sound/Main/SFX/Reload.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.export();
	}

	public void update(double delta) {
		if (slots <= 1) return;
		timingDevice.setClocks();
		
		if (reloading) {
			if (timingDevice.getTimer("reload").progressedToRoof()) {
				reloading = false;
				timingDevice.getTimer("reload").init();
			}
		}
		
		if (KeyProcessor.isDown(KeyProcessor.Key.RELOAD) && !isReloading()) reload();
	}

	public void render(ExtendedGraphics2D g) {
		if (slots <= 1) return;
		
		Point p;
		
		g.setColor(new Color(70, 70, 70, 90));
		for (int i = 0; i < slots; i++)
			g.drawRect(initX + i * (DIM.width + SPACE), INIT_Y, DIM.width, DIM.height);
		
		g.setColor(new Color(255, 255, 255, 180));
		for (int i = 0; i < bullets; i++)
			g.fillRect(initX + i * (DIM.width + SPACE), INIT_Y, DIM.width, DIM.height);
		
		if (!isFull()) {
			int x = Structure.atBossMap() ? Game.WIDTH - 329 : Game.WIDTH / 2 - 90;
			p = new Point(x, INIT_Y + DIM.height + 20);
			
			String warning = new String("PRESS CAPS-LOCK TO RELOAD");
			g.setFont(FONT);
			if (!isEmpty()) {
				g.setColor(Color.WHITE);
				g.drawString(warning, (int) p.getX(), (int) p.getY());
			}
			else {
				g.setColor(new Color(255, 223, 72));
				g.drawOutlineString(warning, p, 1, WARINIG_COLOR, WARNING_OUTLINE);
			}
		}
	}
	
	public boolean useOne() {
		if (slots <= 1) return true;
		if (isReloading()) return false;
		
		if (--bullets >= 0) return true;
		else {
			bullets = 0;
			return false;
		}
	}
	
	public void reload() {
		if (isReloading()) return; 
		musicBox.play("reload");
		reloading = true;
		manageBullets();
	}
	
	public void changeWeapon(int s) {
		ammo = ammoMngr.getActiveAmmo();
		slots = s;
		manageBullets();
		reload();
		relocate(Structure.atBossMap());
	}
	
	private void manageBullets() {
		int amount = ammo.getAmount();
		if (slots <= amount || amount == -1) bullets = slots;
		else bullets = amount;
	}
	
	public void relocate(boolean boss) {
		int length = slots * DIM.width + (slots - 1) * SPACE;
		initX = (boss) ? Game.WIDTH - 239 - length / 2 : Game.WIDTH / 2 - length / 2;
	}
	
	public int getSlots() { return slots; }
	public int getBullets() { return bullets; }
	public boolean isReloading() { return reloading; }
	public boolean isEmpty() { return bullets == 0 || isReloading(); }
	public boolean isFull() { return bullets == slots; }
	public void terminate() { musicBox.requestRemovalAll(); }
	public List<Graphable> getList() { return null; }
}