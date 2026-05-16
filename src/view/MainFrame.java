package view;

import panels.*;

import dbquery.EmployeeQuery;
import logic.SessionLogic;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private ApplyLeavePanel applyLeavePanel;
    private ProfilePanel profilePanel;
    private DirectoryPanel directoryPanel;
    private ManagementPanel managementPanel;
    private ReportsPanel reportsPanel;

    private final JButton btnApplyLeave = new JButton("Apply Leave");
    private final JButton btnProfile = new JButton("Profile");
    private final JButton btnDirectory = new JButton("Directory");
    private final JButton btnManagement = new JButton("Management");
    private final JButton btnReports = new JButton("Reports");
    private final JButton btnExit = new JButton("Exit");
    private final JLabel lblWelcome = new JLabel("Welcome");
    private final JLabel lblLogo = new JLabel();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public static final String CARD_APPLY_LEAVE = "APPLY_LEAVE";
    public static final String CARD_PROFILE = "PROFILE";
    public static final String CARD_DIRECTORY = "DIRECTORY";
    public static final String CARD_MANAGEMENT = "MANAGEMENT";
    public static final String CARD_REPORTS = "REPORTS";

    public MainFrame() {
        setTitle("HRMS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        buildContentPanel();
        wireNavButtons();

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        pack();
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(102, 255, 255));
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(8, 8, 16, 8));

        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblWelcome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 41));
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        
        sidebar.add(lblWelcome);

        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));

        try {
            java.net.URL imgUrl = getClass().getResource("/Image/backgroundremoved_Hrms_logo_2.png");
            if (imgUrl != null) {
                lblLogo.setIcon(new ImageIcon(imgUrl));
            }
        } catch (Exception ignored) {
            
        }
        
        lblLogo.setBackground(Color.WHITE);
        lblLogo.setOpaque(true);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBorder(new javax.swing.border.LineBorder(Color.BLACK, 5, true));
        sidebar.add(lblLogo);

        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        Dimension btnSize = new Dimension(75, 44);
        for (JButton btn : new JButton[]{
            btnApplyLeave, btnProfile, btnDirectory, btnManagement, btnReports}) {
            btn.setPreferredSize(btnSize);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        sidebar.add(Box.createVerticalGlue());
        btnExit.setPreferredSize(new Dimension(75, 44));
        btnExit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 63));
        btnExit.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(btnExit);

        return sidebar;
    }

    private void buildContentPanel() {
        applyLeavePanel = new ApplyLeavePanel();
        profilePanel = new ProfilePanel();
        directoryPanel = new DirectoryPanel();
        managementPanel = new ManagementPanel();
        reportsPanel = new ReportsPanel();

        contentPanel.add(applyLeavePanel, CARD_APPLY_LEAVE);
        contentPanel.add(profilePanel, CARD_PROFILE);
        contentPanel.add(directoryPanel, CARD_DIRECTORY);
        contentPanel.add(managementPanel, CARD_MANAGEMENT);
        contentPanel.add(reportsPanel, CARD_REPORTS);
    }

    private void wireNavButtons() {
        btnApplyLeave.addActionListener(e -> showCard(CARD_APPLY_LEAVE));
        btnProfile.addActionListener(e -> showCard(CARD_PROFILE));
        btnDirectory.addActionListener(e -> showCard(CARD_DIRECTORY));
        btnManagement.addActionListener(e -> showCard(CARD_MANAGEMENT));
        btnReports.addActionListener(e -> showCard(CARD_REPORTS));
        btnExit.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Do you want to exit the system?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    public void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    public void handleUserSession(String name, String role, int empId) {

        lblWelcome.setText("User: " + name + " (" + role + ")");

        Object[] emp = EmployeeQuery.getById(empId);
        if (emp != null) {
            profilePanel.loadProfile(emp);
        }

        applyLeavePanel.setEmpId(empId);

        boolean isEmployee = SessionLogic.isEmployee(role);
        btnDirectory.setVisible(!isEmployee);
        btnManagement.setVisible(!isEmployee);
        btnReports.setVisible(!isEmployee);

        showCard(isEmployee ? CARD_APPLY_LEAVE : CARD_MANAGEMENT);
    }
}
