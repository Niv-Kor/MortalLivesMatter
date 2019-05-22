package morlivm.system.math;

public final class Physics
{
	public enum Axis { X, Y, Z; }
	
	public enum Vector {
		NONE(0),
		UP(-1),
		DOWN(1),
		LEFT(-1),
		RIGHT(1);
		
		private int vector;
		
		private Vector(int vector) {
			this.vector = vector;
		}
		
		public Vector oppose() {
			switch (name()) {
				case "UP": return Vector.DOWN;
				case "DOWN": return Vector.UP;
				case "LEFT": return Vector.RIGHT;
				case "RIGHT": return Vector.LEFT;
				default: return Vector.NONE;
			}
		}
		
		public double generate() {
			if (vector < 0) return RNG.generateDouble(-1, -0.05);
			else if (vector > 0) return RNG.generateDouble(0.05, 1);
			else return 0;
		}
		
		public int straight() { return vector; }
	}
	
	public final static double METER = 60.0;
}