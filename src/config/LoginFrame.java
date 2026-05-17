package config;

import theme.SystemTheme;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class LoginFrame extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(LoginFrame.class.getName());

    private static final Color COLOR_PRIMARY_BRAND = SystemTheme.ACCENT_COLOR;
    private static final Color COLOR_BG_FORM = SystemTheme.PRIMARY_COLOR;             
    private static final Color COLOR_TEXT_MAIN = SystemTheme.TEXT_COLOR; 

    private JPanel jPanel1; 
    private JPanel jPanel2; 
    private JLabel jLabel1; 
    private JLabel jLabel2;
    private JLabel jLabel3; 
    private JLabel jLabel4;
    private JTextField txtUsername;
    
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        initComponents();
        
        this.getRootPane().setDefaultButton(btnLogin);
    }

    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("HRMS - Login");
        setResizable(false);

        getContentPane().setLayout(new BorderLayout());

        jPanel1 = new JPanel(new GridBagLayout());
        jPanel1.setBackground(COLOR_PRIMARY_BRAND);
        jPanel1.setPreferredSize(new Dimension(300, 500));
                
        String imgPath = "Hrms_Logo.png";
        
        jLabel4 = new JLabel();
        try {
            java.net.URL imgURL = getClass().getResource("/Image/" + imgPath);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                
                int targetWidth = 250;
                int targetHeight = 140;
                
                Image scaledImage = originalIcon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                
                jLabel4.setIcon(new ImageIcon(scaledImage));
            } else {
                logger.log(Level.WARNING, "Logo image not found at path /Image/{0}", imgPath);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error scaling logo image", e);
        }
        jPanel1.add(jLabel4);

        jPanel2 = new JPanel(new GridBagLayout());
        jPanel2.setBackground(COLOR_BG_FORM);
        jPanel2.setPreferredSize(new Dimension(400, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        jLabel1 = new JLabel("LOGIN FORM");
        jLabel1.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        jLabel1.setForeground(COLOR_TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 45, 45, 45); 
        jPanel2.add(jLabel1, gbc);

        jLabel2 = new JLabel("Username");
        jLabel2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jLabel2.setForeground(COLOR_TEXT_MAIN);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 45, 6, 45);
        jPanel2.add(jLabel2, gbc);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 45, 20, 45); 
        jPanel2.add(txtUsername, gbc);

        jLabel3 = new JLabel("Password");
        jLabel3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jLabel3.setForeground(COLOR_TEXT_MAIN);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 45, 6, 45);
        jPanel2.add(jLabel3, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 45, 25, 45);
        jPanel2.add(txtPassword, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(COLOR_BG_FORM);

        btnLogin = new JButton("Login");
        btnLogin.setPreferredSize(new Dimension(95, 35));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.addActionListener(this::btnLoginActionPerformed);
        buttonPanel.add(btnLogin);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 45, 0, 45);
        jPanel2.add(buttonPanel, gbc);

        getContentPane().add(jPanel1, BorderLayout.WEST);
        getContentPane().add(jPanel2, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void btnLoginActionPerformed(ActionEvent evt) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        String sql = "SELECT * FROM employees WHERE username = ? AND password = ?";

        try (Connection conn = config.DBConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String userRole = rs.getString("role");
                int empId = rs.getInt("emp_id");

                view.MainFrame dashboard = new view.MainFrame();
                dashboard.handleUserSession(fullName, userRole, empId);

                dashboard.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "System Error: " + e.getMessage());
        }
    }
}
