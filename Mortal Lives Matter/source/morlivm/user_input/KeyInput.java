package morlivm.user_input;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class KeyInput extends JPanel implements KeyListener
{
	private boolean enabled;
	private int lastKeyPressed;
	
	public KeyInput() {
		super();
		enabled = true;
		lastKeyPressed = -1;
	}
	
	public void addNotify() {
		super.addNotify();
		addKeyListener(this);
		requestFocus();
	}
	
	public void keyPressed(KeyEvent key) {
		if (!enabled) return;
		lastKeyPressed = key.getKeyCode();
		KeyProcessor.keySet(KeyProcessor.findKey(key.getKeyCode()), true);
	}
	
	public void keyReleased(KeyEvent key) {
		KeyProcessor.keySet(KeyProcessor.findKey(key.getKeyCode()), false);
	}
	
	public void enable(boolean flag) {
		enabled = flag;
		if (!flag) KeyProcessor.release();
	}
	
	public void keyTyped(KeyEvent key) {}
	public void requestKeyFocus() { requestFocus(); }
	public int getLastKeyPressed() { return lastKeyPressed; }
	public void resetLastKeyPressed() { lastKeyPressed = -1; }
	public boolean isEnabled() { return enabled; }
}