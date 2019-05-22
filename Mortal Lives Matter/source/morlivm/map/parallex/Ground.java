package morlivm.map.parallex;
import java.util.ArrayList;
import java.util.List;

import morlivm.content.Entity;
import morlivm.content.mortal.Player;
import morlivm.control_panel.Megaphone;
import morlivm.database.DataManager;
import morlivm.main.Structure;
import morlivm.main.testing.Tester;
import morlivm.map.AttachManager;
import morlivm.map.Attachable;
import morlivm.map.Barrier;
import morlivm.map.Boundary;
import morlivm.map.Earthquake;
import morlivm.map.Magnet;
import morlivm.map.Savepoint;
import morlivm.map.orientation.Topology;
import morlivm.map.portal.LevelPortal;
import morlivm.map.portal.Portal;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.sheet.Pamphlet;
import morlivm.system.math.Percent;
import morlivm.system.math.Physics.Axis;
import morlivm.system.sound.Sound;
import morlivm.system.sound.Tune;

public class Ground extends GeoLayer implements AttachManager, Attachable
{
	private final static int FRAME_RATE = 1;
	private final static double TRANSPARENCY = 1;
	
	private static Point originPoint;
	private int barrierAmount, barrierSpace;
	private Portal portal;
	private Barrier[][] spikes;
	private Savepoint savepoint;
	private Earthquake earthquake;
	private Magnet spawnMagnet;
	
	public Ground(Point point, GameState gs) {
		super(Structure.getDatabase().ground, point	, gs);
		
		this.gameState = gs;
		this.spawnMagnet = new Magnet();
		this.barrierAmount = ldb.waves.length - 1;
		this.barrierSpace = ldb.rightWall / (barrierAmount + 1);
		musicBox.put(new Tune("open barrier", "/Sound/Main/SFX/OpenBarrier.wav", Sound.Clique.GENERAL, Sound.Genre.SFX, false));
		musicBox.export();
	}
	
	public void init() {
		this.earthquake = new Earthquake(gameState);
		this.spikes = new Barrier[ldb.waves.length - 1][8];
		
		double spikeX, spikeY;
		Pamphlet spikesPamphlet = DataManager.retSheet("ss$a$barrier_" + ldb.mapName);
		
		for (int a = 0, b = barrierSpace; a < barrierAmount; a++, b *= 2) {
			for (int i = 0, j = 0, k = 0; i < spikes[a].length; i++, j += 25, k += 35) {
				spikeX = getX() + b + j;
				spikeY = Topology.topLim(spikeX) + k - Percent.percentOfNum(80, spikesPamphlet.getDimension().height);
				spikes[a][i] = new Barrier(new Point(spikeX, spikeY), this);
			}
		}
		
		if (!Structure.atBossMap()) {
			Pamphlet savepointSheet = DataManager.retSheet("ss$a$savepoint_" + Structure.getDatabase().mapName);
			double x = Percent.percentOfNum(75, ldb.rightWall);
			Point savepointP = new Point(x, Topology.topLim(x) - savepointSheet.getDimension().height);
			
			this.savepoint = new Savepoint(savepointP, gameState);
		}
		
		if (Tester.openBarriers) {
			for (int i = 0; i < barrierAmount; i++) clearNextWave();
			if (Tester.openPortal) clearNextWave();
		}
	}
	
	public void update(double delta) {
		//move map instead of player as he moves
		moveLengthwise(this, delta);
		
		if (!Magnet.isActive())	{
			//save last y and update it
			double preY = Math.round(getY());
			setY(Topology.groundY(entity.getMidX()));
			
			//update y again, according to the incline's vector
			if (usingImapctZone())
				setY(getY() + delta * entity.getSpeed() / FRAME_RATE *
					 Topology.vectorY(entity.getMidX(), entity.getUserDirect(Axis.X).oppose()) * 2);
			
			//move spawnManager's components if necessarry
			if (Math.round(preY) != Math.round(getY())) {
				spawnMagnet.add(gameState.getSpawnManager(), false);
				spawnMagnet.translateY(Math.round(getY()) - Math.round(preY));
				spawnMagnet.close();
			}
		}
		
		for (int a = 0; a < barrierAmount; a++)
			for (int i = 0; i < spikes[a].length; i++)
				spikes[a][i].update(delta);
		
		if (savepoint != null) savepoint.update(delta);
		portal.update(delta);
		earthquake.update(delta);
	}
	
	public void render(ExtendedGraphics2D g) {
		super.render(g);
		
		if (savepoint != null) savepoint.render(g);
		portal.render(g);
		
		for (int a = 0; a < barrierAmount; a++)
			for (int i = 0; i < spikes[a].length; i++)
				spikes[a][i].render(g);
	}
	
