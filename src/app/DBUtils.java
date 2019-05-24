package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens connection to database.
 * Default value is "savt" database.
 */

public class DBUtils {

    public static Connection getConnection() throws SQLException {
        return DriverManager
                .getConnection("jdbc:mysql://localhost/savt?useUnicode=yes&characterEncoding=UTF-8",
                        "root", ""); // localhost;
    }

}
