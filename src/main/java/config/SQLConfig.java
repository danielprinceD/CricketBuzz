package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConfig {
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public static Connection connection = null;
    static {
    	try {
    		connection = DriverManager.getConnection(DB_URL , USER , PASS);
		} catch (SQLException e) {
			e.printStackTrace();
			connection = null;
		}
    	
    }
    
}
