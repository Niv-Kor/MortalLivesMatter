package morlivm.database.primary_key;
import morlivm.database.ProjectileData;

public class ProjectilePK extends PrimaryKey
{
	public ProjectilePK(String projName, Integer version) {
		super(2);
		this.classID = ProjectileData.TABLE_NAME;
		add(projName);
		add(version);
	}
}