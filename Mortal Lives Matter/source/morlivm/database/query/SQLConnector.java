package morlivm.database.query;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLConnector
{
	private final static String HOST = "jdbc:mysql://localhost:3306/morlivmdb?useLegacyDatetimeCode=false&serverTimezone=UTC";
	private final static String USERNAME = "root";
	private final static String PASSWORD = "P2413567cu221";
	
	private Connection connection;
	
	public SQLConnector() {}
	
	public Statement connect() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(HOST, USERNAME, PASSWORD);
			return connection.createStatement();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not connect to MySQL database");
		}
		
		return null;
	}
}