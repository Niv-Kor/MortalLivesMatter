package morlivm.database.primary_key;
import morlivm.database.LevelData;

public class LevelPK extends PrimaryKey
{
	public LevelPK(String mapName, Integer level) {
		super(2);
		this.classID = LevelData.TABLE_NAME;			
		add(mapName);
		add(level);
	}
}