package morlivm.system.sound;
import java.util.ArrayList;
import java.util.LinkedList;

import morlivm.system.math.RNG;

public class MusicBox
{
	public LinkedList<Tune> tunes;
	private boolean terminates;
	
	public MusicBox() {
		this.tunes = new LinkedList<Tune>();
	}
	
	public void export() {
		for (int i = 0; i < tunes.size(); i++)
			Sound.load(tunes.get(i));
	}
	
	//specific
	public Tune getTune(String name) {
		for (Tune t : tunes)
			if (t.getName().equals(name)) return t;
		
		return null;
	}
	
	//random
	public Tune getTune(Sound.Clique clique) {
		LinkedList<Tune> list = new LinkedList<Tune>();
		
		for (Tune t : tunes)
			if (t.getClique() == clique)
				list.add(t);
		
		return list.size() > 0 ? list.get(RNG.generate(0, list.size() - 1)) : null;
	}
	
	//all
	public LinkedList<Tune> getAllTunes(Sound.Clique clique) {
		LinkedList<Tune> list = new LinkedList<Tune>();
		
		for (Tune t : tunes)
			if (t.getClique() == clique)
				list.add(t);
		
		return list;
	}
	
	public boolean exists(Tune tune) {
		for (Tune t : tunes) 
			if (t == tune) return true;

		return false;
	}
	
	public void requestRemovalAll() {
		if (terminates) return;
		else terminates = true;
		
		for (int i = 0; i < tunes.size(); i++)
			Sound.requestRemoval(tunes.get(i));
	}
	
	public void put(Tune t) {
		boolean sameName = false;
		ArrayList<Integer> counter = new ArrayList<Integer>();
		
		//check if there's already another tune with the same name
		for (int i = 0; i < tunes.size(); i++) {
			if (t.getName().equals(tunes.get(i).getName())) {
				sameName = true;
				counter.add(i + 1);
			}
		}
		
		//display the error, but still add the new tune
		String error;
		if (sameName) {
			error = "Adding \"" + t.getName() + "\" to the music box encountered a conflict\nwith the following tunes: ";
			for (int i = 0; i < counter.size(); i++) {
				error = new String(error.concat("#" + counter.get(i)));
				error = i != counter.size() - 1 ? error.concat(", ") : error.concat("; ");
			}
			
			error = new String(error.concat("from the list:\n"));
			for (int i = 0; i < tunes.size(); i++)
				error = new String(error.concat((i + 1) + "." + tunes.get(i).getName() + "\n"));
			
			System.err.println(error);
		}
		
		tunes.add(t);
	}
	
	public void put(Tune[] t) { for (int i = 0; i < t.length; i++) put(t[i]); }
	
	public void play(Tune t) { Sound.play(t); } //plays specific
	public void play(String name) {
		Tune temp = getTune(name);
		if (temp != null) Sound.play(temp);
		else System.err.println(name + " could not be played");
	} //plays specific
	
	public void play(Sound.Clique clique) {
		Tune temp = getTune(clique);
		if (temp != null) Sound.play(temp);
	} //plays random
	
	public void loop(Tune t) { Sound.loop(t); } //loops specific
	public void loop(String name) {
		Tune temp = getTune(name);
		if (temp != null) Sound.loop(temp);
	} //loops specific
	
	public void loop(Sound.Clique clique) {
		Tune temp = getTune(clique);
		if (temp != null) Sound.loop(temp);
	} //loops random
	
	public void stop() { for (int i = 0; i < tunes.size(); i++) Sound.stop(tunes.get(i)); }
	public void stop(Tune t) { Sound.stop(t); } //stops specific
	public void stop(String name) {
		Tune temp = getTune(name);
		if (temp != null) Sound.stop(temp);
	} //stops specific
	
	public void stop(Sound.Clique clique) {
		LinkedList<Tune> all = getAllTunes(clique);
		for (Tune t : all) Sound.stop(t);
	}
	
	public void resume(String name) {
		Tune temp = getTune(name);
		if (temp != null) Sound.resume(temp);
	} //resumes specific
	
	public void resume(Sound.Clique clique) {
		LinkedList<Tune> all = getAllTunes(clique);
		for (Tune t : all) Sound.resume(t);
	}
	
	public void resumeLoop(String name) {
		Tune temp = getTune(name);
		if (temp != null) Sound.resumeLoop(temp);
	} //resumes specific
	
	public void resumeLoop(Sound.Clique clique) {
		LinkedList<Tune> all = getAllTunes(clique);
		for (Tune t : all) Sound.resumeLoop(t);
	}
	
	public void requestRemoval(Tune t) { Sound.requestRemoval(t); }
	public int size() { return tunes.size(); }
}