package morlivm.system.UI;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import morlivm.content.mortal.SpawnManager;
import morlivm.main.Game;
import morlivm.state.GameState;
import morlivm.state.Pause;
import morlivm.system.math.Physics;
import morlivm.user_input.MouseInput;

public class Cursor
{
	public static final int CURSOR_PIX_DIM = 32;
	public static final int DEFAULT = 0, POINTER = 1, CLICKER = 2;
	public static final int TARGET = 3;

	private static Game game;
	private static int currentCursor;
	private static MouseInput mouseInput;
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
	private static Image defaultive, pointer, clicker, target;
	private static java.awt.Cursor[] cursors;
	
	public static void init(Game g) {
		game = g;
		currentCursor = DEFAULT;
		mouseInput = Game.getMouseInput();
		cursors = new java.awt.Cursor[5];
		defaultive = toolkit.getImage(Cursor.class.getResource("/Cursor/Default.png"));
		pointer = toolkit.getImage(Cursor.class.getResource("/Cursor/Pointer.png"));
		clicker = toolkit.getImage(Cursor.class.getResource("/Cursor/Clicker.png"));
		target = toolkit.getImage(Cursor.class.getResource("/Cursor/Blank.gif"));
		
		cursors[DEFAULT] = toolkit.createCustomCursor
				(defaultive, new Point(0, 0), "");
		
		cursors[POINTER] = toolkit.createCustomCursor
				(pointer, new Point(0, 0), "");
		
		cursors[CLICKER] = toolkit.createCustomCursor
				(clicker, new Point(0, 0), "");
		
		cursors[TARGET] = toolkit.createCustomCursor
				(target, new Point(0, 0), "");
	}
	
	public static void update(double delta) {
		boolean inGame = Game.getStateManager().getCurrentState() instanceof GameState && !Pause.isRunning();
		
		if (!mouseInput.prohibitedHovers()) {
			//avoid maintaining clicker cursor when dragging it off the hovering target
			if (currentCursor == CLICKER && mouseInput.release(Physics.Vector.LEFT)) setCursor(Cursor.DEFAULT);
			
			//outside game state
			if (!inGame && !mouseInput.press()) {
				if (mouseInput.getHoverManager().isEmpty()) setCursor(Cursor.DEFAULT);
				else setCursor(Cursor.POINTER);
			}
			
			//during game state
			if (inGame && mouseInput.getHoverManager().isEmpty() && currentCursor != CLICKER) {
				if (!SpawnManager.isActivated()) setCursor(Cursor.DEFAULT);
				else setCursor(Cursor.TARGET);
			}
		}
	}
	
	public static void setCursor(int c) {
		if (c < 0 || c >= cursors.length || c == getCursor()) return;
		game.setCursor(cursors[c]);
		currentCursor = c;
	}
	
	public static int getCursor() { return currentCursor; }
}