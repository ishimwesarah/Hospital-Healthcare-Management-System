package com.hospital.performance;

import com.hospital.db.DatabaseConnection;
import com.hospital.service.PatientService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

/**
 * Standalone tool (not a JUnit test) measuring the performance impact of PatientCache.
 *
 * "Without cache" is simulated by creating a FRESH PatientService for every call — since each
 * new instance starts with an empty cache, every call is forced to hit the database, exactly
 * what would happen if no caching layer existed at all.
 *
 * "With cache" uses a SINGLE PatientService instance: the first call populates the cache
 * (a real DB hit), and every call after that is served from the in-memory HashMap.
 */
public class CachePerformanceTest {

    private static final int RECORD_COUNT = 5_000;
    private static final int REPETITIONS = 50;
    private static final String SYNTHETIC_EMAIL_PREFIX = "cache_perf_test_";

    public static void main(String[] args) {
        try {
            System.out.println("=== Patient Cache Performance Test ===");
            System.out.println("Inserting " + RECORD_COUNT + " synthetic patient records...");
            insertSyntheticPatients(RECORD_COUNT);
            System.out.println("Insert complete.\n");

            System.out.println("--- WITHOUT cache (fresh PatientService per call, every call hits the DB) ---");
            long withoutCacheMillis = timeWithoutCache(REPETITIONS);
            double avgWithoutCache = withoutCacheMillis / (double) REPETITIONS;
            System.out.printf("Average time over %d calls: %.3f ms%n%n", REPETITIONS, avgWithoutCache);

            System.out.println("--- WITH cache (single PatientService, first call populates cache) ---");
            long withCacheMillis = timeWithCache(REPETITIONS);
            double avgWithCache = withCacheMillis / (double) REPETITIONS;
            System.out.printf("Average time over %d calls (including the initial DB-hit call): %.3f ms%n%n",
                    REPETITIONS, avgWithCache);

            double improvementPct = ((avgWithoutCache - avgWithCache) / avgWithoutCache) * 100;

            System.out.println("=== Summary ===");
            System.out.printf("Without cache: %.3f ms (avg per call)%n", avgWithoutCache);
            System.out.printf("With cache:    %.3f ms (avg per call, including one real DB hit)%n", avgWithCache);
            System.out.printf("Improvement:   %.1f%% faster with cache%n", improvementPct);

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

    private static long timeWithoutCache(int repetitions) throws SQLException {
        long start = System.nanoTime();
        for (int i = 0; i < repetitions; i++) {
            new PatientService().getAllPatients(); // fresh instance -> empty cache -> forced DB hit every time
        }
        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }

    private static long timeWithCache(int repetitions) throws SQLException {
        PatientService service = new PatientService(); // ONE instance, reused for all repetitions
        long start = System.nanoTime();
        for (int i = 0; i < repetitions; i++) {
            service.getAllPatients(); // first call: DB hit + cache populate. All others: cache hit.
        }
        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }

    private static void insertSyntheticPatients(int count) throws SQLException {
        String sql = "INSERT INTO patients (first_name, last_name, date_of_birth, gender, phone_number, email, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Random random = new Random();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (int i = 0; i < count; i++) {
                stmt.setString(1, randomLetters(random, 8));
                stmt.setString(2, randomLetters(random, 8));
                stmt.setDate(3, java.sql.Date.valueOf("1990-01-01"));
                stmt.setString(4, "Other");
                stmt.setString(5, null);
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
}