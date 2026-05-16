/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

/**
 *
 * @author jaymark
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;


public class DBConnection {
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // The URL for your hrms_db
            String url = "jdbc:mysql://localhost:3306/hrms_db";
            String user = "root"; // Default XAMPP user
            String pass = "";     // Default XAMPP password is empty
            
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Connection Failed: " + e.getMessage());
        }
        return conn;
    }
}
