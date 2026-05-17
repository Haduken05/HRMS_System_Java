package dbquery;

import config.DBConnection;
import java.sql.*;
import java.util.Calendar;

public class AttendanceQuery {

    private static final int LATE_HOUR        = 8;
    private static final int LATE_MINUTE      = 0;
    private static final int HALF_DAY_HOUR    = 12;
    private static final int AFTER_HOURS_HOUR = 17; // 5:00 PM

    public static String deriveStatus() {
        Calendar now = Calendar.getInstance();
        int hour   = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        if (hour > AFTER_HOURS_HOUR || (hour == AFTER_HOURS_HOUR && minute >= 0)) {
            return "After Hours";
        }
        if (hour >= HALF_DAY_HOUR) {
            return "Half-Day";
        }
        if (hour < LATE_HOUR || (hour == LATE_HOUR && minute == LATE_MINUTE)) {
            return "Present";
        }
        return "Late";
    }

    public static String peekStatus() {
        return deriveStatus();
    }

    public static int clockIn(int empId) {
        String status = deriveStatus();
        String sql = "INSERT INTO attendance (emp_id, time_in, date_logged, status) "
                   + "VALUES (?, NOW(), CURDATE(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, empId);
            pst.setString(2, status);
            pst.executeUpdate();
            ResultSet keys = pst.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean clockOut(int attendanceId) {
        String sql = "UPDATE attendance SET time_out = NOW() WHERE attendance_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, attendanceId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasOpenEntry(int empId) {
        String sql = "SELECT attendance_id FROM attendance "
                   + "WHERE emp_id = ? AND date_logged = CURDATE() AND time_out IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}