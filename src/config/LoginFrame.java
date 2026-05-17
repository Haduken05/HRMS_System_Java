package config;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class LoginFrame extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(LoginFrame.class.getName());

    // ==========================================
    // UI COLOR PALETTE
    // ==========================================
    private static final Color COLOR_PRIMARY_BRAND = new Color(255, 0, 255); // Magenta Sidebar
    private static final Color COLOR_BG_FORM = Color.WHITE;             // Form Background
    private static final Color COLOR_TEXT_MAIN = new Color(33, 33, 33);   // Dark Charcoal/Black

    // UI Components
    private JPanel jPanel1; // Left Panel (Logo)
    private JPanel jPanel2; // Right Panel (Form)
    private JLabel jLabel1; // Title
    private JLabel jLabel2; // Username Label
    private JLabel jLabel3; // Password Label
    private JLabel jLabel4; // Logo Image Holder
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    // Unused NetBeans remnants preserved for compatibility
    private JScrollPane jScrollPane1;
    private JTextArea jTextArea1;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        // Initialize unused components to prevent null pointers
        jTextArea1 = new JTextArea(5, 20);
        jScrollPane1 = new JScrollPane(jTextArea1);

        // Frame configurations
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("HRMS - Login");
        setResizable(false);

        // Use a main BorderLayout split into Left (West) and Right (Center)
        getContentPane().setLayout(new BorderLayout());

        // ==========================================
        // LEFT PANEL: Logo Panel
        // ==========================================
        jPanel1 = new JPanel(new GridBagLayout());
        jPanel1.setBackground(COLOR_PRIMARY_BRAND);
        jPanel1.setPreferredSize(new Dimension(300, 500));

        jLabel4 = new JLabel();
        try {
            jLabel4.setIcon(new ImageIcon(getClass().getResource("/Image/Hrms_logo_2.png")));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Logo image not found at /Image/Hrms_logo_2.png");
        }
        jPanel1.add(jLabel4);

        // ==========================================
        // RIGHT PANEL: Credentials Form
        // ==========================================
        jPanel2 = new JPanel(new GridBagLayout());
        jPanel2.setBackground(COLOR_BG_FORM);
        jPanel2.setPreferredSize(new Dimension(400, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 1. Form Title
        jLabel1 = new JLabel("LOGIN FORM");
        jLabel1.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        jLabel1.setForeground(COLOR_TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 45, 45, 45); // Generous space below title
        jPanel2.add(jLabel1, gbc);

        // 2. Username Label
        jLabel2 = new JLabel("Username");
        jLabel2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jLabel2.setForeground(COLOR_TEXT_MAIN);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 45, 6, 45); // Small space below label
        jPanel2.add(jLabel2, gbc);

        // 3. Username Field
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 45, 20, 45); // Spacing gap before next block element
        jPanel2.add(txtUsername, gbc);

        // 4. Password Label
        jLabel3 = new JLabel("Password");
        jLabel3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jLabel3.setForeground(COLOR_TEXT_MAIN);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 45, 6, 45);
        jPanel2.add(jLabel3, gbc);

        // 5. Password Field
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 45, 25, 45);
        jPanel2.add(txtPassword, gbc);

        // 6. Button Section (Right aligned)
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

        // Bind main containers to the Frame Content Pane
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

        String sql = "SELECT * FROM employees WHERE full_name = ? AND password = ?";

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
