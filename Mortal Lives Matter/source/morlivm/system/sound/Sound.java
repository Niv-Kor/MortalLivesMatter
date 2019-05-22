package morlivm.system.sound;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import morlivm.memory.Memory;
import morlivm.memory.StaticSaveable;

public class Sound implements StaticSaveable
{
	public static enum Genre {
		BGM(0),
		SFX(1);
		
		public int index;
		
		private Genre(int index) {
			this.index = index;
		}
	}
	
	public static enum Clique {
		GENERAL("G-general", 0),
		
		//mortals
		M_SPAWN("M-spawn", 1),
		M_BREATH("M-breath", 2),
		M_HURT("M-hurt", 3),
		M_DIE("M-die", 4),
		
		//projectiles
		P_LAUNCH("P-launch", 5),
		P_HIT("P-hit", 6),
		P_ACTIVATE("P-activate", 7),
		P_EXPLODE("P-explode", 8),
		
		//leveles
		A_BGM("A-bgm", 9),
		A_ATMOSPHERE("A-atmosphere", 10);
		
		public String name;
		private int index;
		
		private Clique(String name, int index) {
			this.name = name;
			this.index = index;
		}
		
		public int getIndex() { return index; }
	}
	
	public final static float MAX_VOLUME = 1f;
	public final static float MIN_VOLUME = 0f;
	
	private static HashMap<Tune, Clip> clips;
	private static LinkedList<Tune> tunes, pendingRemoval;
	private static float[] typeVol, presVol;
	private static int gap;
	
	public static void init() {
		clips = new HashMap<Tune, Clip>();
		tunes = new LinkedList<Tune>();
		pendingRemoval = new LinkedList<Tune>();
		presVol = new float[2];
		typeVol = new float[2];
		load();
		saveVol();
		gap = 0;
	}
	
	public static void load(Tune t) {
		if (clips.get(t) != null) return;

		Clip clip;
		
		try {
			InputStream in = Sound.class.getResourceAsStream(t.getPath());
			InputStream bin = new BufferedInputStream(in);
			AudioInputStream ais = AudioSystem.getAudioInputStream(bin);
			AudioFormat baseFormat = ais.getFormat();
			AudioFormat decodeFormat = new AudioFormat(
									   AudioFormat.Encoding.PCM_SIGNED,
									   baseFormat.getSampleRate(),	16,
									   baseFormat.getChannels(),
									   baseFormat.getChannels() * 2,
									   baseFormat.getSampleRate(), false);
			AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat, ais);
			clip = AudioSystem.getClip();
			clip.open(dais);
			clips.put(t, clip);
			tunes.add(t);
			setVolume(t, typeVol[t.getGenre().index]);
		}
		catch(Exception e) { System.err.println("ERROR: Could not find the sound file directory " + t.getPath()); }
	}
	
	public static void update(double delta) {
		Tune t;
		
		//timing tunes
		for (int i = 0; i < tunes.size(); i++) {
			t = tunes.get(i);
			if (t.getGenre() == Genre.SFX && isPlaying(t)) t.time();
		}
		
		//attempt to remove terminated tunes
		for (int i = 0; i < pendingRemoval.size(); i++) {
			t = pendingRemoval.get(i);
			if (t.isLoopable() || !isPlaying(t)) remove(t);
		}
	}
	
	public static void play(Tune t) {
		if (!t.isUseable()) return;
		play(t, gap);
	}
	
	private static void play(Tune t, int i) {
		Clip c = clips.get(t);
		if (c == null) return;
		if (c.isRunning()) c.stop();
		c.setFramePosition(i);
		t.activate();
		while(!c.isRunning()) c.start();
	}
	
	public static void stop(Tune t) {
		if (clips.get(t) == null) return;
		if (clips.get(t).isRunning()) clips.get(t).stop();
	}
	
	public static void resume(Tune t) {
		clips.get(t).start();
		t.activate();
	}
	
	public static void resumeLoop(Tune t) {
		if (isPlaying(t)) return;
		Clip c = clips.get(t);
		if(c == null) return;
		t.activate();
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public static void loop(Tune t) {
		if (isPlaying(t)) return;
		loop(t, gap, gap, clips.get(t).getFrameLength() - 1);
	}
	
	public static void loop(Tune t, int frame) {
		loop(t, frame, gap, clips.get(t).getFrameLength() - 1);
	}
	
	public static void loop(Tune t, int start, int end) {
		loop(t, gap, start, end);
	}
	
	public static void loop(Tune t, int frame, int start, int end) {
		if (isPlaying(t)) return;
		
		Clip c = clips.get(t);
		if (c == null) return;
		if (c.isRunning()) c.stop();
		c.setLoopPoints(start, end);
		c.setFramePosition(frame);
		c.loop(Clip.LOOP_CONTINUOUSLY);
		t.activate();
	}
	
	public static void setPosition(Tune t, int frame) {
		clips.get(t).setFramePosition(frame);
	}
	
	public static void close(Tune t) {
		stop(t);
		clips.get(t).close();
	}
	
	public static void remove(Tune t) {
		stop(t);
		clips.remove(t);
		tunes.remove(t);
		pendingRemoval.remove(t);
	}
	
	public static void setVolume(Tune t, float volume) {
	    if (volume < MIN_VOLUME) volume = MIN_VOLUME;
	    if (volume > MAX_VOLUME) volume = MAX_VOLUME;
	    FloatControl gainControl = (FloatControl) clips.get(t).getControl(FloatControl.Type.MASTER_GAIN);
	    gainControl.setValue(20f * (float) Math.log10(volume));
	    Sound.save();
	}
	
	public static boolean isPlaying(Tune t) {
		Clip c = clips.get(t);
		if (c == null) return false;
		return c.isRunning();
	}
	
	public static void setCollectiveVolume(Genre type, float volume) {
		Tune tune;
		
		for (int i = 0; i < tunes.size(); i++) {
			tune = tunes.get(i);
			if (tune.getGenre() == type) setVolume(tune, volume);
		}
		
		if (volume > 1f) volume = 1f;
		else if (volume < 0f) volume = 0f;
		typeVol[type.index] = volume;
	}
	
	public static Tune getTune(String name) {
		Tune tune;
		
		for (int i = 0; i < tunes.size(); i++) {
			tune = tunes.get(i);
			if (tune.getName().equals(name)) return tune;
		}
		return null;
	}
	
	public static void mute(boolean flag) {
		if (flag) {
			saveVol();
			for (Genre g : Genre.values()) setCollectiveVolume(g, 0);
		}
		else restoreVol();
	}
	
	private static void saveVol() { for (Genre g : Genre.values()) presVol[g.index] = typeVol[g.index]; }
	private static void restoreVol() { for (Genre g : Genre.values()) setCollectiveVolume(g, typeVol[g.index]); }
	public static float getVolume(Genre type) { return typeVol[type.index]; }
	public static double getVolumePercent(Genre type) { return (double) (typeVol[type.index] * 100); }
	public static int getFrames(Tune t) { return clips.get(t).getFrameLength(); }
	public static int getPosition(Tune t) { return clips.get(t).getFramePosition(); }
	public static void requestRemoval(Tune t) { pendingRemoval.add(t); }
	
	public static void save() {
		Memory.save(getVolume(Genre.BGM), Memory.Element.BGM);
		Memory.save(getVolume(Genre.SFX), Memory.Element.SFX);
	}

	public static void load() {
		setCollectiveVolume(Genre.BGM, Memory.loadFloat(Memory.Element.BGM));
		setCollectiveVolume(Genre.SFX, Memory.loadFloat(Memory.Element.SFX));
	}
}