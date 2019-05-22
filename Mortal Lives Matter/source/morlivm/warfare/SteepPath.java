package morlivm.warfare;
import morlivm.content.mortal.Player;
import morlivm.map.parallex.GeoLayer;

public class SteepPath
{
	private final static double GRAVITY = -10;
	
	private double initX, initY;
	private double velocity, vX, vY;
	private double theta;
	//private double fireHeight = 0;
	//private double range;
	//private Map ground;
	private double peakPointX, peakPointY;
	//private int directX, directY;
	private double x, y;
	//private Player player;
	
	public SteepPath(double peakPointX, double peakPointY, Player player, GeoLayer ground) {
		
		//this.player = player;
		//this.ground = ground;
		this.peakPointX = peakPointX;
		this.peakPointY = peakPointY;
		
		//this.directX = player.getDirectX();
		//this.directY = 1;
		
		//this.initX = player.getX() + player.getWidth() / 2 * directX;
		this.initY = ground.getY() + 55;
		
		this.x = initX;
		this.y = initY;
		
		this.velocity = 25; //pixels per second
		
		this.theta = thetaCalc(peakPointX, peakPointY, x, y);
		
		this.vY = velocity * Math.sin(theta);
		this.vX = velocity * Math.cos(theta);
	}
	
	public void update(double delta, double x, double y) {
		
		this.theta = thetaCalc(peakPointX, peakPointY, x, y);
		
		this.vX = velocity * Math.cos(theta);
		this.x = velocity * Math.cos(theta) * delta;
		
		this.vY = velocity * Math.sin(theta) + GRAVITY * delta;
		this.y = 0.5 * GRAVITY * Math.pow(delta, 2) + velocity * Math.sin(theta) * delta;
	}
	
	private double thetaCalc(double peakPointX, double peakPointY, double x, double y) {
		
		double tempX, tempY;
		
		if (x != initX) tempX = x;
		else tempX = initX;
		
		if (y != initY) tempY = y;
		else tempY = initY;
		
		double dx = Math.abs(peakPointX - tempX);
		double dy = Math.abs(peakPointY - tempY);
		
		return Math.atan((Math.tan(dy / dx)));
	}
	
	public double getAngle() { return theta; }
	public double getX() { return vX; }
	public double getY() { return vY; }
}