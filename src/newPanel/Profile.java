package newPanel;

import com.toedter.calendar.JDateChooser;
import dataObject.Employee;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern Profile Management Interface with Tab Switching
 * @author Coke
 */
public class Profile extends JPanel {

    // Color Palette Constants
    private final Color COLOR_BG = Color.decode("#F8FAFC");
    private final Color COLOR_CARD = Color.WHITE;
    private final Color COLOR_FIELD_BG = Color.decode("#E2E8F0");
    private final Color COLOR_TEXT_MAIN = Color.decode("#0F172A");
    private final Color COLOR_TEXT_MUTED = Color.decode("#64748B");
    private final Color COLOR_ACCENT_BLUE = Color.decode("#0EA5E9");
    private final Color COLOR_BTN_DARK = Color.decode("#000000");

    // --- CLASS LEVEL FIELD DECLARATIONS ---
    private JLabel lblAvatarCircle, lblProfileName, lblEmployeeID;
    private JLabel tabPersonalInfo, tabChangePassword;
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    
    // Personal Info Fields
    private JTextField txtFullName, txtEmail, txtPhone, txtDepartment, txtPosition, txtHireDate;
    
    // Change Password Fields
    private JPasswordField txtCurrentPwd, txtNewPwd, txtConfirmPwd;
    
    private JButton btnSaveChanges;

