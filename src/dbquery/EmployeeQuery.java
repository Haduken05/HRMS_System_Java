package dbquery;

import config.DBConnection;
import dataObject.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeQuery {

    public static List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT emp_id, full_name, department, position, contact_no, role FROM employees";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Employee(
                        rs.getInt("emp_id"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getString("contact_no"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Employee> searchDirectory(String nameSearch, String deptFilter) {
        List<Employee> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT emp_id, full_name, department, position, contact_no, role FROM employees WHERE 1=1");
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
                list.add(new Employee(
                        rs.getInt("emp_id"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getString("contact_no"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Employee getById(int empId) {
        String sql = "SELECT * FROM employees WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Employee(
                        rs.getInt("emp_id"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getString("contact_no"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
