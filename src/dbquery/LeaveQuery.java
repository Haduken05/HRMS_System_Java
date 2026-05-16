package dbquery;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveQuery {

    // Used by btnSubmit — checks if the employee exists before filing
    public static String getEmployeeNameById(int empId) {
        String sql = "SELECT full_name FROM employees WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("full_name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // null means employee not found
    }

    // Used by btnSubmit — saves the leave request
    public static boolean submitRequest(int empId, String leaveType,
                                         java.util.Date startDate, java.util.Date endDate,
                                         String medCertPath) {
        String sql = "INSERT INTO leave_requests " +
                     "(emp_id, leave_type, start_date, end_date, medical_cert, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            pst.setString(2, leaveType);
            pst.setDate(3, new java.sql.Date(startDate.getTime()));
            pst.setDate(4, new java.sql.Date(endDate.getTime()));
            pst.setString(5, medCertPath);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Used by displayPendingRequests — fetches only Pending rows with employee name
    public static List<Object[]> getPendingRequests() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT lr.request_id, lr.emp_id, e.full_name, " +
                     "lr.leave_type, lr.start_date, lr.end_date, lr.status " +
                     "FROM leave_requests lr " +
                     "INNER JOIN employees e ON lr.emp_id = e.emp_id " +
                     "WHERE lr.status = 'Pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getInt("request_id"),
                    rs.getInt("emp_id"),
                    rs.getString("full_name"),
                    rs.getString("leave_type"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    // Used by btnApprove and btnDisapprove
    public static boolean updateStatus(String requestId, String newStatus) {
        String sql = "UPDATE leave_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, newStatus);
            pst.setString(2, requestId);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}