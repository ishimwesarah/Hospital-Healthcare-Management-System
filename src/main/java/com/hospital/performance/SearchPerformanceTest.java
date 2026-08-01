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

    // Only this fraction of rows will actually match the search term below.
    // Realistic patient searches target a specific person, not 6% of the hospital's records —
    // low selectivity (searching for something common) is exactly when a sequential scan
    // beats an index scan, so we need the search term to be genuinely rare to see the index help.
    private static final double MATCHING_FRACTION = 0.005; // ~0.5% of rows will start with "Zq"
    private static final String SEARCH_TERM = "Zq";

    public static void main(String[] args) {
        PatientDAO dao = new PatientDAO();

        try {
            System.out.println("=== Patient Search Performance Test ===");
            System.out.println("Inserting " + RECORD_COUNT + " synthetic patient records...");
            insertSyntheticPatients(RECORD_COUNT);
            System.out.println("Insert complete.\n");

            System.out.println("--- WITH indexes (idx_patients_lower_first_name / _last_name) ---");
            printQueryPlan(SEARCH_TERM);
            long withIndexMillis = timeSearch(dao, SEARCH_TERM, SEARCH_REPETITIONS);
            System.out.printf("Average time over %d runs (connection reused): %.3f ms%n%n",
                    SEARCH_REPETITIONS, withIndexMillis / (double) SEARCH_REPETITIONS);

            System.out.println("Dropping indexes...");
            dropIndexes();

            System.out.println("--- WITHOUT indexes ---");
            printQueryPlan(SEARCH_TERM);
            long withoutIndexMillis = timeSearch(dao, SEARCH_TERM, SEARCH_REPETITIONS);
            System.out.printf("Average time over %d runs (connection reused): %.3f ms%n%n",
                    SEARCH_REPETITIONS, withoutIndexMillis / (double) SEARCH_REPETITIONS);

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

    // Prints Postgres's actual query plan (Index Scan vs Seq Scan) and its own internal execution
    // time, which excludes JDBC connection overhead — this is the authoritative number for the report.
    private static void printQueryPlan(String searchTerm) throws SQLException {
        String sql = "EXPLAIN ANALYZE SELECT * FROM patients " +
                "WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
        String pattern = searchTerm.toLowerCase() + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("  " + rs.getString(1));
                }
            }
        }
    }

    // Reuses ONE connection across all repetitions, so we're timing query execution,
    // not repeated TCP handshake + auth overhead.
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
        int matchTarget = (int) (count * MATCHING_FRACTION);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (int i = 0; i < count; i++) {
                String firstName;
                String lastName;

                if (i < matchTarget) {
                    // The rare, deliberately searchable group — e.g. "Zqvuu123 Zqrhen456"
                    firstName = "Zq" + randomLetters(random, 6);
                    lastName = "Zq" + randomLetters(random, 6);
                } else {
                    // Everything else: random letters, guaranteed NOT to start with "Zq"
                    firstName = randomLetters(random, 8);
                    lastName = randomLetters(random, 8);
                }

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

    private static String randomLetters(Random random, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
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