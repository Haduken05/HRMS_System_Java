package newPanel;

import dbquery.AttendanceQuery;
import dbquery.EmployeeQuery;
import dataObject.Employee;
import theme.SystemTheme;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AttendanceTracker extends JPanel {

    private final Color BG         = SystemTheme.ACCENT_COLOR;
    private final Color FIELD_BG   = SystemTheme.FIELD_BG;
    private final Color BORDER_CLR = SystemTheme.BORDER_COLOR;
    private final Color TEXT_MAIN  = SystemTheme.TEXT_MAIN;
    private final Color TEXT_MUTED = SystemTheme.TEXT_MUTED;
    private final Color BTN_SUBMIT = SystemTheme.BTN_YES;
    private final Color BTN_TIMEOUT = Color.decode("#7C3AED"); 
    private final Color COLOR_ERR  = SystemTheme.BTN_NO;

    private static final Color BADGE_PRESENT          = Color.decode("#D1FAE5");
    private static final Color BADGE_LATE             = Color.decode("#FEF9C3");
    private static final Color BADGE_HALFDAY          = Color.decode("#FFE4E6");
    private static final Color BADGE_AFTERHOURS       = Color.decode("#EDE9FE");
    private static final Color BADGE_BORDER_PRESENT   = Color.decode("#6EE7B7");
    private static final Color BADGE_BORDER_LATE      = Color.decode("#FDE047");
    private static final Color BADGE_BORDER_HALF      = Color.decode("#FDA4AF");
    private static final Color BADGE_BORDER_AFTERHOURS= Color.decode("#C4B5FD");
    private static final Color BADGE_TEXT_PRESENT     = Color.decode("#065F46");
    private static final Color BADGE_TEXT_LATE        = Color.decode("#92400E");
    private static final Color BADGE_TEXT_HALF        = Color.decode("#9F1239");
    private static final Color BADGE_TEXT_AFTERHOURS  = Color.decode("#4C1D95");

    private JLabel tabTimeIn, tabTimeOut;
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    private JTextField txtInEmpID, txtInFullName, txtInDepartment, txtInPosition;
    private JLabel     lblInPreview, lblStatusBadge, lblAfterHours;
    private JButton    btnCheckAttendance;
    private Employee   resolvedInEmployee = null;

    private JTextField txtOutEmpID, txtOutFullName, txtOutDepartment, txtOutPosition;
    private JLabel     lblOutPreview, lblOTBadge;
    private JButton    btnTimeOut;
    private Employee   resolvedOutEmployee = null;

    private JTextField txtTimeIn;   
    private Timer      runningClockTimer;
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("h:mm a  -  MMMM d, yyyy");

    public AttendanceTracker() {
        
        AttendanceQuery.autoTimeoutForgottenEntries();
        initComponents();
        startRealTimeClock();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(800, 700));
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // HEADER
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(BG);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Employee Attendance Tracker");
        titleLabel.setFont(SystemTheme.HEADER_TEXT);
        titleLabel.setForeground(TEXT_MAIN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel breadcrumb = new JLabel("Dashboard / Attendance Tracker");
        breadcrumb.setFont(SystemTheme.NORMAL_TEXT);
        breadcrumb.setForeground(TEXT_MUTED);
        breadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtTimeIn = createStyledTextField();
        txtTimeIn.setEditable(false);
        txtTimeIn.setForeground(Color.decode("#2563EB"));
        txtTimeIn.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(14));
        headerPanel.add(txtTimeIn);
        headerPanel.add(Box.createVerticalStrut(18));

        add(headerPanel, BorderLayout.NORTH);

        JPanel tabStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        tabStrip.setBackground(BG);
        tabStrip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                Color.decode("#D1D5DB")));

        tabTimeIn  = makeTab("Time In");
        tabTimeOut = makeTab("Time Out");
        tabTimeOut.setVisible(false);   // hidden until 5 PM

        setTabActive(tabTimeIn);
        setTabInactive(tabTimeOut);

        tabStrip.add(tabTimeIn);
        tabStrip.add(tabTimeOut);

        // ARD PANEL
        cardLayout  = new CardLayout();
        cardsPanel  = new JPanel(cardLayout);
        cardsPanel.setBackground(BG);

        cardsPanel.add(buildTimeInCard(),  "TIME_IN");
        cardsPanel.add(buildTimeOutCard(), "TIME_OUT");

        // ASSEMBLE CENTER
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(BG);
        centerWrapper.add(tabStrip,   BorderLayout.NORTH);
        centerWrapper.add(cardsPanel, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        tabTimeIn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                setTabActive(tabTimeIn);
                setTabInactive(tabTimeOut);
                cardLayout.show(cardsPanel, "TIME_IN");
            }
        });
        tabTimeOut.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                setTabActive(tabTimeOut);
                setTabInactive(tabTimeIn);
                cardLayout.show(cardsPanel, "TIME_OUT");
            }
        });
    }

    //  TIME IN CARD
    private JPanel buildTimeInCard() {
        JPanel card = new JPanel();
        card.setBackground(BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 0, 0, 0));

        card.add(createFieldLabel("Employee ID"));
        txtInEmpID = createStyledTextField();
        card.add(txtInEmpID);

        lblInPreview = new JLabel(" ");
        lblInPreview.setFont(SystemTheme.BOLD_TEXT);
        lblInPreview.setForeground(COLOR_ERR);
        lblInPreview.setBorder(new EmptyBorder(4, 2, 6, 0));
        lblInPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblInPreview);
        card.add(Box.createVerticalStrut(8));

        card.add(createFieldLabel("Full Name"));
        txtInFullName = createStyledTextField();
        txtInFullName.setEditable(false);
        card.add(txtInFullName);
        card.add(Box.createVerticalStrut(14));

        card.add(createFieldLabel("Department"));
        txtInDepartment = createStyledTextField();
        txtInDepartment.setEditable(false);
        card.add(txtInDepartment);
        card.add(Box.createVerticalStrut(14));

        card.add(createFieldLabel("Position"));
        txtInPosition = createStyledTextField();
        txtInPosition.setEditable(false);
        card.add(txtInPosition);
        card.add(Box.createVerticalStrut(14));

        // Status badge
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setBackground(BG);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblStatusBadge = new JLabel("  Present  ");
        lblStatusBadge.setFont(SystemTheme.BOLD_TEXT);
        lblStatusBadge.setOpaque(true);
        applyBadgeStyle(lblStatusBadge, "Present");
        badgeRow.add(lblStatusBadge);
        card.add(badgeRow);
        card.add(Box.createVerticalStrut(6));

        // After-hours warning
        lblAfterHours = new JLabel(
                " The workday has ended (after 5:00 PM). "
                + "This record will be marked as After Hours.");
        lblAfterHours.setFont(SystemTheme.NORMAL_TEXT);
        lblAfterHours.setForeground(BADGE_TEXT_AFTERHOURS);
        lblAfterHours.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAfterHours.setVisible(false);
        card.add(lblAfterHours);
        card.add(Box.createVerticalGlue());

        // Button
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCheckAttendance = new JButton("Check Attendance");
        styleButton(btnCheckAttendance, BTN_SUBMIT);
        btnCheckAttendance.addActionListener(this::onTimeInClicked);
        btnRow.add(btnCheckAttendance);
        card.add(btnRow);

        // Live ID
        txtInEmpID.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { resolveInEmployee(); }
            @Override public void removeUpdate(DocumentEvent e)  { resolveInEmployee(); }
            @Override public void changedUpdate(DocumentEvent e) { resolveInEmployee(); }
        });

        return card;
    }

    //  TIME OUT CARD
    private JPanel buildTimeOutCard() {
        JPanel card = new JPanel();
        card.setBackground(BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 0, 0, 0));

        card.add(createFieldLabel("Employee ID"));
        txtOutEmpID = createStyledTextField();
        card.add(txtOutEmpID);

        lblOutPreview = new JLabel(" ");
        lblOutPreview.setFont(SystemTheme.BOLD_TEXT);
        lblOutPreview.setForeground(COLOR_ERR);
        lblOutPreview.setBorder(new EmptyBorder(4, 2, 6, 0));
        lblOutPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblOutPreview);
        card.add(Box.createVerticalStrut(8));

        card.add(createFieldLabel("Full Name"));
        txtOutFullName = createStyledTextField();
        txtOutFullName.setEditable(false);
        card.add(txtOutFullName);
        card.add(Box.createVerticalStrut(14));

        card.add(createFieldLabel("Department"));
        txtOutDepartment = createStyledTextField();
        txtOutDepartment.setEditable(false);
        card.add(txtOutDepartment);
        card.add(Box.createVerticalStrut(14));

        card.add(createFieldLabel("Position"));
        txtOutPosition = createStyledTextField();
        txtOutPosition.setEditable(false);
        card.add(txtOutPosition);
        card.add(Box.createVerticalStrut(14));

        // OT badge - hidden until OT is detected
        JPanel otBadgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        otBadgeRow.setBackground(BG);
        otBadgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblOTBadge = new JLabel("  Overtime  ");
        lblOTBadge.setFont(SystemTheme.BOLD_TEXT);
        lblOTBadge.setOpaque(true);
        lblOTBadge.setBackground(Color.decode("#EDE9FE"));
        lblOTBadge.setForeground(Color.decode("#4C1D95"));
        lblOTBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#C4B5FD"), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        lblOTBadge.setVisible(false);   // shown only when OT threshold is met
        otBadgeRow.add(lblOTBadge);
        card.add(otBadgeRow);
        card.add(Box.createVerticalGlue());

        // Button
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTimeOut = new JButton("Time Out");
        styleButton(btnTimeOut, BTN_TIMEOUT);
        btnTimeOut.addActionListener(this::onTimeOutClicked);
        btnRow.add(btnTimeOut);
        card.add(btnRow);

        // Live ID
        txtOutEmpID.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { resolveOutEmployee(); }
            @Override public void removeUpdate(DocumentEvent e)  { resolveOutEmployee(); }
            @Override public void changedUpdate(DocumentEvent e) { resolveOutEmployee(); }
        });

        return card;
    }

    //  LIVE RESOLUTION - TIME IN
    private void resolveInEmployee() {
        String text = txtInEmpID.getText().trim();
        clearInFields();

        if (text.isEmpty()) { lblInPreview.setText(" "); resolvedInEmployee = null; return; }

        int id;
        try { id = Integer.parseInt(text); }
        catch (NumberFormatException ex) {
            lblInPreview.setText("X  ID must be a number");
            lblInPreview.setForeground(COLOR_ERR);
            resolvedInEmployee = null; return;
        }

        if (id == 5) {
            lblInPreview.setText("X  You're selecting the Attendance Kiosk. Change it to proceed.");
            lblInPreview.setForeground(COLOR_ERR);
            resolvedInEmployee = null; return;
        }

        Employee emp = EmployeeQuery.getById(id);
        if (emp != null) {
            resolvedInEmployee = emp;
            lblInPreview.setText(" ");
            txtInFullName.setText(emp.fullName);
            txtInDepartment.setText(emp.department);
            txtInPosition.setText(emp.position);
        } else {
            resolvedInEmployee = null;
            lblInPreview.setText("X  No employee found with this ID");
            lblInPreview.setForeground(COLOR_ERR);
        }
    }

    //  LIVE RESOLUTION - TIME OUT
    private void resolveOutEmployee() {
        String text = txtOutEmpID.getText().trim();
        clearOutFields();

        if (text.isEmpty()) { lblOutPreview.setText(" "); resolvedOutEmployee = null; return; }

        int id;
        try { id = Integer.parseInt(text); }
        catch (NumberFormatException ex) {
            lblOutPreview.setText("X  ID must be a number");
            lblOutPreview.setForeground(COLOR_ERR);
            resolvedOutEmployee = null; return;
        }

        if (id == 5) {
            lblOutPreview.setText("X  You're selecting the Attendance Kiosk. Change it to proceed.");
            lblOutPreview.setForeground(COLOR_ERR);
            resolvedOutEmployee = null; return;
        }

        Employee emp = EmployeeQuery.getById(id);
        if (emp != null) {

            if (!AttendanceQuery.hasOpenEntry(emp.empId)) {
                lblOutPreview.setText("X  " + emp.fullName + " has no open Time In for today.");
                lblOutPreview.setForeground(COLOR_ERR);
                resolvedOutEmployee = null;
                clearOutFields();
                return;
            }

            resolvedOutEmployee = emp;
            lblOutPreview.setText(" ");
            txtOutFullName.setText(emp.fullName);
            txtOutDepartment.setText(emp.department);
            txtOutPosition.setText(emp.position);

            // Show OT badge if already past the 1-hour OT threshold
            int otHours = AttendanceQuery.computeOTHours();
            if (otHours > 0) {
                lblOTBadge.setText("  Overtime: +" + otHours + "h credit  ");
                lblOTBadge.setVisible(true);
            } else {
                lblOTBadge.setVisible(false);
            }
        } else {
            resolvedOutEmployee = null;
            lblOutPreview.setText("X  No employee found with this ID");
            lblOutPreview.setForeground(COLOR_ERR);
        }
    }

    private void clearInFields() {
        txtInFullName.setText("");
        txtInDepartment.setText("");
        txtInPosition.setText("");
    }

    private void clearOutFields() {
        txtOutFullName.setText("");
        txtOutDepartment.setText("");
        txtOutPosition.setText("");
        if (lblOTBadge != null) lblOTBadge.setVisible(false);
    }

    //  BUTTON ACTIONS
    private void onTimeInClicked(ActionEvent evt) {
        if (resolvedInEmployee == null) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Employee ID first.",
                    "No Employee Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int empId = resolvedInEmployee.empId;

        if (AttendanceQuery.hasAttendanceToday(empId)) {
            if (AttendanceQuery.hasOpenEntry(empId)) {

                JOptionPane.showMessageDialog(this,
                        "<html><b>" + resolvedInEmployee.fullName
                        + "</b> is already clocked in and has not timed out yet.</html>",
                        "Already Clocked In", JOptionPane.WARNING_MESSAGE);
            } else {

                JOptionPane.showMessageDialog(this,
                        "<html><b>" + resolvedInEmployee.fullName
                        + "</b> has already completed their attendance for today.<br>"
                        + "Duplicate entries are not allowed.</html>",
                        "Attendance Already Recorded", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        String status = AttendanceQuery.peekStatus();
        int newId = AttendanceQuery.clockIn(empId);
        if (newId < 0) {
            JOptionPane.showMessageDialog(this,
                    "Failed to record attendance. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "<html>✓ Attendance recorded for <b>" + resolvedInEmployee.fullName + "</b><br>"
                + "Time In: " + txtTimeIn.getText() + "<br>"
                + "Status: <b>" + status + "</b></html>",
                "Attendance Logged", JOptionPane.INFORMATION_MESSAGE);

        txtInEmpID.setText("");
        lblInPreview.setText(" ");
        resolvedInEmployee = null;
        clearInFields();
    }

    private void onTimeOutClicked(ActionEvent evt) {
        if (resolvedOutEmployee == null) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Employee ID first.",
                    "No Employee Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int empId = resolvedOutEmployee.empId;
        int otHours = AttendanceQuery.computeOTHours();

        int result = AttendanceQuery.clockOut(empId);
        if (result < 0) {
            JOptionPane.showMessageDialog(this,
                    "Failed to record Time Out. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String msg = "<html>✓ Time Out recorded for <b>" + resolvedOutEmployee.fullName + "</b><br>"
                   + "Time Out: " + txtTimeIn.getText();
        if (otHours > 0) {
            msg += "<br>Overtime: <b>+" + otHours + " hour(s)</b> credited to OT balance";
        }
        msg += "</html>";

        JOptionPane.showMessageDialog(this, msg,
                otHours > 0 ? "Time Out — Overtime Recorded" : "Time Out Recorded",
                JOptionPane.INFORMATION_MESSAGE);

        txtOutEmpID.setText("");
        lblOutPreview.setText(" ");
        resolvedOutEmployee = null;
        clearOutFields();
    }

    //  CLOCK
    private void startRealTimeClock() {
        Runnable tick = () -> {
            String status = AttendanceQuery.peekStatus();
            txtTimeIn.setText(timeFormat.format(new Date()));
            applyBadgeStyle(lblStatusBadge, status);

            boolean afterHours = "After Hours".equals(status);
            lblAfterHours.setVisible(afterHours);

            // Show / hide the Time Out tab at the 5 PM boundary
            if (afterHours && !tabTimeOut.isVisible()) {
                tabTimeOut.setVisible(true);
            } else if (!afterHours && tabTimeOut.isVisible()) {
                tabTimeOut.setVisible(false);
                // If someone is on the time-out tab before 5 PM somehow, snap back
                setTabActive(tabTimeIn);
                setTabInactive(tabTimeOut);
                cardLayout.show(cardsPanel, "TIME_IN");
            }

            // Refresh the OT badge on the time-out tab live if an employee is loaded
            if (resolvedOutEmployee != null) {
                int ot = AttendanceQuery.computeOTHours();
                if (ot > 0) {
                    lblOTBadge.setText("  Overtime: +" + ot + "h credit  ");
                    lblOTBadge.setVisible(true);
                } else {
                    lblOTBadge.setVisible(false);
                }
            }
        };
        tick.run();
        runningClockTimer = new Timer(1000, e -> tick.run());
        runningClockTimer.start();
    }

    //  STYLE HELPERS
    private void applyBadgeStyle(JLabel badge, String status) {
        switch (status) {
            case "Late":
                badge.setText("  Late  ");
                badge.setBackground(BADGE_LATE);
                badge.setForeground(BADGE_TEXT_LATE);
                badge.setBorder(BadgeBorder(BADGE_BORDER_LATE));
                break;
            case "Half-Day":
                badge.setText("  Half-Day  ");
                badge.setBackground(BADGE_HALFDAY);
                badge.setForeground(BADGE_TEXT_HALF);
                badge.setBorder(BadgeBorder(BADGE_BORDER_HALF));
                break;
            case "After Hours":
                badge.setText("  After Hours  ");
                badge.setBackground(BADGE_AFTERHOURS);
                badge.setForeground(BADGE_TEXT_AFTERHOURS);
                badge.setBorder(BadgeBorder(BADGE_BORDER_AFTERHOURS));
                break;
            default:
                badge.setText("  Present  ");
                badge.setBackground(BADGE_PRESENT);
                badge.setForeground(BADGE_TEXT_PRESENT);
                badge.setBorder(BadgeBorder(BADGE_BORDER_PRESENT));
                break;
        }
    }

    private javax.swing.border.Border BadgeBorder(Color borderColor) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(4, 10, 4, 10));
    }

    private JLabel makeTab(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(SystemTheme.BOLD_TEXT);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private void setTabActive(JLabel label) {
        label.setForeground(TEXT_MAIN);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, TEXT_MAIN),
                new EmptyBorder(12, 5, 10, 5)));
    }

    private void setTabInactive(JLabel label) {
        label.setForeground(TEXT_MUTED);
        label.setBorder(new EmptyBorder(12, 5, 13, 5));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(SystemTheme.BOLD_TEXT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(160, 42));
        btn.setBorder(new LineBorder(BORDER_CLR, 1, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SystemTheme.BOLD_TEXT);
        label.setForeground(TEXT_MAIN);
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(SystemTheme.NORMAL_TEXT);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_MAIN);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }
}