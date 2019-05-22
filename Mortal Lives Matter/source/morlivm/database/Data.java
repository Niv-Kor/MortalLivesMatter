package morlivm.database;

import morlivm.database.primary_key.PrimaryKey;
import morlivm.database.query.SQLRetriever;

public abstract class Data
{
	public String tableName = "";
	
	public Data(PrimaryKey pk) {
		extractPrimaryKey(pk);
	}
	
	protected String extractPropertyVARCHAR(String property) {
		return SQLRetriever.retVARCHAR(
			  "SELECT " + property + " "
			+ "FROM " + tableName + " "
			+ condition(),
			property);
	}
	
	protected int extractPropertyINT(String property) {
		return SQLRetriever.retINT(
			  "SELECT " + property + " "
			+ "FROM " + tableName + " "
			+ condition(),
			property);
	}
	
	protected double extractPropertyDECIMAL(String property) {
		return SQLRetriever.retDECIMAL(
			  "SELECT " + property + " "
			+ "FROM " + tableName + " "
			+ condition(),
			property);
	}
	
	protected boolean extractPropertyBOOLEAN(String property) {
		return SQLRetriever.retBOOLEAN(
			  "SELECT " + property + " "
			+ "FROM " + tableName + " "
			+ condition(),
			property);
	}
	
	protected abstract String condition();
	protected abstract void download();
	protected abstract void extractPrimaryKey(PrimaryKey pk);
}