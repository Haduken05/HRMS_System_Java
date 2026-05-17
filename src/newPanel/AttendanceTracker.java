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

    private final Color BG = SystemTheme.ACCENT_COLOR;
    private final Color FIELD_BG = SystemTheme.FIELD_BG;
    private final Color BORDER_CLR = SystemTheme.BORDER_COLOR;
    private final Color TEXT_MAIN = SystemTheme.TEXT_MAIN;
    private final Color TEXT_MUTED = SystemTheme.TEXT_MUTED;
    private final Color BTN_SUBMIT = SystemTheme.BTN_YES;
    private final Color COLOR_ERR = SystemTheme.BTN_NO;

    // Status badge palette
    private static final Color BADGE_PRESENT = Color.decode("#D1FAE5");
    private static final Color BADGE_LATE = Color.decode("#FEF9C3");
    private static final Color BADGE_HALFDAY = Color.decode("#FFE4E6");
    private static final Color BADGE_AFTERHOURS = Color.decode("#EDE9FE");
    private static final Color BADGE_BORDER_PRESENT = Color.decode("#6EE7B7");
    private static final Color BADGE_BORDER_LATE = Color.decode("#FDE047");
    private static final Color BADGE_BORDER_HALF = Color.decode("#FDA4AF");
    private static final Color BADGE_BORDER_AFTERHOURS = Color.decode("#C4B5FD");
    private static final Color BADGE_TEXT_PRESENT = Color.decode("#065F46");
    private static final Color BADGE_TEXT_LATE = Color.decode("#92400E");
    private static final Color BADGE_TEXT_HALF = Color.decode("#9F1239");
    private static final Color BADGE_TEXT_AFTERHOURS = Color.decode("#4C1D95");

    private JTextField txtEmpID;
    private JTextField txtFullName;
    private JTextField txtDepartment;
    private JTextField txtPosition;
    private JTextField txtTimeIn;      
    private JLabel lblPreview;
    private JLabel lblStatusBadge;  
    private JLabel lblAfterHours;   
    private JButton btnCheckAttendance;

    private Employee resolvedEmployee = null;

    private Timer runningClockTimer;
    private final SimpleDateFormat timeFormat
            = new SimpleDateFormat("h:mm a  —  MMMM d, yyyy");

    public AttendanceTracker() {
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

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(25));
        add(headerPanel, BorderLayout.NORTH);

        // FORM COLUMN
        JPanel leftColumn = new JPanel();
        leftColumn.setBackground(BG);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        // Employee ID
        leftColumn.add(createFieldLabel("Employee ID"));
        txtEmpID = createStyledTextField();
        leftColumn.add(txtEmpID);

        lblPreview = new JLabel(" ");
        lblPreview.setFont(SystemTheme.BOLD_TEXT);
        lblPreview.setForeground(COLOR_ERR);
        lblPreview.setBorder(new EmptyBorder(4, 2, 6, 0));
        lblPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftColumn.add(lblPreview);

        leftColumn.add(Box.createVerticalStrut(8));

        // Auto-filled fields
        leftColumn.add(createFieldLabel("Full Name"));
        txtFullName = createStyledTextField();
        txtFullName.setEditable(false);
        leftColumn.add(txtFullName);
        leftColumn.add(Box.createVerticalStrut(14));

        leftColumn.add(createFieldLabel("Department"));
        txtDepartment = createStyledTextField();
        txtDepartment.setEditable(false);
        leftColumn.add(txtDepartment);
        leftColumn.add(Box.createVerticalStrut(14));

        leftColumn.add(createFieldLabel("Position"));
        txtPosition = createStyledTextField();
        txtPosition.setEditable(false);
        leftColumn.add(txtPosition);
        leftColumn.add(Box.createVerticalStrut(14));

        // TIMESTAMP (full width, restored)
        leftColumn.add(createFieldLabel("Current Time"));
        txtTimeIn = createStyledTextField();
        txtTimeIn.setEditable(false);
        txtTimeIn.setForeground(Color.decode("#2563EB"));
        leftColumn.add(txtTimeIn);
        leftColumn.add(Box.createVerticalStrut(10));

        // STATUS BADGE ROW (sits beneath the clock)
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setBackground(BG);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStatusBadge = new JLabel("  Present  ");
        lblStatusBadge.setFont(SystemTheme.BOLD_TEXT);
        lblStatusBadge.setOpaque(true);
        applyBadgeStyle(lblStatusBadge, "Present"); // initialise colours
        badgeRow.add(lblStatusBadge);

        leftColumn.add(badgeRow);
        leftColumn.add(Box.createVerticalStrut(6));

        // AFTER HOURS WARNING
        lblAfterHours = new JLabel(
                " The workday has ended (after 5:00 PM). "
                + "This record will be marked as After Hours.");
        lblAfterHours.setFont(SystemTheme.NORMAL_TEXT);
        lblAfterHours.setForeground(BADGE_TEXT_AFTERHOURS);
        lblAfterHours.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAfterHours.setVisible(false); // shown dynamically by the clock tick
        leftColumn.add(lblAfterHours);

        // BUTTON ROW
        JPanel bottomArea = new JPanel();
        bottomArea.setBackground(BG);
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBorder(new EmptyBorder(30, 0, 0, 0));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonRow.setBackground(BG);

        btnCheckAttendance = new JButton("Check Attendance");
        btnCheckAttendance.setFont(SystemTheme.BOLD_TEXT);
        btnCheckAttendance.setForeground(Color.WHITE);
        btnCheckAttendance.setBackground(BTN_SUBMIT);
        btnCheckAttendance.setFocusPainted(false);
        btnCheckAttendance.setOpaque(true);
        btnCheckAttendance.setPreferredSize(new Dimension(190, 42));
        btnCheckAttendance.setBorder(new LineBorder(BORDER_CLR, 1, true));
        btnCheckAttendance.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckAttendance.addActionListener(this::btnCheckAttendanceActionPerformed);

        buttonRow.add(btnCheckAttendance);
        bottomArea.add(buttonRow);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(BG);
        centerWrapper.add(leftColumn, BorderLayout.CENTER);
        centerWrapper.add(bottomArea, BorderLayout.SOUTH);
        add(centerWrapper, BorderLayout.CENTER);

        txtEmpID.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                resolveEmployeeId();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resolveEmployeeId();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                resolveEmployeeId();
            }
        });
    }

    //  LIVE EMPLOYEE RESOLUTION
    private void resolveEmployeeId() {
        String text = txtEmpID.getText().trim();
        clearEmployeeFields();

        if (text.isEmpty()) {
            lblPreview.setText(" ");
            resolvedEmployee = null;
            return;
        }

        int id;
        try {
            id = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            lblPreview.setText("X  ID must be a number");
            lblPreview.setForeground(COLOR_ERR);
            resolvedEmployee = null;
            return;
        }

        if (id == 5) {
            lblPreview.setText("X  You're selecting the Attendance Kiosk. Change it to proceed.");
            lblPreview.setForeground(COLOR_ERR);
            resolvedEmployee = null;
            return;
        }

        Employee emp = EmployeeQuery.getById(id);
        if (emp != null) {
            resolvedEmployee = emp;
            lblPreview.setText(" ");
            txtFullName.setText(emp.fullName);
            txtDepartment.setText(emp.department);
            txtPosition.setText(emp.position);
        } else {
            resolvedEmployee = null;
            lblPreview.setText("X  No employee found with this ID");
            lblPreview.setForeground(COLOR_ERR);
        }
    }

    private void clearEmployeeFields() {
        txtFullName.setText("");
        txtDepartment.setText("");
        txtPosition.setText("");
    }

    //  CHECK ATTENDANCE BUTTON
    private void btnCheckAttendanceActionPerformed(ActionEvent evt) {
        if (resolvedEmployee == null) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Employee ID first.",
                    "No Employee Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int empId = resolvedEmployee.empId;

        if (AttendanceQuery.hasOpenEntry(empId)) {
            JOptionPane.showMessageDialog(this,
                    "<html><b>" + resolvedEmployee.fullName + "</b> is already clocked in today.<br>"
                    + "Please use the clock-out station to log their departure.</html>",
                    "Already Clocked In", JOptionPane.INFORMATION_MESSAGE);
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
                "<html>✓ Attendance recorded for <b>" + resolvedEmployee.fullName + "</b><br>"
                + "Time In: " + txtTimeIn.getText() + "<br>"
                + "Status: <b>" + status + "</b></html>",
                "Attendance Logged", JOptionPane.INFORMATION_MESSAGE);

        txtEmpID.setText("");
        lblPreview.setText(" ");
        resolvedEmployee = null;
        clearEmployeeFields();
    }

    //  CLOCK + BADGE + AFTER-HOURS WARNING
    private void startRealTimeClock() {
        Runnable tick = () -> {
            String status = AttendanceQuery.peekStatus();
            txtTimeIn.setText(timeFormat.format(new Date()));
            applyBadgeStyle(lblStatusBadge, status);
            lblAfterHours.setVisible("After Hours".equals(status));
        };
        tick.run();
        runningClockTimer = new Timer(1000, e -> tick.run());
        runningClockTimer.start();
    }

    //  BADGE STYLE HELPER  (single place to update colours + text)
    private void applyBadgeStyle(JLabel badge, String status) {
        switch (status) {
            case "Late":
                badge.setText("  Late  ");
                badge.setBackground(BADGE_LATE);
                badge.setForeground(BADGE_TEXT_LATE);
                badge.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BADGE_BORDER_LATE, 1, true),
                        new EmptyBorder(4, 10, 4, 10)));
                break;
            case "Half-Day":
                badge.setText("  Half-Day  ");
                badge.setBackground(BADGE_HALFDAY);
                badge.setForeground(BADGE_TEXT_HALF);
                badge.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BADGE_BORDER_HALF, 1, true),
                        new EmptyBorder(4, 10, 4, 10)));
                break;
            case "After Hours":
                badge.setText("  After Hours  ");
                badge.setBackground(BADGE_AFTERHOURS);
                badge.setForeground(BADGE_TEXT_AFTERHOURS);
                badge.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BADGE_BORDER_AFTERHOURS, 1, true),
                        new EmptyBorder(4, 10, 4, 10)));
                break;
            default: // Present
                badge.setText("  Present  ");
                badge.setBackground(BADGE_PRESENT);
                badge.setForeground(BADGE_TEXT_PRESENT);
                badge.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BADGE_BORDER_PRESENT, 1, true),
                        new EmptyBorder(4, 10, 4, 10)));
                break;
        }
    }

    //  STYLE HELPERS
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
