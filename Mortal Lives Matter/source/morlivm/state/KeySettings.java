package morlivm.state;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import morlivm.database.DataManager;
import morlivm.main.Game;
import morlivm.system.UI.Button;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Physics;
import morlivm.user_input.KeyProcessor;

public class KeySettings implements Graphable
{
	private static enum KeySet {
		UP(KeyProcessor.Key.UP),
		DOWN(KeyProcessor.Key.DOWN),
		LEFT(KeyProcessor.Key.LEFT),
		RIGHT(KeyProcessor.Key.RIGHT),
		RUN(KeyProcessor.Key.RUN),
		JUMP(KeyProcessor.Key.JUMP),
		ATTACK(KeyProcessor.Key.ATTACK),
		STAB(KeyProcessor.Key.STAB),
		PUNCH(KeyProcessor.Key.PUNCH),
		RELOAD(KeyProcessor.Key.RELOAD);
		
		public KeyProcessor.Key relatedKey;
		public boolean request;
		public BufferedImage icon;
		
		private KeySet(KeyProcessor.Key relatedKey) {
			this.relatedKey = relatedKey;
			this.icon = ImageHandler.load("/States/Settings/Icons/" + this.name() + ".png");
		}
		
		public void set(int key) { relatedKey.bind(key); }
	}
	
	private final static Font ATTRIBUTE_FONT = new Font("Myriad Pro", Font.BOLD, 20);
	private final static Font KEY_FONT = new Font("Arial", Font.BOLD, 16);
	private final static Color ATTRIBUTE_COLOR = Color.WHITE;
	private final static Color UNUSED_COLOR = new Color(218, 62, 62);
	private final static Color[] KEY_COLOR = {new Color(0, 240, 255, 230), new Color(255, 228, 8, 230)};
	private final static Point KEY_SET_STARTING_POINT = new Point(60, 240);
	private final static Pamphlet RESTORE_SS = DataManager.retSheet("ss$g$restore_button");
	private final static double TEXT_START = KEY_SET_STARTING_POINT.getX() + 150;
	
	private HashMap<Integer, String> keyboard;
	private List<KeySet> keys;
	private Point point;
	private Button[] setButtons, restore;
	private BufferedImage keysetTitle, keysetUI;
	
	public KeySettings() {
		this.keysetTitle = ImageHandler.load("/States/Settings/KeySettingTitle.png");
		this.keysetUI = ImageHandler.load("/States/Settings/KeySet.png");
		this.point = new Point(KEY_SET_STARTING_POINT);
		
		//restore buttons
		restore = new Button[1];
		for (int i = 0; i < restore.length; i++) {
			restore[i] = new Button(new Animation(RESTORE_SS),
									new Point(KEY_SET_STARTING_POINT,
									keysetUI.getWidth() / 2 - RESTORE_SS.getDimension().width / 2,
									keysetUI.getHeight() + 20, 0), RESTORE_SS.getDimension());
		}
		
		this.keys = Arrays.asList(KeySet.values());
		this.setButtons = new Button[keys.size()];
		for (int i = 0; i < setButtons.length; i++) {
			setButtons[i] = new Button("/States/Settings/Set.png",
									   "/States/Settings/SetPtr.png",
									   "/States/Settings/SetClk.png",
									   new Point(TEXT_START - 110, KEY_SET_STARTING_POINT.getY() + 27 + 50 * i),
									   new Dimension(33, 19));
		}
	}
	
