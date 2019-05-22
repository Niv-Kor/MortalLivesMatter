package morlivm.main;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import morlivm.memory.Loadable;
import morlivm.memory.LoadedSectionsQueue;
import morlivm.system.performance.Stability;

public class Loader implements Runnable
{
	private static class LoadedUnit
	{
		private boolean start, uploaded;
		private Loadable component;
		private LoadedSectionsQueue sections;
		
		public LoadedUnit(Loadable component, boolean startWhenReady) {
			this.component = component;
			this.start = startWhenReady;
		}
		
		public void upload() {
			//try to upload the content to see if it's divided to sections
			if (sections == null) sections = component.upload();
			
			//if it's not divided to sections, mark as uploaded
			if (sections == null || sections.isEmpty()) uploaded = true;
			else { //execute one section at a time
				sections.dequeue().execute();
				uploaded = sections.isEmpty();
			}
		}
		
		public String getDiscription() {
			return sections != null && sections.peek() != null ? sections.peek().getDiscription() : "";
		}
		
		public void execute() {	component.execute(); }
		public boolean wasUploaded() { return uploaded; }
		public boolean needsExecution() { return start; }
		public Loadable getComponent() { return component; }
		public String getCode() { return component.getLoadedUnitCode(); }
	}
	
	private static Stability stability;
	private static Queue<LoadedUnit> pendingUpload, pendingExecution;
	private static List<String> history;
	private Thread thread;
	private volatile boolean running;
	
	public void init() {
		pendingUpload = new LinkedList<LoadedUnit>();
		pendingExecution = new LinkedList<LoadedUnit>();
		history = new ArrayList<String>();
		stability = new Stability();
	}
	
	public void update() {
		LoadedUnit lu;
		
		if (!pendingUpload.isEmpty()) {
			lu = pendingUpload.peek();
			
			//upload at least once and until all sections are uploaded
			if (!lu.wasUploaded()) lu.upload();
			//check if upload was successful and proceed
			else if (lu.getComponent().uploadTest()) {
				
				//send the loaded unit towards execution if necessary
				if (lu.needsExecution() && !exists(lu.getComponent(), pendingExecution))
					pendingExecution.add(lu);
				//toss the loaded unit and save its data for later use
				else if (!lu.needsExecution()) history.add(lu.getCode());
				
				pendingUpload.remove();
			}
		}
		
		if (!pendingExecution.isEmpty()) {
			lu = pendingExecution.remove();
			lu.execute();
			history.add(lu.getCode());
		}
	}
	
	public void run() {
		init();
		while (running) {
			update();
			try { Thread.sleep(8); }
			catch(Exception e) {}
		}
	}

	public synchronized void start() {
		if (running)
			return;
		thread = new Thread(this);
		running = true;
		thread.start();
	}

	public synchronized void stop() {
		if (!running)
			return;
		running = false;
		thread.interrupt();
	}
	
	public static void load(Loadable component, boolean startWhenReady) {
		if (!exists(component) && !finished(component))
			pendingUpload.add(new LoadedUnit(component, startWhenReady));
	}
	
	private static boolean exists(Loadable component) {
		return (exists(component, pendingUpload) || exists(component, pendingExecution));
	}
	
	private static boolean exists(Loadable component, Queue<LoadedUnit> queue) {
		for (LoadedUnit lu : queue)
			if (lu.getCode().equals(component.getLoadedUnitCode())) return true;
		
		return false;
	}
	
	public static boolean finished(Loadable component) {
		if (component == null) return false;
		return history.contains(component.getLoadedUnitCode());
	}
	
	public static String getDiscription(Loadable component) {
		LoadedUnit temp = null;
		
		for (LoadedUnit lu : pendingUpload) {
			if (component == lu.getComponent()) {
				temp = lu;
				break;
			}
		}
		
		return temp != null ? temp.getDiscription() : "";
	}
	
	public static void appandHistory(Loadable component) { history.add(component.getLoadedUnitCode()); }
	public static void clearFromHistory(Loadable component) { history.remove(component.getLoadedUnitCode()); }
	public static Stability getStability() { return stability; }
}