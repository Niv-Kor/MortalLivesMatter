package morlivm.database.primary_key;
import morlivm.database.MortalData;

public class MortalPK extends PrimaryKey
{
	public MortalPK(Integer id) {
		super(1);
		this.classID = MortalData.TABLE_NAME;
		add(id);
	}
}