	public void init() {
		keyboard = new HashMap<Integer, String>();
		keyboard.put(KeyProcessor.NAN, "");
		keyboard.put(KeyEvent.VK_DEAD_TILDE, "~");
		keyboard.put(KeyEvent.VK_1, "1");
		keyboard.put(KeyEvent.VK_2, "2");
		keyboard.put(KeyEvent.VK_3, "3");
		keyboard.put(KeyEvent.VK_4, "4");
		keyboard.put(KeyEvent.VK_5, "5");
		keyboard.put(KeyEvent.VK_6, "6");
		keyboard.put(KeyEvent.VK_7, "7");
		keyboard.put(KeyEvent.VK_8, "8");
		keyboard.put(KeyEvent.VK_9, "9");
		keyboard.put(KeyEvent.VK_0, "0");
		keyboard.put(KeyEvent.VK_MINUS, "-");
		keyboard.put(KeyEvent.VK_EQUALS, "=");
		keyboard.put(KeyEvent.VK_BACK_SPACE, "BACK SPACE");
		keyboard.put(KeyEvent.VK_TAB, "TAB");
		keyboard.put(KeyEvent.VK_CAPS_LOCK, "CAPS LOCK");
		keyboard.put(KeyEvent.VK_SHIFT, "SHIFT");
		keyboard.put(KeyEvent.VK_CONTROL, "CTRL");
		keyboard.put(KeyEvent.VK_ALT, "ALT");
		keyboard.put(KeyEvent.VK_SPACE, "SPACE");
		keyboard.put(KeyEvent.VK_ENTER, "ENTER");
		keyboard.put(KeyEvent.VK_A, "A");
		keyboard.put(KeyEvent.VK_B, "B");
		keyboard.put(KeyEvent.VK_C, "C");
		keyboard.put(KeyEvent.VK_D, "D");
		keyboard.put(KeyEvent.VK_E, "E");
		keyboard.put(KeyEvent.VK_F, "F");
		keyboard.put(KeyEvent.VK_G, "G");
		keyboard.put(KeyEvent.VK_H, "H");
		keyboard.put(KeyEvent.VK_I, "I");
		keyboard.put(KeyEvent.VK_J, "J");
		keyboard.put(KeyEvent.VK_K, "K");
		keyboard.put(KeyEvent.VK_L, "L");
		keyboard.put(KeyEvent.VK_M, "M");
		keyboard.put(KeyEvent.VK_N, "N");
		keyboard.put(KeyEvent.VK_O, "O");
		keyboard.put(KeyEvent.VK_P, "P");
		keyboard.put(KeyEvent.VK_Q, "Q");
		keyboard.put(KeyEvent.VK_R, "R");
		keyboard.put(KeyEvent.VK_S, "S");
		keyboard.put(KeyEvent.VK_T, "T");
		keyboard.put(KeyEvent.VK_U, "U");
		keyboard.put(KeyEvent.VK_V, "V");
		keyboard.put(KeyEvent.VK_W, "W");
		keyboard.put(KeyEvent.VK_X, "X");
		keyboard.put(KeyEvent.VK_Y, "Y");
		keyboard.put(KeyEvent.VK_Z, "Z");
		keyboard.put(KeyEvent.VK_COMMA, ",");
		keyboard.put(KeyEvent.VK_PERIOD, ".");
		keyboard.put(KeyEvent.VK_SLASH, "/");
		keyboard.put(KeyEvent.VK_SEMICOLON, ";");
		keyboard.put(KeyEvent.VK_OPEN_BRACKET, "[");
		keyboard.put(KeyEvent.VK_CLOSE_BRACKET, "]");
		keyboard.put(KeyEvent.VK_CLOSE_BRACKET, "]");
		keyboard.put(KeyEvent.VK_BACK_SLASH, "\\");
		keyboard.put(KeyEvent.VK_F1, "F1");
		keyboard.put(KeyEvent.VK_F2, "F2");
		keyboard.put(KeyEvent.VK_F3, "F3");
		keyboard.put(KeyEvent.VK_F4, "F4");
		keyboard.put(KeyEvent.VK_F5, "F5");
		keyboard.put(KeyEvent.VK_F6, "F6");
		keyboard.put(KeyEvent.VK_F7, "F7");
		keyboard.put(KeyEvent.VK_F8, "F8");
		keyboard.put(KeyEvent.VK_F9, "F9");
		keyboard.put(KeyEvent.VK_F9, "F9");
		keyboard.put(KeyEvent.VK_F10, "F10");
		keyboard.put(KeyEvent.VK_F11, "F11");
		keyboard.put(KeyEvent.VK_F12, "F12");
		keyboard.put(KeyEvent.VK_ESCAPE, "ESC");
		keyboard.put(KeyEvent.VK_PRINTSCREEN, "PRINT SCREEN");
		keyboard.put(KeyEvent.VK_SCROLL_LOCK, "SCROLL LOCK");
		keyboard.put(KeyEvent.VK_PAUSE, "PAUSE");
		keyboard.put(KeyEvent.VK_INSERT, "INSERT");
		keyboard.put(KeyEvent.VK_HOME, "HOME");
		keyboard.put(KeyEvent.VK_PAGE_UP, "PAGE UP");
		keyboard.put(KeyEvent.VK_PAGE_DOWN, "PAGE DOWN");
		keyboard.put(KeyEvent.VK_END, "END");
		keyboard.put(KeyEvent.VK_DELETE, "DELETE");
		keyboard.put(KeyEvent.VK_UP, "UP KEY");
		keyboard.put(KeyEvent.VK_DOWN, "DOWN KEY");
		keyboard.put(KeyEvent.VK_LEFT, "LEFT KEY");
		keyboard.put(KeyEvent.VK_RIGHT, "RIGHT KEY");
		keyboard.put(KeyEvent.VK_NUM_LOCK, "NUM LOCK");
		keyboard.put(KeyEvent.VK_NUMPAD0, "NUMPAD 0");
		keyboard.put(KeyEvent.VK_NUMPAD1, "NUMPAD 1");
		keyboard.put(KeyEvent.VK_NUMPAD2, "NUMPAD 2");
		keyboard.put(KeyEvent.VK_NUMPAD3, "NUMPAD 3");
		keyboard.put(KeyEvent.VK_NUMPAD4, "NUMPAD 4");
		keyboard.put(KeyEvent.VK_NUMPAD5, "NUMPAD 5");
		keyboard.put(KeyEvent.VK_NUMPAD6, "NUMPAD 6");
		keyboard.put(KeyEvent.VK_NUMPAD7, "NUMPAD 7");
		keyboard.put(KeyEvent.VK_NUMPAD8, "NUMPAD 8");
		keyboard.put(KeyEvent.VK_NUMPAD9, "NUMPAD 9");
	}
	
