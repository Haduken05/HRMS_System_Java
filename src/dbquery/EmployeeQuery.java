package dbquery;

import config.DBConnection;
import dataObject.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeQuery {

    public static List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT emp_id, full_name, username, department, position, contact_no, "
                + "profile_pic, role, password, hire_date, vl_credits, sl_credits FROM employees ORDER BY emp_id ASC";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Employee> searchDirectory(String nameSearch, String deptFilter) {
        List<Employee> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT emp_id, full_name, username, department, position, contact_no, "
                + "profile_pic, role, password, hire_date, vl_credits, sl_credits FROM employees WHERE 1=1");

        if (!nameSearch.isEmpty()) {
            sql.append(" AND LOWER(full_name) LIKE LOWER(?)");
        }
        if (!deptFilter.equals("All")) {
            sql.append(" AND department = ?");
        }
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (!nameSearch.isEmpty()) {
                pst.setString(i++, "%" + nameSearch + "%");
            }
            if (!deptFilter.equals("All")) {
                pst.setString(i++, deptFilter);
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Employee getById(int empId) {
        String sql = "SELECT emp_id, full_name, username, department, position, contact_no, "
                + "profile_pic, role, password, hire_date, vl_credits, sl_credits FROM employees WHERE emp_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int insertEmployee(String fullName, String department,
            String position, String contactNo,
            String role) {
        String sql = "INSERT INTO employees (full_name, department, position, contact_no, role) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, fullName);
            pst.setString(2, department);
            pst.setString(3, position);
            pst.setString(4, contactNo);
            pst.setString(5, role);
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

    public static boolean archiveAndDelete(int empId, String reason) {
        String archiveSql
                = "INSERT INTO archived_employees "
                + "(emp_id, full_name, department, position, contact_no, role, "
                + " hire_date, offboard_reason) "
                + "SELECT emp_id, full_name, department, position, contact_no, role, "
                + "       hire_date, ? "
                + "FROM employees WHERE emp_id = ?";

        String deleteSql = "DELETE FROM employees WHERE emp_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // start transaction

            // Step 1 — copy to archive
            try (PreparedStatement archivePst = conn.prepareStatement(archiveSql)) {
                archivePst.setString(1, reason);
                archivePst.setInt(2, empId);
                int archived = archivePst.executeUpdate();
                if (archived == 0) {
                    conn.rollback();
                    return false; // employee not found
                }
            }

            // Step 2 — remove from active table
            try (PreparedStatement deletePst = conn.prepareStatement(deleteSql)) {
                deletePst.setInt(1, empId);
                deletePst.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean deleteEmployee(int empId) {
        String sql = "DELETE FROM employees WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("emp_id"),
                rs.getString("full_name"),
                rs.getString("username"),
                rs.getString("department"),
                rs.getString("position"),
                rs.getString("contact_no"),
                rs.getString("profile_pic"), // ← add
                rs.getString("role"),
                rs.getString("password"),
                rs.getDate("hire_date"),
                rs.getInt("vl_credits"),
                rs.getInt("sl_credits")
        );
    }

    public static boolean updatePassword(int empId, String newPassword) {
        String sql = "UPDATE employees SET password = ? WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, newPassword);
            pst.setInt(2, empId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean resetCreditsForNewYear() {
        String sql = "UPDATE employees SET vl_credits = 8, sl_credits = 8";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
