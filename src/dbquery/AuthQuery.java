package dbquery;

import config.DBConnection;
import dataObject.SessionUser;

import java.sql.*;

public class AuthQuery {

    public static SessionUser login(String username, String password) {
        String sql = "SELECT emp_id, full_name, username, role, profile_pic "
                + "FROM employees WHERE username = ? AND password = ?";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new SessionUser(
                        rs.getInt("emp_id"),
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("profile_pic")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
