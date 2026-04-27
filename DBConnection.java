package eventmgmt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * Singleton JDBC connection utility.
 * Event Management System | Java + MySQL
 */
public class DBConnection {

   private static final String URL = "jdbc:mysql://localhost:3306/event_mgmt_db?useSSL=false&allowPublicKeyRetrieval=true";
   private static final String USER     = "root";
   private static final String PASSWORD = "Nidhii_09*";
   private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to event_mgmt_db successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            System.err.println("[DB] Close error: " + e.getMessage());
        }
    }
}
