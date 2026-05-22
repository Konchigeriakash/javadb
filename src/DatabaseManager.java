import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    public static final String DB_NAME = "javadb";
    private static final String URL =
            "jdbc:mysql://172.23.169.219:3306/javadb";
    private static final String USER = "javauser";
    private static final String PASS = "JavaUser@123";

    private DatabaseManager() {
    }

    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
