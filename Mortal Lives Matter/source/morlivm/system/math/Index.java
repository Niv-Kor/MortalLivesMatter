package morlivm.system.math;

public class Index
{
	public int row, col;
	
	public Index(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public Object getVal(Object[][] obj2D) {
		return obj2D[row][col];
	}
	
	public void setVal(Object[][] obj2D, Object obj) {
		obj2D[row][col] = obj;
	}
	
	public boolean equals(Index other) {
		return row == other.row && col == other.col;
	}
	
	public String toString() {
		return "(" + row + ", " + col + ")";
	}
}