package dbquery;

import config.DBConnection;
import dataObject.LeaveReport;
import dataObject.StatusCount;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportQuery {

    public static List<LeaveReport> getLeaveReport(String statusFilter) {
        List<LeaveReport> list = new ArrayList<>();
        String sql = "SELECT e.full_name, l.leave_type, l.start_date, l.end_date, l.status "
                + "FROM employees e JOIN leave_requests l ON e.emp_id = l.emp_id";
        if (!statusFilter.equals("All")) {
            sql += " WHERE l.status = ?";
        }

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            if (!statusFilter.equals("All")) {
                pst.setString(1, statusFilter);
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new LeaveReport(
                        rs.getString("full_name"),
                        rs.getString("leave_type"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static StatusCount getStatusCounts() {
        String sql = "SELECT "
                + "SUM(CASE WHEN status = 'Pending'     THEN 1 ELSE 0 END) AS pending, "
                + "SUM(CASE WHEN status = 'Approved'    THEN 1 ELSE 0 END) AS approved, "
                + "SUM(CASE WHEN status = 'Disapproved' THEN 1 ELSE 0 END) AS disapproved "
                + "FROM leave_requests";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return new StatusCount(
                        rs.getInt("pending"),
                        rs.getInt("approved"),
                        rs.getInt("disapproved")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StatusCount(0, 0, 0);
    }
}
