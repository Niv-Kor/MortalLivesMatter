package morlivm.map;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import morlivm.content.mortal.AeroEnemy;
import morlivm.content.mortal.Boss;
import morlivm.content.mortal.EarthEnemy;
import morlivm.content.mortal.Mortal;
import morlivm.main.Game;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.math.Physics.Axis;
import morlivm.system.math.Physics.Vector;
import morlivm.system.math.RangedDouble;

public class Landmark
{
	public static enum ArrowType {
		EARTH(0),
		AERO(1),
		BOSS(2);
		
		public static enum Axis {
			HORIZONTAL(0),
			VERTICAL(1);
			
			private int index;
			
			private Axis(int index) {
				this.index = index;
			}
			
			public int index() { return index; }
		}
		
		private final static String DIRECTORY = "/sheets/arena/landmark/";
		
		private Animation[] animation;
		private int index;
		
		private ArrowType(int index) {
			this.index = index;
			this.animation = new Animation[2];
			animation[0] = new Animation(DIRECTORY + name() + ".png");
			animation[1] = new Animation(DIRECTORY + "UPPER " + name() + ".png");
		}
		
		public static ArrowType findType(Mortal mortal) {
			if (mortal instanceof EarthEnemy) return EARTH;
			else if (mortal instanceof AeroEnemy) return AERO;
			else if (mortal instanceof Boss) return BOSS;
			else return EARTH;
		}
		
		public Axis getAxis(Animation animation) {
			if (animation == this.animation[0]) return Axis.HORIZONTAL;
			else return Axis.VERTICAL;
		}
		
		public Animation getSprite(Axis direct) { return animation[direct.index()]; }
		public int index() { return index; }
	}
	
	private static abstract class Arrow {}
	
	public static class SingleArrow extends Arrow implements Graphable
	{
		private ArrowType type;
		private Mortal mortal;
		private Point point;
		private Dimension dim;
		private Animation animation;
		private ArrowGroup group;
		private Vector pointer;
		private boolean show, joined, leader, inFrame;
		private int rank;
		
		public SingleArrow(Mortal mortal) {
			this.mortal = mortal;
			this.type = ArrowType.findType(mortal);
			this.point = new Point();
			this.animation = type.getSprite(ArrowType.Axis.HORIZONTAL);
			this.dim = animation.getSprite().getDimension();
			this.rank = -1;
			
			int midMap = Game.WIDTH / 2;
			dim.width *= (mortal.getX() > midMap) ? -1 : 1;
		}
		
		public void update(double delta) {
			updateCoordinates();
		}

		public void render(ExtendedGraphics2D g) {
			if (!show || (joined && !leader)) return;
			g.drawImage(animation.getImage(), point, dim);
		}
		
		private void updateCoordinates() {
			Point leftScreenP = new Point(0, 0);
			Point rightScreenP = new Point(Game.WIDTH, 0);
			Point horEnemyP = new Point(getExtremeEnemyPoint(Axis.X), mortal.getY() + mortal.getDimension().height / 2);
			Point verEnemyP = new Point(mortal.getMidX(), getExtremeEnemyPoint(Axis.Y));
			
			//vertical
			if (verEnemyP.smallerThan(leftScreenP, Axis.Y, 0)) {
				animation = type.getSprite(ArrowType.Axis.VERTICAL);
				dim = animation.getSprite().getDimension();
				pointer = Vector.UP;
				setX(verEnemyP.getX());
				setY(WALL_SPACE);
				show(true);
				return;
			}
			else { //horizontal
				animation = type.getSprite(ArrowType.Axis.HORIZONTAL);
				dim = animation.getSprite().getDimension();
				
				if (horEnemyP.smallerThan(leftScreenP, Axis.X, 0)) {
					pointer = Vector.LEFT;
					setX(WALL_SPACE);
					setY(horEnemyP.getY());
					show(true);
					inFrame = false;
					changeDirection();
				}
				else if (horEnemyP.largerThan(rightScreenP, Axis.X, 0)) {
					pointer = Vector.RIGHT;
					setX(Game.WIDTH - WALL_SPACE);
					setY(horEnemyP.getY());
					show(true);
					inFrame = false;
					changeDirection();
				}
				else {
					if (joined) {
						Landmark.ungroup(this);
						joinGroup(false, null);
					}
					inFrame = true;
					show(false);
				}
			}
		}
		
