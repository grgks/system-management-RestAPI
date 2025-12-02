package gr.aueb.cf.system_management_restAPI.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing the test database.
 * Provides static methods to:
 * 1. Delete all data from all tables except specific ones (e.g., 'cities').
 * 2. Reset AUTO_INCREMENT for each table.
 * Uses DataSource provided by Spring (which handles connection properties).
 * No instances of this class are allowed.
 */
public class TestDBHelper {

    /**
     * Private constructor to prevent instantiation
     * of this utility class.
     */
    private TestDBHelper() {
        // no instances
    }

    /**
     * Deletes all data from the test database except selected tables
     * and resets AUTO_INCREMENT for each table.
     * Uses the provided DataSource from Spring context.
     *
     * @param dataSource the DataSource bean from Spring
     * @throws SQLException if deletion or alter fails
     */
    public static void eraseData(DataSource dataSource) throws SQLException {
        String sqlFKOff = "SET @@foreign_key_checks = 0";    // Disable foreign key checks
        String sqlFKOn  = "SET @@foreign_key_checks = 1";    // Re-enable foreign key checks

        try (Connection connection = dataSource.getConnection();
             PreparedStatement psFKOff = connection.prepareStatement(sqlFKOff)) {

            // Safety check: MUST be test database
            String dbName = connection.getCatalog();
            if (!dbName.contains("_test")) {
                throw new IllegalStateException(
                        "SAFETY CHECK FAILED! Refusing to erase non-test database: " + dbName +
                                "\nTests should only use databases ending with '_test'!"
                );
            }


            // Disable foreign key checks
            psFKOff.executeUpdate();

            // Find all tables from schema
            String schema = connection.getCatalog();
            List<String> tables = getAllTables(connection, schema);

            // Delete data & reset AUTO_INCREMENT for each table
            for (String table : tables) {
                if (!table.equals("cities")) {       // if we want to keep a table
                    try (PreparedStatement psDel = connection.prepareStatement("DELETE FROM " + table);
                         PreparedStatement psAI  = connection.prepareStatement("ALTER TABLE " + table + " AUTO_INCREMENT=1")) {
                        psDel.executeUpdate();      // Delete all rows
                        psAI.executeUpdate();       // Reset AUTO_INCREMENT
                    }
                }
            }

            // Re-enable foreign key checks
            try (PreparedStatement psFKOn = connection.prepareStatement(sqlFKOn)) {
                psFKOn.executeUpdate();
            }
        }
    }

    /**
     * Returns the names of all tables in a given schema.
     *
     * @param connection active JDBC connection
     * @param schema schema name
     * @return list of table names
     * @throws SQLException if the query fails
     */
    private static List<String> getAllTables(Connection connection, String schema) throws SQLException {
        List<String> tables = new ArrayList<>();
        String sqlSelect = "SELECT TABLE_NAME FROM information_schema.tables WHERE TABLE_SCHEMA = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlSelect)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        }
        return tables;
    }
}