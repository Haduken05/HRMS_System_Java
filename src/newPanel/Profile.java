package newPanel;

import theme.SystemTheme;
import dataObject.Employee;
import dataObject.LeaveRequestEntity;
import dbquery.LeaveQuery;
import dbquery.EmployeeQuery;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class Profile extends JPanel {

    private final Color COLOR_BG = SystemTheme.CARD_BG;
    private final Color COLOR_CARD = Color.WHITE;
    private final Color COLOR_FIELD_BG = SystemTheme.FIELD_BG;
    private final Color COLOR_TEXT_MAIN = SystemTheme.TEXT_MAIN;
    private final Color COLOR_TEXT_MUTED = SystemTheme.TEXT_MUTED;
    private final Color COLOR_SUBTEXT = SystemTheme.ACCENT_COLOR;
    private final Color COLOR_TEXT_WHITE = SystemTheme.TEXT_COLOR;
    private final Color COLOR_TEXT_INDICATOR = SystemTheme.TEXT_INDICATOR;
    private final Color COLOR_SUCCESS = SystemTheme.BTN_YES;
    private final Color COLOR_ACCENT = SystemTheme.TEXT_INDICATOR;
    private final Color COLOR_DANGER = SystemTheme.BTN_NO;
    private final Color COLOR_BTN_DARK = SystemTheme.BTN_DARK;
    private final Color COLOR_ROW_SELECT = SystemTheme.ROW_SELECTED;
    private final Color COLOR_SL_BADGE = SystemTheme.BADGE_SL;
    private final Color COLOR_VL_BADGE = SystemTheme.BADGE_VL;
    private final Color COLOR_TAB_UNDERLINE = SystemTheme.BTN_DARK;

    private static final SimpleDateFormat DATE_FMT
            = new SimpleDateFormat("MMMM d, yyyy", java.util.Locale.ENGLISH);

    private JLabel lblAvatarCircle, lblProfileName, lblEmployeeID;
    private JLabel lblSickLeave, lblVacationLeave;
    private JLabel tabPersonalInfo, tabApprovedLeaves, tabPendingLeaves, tabChangePassword;
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    private JTextField txtFullName, txtDepartment, txtPosition,
            txtPhone, txtHireDate, txtRole;

    private JPasswordField txtCurrentPwd, txtNewPwd, txtConfirmPwd;

    private DefaultTableModel approvedTableModel;
    private DefaultTableModel pendingTableModel;

    private JButton btnSaveChanges;

    private int currentEmpId = -1;
    private String currentPassword = null;

    private JTable approvedTable;
    private JTable pendingTable;

    public Profile() {
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(900, 780));
        setBackground(COLOR_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Profile");
        titleLabel.setFont(SystemTheme.HEADER_TEXT);
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel breadcrumb = new JLabel("Dashboard / Profile");
        breadcrumb.setFont(SystemTheme.NORMAL_TEXT);
        breadcrumb.setForeground(COLOR_TEXT_MUTED);
        breadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(12));

        add(headerPanel, BorderLayout.NORTH);

        // Main card
        JPanel mainSplitCard = new JPanel(new BorderLayout());
        mainSplitCard.setBackground(COLOR_CARD);
        mainSplitCard.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));

        // Left column
        JPanel leftColumn = new JPanel(new GridBagLayout());
        leftColumn.setBackground(COLOR_CARD);
        leftColumn.setPreferredSize(new Dimension(240, 0));
        leftColumn.setBorder(new EmptyBorder(30, 20, 30, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        lblAvatarCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(COLOR_FIELD_BG);
                g2.fillOval(0, 0, w - 1, h - 1);

                if (getIcon() instanceof ImageIcon) {
                    Image img = ((ImageIcon) getIcon()).getImage();

                    java.awt.geom.Ellipse2D.Float circle
                            = new java.awt.geom.Ellipse2D.Float(0, 0, w - 1, h - 1);
                    g2.setClip(circle);
                    g2.drawImage(img, 0, 0, w, h, this);
                    g2.setClip(null);

                    g2.setColor(Color.decode("#CBD5E1"));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(1, 1, w - 3, h - 3);
                }

                g2.dispose();
            }
        };
        lblAvatarCircle.setPreferredSize(new Dimension(130, 130));
        lblAvatarCircle.setMinimumSize(new Dimension(130, 130));
        lblAvatarCircle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        leftColumn.add(lblAvatarCircle, gbc);

        lblProfileName = new JLabel("—", SwingConstants.CENTER);
        lblProfileName.setFont(SystemTheme.LARGE_TEXT_BOLD);
        lblProfileName.setForeground(COLOR_TEXT_MAIN);
        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        leftColumn.add(lblProfileName, gbc);

        lblEmployeeID = new JLabel("EMP000", SwingConstants.CENTER);
        lblEmployeeID.setFont(SystemTheme.NORMAL_TEXT);
        lblEmployeeID.setForeground(COLOR_TEXT_MUTED);
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 25, 0);
        leftColumn.add(lblEmployeeID, gbc);

        lblSickLeave = new JLabel("—");
        JPanel pnlSL = createCreditBox("Sick Leave Credits", lblSickLeave, COLOR_SL_BADGE);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 12, 0);
        leftColumn.add(pnlSL, gbc);

        lblVacationLeave = new JLabel("—");
        JPanel pnlVL = createCreditBox("Vacation Leave Credits", lblVacationLeave, COLOR_VL_BADGE);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        leftColumn.add(pnlVL, gbc);

        // Right column
        JPanel rightColumn = new JPanel(new BorderLayout());
        rightColumn.setBackground(COLOR_CARD);
        rightColumn.setBorder(BorderFactory.createMatteBorder(
                0, 1, 0, 0, Color.decode("#CBD5E1")));

        // Tab strip - four tabs
        JPanel tabsHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        tabsHeader.setBackground(COLOR_CARD);
        tabsHeader.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, Color.decode("#E2E8F0")));

        tabPersonalInfo = makeTab("Personal Information");
        tabApprovedLeaves = makeTab("Approved Leaves");
        tabPendingLeaves = makeTab("Pending Leaves");
        tabChangePassword = makeTab("Change Password");

        setTabActive(tabPersonalInfo);
        setTabInactive(tabApprovedLeaves);
        setTabInactive(tabPendingLeaves);
        setTabInactive(tabChangePassword);

        tabsHeader.add(tabPersonalInfo);
        tabsHeader.add(tabApprovedLeaves);
        tabsHeader.add(tabPendingLeaves);
        tabsHeader.add(tabChangePassword);
        rightColumn.add(tabsHeader, BorderLayout.NORTH);

        // Cards
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(COLOR_CARD);

        // CARD 1 - Personal Info
        JPanel personalGrid = new JPanel(new GridLayout(3, 2, 25, 20));
        personalGrid.setBackground(COLOR_CARD);
        personalGrid.setBorder(new EmptyBorder(30, 30, 30, 30));

        txtFullName = createReadOnlyField();
        txtDepartment = createReadOnlyField();
        txtPosition = createReadOnlyField();
        txtPhone = createReadOnlyField();
        txtHireDate = createReadOnlyField();
        txtRole = createReadOnlyField();

        personalGrid.add(createFieldWrapper("Full Name", txtFullName));
        personalGrid.add(createFieldWrapper("Department", txtDepartment));
        personalGrid.add(createFieldWrapper("Position", txtPosition));
        personalGrid.add(createFieldWrapper("Contact No.", txtPhone));
        personalGrid.add(createFieldWrapper("Hire Date", txtHireDate));
        personalGrid.add(createFieldWrapper("Role", txtRole));

        // CARD 2 - Approved Leaves
        String[] leaveCols = {"Request ID", "Type", "Start Date", "End Date", "Duration"};
        approvedTableModel = new DefaultTableModel(leaveCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        approvedTable = buildLeaveTable(approvedTableModel, false);
        JScrollPane approvedScroll = new JScrollPane(approvedTable);
        approvedScroll.setBorder(new EmptyBorder(20, 30, 20, 30));
        approvedScroll.getViewport().setBackground(COLOR_CARD);

        JPanel approvedCard = new JPanel(new BorderLayout());
        approvedCard.setBackground(COLOR_CARD);
        approvedCard.add(approvedScroll, BorderLayout.CENTER);

        // CARD 3 - Pending Leaves
        String[] pendingCols = {"Request ID", "Type", "Start Date", "End Date", "Duration", "Status"};
        pendingTableModel = new DefaultTableModel(pendingCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        pendingTable = buildLeaveTable(pendingTableModel, true);
        JScrollPane pendingScroll = new JScrollPane(pendingTable);
        pendingScroll.setBorder(new EmptyBorder(20, 30, 20, 30));
        pendingScroll.getViewport().setBackground(COLOR_CARD);

        JPanel pendingCard = new JPanel(new BorderLayout());
        pendingCard.setBackground(COLOR_CARD);
        pendingCard.add(pendingScroll, BorderLayout.CENTER);

        // CARD 4 - Change Password
        JPanel pwdGrid = new JPanel(new GridLayout(3, 1, 0, 20));
        pwdGrid.setBackground(COLOR_CARD);
        pwdGrid.setBorder(new EmptyBorder(30, 30, 30, 200));

        txtCurrentPwd = createStyledPasswordField();
        txtNewPwd = createStyledPasswordField();
        txtConfirmPwd = createStyledPasswordField();

        JButton btnShowCurrent = createEyeButton();
        JButton btnShowNew = createEyeButton();
        JButton btnShowConfirm = createEyeButton();

        wireEyeToggle(btnShowCurrent, txtCurrentPwd);
        wireEyeToggle(btnShowNew, txtNewPwd);
        wireEyeToggle(btnShowConfirm, txtConfirmPwd);

        pwdGrid.add(createFieldWrapper("Current Password", txtCurrentPwd, btnShowCurrent));
        pwdGrid.add(createFieldWrapper("New Password", txtNewPwd, btnShowNew));
        pwdGrid.add(createFieldWrapper("Confirm Password", txtConfirmPwd, btnShowConfirm));

        cardsPanel.add(personalGrid, "PERSONAL");
        cardsPanel.add(approvedCard, "APPROVED");
        cardsPanel.add(pendingCard, "PENDING");
        cardsPanel.add(pwdGrid, "PASSWORD");

        rightColumn.add(cardsPanel, BorderLayout.CENTER);

        // Footer
        JPanel formFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 25));
        formFooter.setBackground(COLOR_CARD);
        btnSaveChanges = new JButton("Save Changes");
        styleButton(btnSaveChanges, COLOR_BTN_DARK, Color.WHITE);
        btnSaveChanges.setPreferredSize(new Dimension(160, 40));
        formFooter.add(btnSaveChanges);
        rightColumn.add(formFooter, BorderLayout.SOUTH);

        mainSplitCard.add(leftColumn, BorderLayout.WEST);
        mainSplitCard.add(rightColumn, BorderLayout.CENTER);
        add(mainSplitCard, BorderLayout.CENTER);

        // Tab listeners
        tabPersonalInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activateTab("PERSONAL");
                btnSaveChanges.setVisible(false);
            }
        });
        tabApprovedLeaves.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activateTab("APPROVED");
                loadApprovedLeaves();
                btnSaveChanges.setVisible(false);
            }
        });
        tabPendingLeaves.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activateTab("PENDING");
                loadPendingLeaves();
                btnSaveChanges.setVisible(false);
            }
        });
        tabChangePassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activateTab("PASSWORD");
                btnSaveChanges.setText("Update Password");
                btnSaveChanges.setVisible(true);
                btnSaveChanges.addActionListener(er -> handlePasswordChange());
            }
        });

        btnSaveChanges.setVisible(false);
    }

    //  Public API
    public void loadProfile(Employee emp) {
        currentEmpId = emp.empId;
        currentPassword = emp.password;

        lblProfileName.setText(emp.fullName);
        lblEmployeeID.setText("EMP" + String.format("%03d", emp.empId));

        txtFullName.setText(emp.fullName);
        txtDepartment.setText(emp.department);
        txtPosition.setText(emp.position);
        txtPhone.setText(emp.contactNo);
        txtRole.setText(emp.role);
        txtHireDate.setText(emp.hireDate != null ? DATE_FMT.format(emp.hireDate) : "—");

        lblVacationLeave.setText(String.valueOf(emp.vlCredits));
        lblSickLeave.setText(String.valueOf(emp.slCredits));

        loadAvatar(emp.profilePic);
    }

    public void refreshCredits() {
        if (currentEmpId < 0) {
            return;
        }
        int vl = LeaveQuery.getVLCredits(currentEmpId);
        int sl = LeaveQuery.getSLCredits(currentEmpId);
        lblVacationLeave.setText(vl >= 0 ? String.valueOf(vl) : "—");
        lblSickLeave.setText(sl >= 0 ? String.valueOf(sl) : "—");
    }

    private void loadApprovedLeaves() {
        approvedTableModel.setRowCount(0);
        if (currentEmpId < 0) {
            return;
        }

        List<LeaveRequestEntity> leaves = LeaveQuery.getApprovedLeaves(currentEmpId);
        for (LeaveRequestEntity e : leaves) {
            String start = e.getStartDate() != null ? DATE_FMT.format(e.getStartDate()) : "—";
            String end = e.getEndDate() != null ? DATE_FMT.format(e.getEndDate()) : "—";
            long duration = calcDays(e.getStartDate(), e.getEndDate());
            approvedTableModel.addRow(new Object[]{
                e.getRequestId(),
                e.getLeaveType().equals("VL") ? "Vacation Leave" : "Sick Leave",
                start, end,
                duration + (duration == 1 ? " day" : " days")
            });
        }
    }

    private void loadPendingLeaves() {
        pendingTableModel.setRowCount(0);
        if (currentEmpId < 0) {
            return;
        }

        List<LeaveRequestEntity> leaves = LeaveQuery.getPendingLeavesByEmployee(currentEmpId);
        for (LeaveRequestEntity e : leaves) {
            String start = e.getStartDate() != null ? DATE_FMT.format(e.getStartDate()) : "—";
            String end = e.getEndDate() != null ? DATE_FMT.format(e.getEndDate()) : "—";
            long duration = calcDays(e.getStartDate(), e.getEndDate());
            pendingTableModel.addRow(new Object[]{
                e.getRequestId(),
                e.getLeaveType().equals("VL") ? "Vacation Leave" : "Sick Leave",
                start, end,
                duration + (duration == 1 ? " day" : " days"),
                "Pending"
            });
        }
    }

    private long calcDays(java.util.Date a, java.util.Date b) {
        return java.util.concurrent.TimeUnit.MILLISECONDS
                .toDays(b.getTime() - a.getTime()) + 1;
    }

    //  Tab helpers
    private void activateTab(String card) {
        setTabInactive(tabPersonalInfo);
        setTabInactive(tabApprovedLeaves);
        setTabInactive(tabPendingLeaves);
        setTabInactive(tabChangePassword);
        switch (card) {
            case "PERSONAL" ->
                setTabActive(tabPersonalInfo);
            case "APPROVED" ->
                setTabActive(tabApprovedLeaves);
            case "PENDING" ->
                setTabActive(tabPendingLeaves);
            case "PASSWORD" ->
                setTabActive(tabChangePassword);
        }
        cardLayout.show(cardsPanel, card);
    }

    private JLabel makeTab(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(SystemTheme.BOLD_TEXT);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private void setTabActive(JLabel label) {
        label.setForeground(COLOR_TEXT_MAIN);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_TAB_UNDERLINE),
                new EmptyBorder(15, 5, 12, 5)));
    }

    private void setTabInactive(JLabel label) {
        label.setForeground(COLOR_TEXT_MUTED);
        label.setBorder(new EmptyBorder(15, 5, 15, 5));
    }

    private JTable buildLeaveTable(DefaultTableModel model, boolean hasPendingStatus) {
        JTable table = new JTable(model);
        table.setFont(SystemTheme.NORMAL_TEXT);
        table.setRowHeight(36);
        table.setGridColor(Color.decode("#F1F5F9"));

        table.setSelectionBackground(COLOR_ROW_SELECT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(SystemTheme.BOLD_TEXT);
        header.setBackground(Color.decode("#F8FAFC"));
        header.setForeground(COLOR_TEXT_MUTED);
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 8, 0, 8));

                if (sel) {

                    setForeground(Color.WHITE);
                } else if (hasPendingStatus && c == model.getColumnCount() - 1 && v != null) {

                    String val = v.toString();
                    if (val.equalsIgnoreCase("Pending")) {
                        setForeground(COLOR_ACCENT);
                    } else if (val.equalsIgnoreCase("Approved")) {
                        setForeground(COLOR_SUCCESS);
                    } else if (val.equalsIgnoreCase("Disapproved")) {
                        setForeground(COLOR_DANGER);
                    } else {
                        setForeground(COLOR_TEXT_MAIN);
                    }
                    setFont(SystemTheme.BOLD_TEXT);
                } else {
                    setForeground(COLOR_TEXT_MAIN);
                    setFont(SystemTheme.NORMAL_TEXT);
                }
                return this;
            }
        });

        return table;
    }

    private JPanel createCreditBox(String title, JLabel valueLabel, Color bgColor) {
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.decode("#E2E8F0"));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(12, 15, 12, 15));
        container.setPreferredSize(new Dimension(200, 70));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SystemTheme.SMALL_TEXT_BOLD);
        titleLabel.setForeground(COLOR_SUBTEXT);

        valueLabel.setFont(SystemTheme.BIG_TEXT_BOLD);
        valueLabel.setForeground(COLOR_TEXT_WHITE);

        container.add(titleLabel, BorderLayout.NORTH);
        container.add(valueLabel, BorderLayout.CENTER);
        return container;
    }

    private JTextField createReadOnlyField() {
        JTextField tf = new JTextField();
        tf.setFont(SystemTheme.NORMAL_TEXT);
        tf.setBackground(COLOR_FIELD_BG);
        tf.setForeground(COLOR_TEXT_MAIN);
        tf.setEditable(false);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        tf.setPreferredSize(new Dimension(0, 40));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(SystemTheme.NORMAL_TEXT);
        pf.setBackground(COLOR_CARD);
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        pf.setPreferredSize(new Dimension(0, 40));
        return pf;
    }

    private JPanel createFieldWrapper(String labelText, JComponent input) {
        JPanel wrap = new JPanel();
        wrap.setBackground(COLOR_CARD);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(SystemTheme.BOLD_TEXT);
        lbl.setForeground(COLOR_TEXT_MAIN);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(lbl);
        wrap.add(input);
        return wrap;
    }

    private JPanel createFieldWrapper(String labelText, JPasswordField input, JButton eyeBtn) {
        JPanel wrap = new JPanel();
        wrap.setBackground(COLOR_CARD);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(SystemTheme.BOLD_TEXT);
        lbl.setForeground(COLOR_TEXT_MAIN);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(COLOR_CARD);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(input, BorderLayout.CENTER);
        row.add(eyeBtn, BorderLayout.EAST);

        wrap.add(lbl);
        wrap.add(row);
        return wrap;
    }

    private JButton createEyeButton() {
        JButton btn = new JButton("View");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setPreferredSize(new Dimension(60, 40));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(COLOR_BTN_DARK);
        btn.setForeground(COLOR_TEXT_WHITE);
        btn.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));
        btn.setToolTipText("Show / Hide password");
        return btn;
    }

    private void wireEyeToggle(JButton eyeBtn, JPasswordField field) {
        eyeBtn.addActionListener(e -> {
            if (field.getEchoChar() == 0) {

                field.setEchoChar('*');
                eyeBtn.setForeground(COLOR_TEXT_WHITE);
            } else {

                field.setEchoChar((char) 0);
                eyeBtn.setForeground(COLOR_TEXT_INDICATOR);
            }
        });
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(SystemTheme.BOLD_TEXT);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
    }
    
    private void handlePasswordChange() {
        String current = new String(txtCurrentPwd.getPassword()).trim();
        String newPwd  = new String(txtNewPwd.getPassword()).trim();
        String confirm = new String(txtConfirmPwd.getPassword()).trim();

        if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all password fields.",
                    "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!current.equals(currentPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Current password is incorrect.",
                    "Wrong Password", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPwd.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                    "New password and confirmation do not match.",
                    "Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPwd.equals(currentPassword)) {
            JOptionPane.showMessageDialog(this,
                    "New password must be different from your current password.",
                    "Same Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm2 = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to change your password?",
                "Confirm Change", JOptionPane.YES_NO_OPTION);
        if (confirm2 != JOptionPane.YES_OPTION) return;

        boolean ok = EmployeeQuery.updatePassword(currentEmpId, newPwd); 
        
        if (ok) {
            currentPassword = newPwd;
            txtCurrentPwd.setText("");
            txtNewPwd.setText("");
            txtConfirmPwd.setText("");
            JOptionPane.showMessageDialog(this,
                    "Password updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update password. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAvatar(String profilePicPath) {
        lblAvatarCircle.setIcon(null);

        if (profilePicPath == null || profilePicPath.isBlank()) {
            lblAvatarCircle.repaint();
            return;
        }

        File imgFile = new File("profile/" + profilePicPath);
        if (!imgFile.exists()) {

            imgFile = new File(profilePicPath);
        }
        if (!imgFile.exists()) {
            lblAvatarCircle.repaint();
            return;
        }

        try {
            java.awt.image.BufferedImage raw
                    = javax.imageio.ImageIO.read(imgFile);
            if (raw == null) {
                return;
            }

            Image scaled = raw.getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            lblAvatarCircle.setIcon(new ImageIcon(scaled));
            lblAvatarCircle.repaint();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
}
