package config;

import theme.SystemTheme;
import dbquery.AuthQuery;
import dataObject.SessionUser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.*;

public class LoginFrame extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(LoginFrame.class.getName());

    private static final Color COLOR_PRIMARY_BRAND = SystemTheme.ACCENT_COLOR;
    private static final Color COLOR_BG_FORM = SystemTheme.PRIMARY_COLOR;
    private static final Color COLOR_TEXT_MAIN = SystemTheme.TEXT_COLOR;

    private JPanel jPanel1, jPanel2;
    private JLabel jLabel1, jLabel2, jLabel3, jLabel4;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private final view.MainFrame existingDashboard;
     
    public LoginFrame() {
        this(null);
    }

    public LoginFrame(view.MainFrame existingDashboard) {
        this.existingDashboard = existingDashboard;
        initComponents();
        getRootPane().setDefaultButton(btnLogin);
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("HRMS - Login");
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());

        jPanel1 = new JPanel(new GridBagLayout());
        jPanel1.setBackground(COLOR_PRIMARY_BRAND);
        jPanel1.setPreferredSize(new Dimension(300, 500));

        jLabel4 = new JLabel();
        try {
            java.net.URL imgURL = getClass().getResource("/Image/Hrms_Logo.png");
            if (imgURL != null) {
                Image scaled = new ImageIcon(imgURL).getImage()
                        .getScaledInstance(250, 140, Image.SCALE_SMOOTH);
                jLabel4.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception ignored) {
        }
        jPanel1.add(jLabel4);

        jPanel2 = new JPanel(new GridBagLayout());
        jPanel2.setBackground(COLOR_BG_FORM);
        jPanel2.setPreferredSize(new Dimension(400, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        String heading = (existingDashboard != null) ? "SWITCH ACCOUNT" : "LOGIN FORM";
        jLabel1 = new JLabel(heading);
        jLabel1.setFont(SystemTheme.BIG_TEXT_PLAIN);
        jLabel1.setForeground(COLOR_TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 45, 45, 45);
        jPanel2.add(jLabel1, gbc);

        jLabel2 = new JLabel("Username");
        jLabel2.setFont(SystemTheme.BOLD_TEXT);
        jLabel2.setForeground(COLOR_TEXT_MAIN);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 45, 6, 45);
        jPanel2.add(jLabel2, gbc);

        txtUsername = new JTextField();
        txtUsername.setFont(SystemTheme.NORMAL_TEXT);
        txtUsername.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 45, 20, 45);
        jPanel2.add(txtUsername, gbc);

        jLabel3 = new JLabel("Password");
        jLabel3.setFont(SystemTheme.BOLD_TEXT);
        jLabel3.setForeground(COLOR_TEXT_MAIN);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 45, 6, 45);
        jPanel2.add(jLabel3, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(SystemTheme.NORMAL_TEXT);
        txtPassword.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 45, 25, 45);
        jPanel2.add(txtPassword, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(COLOR_BG_FORM);
        btnLogin = new JButton(existingDashboard != null ? "Switch" : "Login");
        btnLogin.setPreferredSize(new Dimension(95, 35));
        btnLogin.setFont(SystemTheme.BOLD_TEXT);
        btnLogin.addActionListener(this::btnLoginActionPerformed);
        buttonPanel.add(btnLogin);

        if (existingDashboard != null) {
            JButton btnCancel = new JButton("Cancel");
            btnCancel.setPreferredSize(new Dimension(95, 35));
            btnCancel.setFont(SystemTheme.BOLD_TEXT);
            btnCancel.addActionListener(e -> {
                this.dispose();
                existingDashboard.setVisible(true);
            });
            buttonPanel.add(Box.createHorizontalStrut(8));
            buttonPanel.add(btnCancel);
        }

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

        SessionUser user = AuthQuery.login(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password.");
            return;
        }

        if (existingDashboard != null) {
            existingDashboard.handleUserSession(user);
            existingDashboard.setVisible(true);
            this.dispose();
        } else {
            view.MainFrame dashboard = new view.MainFrame();
            dashboard.handleUserSession(user);
            dashboard.setVisible(true);
            this.dispose();
        }
    }
}
