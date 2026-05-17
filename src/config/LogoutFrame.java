package config;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import theme.SystemTheme;

public class LogoutFrame extends javax.swing.JFrame {

    private JPanel mainPanel;
    private CircularIconPanel avatarPanel;
    private JLabel lblTitle, lblSubtitle;
    private JButton btnCancel, btnYes;

    private final view.MainFrame parentDashboard;

    public LogoutFrame(view.MainFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
        initComponents();
    }

    private void initComponents() {
        setTitle("Confirm Logout");
        setResizable(false);
        setUndecorated(true);

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.decode("#E6E6E6"));
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#CCCCCC"), 1));
        mainPanel.setPreferredSize(new Dimension(450, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        avatarPanel = new CircularIconPanel("logout.png", 120);
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 0, 20, 0);
        mainPanel.add(avatarPanel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblTitle = new JLabel("Logout", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.BLACK);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 15, 20);
        mainPanel.add(lblTitle, gbc);

        lblSubtitle = new JLabel("Are you sure you want to logout?", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSubtitle.setForeground(Color.decode("#2B2B2B"));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 20, 15, 20);
        mainPanel.add(lblSubtitle, gbc);

        JLabel lblSwitch = new JLabel("or switch to a different account", SwingConstants.CENTER);
        lblSwitch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSwitch.setForeground(Color.decode("#1D4ED8")); 
        lblSwitch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSwitch.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                parentDashboard.setVisible(false);
                LogoutFrame.this.dispose();
                new LoginFrame(parentDashboard).setVisible(true);
            }
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 20, 25, 20);
        mainPanel.add(lblSwitch, gbc);

        JPanel buttonContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonContainer.setBackground(Color.decode("#E6E6E6"));

        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBackground(SystemTheme.BTN_DARK);
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(160, 45));
        btnCancel.setBorder(BorderFactory.createLineBorder(Color.decode("#1C1C1C"), 1, true));
        btnCancel.addActionListener(this::btnCancelActionPerformed);
        buttonContainer.add(btnCancel);

        btnYes = new JButton("Yes, Logout");
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnYes.setForeground(Color.WHITE);
        btnYes.setBackground(SystemTheme.BTN_NO);
        btnYes.setFocusPainted(false);
        btnYes.setBorder(BorderFactory.createEmptyBorder());
        btnYes.setOpaque(true);
        btnYes.addActionListener(this::btnYesActionPerformed);
        buttonContainer.add(btnYes);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 40, 40, 40);
        mainPanel.add(buttonContainer, gbc);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(parentDashboard);
    }

    private void btnCancelActionPerformed(ActionEvent evt) {
        this.dispose();
    }

    private void btnYesActionPerformed(ActionEvent evt) {

        this.dispose();
        if (parentDashboard != null) {
            parentDashboard.dispose();
        }
        new LoginFrame().setVisible(true);
    }

    private static class CircularIconPanel extends JPanel {

        private Image scaledIcon;

        public CircularIconPanel(String imgName, int panelSize) {
            setPreferredSize(new Dimension(panelSize, panelSize));
            setOpaque(false);
            try {
                Image raw = null;
                java.io.InputStream is = getClass().getResourceAsStream("/Image/" + imgName);
                if (is != null) {
                    raw = javax.imageio.ImageIO.read(is);
                }

                if (raw == null) {
                    java.io.InputStream is2 = getClass().getClassLoader()
                            .getResourceAsStream("Image/" + imgName);
                    if (is2 != null) {
                        raw = javax.imageio.ImageIO.read(is2);
                    }
                }
                if (raw == null) {
                    java.io.File f = new java.io.File("src/Image/" + imgName);
                    if (f.exists()) {
                        raw = javax.imageio.ImageIO.read(f);
                    }
                }
                if (raw != null) {
                    int iconSize = (int) (panelSize * 0.65);
                    scaledIcon = raw.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                }
            } catch (Exception e) {
                System.err.println("Error loading icon: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setColor(Color.WHITE);
            g2.fillOval(0, 0, getWidth(), getHeight());
            if (scaledIcon != null) {
                int x = (getWidth() - scaledIcon.getWidth(null)) / 2;
                int y = (getHeight() - scaledIcon.getHeight(null)) / 2;
                g2.drawImage(scaledIcon, x, y, null);
            }
            g2.dispose();
        }
    }
}