	public void update(double delta) {
		boolean set;
		KeySet key;
		for (int i = 0; i < setButtons.length; i++) {
			key = keys.get(i);
			set = key.request;
			
			if (setButtons[i].attend(Button.Action.CLICK, Physics.Vector.LEFT)) {
				requestKeySet(key, !set);
				Game.getKeyInput().resetLastKeyPressed();
			}
			setButtons[i].change(set);
			
			int newKey = Game.getKeyInput().getLastKeyPressed();
			if (set && newKey != -1 && isPossibleKey(newKey)) {
				key.set(newKey);
				key.request = false;
				removeDuplicatedOf(key);
			}
			if (set) Game.getKeyInput().resetLastKeyPressed();
		}
		
		for (int i = 0; i < restore.length; i++)
			if (restore[i].attend(Button.Action.CLICK, Physics.Vector.LEFT)) {
				for (int j = 0; j < keys.size(); j++) keys.get(j).relatedKey.setDefault();
				cancelSet();
			}
	}

	public void render(ExtendedGraphics2D g) {
		g.drawImage(keysetTitle, new Point(point, 0, -65, 0), null);
		
		g.drawImage(keysetUI, point, null, 220);
		for (int i = 0; i < restore.length; i++) restore[i].render(g);
		
		KeySet temp;
		for (int i = 0; i < keys.size(); i++) {
			temp = keys.get(i);
			
			setButtons[i].render(g);
			g.drawImage(temp.icon, new Point(TEXT_START - 50, getY() + 20 + 50 * i),
						new Dimension(32, 32), 200);
			
			g.setFont(ATTRIBUTE_FONT);
			g.drawOutlineString(temp.name(), new Point(TEXT_START, getY() + 45 + 50 * i),
								0, getAttributeColor(temp), null);
			
			g.setColor(new Color(255, 255, 255, 170));
			g.drawLine((int) TEXT_START + 100,
					   (int) KEY_SET_STARTING_POINT.getY() + 25,
					   (int) TEXT_START + 100,
					   (int) KEY_SET_STARTING_POINT.getY() + keysetUI.getHeight() - 30);
			
			g.setFont(KEY_FONT);
			g.drawOutlineString(keyboard.get(temp.relatedKey.binding), new Point(TEXT_START + 120,
								getY() + 45 + 50 * i), 0, getKeyColor(temp), null);
		}
	}
	
	private void requestKeySet(KeySet k, boolean flag) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i) == k) keys.get(i).request = flag;
			else keys.get(i).request = false;
		}
	}
	
	private Color getKeyColor(KeySet k) {
		int ind = !k.request ? 0 : 1;
		return KEY_COLOR[ind];
	}
	
	private Color getAttributeColor(KeySet k) {
		return (k.relatedKey.binding != KeyProcessor.NAN) ? ATTRIBUTE_COLOR : UNUSED_COLOR;
	}
	
	private void removeDuplicatedOf(KeySet key) {
		KeySet temp;
		
		for (int i = 0; i < keys.size(); i++) {
			temp = keys.get(i);
			if (temp == key) continue;
			else if (temp.relatedKey.binding == key.relatedKey.binding)
				temp.relatedKey.bind(KeyProcessor.NAN);
		}
	}
	
	private boolean isPossibleKey(int keyCode) {
		String keyDisplay = keyboard.get(keyCode);
		return keyDisplay != null;
	}
	
	public void cancelSet() {
		for (int i = 0; i < setButtons.length; i++) {
			setButtons[i].change(false);
			keys.get(i).request = false;
		}
	}
	
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return null; }
	public Point getPoint() { return point; }
	public void setX(double x) { point.setX(x); }
	public void setY(double y) { point.setY(y); }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
}