		private double getExtremeEnemyPoint(Axis axis) {
			Point[] points = new Point[3];
			Point max = new Point();
			boolean largest = mortal.getDirectX() == Vector.RIGHT;
			
			if (axis == Axis.Y) max = mortal.getMass().getD();
			else if (axis == Axis.X) {
				if (largest) {
					points[0] = mortal.getHead().getB();
					points[1] = mortal.getMass().getB();
					points[2] = mortal.getLegs().getB();
				}
				else {
					points[0] = mortal.getHead().getA();
					points[1] = mortal.getMass().getA();
					points[2] = mortal.getLegs().getA();
				}
				
				max = points[0];
				for (int i = 1; i < 3; i++) {
					if (largest && points[i].largerThan(max, Axis.X, 0)) max = points[i];
					else if (!largest && points[i].smallerThan(max, Axis.X, 0)) max = points[i];
				}
			}
			return (axis == Axis.X) ? max.getX() : max.getY();
		}
		
		private void changeDirection() {
			int midMap = Game.WIDTH / 2;
			
			if (getX() < midMap && dim.width < 0) {
				setX(getX() + dim.width);
				dim.width *= -1;
			}
			else if (getX() > midMap && dim.width > 0) {
				setX(getX() + dim.width);
				dim.width *= -1;
			}
		}
		
		public RangedDouble getRange() {
			boolean isHorizontal = getAxis() == ArrowType.Axis.HORIZONTAL;
			double axisVal = isHorizontal ? getY() : getX();
			double space = isHorizontal ? getDimension().height / 2 : getDimension().width / 2;
			return new RangedDouble(axisVal - space, axisVal + space);
		}
		
		public void joinGroup(boolean flag, ArrowGroup g) {
			joined = flag;
			
			if (leader && group != g) leader = false;
			group = g;
			
			if (flag) show(false);
			else leader = false;
		}
		
		public void makeGroupLeader(boolean flag) {
			if (group == null) return;
			
			if (flag) {
				joinGroup(true, group);
				show(true);
			}
			else joinGroup(false, null);
			
			leader = flag;
		}
		
		public ArrowType getType() { return type; }
		public Mortal getMortal() { return mortal; }
		public void show(boolean flag) { show = flag; }
		public void setDimension(Dimension d) { dim = new Dimension(d); }
		public Dimension getDimension() { return dim; }
		public Point getPoint() { return point; }
		public void setX(double x) { point.setX(x); }
		public void setY(double y) { point.setY(y); }
		public double getX() { return point.getX(); }
		public double getY() { return point.getY(); }
		public int getRank() { return rank; }
		public void setRank(int r) { rank = r; }
		public boolean isInFrame() { return inFrame; }
		public boolean isJoinedAGroup() { return joined; }
		public boolean isGroupLeader() { return leader; }
		public ArrowGroup getGroup() { return group; }
		public Vector getPointer() { return pointer; }
		public ArrowType.Axis getAxis() { return type.getAxis(animation); }
	}
	
	public static class ArrowGroup extends Arrow implements Graphable
	{
		private Stack<SingleArrow> arrows;
		private SingleArrow leader;
		
		public ArrowGroup(List<SingleArrow> arrowList) {
			this.arrows = new Stack<SingleArrow>();
			SingleArrow a;
			
			for (int i = 0; i < arrowList.size(); i++) {
				a = arrowList.get(i);
			
				arrows.push(a);
				a.joinGroup(true, this);
				
				if (i == 0) {
					a.makeGroupLeader(true);
					leader = a;
				}
				else Landmark.remove(a);
			}
		}
		
		public void update(double delta) {
			for (int i = 0; i < size(); i++)
				get(i).update(delta);
			
			split();
		}

