package gr.aueb.cf.system_management_restAPI.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TestDBHelper {

    @Autowired
    private DataSource dataSource;

    /**
     * Διαγράφει όλα τα δεδομένα από τη test DB εκτός από τον πίνακα 'cities'.
     * Η μέθοδος τρέχει σε transaction, οπότε rollback γίνεται αυτόματα σε περίπτωση exception.
     */
    @Transactional
    public void eraseData() throws SQLException {
        // Απενεργοποίηση foreign key checks
        String sqlFKOff = "SET @@foreign_key_checks = 0";
        String sqlFKOn  = "SET @@foreign_key_checks = 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement psFKOff = connection.prepareStatement(sqlFKOff)) {

            psFKOff.executeUpdate();

            // Ανάγνωση όλων των πινάκων της βάσης (από το current schema)
            String schema = connection.getCatalog(); // παίρνει το schema από το DataSource application-test.properties
            List<String> tables = getAllTables(connection, schema);

            // Διαγραφή δεδομένων & reset AUTO_INCREMENT
            for (String table : tables) {
                if (!table.equals("cities")) {
                    try (PreparedStatement psDel = connection.prepareStatement("DELETE FROM " + table);
                         PreparedStatement psAI  = connection.prepareStatement("ALTER TABLE " + table + " AUTO_INCREMENT=1")) {
                        psDel.executeUpdate();
                        psAI.executeUpdate();
                    }
                }
            }

            // Επανενεργοποίηση foreign key checks
            try (PreparedStatement psFKOn = connection.prepareStatement(sqlFKOn)) {
                psFKOn.executeUpdate();
            }

        }
    }

    /**
     * Διαβάζει όλα τα ονόματα των πινάκων ενός schema και τα επιστρέφει ως λίστα.
     */
    private List<String> getAllTables(Connection connection, String schema) throws SQLException {
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