    public Profile() {
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(800, 700));
        setBackground(COLOR_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- 1. HEADER SECTION ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel breadcrumb = new JLabel("Dashboard / Profile");
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        breadcrumb.setForeground(COLOR_TEXT_MUTED);
        breadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(25));
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. MAIN SPLIT PROFILE CONTAINER ---
        JPanel mainSplitCard = new JPanel(new BorderLayout());
        mainSplitCard.setBackground(COLOR_CARD);
        mainSplitCard.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));

        // LEFT COLUMN (Avatar)
        JPanel leftColumn = new JPanel();
        leftColumn.setBackground(COLOR_CARD);
        leftColumn.setPreferredSize(new Dimension(220, 0));
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setBorder(new EmptyBorder(40, 20, 40, 20));

        lblAvatarCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#E2E8F0"));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatarCircle.setPreferredSize(new Dimension(140, 140));
        lblAvatarCircle.setMaximumSize(new Dimension(140, 140));
        lblAvatarCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblProfileName = new JLabel("Employee Name");
        lblProfileName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblProfileName.setForeground(COLOR_TEXT_MAIN);
        lblProfileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblEmployeeID = new JLabel("EMP000");
        lblEmployeeID.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEmployeeID.setForeground(COLOR_TEXT_MUTED);
        lblEmployeeID.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftColumn.add(lblAvatarCircle);
        leftColumn.add(Box.createVerticalStrut(20));
        leftColumn.add(lblProfileName);
        leftColumn.add(Box.createVerticalStrut(4));
        leftColumn.add(lblEmployeeID);

        // RIGHT COLUMN (Tabs & Form)
        JPanel rightColumn = new JPanel(new BorderLayout());
        rightColumn.setBackground(COLOR_CARD);
        rightColumn.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, COLOR_ACCENT_BLUE));

        // --- TAB HEADER ---
        JPanel tabsHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        tabsHeader.setBackground(COLOR_CARD);
        tabsHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));
        
        tabPersonalInfo = new JLabel("Personal Information");
        tabPersonalInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabPersonalInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        tabChangePassword = new JLabel("Change Password");
        tabChangePassword.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabChangePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Initial styling for Personal Info tab
        setTabActive(tabPersonalInfo);
        setTabInactive(tabChangePassword);

        tabsHeader.add(tabPersonalInfo);
        tabsHeader.add(tabChangePassword);
        rightColumn.add(tabsHeader, BorderLayout.NORTH);

        // --- CARD LAYOUT CONTENT ---
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(COLOR_CARD);

        // CARD 1: Personal Info
        JPanel personalInfoGrid = new JPanel(new GridLayout(3, 2, 25, 20));
        personalInfoGrid.setBackground(COLOR_CARD);
        personalInfoGrid.setBorder(new EmptyBorder(30, 30, 30, 30));

        personalInfoGrid.add(createFieldWrapper("Full Name", txtFullName = createStyledTextField()));
        personalInfoGrid.add(createFieldWrapper("Email", txtEmail = createStyledTextField()));
        personalInfoGrid.add(createFieldWrapper("Phone", txtPhone = createStyledTextField()));
        personalInfoGrid.add(createFieldWrapper("Department", txtDepartment = createStyledTextField()));
        personalInfoGrid.add(createFieldWrapper("Position", txtPosition = createStyledTextField()));
        personalInfoGrid.add(createFieldWrapper("Hire Date", txtHireDate = createStyledTextField()));

        // CARD 2: Change Password
        JPanel changePasswordGrid = new JPanel(new GridLayout(3, 1, 0, 20));
        changePasswordGrid.setBackground(COLOR_CARD);
        changePasswordGrid.setBorder(new EmptyBorder(30, 30, 30, 200)); // Pushes fields to be narrower

        changePasswordGrid.add(createFieldWrapper("Current Password", txtCurrentPwd = createStyledPasswordField()));
        changePasswordGrid.add(createFieldWrapper("New Password", txtNewPwd = createStyledPasswordField()));
        changePasswordGrid.add(createFieldWrapper("Confirm Password", txtConfirmPwd = createStyledPasswordField()));

        cardsPanel.add(personalInfoGrid, "PERSONAL");
        cardsPanel.add(changePasswordGrid, "PASSWORD");

        rightColumn.add(cardsPanel, BorderLayout.CENTER);

        // Footer Button
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

        // --- TAB LISTENERS ---
        tabPersonalInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setTabActive(tabPersonalInfo);
                setTabInactive(tabChangePassword);
                cardLayout.show(cardsPanel, "PERSONAL");
                btnSaveChanges.setText("Save Changes");
            }
        });

        tabChangePassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setTabActive(tabChangePassword);
                setTabInactive(tabPersonalInfo);
                cardLayout.show(cardsPanel, "PASSWORD");
                btnSaveChanges.setText("Update Password");
            }
        });
    }

    // --- TAB STYLING LOGIC ---
    private void setTabActive(JLabel label) {
        label.setForeground(COLOR_TEXT_MAIN);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_TEXT_MAIN),
                new EmptyBorder(15, 5, 12, 5)
        ));
    }

    private void setTabInactive(JLabel label) {
        label.setForeground(COLOR_TEXT_MUTED);
        label.setBorder(new EmptyBorder(15, 5, 15, 5));
    }

    // --- HELPERS ---
    private JPanel createFieldWrapper(String labelText, JComponent inputComponent) {
        JPanel wrapper = new JPanel();
        wrapper.setBackground(COLOR_CARD);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(COLOR_TEXT_MAIN);
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(label);
        wrapper.add(inputComponent);
        return wrapper;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(COLOR_CARD);
        textField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        textField.setPreferredSize(new Dimension(0, 40));
        textField.setEditable(false); // Default to read-only for Personal Info
        return textField;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pwdField = new JPasswordField();
        pwdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pwdField.setBackground(COLOR_CARD);
        pwdField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        pwdField.setPreferredSize(new Dimension(0, 40));
        return pwdField;
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
    }

    public void loadProfile(Employee emp) {
        lblProfileName.setText(emp.fullName);
        lblEmployeeID.setText("EMP" + String.format("%03d", emp.empId));
        txtFullName.setText(emp.fullName);
        txtEmail.setText("juan.delacruz@gmail.com"); 
        txtPhone.setText(emp.contactNo);
        txtDepartment.setText(emp.department);
        txtPosition.setText(emp.position);
        txtHireDate.setText("January 10, 2022");       
    }
}