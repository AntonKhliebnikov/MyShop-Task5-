package myshop.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String ENV_DB_URL = "SHOP_DB_URL";
    private static final String ENV_DB_USER = "SHOP_DB_USER";
    private static final String ENV_DB_PASSWORD = "SHOP_DB_PASSWORD";

    private ConnectionManager() {
    }
    public static Connection getConnection() throws SQLException {
        String url = System.getenv(ENV_DB_URL);
        String user = System.getenv(ENV_DB_USER);
        String password = System.getenv(ENV_DB_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }
}
