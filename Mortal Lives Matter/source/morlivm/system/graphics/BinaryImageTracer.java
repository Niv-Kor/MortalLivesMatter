package morlivm.system.graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import morlivm.system.UI.Point;
import morlivm.system.math.Index;

public class BinaryImageTracer
{
	private char[][] binaryImg;
	private List<Integer> polyX, polyY;
	private Polygon polygon;
	
	public BinaryImageTracer(BufferedImage blueprint) {
		binaryImg = ImageHandler.binarize(blueprint);
		polyX = new ArrayList<Integer>();
		polyY = new ArrayList<Integer>();
		createPolygon(blueprint);
	}
	
	private void createPolygon(BufferedImage blueprint) {
		operate();
		detectLonelyPoints();
		
		//create actual polygon
		int[] pX = new int[polyX.size()];
		int[] pY = new int[polyY.size()];
		for (int i = 0; i < pX.length; i++) {
			pX[i] = (int) (polyX.get(i));
			pY[i] = (int) (polyY.get(i));
		}
		
		this.polygon = new Polygon(pX, pY, pX.length);
	}
	
	private char[][] operate() {
		boolean extreme, inBetween;
		
		//mark the boundaries with '2';
		for (int y = 0; y < binaryImg.length; y++) {
			for (int x = 0; x < binaryImg[y].length; x++) {
				extreme = x == 0 || x == binaryImg[y].length - 1;
				inBetween = !extreme ? binaryImg[y][x - 1] == '0' || binaryImg[y][x + 1] == '0' : false;
				if ((extreme || inBetween) && binaryImg[y][x] == '1') binaryImg[y][x] = '2';
			}
		}
		
		//stretch the '2's across the line <left to right>
		for (int y = 0; y < binaryImg.length; y++) {
			for (int x = 0; x < binaryImg[y].length; x++) {
				if (binaryImg[y][x] == '2') {
					for (int i = x; i < binaryImg[y].length; i++) {
						binaryImg[y][i] = '2';
						if (findLinksAmount(new Index(y, i), '2') < 2
						&& !checkSorroundingsExcept(new Index(y, i), 4, '1')) continue;
						else break;
					}
				}
			}
		}
		
		//stretch the '2's across the line <right to left> and detect lone '2's
		for (int y = 0; y < binaryImg.length; y++) {
			for (int x = binaryImg[y].length - 1; x >= 0; x--) {
				if (binaryImg[y][x] == '2') {
					for (int i = x; i >= 0; i--) {
						binaryImg[y][i] = '2';
						if (findLinksAmount(new Index(y, i), '2') < 2
						&& !checkSorroundingsExcept(new Index(y, i), 6, '1')) continue;
						else break;
					}
				}
			}
		}
		
		//find the first '2' and start connecting
		for (int y = 0; y < binaryImg.length; y++) {
			for (int x = 0; x < binaryImg[y].length; x++) {
				if (binaryImg[y][x] == '2') {
					connectLines(new Index(y, x));
					return binaryImg;
				}
			}
		}
		
		return binaryImg;
	}
	
	private int findLinksAmount(Index ind, char c) {
		int counter = 0;
		for (int i = 1; i <= 9; i++)
			if (checkSorrounding(ind, i, c)) counter++;
		
		return counter;
	}
	
	private boolean checkSorroundingsExcept(Index ind, int except, char c) {
		int isThere = 0;
		
		for (int i = 1; i <= 9; i++) {
			if (i == except) continue;
			if (checkSorrounding(ind, i, c)) isThere++;
		}
		
		return isThere == 8;
	}
	
	private boolean checkSorrounding(Index ind, int num, char c) {
		/* 
		 * 1 2 3
		 * 4 5 6
		 * 7 8 9
		 */
		
		int maxRow = binaryImg.length - 1;
		int maxCol = binaryImg[0].length - 1;
		
		switch(num) {
		case 1: return (ind.row > 0 && ind.col > 0 && binaryImg[ind.row - 1][ind.col - 1] == c);
		case 2: return (ind.row > 0 && binaryImg[ind.row - 1][ind.col] == c);
		case 3: return (ind.row > 0 && ind.col < binaryImg[0].length - 1 && binaryImg[ind.row - 1][ind.col + 1] == c);
		case 4: return (ind.col > 0 && binaryImg[ind.row][ind.col - 1] == c);
		case 6: return (ind.col < maxCol && binaryImg[ind.row][ind.col + 1] == c);
		case 7: return (ind.row < maxRow && ind.col > 0 && binaryImg[ind.row  + 1][ind.col - 1] == c);
		case 8: return (ind.row < maxRow && binaryImg[ind.row + 1][ind.col] == c);
		case 9: return (ind.row < maxRow && ind.col < maxCol && binaryImg[ind.row + 1][ind.col + 1] == c);
		default: return false;
		}
	}
	
	private void connectLines(Index ind) {
		if (ind.row < 0 || ind.row > binaryImg.length - 1) return;
		if (ind.col < 0 || ind.col > binaryImg[0].length - 1) return;
		if (binaryImg[ind.row][ind.col] != '2') return;
		else {
			binaryImg[ind.row][ind.col] = '3';
			polyY.add(ind.row);
			polyX.add(ind.col);
			
			connectLines(new Index(ind.row - 1, ind.col - 1));
			connectLines(new Index(ind.row - 1, ind.col));
			connectLines(new Index(ind.row - 1, ind.col + 1));
			connectLines(new Index(ind.row, ind.col - 1));
			connectLines(new Index(ind.row, ind.col + 1));
			connectLines(new Index(ind.row + 1, ind.col - 1));
			connectLines(new Index(ind.row + 1, ind.col));
			connectLines(new Index(ind.row + 1, ind.col + 1));
		}
	}
	
	private void detectLonelyPoints() {
		if (polyX.size() < 5 || polyY.size() < 5) return;
		
		int row;
		
		for (int i = polyY.size() - 1; i > 0; i--) {
			row = polyY.get(i);
			if (Math.abs(row - polyY.get(i - 1)) > 2) {
				polyY.remove(i);
				polyX.remove(i);
			}
		}
		
		polyY.remove(polyY.size() - 1);
		polyX.remove(polyX.size() - 1);
		polyX.add(polyX.get(0));
		polyY.add(polyY.get(0));
	}
	
	public Point getInitPoint() {
		if (polygon.npoints == 0) return new Point();
		else return new Point(polyX.get(0), polyY.get(0));
	}
	
	public void print() {
		for (int i = 0; i < binaryImg.length; i++) {
			for (int j = 0; j < binaryImg[i].length; j++)
				System.out.print(binaryImg[i][j]);
			System.out.println();
		}
		
		System.out.println();
	}
	
	public Polygon getPolygon() { return polygon; }
}