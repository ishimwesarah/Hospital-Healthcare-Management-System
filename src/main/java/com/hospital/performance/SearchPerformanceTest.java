package com.hospital.performance;

import com.hospital.dao.PatientDAO;
import com.hospital.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * Standalone tool (not a JUnit test) that measures the performance impact of the
 * LOWER(...) functional indexes used by PatientDAO.searchPatients().
 *
 * It:
 *   1. Bulk-inserts a large batch of synthetic patients (marked with a recognizable email prefix).
 *   2. Times repeated searches WITH the indexes in place.
 *   3. Drops the indexes.
 *   4. Times the same searches WITHOUT the indexes.
 *   5. Recreates the indexes.
 *   6. Deletes all synthetic data it created.
 *
 * Run this manually (right-click -> Run) whenever you want fresh numbers for the
 * performance report deliverable. It intentionally modifies schema (drops/recreates
 * indexes), so it does NOT run as part of the normal JUnit test suite.
 */
public class SearchPerformanceTest {

    private static final int RECORD_COUNT = 20_000;
    private static final int SEARCH_REPETITIONS = 50;
    private static final String SYNTHETIC_EMAIL_PREFIX = "perf_test_";

    private static final String[] FIRST_NAMES = {
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
            "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica"
    };
    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas"
    };

    public static void main(String[] args) {
        PatientDAO dao = new PatientDAO();

        try {
            System.out.println("=== Patient Search Performance Test ===");
            System.out.println("Inserting " + RECORD_COUNT + " synthetic patient records...");
            insertSyntheticPatients(RECORD_COUNT);
            System.out.println("Insert complete.\n");

            String searchTerm = "Sm"; // matches "Smith" last names, exercises the prefix search path

            System.out.println("--- WITH indexes (idx_patients_lower_first_name / _last_name) ---");
            long withIndexMillis = timeSearch(dao, searchTerm, SEARCH_REPETITIONS);
            System.out.printf("Average time over %d runs: %.3f ms%n%n", SEARCH_REPETITIONS, withIndexMillis / (double) SEARCH_REPETITIONS);

            System.out.println("Dropping indexes...");
            dropIndexes();

            System.out.println("--- WITHOUT indexes ---");
            long withoutIndexMillis = timeSearch(dao, searchTerm, SEARCH_REPETITIONS);
            System.out.printf("Average time over %d runs: %.3f ms%n%n", SEARCH_REPETITIONS, withoutIndexMillis / (double) SEARCH_REPETITIONS);

            System.out.println("Recreating indexes...");
            recreateIndexes();

            double avgWith = withIndexMillis / (double) SEARCH_REPETITIONS;
            double avgWithout = withoutIndexMillis / (double) SEARCH_REPETITIONS;
            double improvementPct = ((avgWithout - avgWith) / avgWithout) * 100;

            System.out.println("=== Summary ===");
            System.out.printf("With index:    %.3f ms (avg)%n", avgWith);
            System.out.printf("Without index: %.3f ms (avg)%n", avgWithout);
            System.out.printf("Improvement:   %.1f%% faster with index%n", improvementPct);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("\nCleaning up synthetic data...");
                cleanUpSyntheticPatients();
                System.out.println("Cleanup complete.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static long timeSearch(PatientDAO dao, String searchTerm, int repetitions) throws SQLException {
        long start = System.nanoTime();
        for (int i = 0; i < repetitions; i++) {
            dao.searchPatients(searchTerm);
        }
        long end = System.nanoTime();
        return (end - start) / 1_000_000; // nanoseconds -> milliseconds
    }

    private static void insertSyntheticPatients(int count) throws SQLException {
        String sql = "INSERT INTO patients (first_name, last_name, date_of_birth, gender, phone_number, email, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Random random = new Random();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (int i = 0; i < count; i++) {
                String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];

                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setDate(3, java.sql.Date.valueOf("1990-01-01"));
                stmt.setString(4, "Other");
                stmt.setString(5, null); // no phone, avoids UNIQUE constraint collisions at scale
                stmt.setString(6, SYNTHETIC_EMAIL_PREFIX + i + "@example.com");
                stmt.setString(7, "Synthetic Test Address");

                stmt.addBatch();

                if (i % 1000 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
            conn.commit();
        }
    }

    private static void cleanUpSyntheticPatients() throws SQLException {
        String sql = "DELETE FROM patients WHERE email LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, SYNTHETIC_EMAIL_PREFIX + "%");
            int deleted = stmt.executeUpdate();
            System.out.println("Deleted " + deleted + " synthetic records.");
        }
    }

    private static void dropIndexes() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP INDEX IF EXISTS idx_patients_lower_first_name");
            stmt.execute("DROP INDEX IF EXISTS idx_patients_lower_last_name");
        }
    }

    private static void recreateIndexes() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE INDEX idx_patients_lower_first_name ON patients (LOWER(first_name))");
            stmt.execute("CREATE INDEX idx_patients_lower_last_name ON patients (LOWER(last_name))");
        }
    }
}