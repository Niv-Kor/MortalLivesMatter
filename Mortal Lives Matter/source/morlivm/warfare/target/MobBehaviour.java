package morlivm.warfare.target;
import morlivm.content.mortal.Mortal;
import morlivm.system.UI.Point;
import morlivm.system.performance.TimingDevice;

public abstract class MobBehaviour extends Target
{
	protected Mortal follower, followee;
	protected TimingDevice timingDevice;
	
	public MobBehaviour(Mortal entity, Mortal followee) {
		super(entity, new Point(followee.getX() + followee.getDimension().width / 2, followee.getZ()));
		this.follower = entity;
		this.followee = followee;
		this.timingDevice = new TimingDevice();
	}
	
	public void setTarget(Mortal e) {
		point = new Point(e.getMidX(), e.getZ());
	}

	public abstract void follow(double delta);
}