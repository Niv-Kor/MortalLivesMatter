package morlivm.state;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import morlivm.system.UI.FloatingPattern;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.GraphicsManager;
import morlivm.system.graphics.ScreenDarkener;
import morlivm.system.sound.MusicBox;
import morlivm.system.sound.Tune;

public class StateManager implements GraphicsManager
{
	private State currentState, pendingState;
	private MusicBox musicBox;
	private FloatingPattern floatingPattern;
	private Stack<State> stateRecords;
	
	public void init() {
		Color[] colors = new Color[10];
		colors[0] = new Color(166, 227, 255);
		colors[1] = new Color(250, 163, 255);
		colors[2] = new Color(159, 237, 7);
		colors[3] = new Color(237, 7, 80);
		for (int i = 4; i < colors.length; i++)
			colors[i] = Color.WHITE;
		
		stateRecords = new Stack<State>();
		floatingPattern = new FloatingPattern(colors);
		musicBox = new MusicBox();
		if (currentState != null) {
			currentState.upload();
		}
	}
	
	public void update(double delta) {
		if (currentState != null) {
			currentState.update(delta);
		}
	}
	
	public void render(ExtendedGraphics2D g) {
		if (currentState != null) {
			currentState.render(g);
		}
	}
	
	public void requestSmoothState(State state, boolean loadingScreen) {
		if (loadingScreen) ScreenDarkener.apply(Color.BLACK, new Load(state, null, null), false);
		else ScreenDarkener.apply(Color.BLACK, state, true);
	}
	
	public void requestState(State state) {
		if (state == null) {
			System.err.println("Error, state given is null.");
			return;
		}
		pendingState = state;
	}
	
	public void forcedState(State state) {
		currentState = state;
	}
	
	public void processStateChanges() {
		if (pendingState != null) {
			forcedState(pendingState);
			pendingState = null;
		}
	}
	
	public void insertStateRecord(State s) {
		stateRecords.push(s);
	}
	
	public boolean isRecorded(State s) {
		for (State st : stateRecords)
			if (st == s) return true;
		
		insertStateRecord(s);
		return false;
	}
	
	public void initMusicBox(Tune[] t) {
		musicBox.requestRemovalAll();
		musicBox = new MusicBox();
		musicBox.put(t);
		musicBox.export();
	}
	
	public MusicBox getMusicBox() { return musicBox; }
	public State getCurrentState() { return currentState; }
	public State getPendingState() { return pendingState; }
	public FloatingPattern getFloatingPattern() { return floatingPattern; }
	public List<? extends Graphable> getList() { return new ArrayList<Graphable>(); }
}