		public void render(ExtendedGraphics2D g) {
			leader.render(g);
			
			Font f = new Font("Arial", Font.BOLD, 20);
			
			g.setColor(Color.WHITE);
			g.setFont(f);
			
			Dimension leaderDim = leader.getDimension();
			String amount = "" + size();
			Point point = new Point();
			int space = 10;
			
			switch(leader.getPointer()) {
				case LEFT: {
					point.setX(leader.getX() + space);
					point.setY(leader.getY() + leaderDim.height / 2);
					break;
				}
				case RIGHT: {
					point.setX(leader.getX() - space);
					point.setY(leader.getY() + leaderDim.height / 2);
					break;
				}
				case UP: {
					point.setX(leader.getX() + leaderDim.width / 2);
					point.setY(leader.getY() + space);
					break;
				}
				case DOWN: {
					point.setX(leader.getX() + leaderDim.width / 2);
					point.setY(leader.getY() - space);
					break;
				}
				default: break;
			}
			
			g.drawString(amount, point);
		}
		
		public void split() {
			if (size() < 2) {
				ungroup();
				return;
			}
			
			int[] ranking = new int[size()], counting = new int[size()];
			
			//init ranking to -1
			for (int i = 0; i < ranking.length; i++)
				ranking[i] = -1;
			
			for (int i = 0, counter = 0; i < size(); i++) {
				if (ranking[i] == -1) {
					ranking[i] = counter++;
					
					for (int j = i + 1; j < size(); j++)
						if (get(i).getRange().intersects(get(j).getRange()) && !get(i).isInFrame())
							ranking[j] = ranking[i];
				}
			}
			
			for (int i = 0; i < ranking.length; i++)
				counting[ranking[i]]++;
			
			System.out.println("counteing: " + Arrays.toString(counting));
			
			for (int i = 0, doubleCheck = 0; i < counting.length; i++)
				if (counting[i] != 0 && ++doubleCheck >= 2) ungroup();
		}
		
		public void remove(SingleArrow a) {
			boolean isLeader = a.isGroupLeader();
			
			arrows.remove(a);
			a.joinGroup(false, null);
			free(a);
			
			if (isLeader) {
				leader = arrows.firstElement();
				leader.makeGroupLeader(true);
			}
		}
		
		public void ungroup() {
			SingleArrow a;
			
			while (!arrows.isEmpty()) {
				a = arrows.pop();
				free(a);
				a.joinGroup(false, null);
			}
			
			Landmark.ungroup(this);
			System.out.println("ungrouped");
		}
		
		public void add(SingleArrow a) {
			if (a.isGroupLeader()) {
				Stack<SingleArrow> importedArrows = a.getGroup().getArrows();
				SingleArrow temp;
				
				while (!importedArrows.isEmpty()) {
					temp = importedArrows.pop();
					arrows.push(temp);
					temp.joinGroup(true, this);
				}
				a.getGroup().ungroup();
			}
			else arrows.push(a);
		}
		
		public Stack<SingleArrow> getArrows() { return arrows; }
		public int size() { return arrows.size(); }
		public SingleArrow get(int i) { return arrows.get(i); }
		public void setDimension(Dimension d) { leader.setDimension(d); }
		public Dimension getDimension() { return leader.getDimension(); }
		public Point getPoint() { return leader.getPoint(); }
		public void setX(double x) { leader.setX(x); }
		public void setY(double y) { leader.setY(y); }
		public double getX() { return leader.getX(); }
		public double getY() { return leader.getY(); }
		private void free(SingleArrow a) { if (Landmark.getArrow(a) == null) Landmark.add(a); }
	}
	
	private static final int WALL_SPACE = 5;
	
	private static List<SingleArrow> list;
	private static List<ArrowGroup> groups;
	
	public static void init() {
		list = new ArrayList<SingleArrow>();
		groups = new ArrayList<ArrowGroup>();
	}

