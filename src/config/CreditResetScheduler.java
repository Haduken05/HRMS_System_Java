package config;

import dbquery.EmployeeQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreditResetScheduler {

    private static final Logger logger = Logger.getLogger(CreditResetScheduler.class.getName());
    private static ScheduledExecutorService scheduler;

    private static final String LAST_RESET_KEY = "credit_reset_year";

    public static void start() {

        checkAndResetIfNewYear();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CreditResetScheduler");
            t.setDaemon(true); // won't block JVM shutdown
            return t;
        });

        long initialDelay = secondsUntilMidnight();
        long oneDayInSeconds = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(
                CreditResetScheduler::checkAndResetIfNewYear,
                initialDelay,
                oneDayInSeconds,
                TimeUnit.SECONDS
        );

        logger.log(Level.INFO, "CreditResetScheduler started. Next check in {0} seconds.", initialDelay);
    }

    private static void checkAndResetIfNewYear() {
        int currentYear = LocalDate.now().getYear();
        int lastResetYear = getLastResetYear();

        if (lastResetYear < currentYear) {
            logger.log(Level.INFO, "New year detected ({0}). Resetting all employee credits to 8...", currentYear);

            boolean ok = EmployeeQuery.resetCreditsForNewYear();
            if (ok) {
                saveLastResetYear(currentYear);
                logger.log(Level.INFO, "Credits reset successfully for year {0}", currentYear);
            } else {
                logger.log(Level.WARNING, "Credit reset FAILED for year {0}", currentYear);
            }
        }
    }

    private static long secondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate()
                .plusDays(1)
                .atStartOfDay();
        return ChronoUnit.SECONDS.between(now, nextMidnight);
    }

    private static int getLastResetYear() {
        String sql = "SELECT config_value FROM app_config WHERE config_key = ?";
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, LAST_RESET_KEY);
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return Integer.parseInt(rs.getString("config_value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; 
    }

    private static void saveLastResetYear(int year) {

        String sql = "INSERT INTO app_config (config_key, config_value) VALUES (?, ?) "
                   + "ON DUPLICATE KEY UPDATE config_value = ?";
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, LAST_RESET_KEY);
            pst.setString(2, String.valueOf(year));
            pst.setString(3, String.valueOf(year));
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}