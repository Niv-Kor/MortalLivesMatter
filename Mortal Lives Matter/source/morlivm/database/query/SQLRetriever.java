package morlivm.database.query;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import morlivm.database.DataManager;

public class SQLRetriever
{
	public static final int NaN = Integer.MAX_VALUE;
	
	private static Statement statement;
	private static ResultSet resultSet;
	private static SQLConnector connector;
	
	public static void init() {
		DataManager.init();
		connector = new SQLConnector();
		statement = connector.connect();
	}
	
	public static int retINT(String query, String column) {
		try {
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) return resultSet.getInt(column);
		}
		catch (Exception e) { printError(query); }
		return NaN;
	}
	
	public static ArrayList<Integer> retListINT(String query, String column) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		try {
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) list.add(resultSet.getInt(column));
		}
		catch (Exception e) { printError(query); }
		return list;
	}
	
	public static double retDECIMAL(String query, String column) {
		try {
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) return resultSet.getDouble(column);
		}
		catch (Exception e) { printError(query); }
		return NaN;
	}
	
	public static ArrayList<Double> retListDECIMAL(String query, String column) {
		ArrayList<Double> list = new ArrayList<Double>();
		
		try {
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) list.add(resultSet.getDouble(column));
		}
		catch (Exception e) { printError(query); }
		return list;
	}
	
	public static String retVARCHAR(String query, String column) {
		try {
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) return resultSet.getString(column);
		}
		catch (Exception e) { printError(query); }
		return null;
	}
	
	public static ArrayList<String> retListVARCHAR(String query, String column) {
		ArrayList<String> list = new ArrayList<String>();
		
		try {
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) list.add(resultSet.getString(column));
		}
		catch (Exception e) { printError(query); }
		return list;
	}
	
	public static boolean retBOOLEAN(String query, String column) {
		try {
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) return resultSet.getBoolean(column);
		}
		catch (Exception e) { printError(query); }
		return false;
	}
	
	public static ArrayList<Boolean> retListBOOLEAN(String query, String column) {
		ArrayList<Boolean> list = new ArrayList<Boolean>();
		
		try {
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) list.add(resultSet.getBoolean(column));
		}
		catch (Exception e) { printError(query); }
		return list;
	}
	
	public static String buildQuery(String select, String from, String where, int equals) {
		return "SELECT " + select + " "
			 + "FROM " + from + " "
			 + "WHERE " + where + " = " + equals + ";";
	}
	
	public static String buildQuery(String select, String from, String where, double equals) {
		return "SELECT " + select + " "
			 + "FROM " + from + " "
			 + "WHERE " + where + " = " + equals + ";";
	}
	
	public static String buildQuery(String select, String from, String where, String equals) {
		return "SELECT " + select + " "
			 + "FROM " + from + " "
			 + "WHERE " + where + " = '" + equals + "';";
	}
	
	public static String buildQuery(String select, String from, String where, boolean equals) {
		return "SELECT " + select + " "
			 + "FROM " + from + " "
			 + "WHERE " + where + " = " + equals + ";";
	}
	
	private static void printError(String query) {
		System.err.println("Could not retrieve data for the query: \n" + query + "\n\n");
	}
}