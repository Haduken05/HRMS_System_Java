package dbquery;

import config.DBConnection;
import dataObject.AttendanceRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AttendanceQuery {

    private static final int LATE_HOUR = 8;
    private static final int LATE_MINUTE = 0;
    private static final int HALF_DAY_HOUR = 12;
    private static final int AFTER_HOURS_HOUR = 17;
    private static final int OT_THRESHOLD_MINS = 60;

    public static String deriveStatus() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
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
        String sql = "INSERT INTO attendance (emp_id, time_in, date_logged, status) VALUES (?, NOW(), CURDATE(), ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, empId);
            pst.setString(2, status);
            pst.executeUpdate();
            ResultSet keys = pst.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int clockOut(int empId) {
        int attendanceId = getOpenEntryId(empId);
        if (attendanceId < 0) {
            return -1;
        }

        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int minutesPast5 = (hour - AFTER_HOURS_HOUR) * 60 + minute;
        boolean isOT = minutesPast5 >= OT_THRESHOLD_MINS;

        String sql = isOT
                ? "UPDATE attendance SET time_out = NOW(), ot_hours = ? WHERE attendance_id = ?"
                : "UPDATE attendance SET time_out = NOW() WHERE attendance_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            if (isOT) {
                pst.setInt(1, minutesPast5 / 60);
                pst.setInt(2, attendanceId);
            } else {
                pst.setInt(1, attendanceId);
            }
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        if (isOT) {
            creditOT(empId, minutesPast5 / 60);
        }
        return attendanceId;
    }

    public static int computeOTHours() {
        Calendar now = Calendar.getInstance();
        int minutesPast5 = (now.get(Calendar.HOUR_OF_DAY) - AFTER_HOURS_HOUR) * 60
                + now.get(Calendar.MINUTE);
        return minutesPast5 < OT_THRESHOLD_MINS ? 0 : minutesPast5 / 60;
    }

    private static void creditOT(int empId, int otHours) {
        String sql = "UPDATE employees SET ot_credits = ot_credits + ? WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, otHours);
            pst.setInt(2, empId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasOpenEntry(int empId) {
        return getOpenEntryId(empId) >= 0;
    }

    public static int getOpenEntryId(int empId) {
        String sql = "SELECT attendance_id FROM attendance WHERE emp_id = ? AND date_logged = CURDATE() AND time_out IS NULL";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("attendance_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void autoTimeoutForgottenEntries() {
        String sql = "UPDATE attendance SET time_out = TIMESTAMP(date_logged, '17:00:00'), "
                + "remarks = 'Missed Time Out' WHERE time_out IS NULL AND date_logged < CURDATE()";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            int fixed = pst.executeUpdate();
            if (fixed > 0) {
                System.out.println("[AttendanceQuery] Auto-timed-out " + fixed + " forgotten entries.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<AttendanceRecord> getAttendanceReport(
            String dateFrom, String dateTo, String deptFilter) {

        List<AttendanceRecord> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT a.attendance_id, a.emp_id, e.full_name, e.department, "
                + "       a.time_in, a.time_out, a.date_logged, a.status, a.remarks, a.ot_hours "
                + "FROM attendance a "
                + "JOIN employees e ON a.emp_id = e.emp_id "
                + "WHERE 1=1 ");

        if (dateFrom != null) {
            sql.append("AND a.date_logged >= ? ");
        }
        if (dateTo != null) {
            sql.append("AND a.date_logged <= ? ");
        }
        if (deptFilter != null) {
            sql.append("AND e.department = ? ");
        }
        sql.append("ORDER BY a.date_logged DESC, a.time_in DESC");

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (dateFrom != null) {
                pst.setString(idx++, dateFrom);
            }
            if (dateTo != null) {
                pst.setString(idx++, dateTo);
            }
            if (deptFilter != null) {
                pst.setString(idx++, deptFilter);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new AttendanceRecord(
                        rs.getInt("attendance_id"),
                        rs.getInt("emp_id"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getTimestamp("time_in"),
                        rs.getTimestamp("time_out"),
                        rs.getString("date_logged"),
                        rs.getString("status"),
                        rs.getString("remarks"),
                        rs.getInt("ot_hours")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean hasAttendanceToday(int empId) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE emp_id = ? AND date_logged = CURDATE()";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
