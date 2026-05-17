package dbquery;

import config.DBConnection;
import dataObject.LeaveRequestEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveQuery {

    public static String getEmployeeNameById(int empId) {
        String sql = "SELECT full_name FROM employees WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<LeaveRequestEntity> getPendingRequests() {
        return getRequestsByStatus("Pending");
    }

    public static List<LeaveRequestEntity> getRequestsByStatus(String status) {
        List<LeaveRequestEntity> list = new ArrayList<>();
        String sql = "SELECT lr.request_id, lr.emp_id, e.full_name, "
                + "lr.leave_type, lr.start_date, lr.end_date, lr.status "
                + "FROM leave_requests lr "
                + "INNER JOIN employees e ON lr.emp_id = e.emp_id "
                + "WHERE lr.status = ? ORDER BY lr.request_id DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean updateStatus(String requestId, String newStatus) {
        String sql = "UPDATE leave_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, newStatus);
            pst.setString(2, requestId);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getVLCredits(int empId) {
        return getCredits(empId, "vl_credits");
    }

    public static int getSLCredits(int empId) {
        return getCredits(empId, "sl_credits");
    }

    private static int getCredits(int empId, String column) {
        String sql = "SELECT " + column + " FROM employees WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean deductCredits(int empId, String leaveType, int days) {
        String column = leaveType.equals("VL") ? "vl_credits" : "sl_credits";
        String sql = "UPDATE employees SET " + column
                + " = GREATEST(0, " + column + " - ?) WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, days);
            pst.setInt(2, empId);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean submitRequest(int empId, String leaveType,
            java.util.Date startDate, java.util.Date endDate, String medCertPath) {
        String sql = "INSERT INTO leave_requests "
                + "(emp_id, leave_type, start_date, end_date, medical_cert, status) "
                + "VALUES (?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            pst.setString(2, leaveType);
            pst.setDate(3, new java.sql.Date(startDate.getTime()));
            pst.setDate(4, new java.sql.Date(endDate.getTime()));
            pst.setString(5, (medCertPath == null || medCertPath.isBlank()) ? null : medCertPath);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //  Overlap check
    public static boolean hasOverlap(int empId, java.util.Date startDate, java.util.Date endDate) {
        String sql = "SELECT COUNT(*) FROM leave_requests "
                + "WHERE emp_id = ? "
                + "AND status IN ('Pending', 'Approved') "
                + "AND start_date <= ? "
                + "AND end_date   >= ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            pst.setDate(2, new java.sql.Date(endDate.getTime()));
            pst.setDate(3, new java.sql.Date(startDate.getTime()));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //  Approved leaves for Profile
    public static List<LeaveRequestEntity> getApprovedLeaves(int empId) {
        List<LeaveRequestEntity> list = new ArrayList<>();
        String sql = "SELECT lr.request_id, lr.emp_id, e.full_name, "
                + "lr.leave_type, lr.start_date, lr.end_date, lr.status "
                + "FROM leave_requests lr "
                + "INNER JOIN employees e ON lr.emp_id = e.emp_id "
                + "WHERE lr.emp_id = ? AND lr.status = 'Approved' "
                + "ORDER BY lr.start_date DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static LeaveRequestEntity mapEntity(ResultSet rs) throws SQLException {
        return new LeaveRequestEntity(
                rs.getInt("request_id"),
                rs.getInt("emp_id"),
                rs.getString("full_name"),
                rs.getString("leave_type"),
                rs.getDate("start_date"),
                rs.getDate("end_date"),
                rs.getString("status")
        );
    }

    public static int getPendingDays(int empId, String leaveType) {
        String sql = "SELECT start_date, end_date FROM leave_requests "
                + "WHERE emp_id = ? AND leave_type = ? AND status = 'Pending'";
        int total = 0;
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            pst.setString(2, leaveType);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                long start = rs.getDate("start_date").getTime();
                long end = rs.getDate("end_date").getTime();
                long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(end - start) + 1;
                total += (int) days;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public static int[] getLeaveDays(String requestId) {
        String sql = "SELECT start_date, end_date FROM leave_requests WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, requestId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                long start = rs.getDate("start_date").getTime();
                long end = rs.getDate("end_date").getTime();
                int days = (int) (java.util.concurrent.TimeUnit.MILLISECONDS.toDays(end - start) + 1);
                return new int[]{days};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<LeaveRequestEntity> getPendingLeavesByEmployee(int empId) {
        List<LeaveRequestEntity> list = new ArrayList<>();
        String sql = "SELECT lr.request_id, lr.emp_id, e.full_name, "
                + "lr.leave_type, lr.start_date, lr.end_date, lr.status "
                + "FROM leave_requests lr "
                + "INNER JOIN employees e ON lr.emp_id = e.emp_id "
                + "WHERE lr.emp_id = ? AND lr.status = 'Pending' "
                + "ORDER BY lr.start_date ASC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