	public static void update(double delta) {
		List<SingleArrow> horPotential = new ArrayList<SingleArrow>();
		List<SingleArrow> verPotential = new ArrayList<SingleArrow>();
		SingleArrow temp;
		int groupIndex;
		
		for (int i = 0; i < list.size(); i++) {
			temp = list.get(i);
			if (!temp.isGroupLeader()) temp.update(delta);
		}
		
		for (int i = 0; i < groups.size(); i++)
			groups.get(i).update(delta);
		
		System.out.println("list size: " + list.size());
		
		for (int i = 0; i < list.size(); i++) {
			temp = list.get(i);
			groupIndex = temp.getAxis().index();
			
			switch(groupIndex) {
				case 0: horPotential.add(temp); break;
				case 1: verPotential.add(temp); break;
			}
			temp.setRank(-1);
		}
		
		regroup(horPotential);
		regroup(verPotential);
	}
	
	private static void regroup(List<SingleArrow> list) {
		List<SingleArrow> currentList = new ArrayList<SingleArrow>();
		ArrowGroup vacuumGroup = null;
		SingleArrow temp1, temp2;
		
		//rank by groups
		for (int t = 0, counter = 1; t < list.size(); t++) {
			temp1 = list.get(t);
			if (temp1.getRank() != -1) continue;
			else {
				temp1.setRank(counter++);
				//System.out.println("here are compa1");
				for (int q = t + 1; q < list.size(); q++) {
					//System.out.println("s = " + list.size());
					temp2 = list.get(q);
					
					if (temp2.getRank() != -1) continue;
					else if (temp1.getRange().intersects(temp2.getRange()))
						temp2.setRank(temp1.getRank());
				}
			}
		}
		
		System.out.print("[");
		for (int x = 0; x < list.size(); x++) {
			if (list.get(x).isGroupLeader()) System.out.print(list.get(x).getRank() + " L");
			else System.out.print(list.get(x).getRank());
				
			if (x < list.size() - 1) System.out.print(", ");
		}
		System.out.print("]");
		System.out.println();
		
		//form or unite groups
		int size = list.size();
		for (int counter = 1; counter <= list.size() && size > 0; counter++) {
			currentList.clear(); //clear list for counter # inputs
			
			for (int j = 0; j < list.size(); j++) {
				temp1 = list.get(j);
				if (temp1.getRank() == counter) {
					currentList.add(temp1);
					size--;
				}
			}
			
			if (currentList.size() >= 2) {
				//find one group leader
				for (SingleArrow current : currentList) {
					if (vacuumGroup == null && current.isGroupLeader()) {
						vacuumGroup = current.getGroup();
						currentList.remove(current);
						break;
					}
				}
				
				//add all to the vacuum group
				if (vacuumGroup != null) {
					for (SingleArrow current : currentList) {
						vacuumGroup.add(current);
						remove(current);
					}
				}
				
				//create a new group
				else {
					System.out.println("before groups: " + groups.size());
					groups.add(new ArrowGroup(currentList));
				}
			}
		}
		
		System.out.println("after groups: " + groups.size());
		System.out.println();
	}

	public static void render(ExtendedGraphics2D g) {
		for (int i = 0; i < list.size(); i++)
			list.get(i).render(g);
		
		for (int i = 0; i < groups.size(); i++)
			groups.get(i).render(g);
	}
	
	public static void add(Mortal mortal) {
		add(new SingleArrow(mortal));
	}
	
	public static void add(SingleArrow arrow) {
		list.add(arrow);
	}
	
	public static void remove(Mortal mortal) {
		remove(getArrow(mortal));
	}
	
	public static void remove(SingleArrow arrow) {
		if (arrow != null) list.remove(arrow);
	}
	
	private static SingleArrow getArrow(Mortal mortal) {
		for (SingleArrow a : list) if (mortal == a.getMortal()) return a;
		return null;
	}
	
	private static SingleArrow getArrow(SingleArrow arrow) {
		for (SingleArrow a : list) if (a == arrow) return a;
		return null;
	}
	
	public static void ungroup(SingleArrow arrow) {
		for (int i = 0; i < groups.size(); i++)
			groups.get(i).remove(arrow);
		
		add(arrow);
	}
	
	public static void ungroup(ArrowGroup group) { groups.remove(group); }
}