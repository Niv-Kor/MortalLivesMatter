package morlivm.user_input;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import morlivm.memory.Memory;
import morlivm.memory.StaticSaveable;

public class KeyProcessor implements StaticSaveable
{
	public static enum Key {
		UP(KeyEvent.VK_W),
		DOWN(KeyEvent.VK_S),
		LEFT(KeyEvent.VK_A),
		RIGHT(KeyEvent.VK_D),
		RUN(KeyEvent.VK_SHIFT),
		JUMP(KeyEvent.VK_SPACE),
		ATTACK(KeyEvent.VK_CONTROL),
		STAB(KeyEvent.VK_F),
		PUNCH(KeyEvent.VK_E),
		RELOAD(KeyEvent.VK_CAPS_LOCK),
		AMMO_1(KeyEvent.VK_1),
		AMMO_2(KeyEvent.VK_2),
		AMMO_3(KeyEvent.VK_3),
		AMMO_4(KeyEvent.VK_4),
		AMMO_5(KeyEvent.VK_5),
		PORTAL(KeyEvent.VK_ENTER),
		PAUSE(KeyEvent.VK_ESCAPE);
		
		public int def;
		public int binding;
		public boolean prevState, state;
		
		private Key(int binding) {
			this.def = binding;
			this.binding = binding;
		}
		
		public void bind(int key) {
			binding = key;
			save();
		}
		
		public void press(boolean flag) { state = flag;	}
		public void update() { prevState = state; }
		public void setDefault() { bind(def); }
	}
	
	public final static int NAN = -1;
	
	private static List<Key> keys = Arrays.asList(Key.values());
	
	public static void keySet(Key k, boolean flag) {
		if (k == null) return;
		k.state = flag;
	}
	
	public static Key findKey(int keyCode) {
		Key temp = null;
		
		for (int i = 0; i < keys.size(); i++) {
			temp = keys.get(i);
			if (temp.binding == keyCode) return temp;
		}
		
		return null;
	}
	
	public static void update(double delta) {
		for(int i = 0; i < keys.size(); i++)
			keys.get(i).update();
	}
	
	public static boolean isPressed(Key k) {
		return k.state && !k.prevState;
	}
	
	public static boolean isDown(Key k) {
		return k.state;
	}
	
	public static boolean anyKeyPressed() {
		for (int i = 0; i < keys.size(); i++)
			if (keys.get(i).state) return true;
		return false;
	}
	
	public static void restoreDefaultBindings() {
		for (int i = 0; i < keys.size(); i++)
			keys.get(i).setDefault();
	}
	
	public static void release() {
		for (int i = 0; i < keys.size(); i++)
			keys.get(i).state = false;
	}
	
	public static void save() {
		String temp = new String();
		
		for (int i = 0; i < keys.size(); i++)
			temp = temp.concat(keys.get(i).binding + " ");
		
		Memory.save(temp, Memory.Element.KEYSETS);
	}
	
	public static void load() {
		String temp = Memory.loadString(Memory.Element.KEYSETS);
		
		for (int i = 0; i < keys.size(); i++) {
			String temp2;
			try { temp2 = new String(temp); }
			catch (NullPointerException e) { temp2 = ""; }
			
			for (int j = 0, counter = 0; j < temp2.length(); j++) {
				if (temp2.charAt(j) == ' ') counter++;
				else if (counter == i) {
					int digits = 1;
					while (temp2.charAt(j + digits) != ' ') digits++;
					temp2 = temp2.substring(j, j + digits);
					keys.get(i).bind(Integer.parseInt(temp2));
					break;
				}
			}
		}
	}
}