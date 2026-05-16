package newPanel;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ApplyLeave extends JPanel {

    // Color Palette Constants
    private final Color COLOR_BG = Color.decode("#F8FAFC");          // Very light blue/gray background
    private final Color COLOR_CARD = Color.WHITE;                    // White container
    private final Color COLOR_FIELD_BG = Color.decode("#E2E8F0");    // Light gray input background
    private final Color COLOR_TEXT_MAIN = Color.decode("#0F172A");   // Dark slate text
    private final Color COLOR_TEXT_MUTED = Color.decode("#334155");  // Secondary dark text
    private final Color COLOR_BTN_SUBMIT = Color.decode("#475569");  // Charcoal/Dark Gray primary button
    private final Color COLOR_BTN_CANCEL = Color.decode("#F1F5F9");  // Off-white secondary button
    
    private JTextField txtEmpID, txtNameEmployee;
    private JComboBox<String> cmbLeaveType;
    private JDateChooser dcFrom;
    private JDateChooser dcTo;
    private JButton btnCancel;
    private JButton btnSubmit;

    public ApplyLeave() {
        initComponents();
    }

    private void initComponents() {
        // Set fixed dimensions
        setPreferredSize(new Dimension(800, 700));
        setBackground(COLOR_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- 1. HEADER SECTION ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("File Leave Request");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align Header

        JLabel breadcrumb = new JLabel("Dashboard / Leave Management / File Leave");
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        breadcrumb.setForeground(COLOR_TEXT_MUTED);
        breadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align Breadcrumb

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(25));
        
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. MAIN CONTENT SPLIT LAYOUT ---
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        contentGrid.setBackground(COLOR_BG);

        // LEFT COLUMN: Form Fields
        JPanel leftColumn = new JPanel();
        leftColumn.setBackground(COLOR_BG);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        // Employee ID Input Field
        leftColumn.add(createFieldLabel("Employee ID"));
        txtEmpID = createStyledTextField();
        leftColumn.add(txtEmpID);
        leftColumn.add(Box.createVerticalStrut(15));

        // Employee Name
        leftColumn.add(createFieldLabel("Employee Name"));
        txtNameEmployee = createStyledTextField();
        leftColumn.add(txtNameEmployee);
        leftColumn.add(Box.createVerticalStrut(15));

        // Leave Type
        leftColumn.add(createFieldLabel("Leave Type"));
        String[] leaveTypes = {"Select Leave Type", "Vacation Leave (VL)", "Sick Leave (SL)", "Emergency Leave (EL)"};
        cmbLeaveType = createStyledComboBox(leaveTypes);
        leftColumn.add(cmbLeaveType);
        leftColumn.add(Box.createVerticalStrut(15));

        // From Date (JDateChooser)
        leftColumn.add(createFieldLabel("From Date"));
        dcFrom = createStyledDateChooser();
        leftColumn.add(dcFrom);
        leftColumn.add(Box.createVerticalStrut(15));

        // To Date (JDateChooser)
        leftColumn.add(createFieldLabel("To Date"));
        dcTo = createStyledDateChooser();
        leftColumn.add(dcTo);

        // RIGHT COLUMN: Drag & Drop Box
        JPanel rightColumn = new JPanel(new BorderLayout());
        rightColumn.setBackground(COLOR_BG);
        
        JPanel uploadLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        uploadLabelPanel.setBackground(COLOR_BG);
        uploadLabelPanel.add(createFieldLabel("Upload"));
        rightColumn.add(uploadLabelPanel, BorderLayout.NORTH);

        // The Dropzone Card
        JPanel dropZone = new JPanel(new GridBagLayout());
        dropZone.setBackground(COLOR_FIELD_BG);
        dropZone.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel uploadText = new JLabel("<html><center>Drag & drop file here<br>or click to upload<br><font color='#64748B'>(PDF, JPG, PNG - Max 5MB)</font></center></html>");
        uploadText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        uploadText.setForeground(COLOR_TEXT_MUTED);
        dropZone.add(uploadText);
        
        rightColumn.add(dropZone, BorderLayout.CENTER);

        // Add both columns to grid
        contentGrid.add(leftColumn);
        contentGrid.add(rightColumn);

        // --- 3. BOTTOM AREA (Rules & Action Buttons) ---
        JPanel bottomArea = new JPanel();
        bottomArea.setBackground(COLOR_BG);
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBorder(new EmptyBorder(25, 0, 0, 0));

        // Policy/Rules Box
        JPanel rulesBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        rulesBox.setBackground(COLOR_FIELD_BG);
        rulesBox.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));
        rulesBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel rulesText = new JLabel("<html><ul>" +
                "<li>Vacation Leave and Sick Leave must be filled 2 days before the leave.</li>" +
                "<li>Sick Leave filed after the leave requires medical certificate if 2 or more days absent.</li>" +
                "</ul></html>");
        rulesText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rulesText.setForeground(COLOR_TEXT_MAIN);
        rulesBox.add(rulesText);
        
        bottomArea.add(rulesBox);
        bottomArea.add(Box.createVerticalStrut(25));

        // Buttons Panel
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonRow.setBackground(COLOR_BG);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCancel = new JButton("Cancel");
        styleButton(btnCancel, COLOR_BTN_CANCEL, COLOR_TEXT_MAIN);
        btnCancel.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));

        btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, COLOR_BTN_SUBMIT, Color.WHITE);

        buttonRow.add(btnCancel);
        buttonRow.add(btnSubmit);
        bottomArea.add(buttonRow);

        // Package structural panels into Center
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(COLOR_BG);
        centerWrapper.add(contentGrid, BorderLayout.CENTER);
        centerWrapper.add(bottomArea, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(COLOR_TEXT_MAIN);
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT); // Enforce Left-align in BoxLayout
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(COLOR_FIELD_BG);
        textField.setForeground(COLOR_TEXT_MAIN);
        textField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        textField.setPreferredSize(new Dimension(0, 40));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT); // Enforce Left-align in BoxLayout
        return textField;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(COLOR_FIELD_BG);
        comboBox.setForeground(COLOR_TEXT_MAIN);
        comboBox.setPreferredSize(new Dimension(0, 40));
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT); // Enforce Left-align in BoxLayout
        return comboBox;
    }

    private JDateChooser createStyledDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateChooser.setBackground(COLOR_FIELD_BG);
        dateChooser.setPreferredSize(new Dimension(0, 40));
        dateChooser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        dateChooser.setAlignmentX(Component.LEFT_ALIGNMENT); // Enforce Left-align in BoxLayout
        
        // Match JDateChooser's internal text field styling
        JTextField textField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        textField.setBackground(COLOR_FIELD_BG);
        textField.setForeground(COLOR_TEXT_MAIN);
        textField.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        return dateChooser;
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(160, 45));
        button.setFocusPainted(false);
        button.setBorderPainted(bg != COLOR_BTN_CANCEL);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // --- USER PROFILE HOOKS ---
    public void setEmployee(int id, String name) {
        txtEmpID.setText(String.valueOf(id));
        txtEmpID.setEditable(false);
        
        txtNameEmployee.setText(name);
        txtNameEmployee.setEditable(false);
    }   

}