	public boolean clearNextWave() {
		if (portal.isOpen()) return false;
		
		for (int a = 0; a < barrierAmount; a++) {
			for (int i = 0; i < 1; i++) {
				if (!spikes[a][i].isClear() && !spikes[a][i].isClearing()) {
					clearWave(a);
					Megaphone.announce("Objective complete!");
					return true;
				}
			}
		}
		
		Megaphone.announce("Objective complete!");
		Megaphone.announce("The portal is now open");
		portal.open();
		return false;
	}
	
	private void clearWave(int wave) {
		boolean succeed = false;;
		
		for (int i = 0; i < spikes[wave].length; i++) {
			if (spikes[wave][i] != null) {
				spikes[wave][i].openGateway();
				succeed = true;
			}
		}
		
		if (succeed) musicBox.play("open barrier");
	}
	
	public int getMaxSpawnArea() {
		for (int a = 0; a < barrierAmount; a++)
			for (int i = 0; i < 1; i++)
				if (!spikes[a][i].isClear() && !spikes[a][i].isClearing())
					return (int) (getX() + (a + 1) * barrierSpace - 100);
		
		return (int) getX() + getWidth() - 100;
	}
	
	public int getMinSpawnArea() { return getMaxSpawnArea() - barrierSpace + 200; }
	
	public int getClosedBarriers() {
		int counter = 0;
		
		for (int a = barrierAmount - 1; a >= 0; a--)
			if (!spikes[a][0].isClear()) counter++;
		
		return counter;
	}
	
	public void setClosedBarriers(int amount) {
		for (int a = amount - 1; a >= 0; a--) {
			for (int b = 0; b < spikes[a].length; b++) {
				spikes[a][b].closeGateway();
			}
		}
	}
	
	public double getClosedBarrierX() {
		for (int a = 0; a < barrierAmount; a++)
			if (!spikes[a][0].isClear()) return spikes[a][0].getX();
		
		return getWidth();
	}
	
	public void connectRulerEntity(Entity e) {
		super.connectRulerEntity(e);
		if (!Structure.atBossMap()) portal = new LevelPortal(gameState, (Player) e);
		//else portal = new DimPortal(gameState, (Player) e, Levels.DIMENSIONAL_PORTAL[Structure.getMap()]);
	}
	
	public void moveAlong(Attachable obj, double delta) {
		moveLengthwise(obj, delta);
		moveHeightwise(obj, delta);
	}
	
	public void moveLengthwise(Attachable obj, double delta) {
		if (usingImapctZone())
			obj.setX(obj.getX() + delta * entity.getSpeed() / FRAME_RATE * entity.getUserDirect(Axis.X).oppose().straight());
	}
	
	public void moveHeightwise(Attachable obj, double delta) {
		if (!Magnet.isActive()) obj.setY(obj.getFixedPoint().getY() + (Math.round(getY()) - obj.getFixedPoint().getZ()));
	}
	
	public List<Attachable> getMagnetizedComponents() {
		List<Attachable> list = new ArrayList<Attachable>();
		list.add(savepoint);
		list.add(portal);
		list.addAll(new Boundary(null).getMagnetizedComponents());
		
		for (int i = 0; i < barrierAmount; i++)
			for (int j = 0; j < spikes[i].length; j++)
				list.add(spikes[i][j]);
		
		return list;
	}
	
	public static Point originPoint() {
		if (originPoint != null) return originPoint;
		else {
			originPoint = new Point();
			return new Point();
			/*
			LevelData ldb = Structure.getDatabase();
			MortalData mdb = DataManager.retChosenPlayer();
			//int playerY = (int) (ldb.playerPosition - mdb.legs.getD().getY());
			int playerY = (int) (1350 - mdb.legs.getD().getY());
			System.out.println("player y is " + playerY);
			
			System.out.println("origin Y is " + -((playerY - Game.HEIGHT / 2)));
			originPoint = new Point(0, -(playerY - Game.HEIGHT / 2));
			return originPoint;
			*/
		}
	}
	
	protected int transparency() {
		return (int) Percent.percentOfNum(TRANSPARENCY * 100, 0xFF);
	}
	
	public void setEarthquake(int richter, boolean moveability) { earthquake.set(richter, moveability); }
	public boolean getEarthquakeMoveability() { return earthquake.getMoveability(); }
	public boolean isQuaking() { return earthquake.isQuaking(); }
	public Point getFixedPoint() { return originPoint(); }
}