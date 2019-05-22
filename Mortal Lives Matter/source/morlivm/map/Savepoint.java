package morlivm.map;
import java.awt.Dimension;
import morlivm.control_panel.Purchasable;
import morlivm.control_panel.SaleAd;
import morlivm.database.DataManager;
import morlivm.main.Game;
import morlivm.main.Structure;
import morlivm.map.orientation.Topology;
import morlivm.map.parallex.Ground;
import morlivm.memory.DeathControl;
import morlivm.state.GameState;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Animation;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.sheet.Pamphlet;

public class Savepoint implements Attachable, Purchasable
{
	private boolean masthead;
	private Point point, fixedPoint;
	private Animation animation;
	private Dimension dim;
	private Ground ground;
	private GameState gameState;
	private SaleAd saleAd;
	private Boundary boundary;
	
	public Savepoint(Point point, GameState gs) {
		this.gameState = gs;
		this.masthead = false;
		this.point = new Point(point);
		this.ground = gs.getArena().getGround();
		this.fixedPoint = new Point(point, 0, 0, Topology.groundY(point.getX()));
		
		Pamphlet sheet = DataManager.retSheet("ss$a$savepoint_" + Structure.getDatabase().mapName);
		this.animation = new Animation(sheet);
		this.dim = new Dimension(sheet.getDimension());
		this.saleAd = new SaleAd(SaleAd.Product.SAVEPOINT, this, point, gs.getControlPanel());
		this.boundary = new Boundary(sheet, this);
		Boundary.add(boundary);
	}
	
	public void update(double delta) {
		saleAd.update(delta);
		ground.moveAlong(this, delta);
		
		int state = masthead ? 2 : 1;
		animation.setRow(state, false);
		animation.update(delta);
	}

	public void render(ExtendedGraphics2D g) {
		g.drawImage(animation.getImage(), point, null);
		saleAd.render(g);
	}

	public void raiseFlag() {
		masthead = true;
		DeathControl.save(true);
	}
	
	public boolean checkPurchaseRelevance() {
		return !masthead && gameState.getPlayer().getPoint().distance(getPoint()) > Game.WIDTH / 4;
	}
	
	public String getDeclineErrorMessage() {
		if (masthead) return "Savepoint is already checked.";
		else if (gameState.getPlayer().getPoint().distance(getPoint()) > Game.WIDTH / 4) return "You are too far to do that.";
		return " "; //formal return statement
	}
	
	public void setX(double x) {
		point.setX(x);
		saleAd.setX(x);
	}
	
	public void setY(double y) {
		point.setY(y);
		saleAd.setY(y);
	}
	
	public void setDimension(Dimension d) {}
	public Dimension getDimension() { return new Dimension(dim); }
	public Point getPoint() { return point; }
	public double getX() { return point.getX(); }
	public double getY() { return point.getY(); }
	public String getAdDescription() { return "savepoint "; }
	public Dimension getAdDimension() { return new Dimension(dim); }
	public void purchase() { raiseFlag(); }
	public Point getFixedPoint() { return fixedPoint; }
}