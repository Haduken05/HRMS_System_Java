package newPanel;

import dataObject.Employee;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class EmployeeManagement extends JPanel {

    // Color Palette Constants (Matching Profile)
    private final Color COLOR_BG = Color.decode("#F8FAFC");
    private final Color COLOR_CARD = Color.WHITE;
    private final Color COLOR_FIELD_BG = Color.decode("#E2E8F0");
    private final Color COLOR_TEXT_MAIN = Color.decode("#0F172A");
    private final Color COLOR_TEXT_MUTED = Color.decode("#64748B");
    private final Color COLOR_ACCENT_BLUE = Color.decode("#0EA5E9");
    private final Color COLOR_BTN_DARK = Color.decode("#000000");
    private final Color COLOR_DANGER = Color.decode("#EF4444");

    // --- CLASS LEVEL UI DECLARATIONS ---
    private JLabel tabDirectory, tabAddEmployee, tabFireEmployee;
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    
    // Directory Panel Components
    private JTextField txtSearchName, txtSearchID;
    private JComboBox<String> cmbSearchDept;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel lblTotalCount;

    // Add Employee Form Fields
    private JTextField txtAddName, txtAddEmail, txtAddPhone, txtAddPosition;
    private JComboBox<String> cmbAddDept;
    private JButton btnSubmitAdd;

    // Fire Employee Components
    private JTextField txtFireID;
    private JTextArea txtFireReason;
    private JButton btnSubmitFire;

    public EmployeeManagement() {
        initComponents();
        loadSampleData();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(1000, 700));
        setBackground(COLOR_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- 1. HEADER SECTION ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Employee Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel breadcrumb = new JLabel("Dashboard / Employees");
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        breadcrumb.setForeground(COLOR_TEXT_MUTED);
        breadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(25));
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. MAIN CONTAINER ---
        JPanel mainContainerCard = new JPanel(new BorderLayout());
        mainContainerCard.setBackground(COLOR_CARD);
        mainContainerCard.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));

        // --- 3. CUSTOM NAVIGATION HEADER TABS ---
        JPanel tabsHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        tabsHeader.setBackground(COLOR_CARD);
        tabsHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));
        
        tabDirectory = new JLabel("Employee Directory");
        tabDirectory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabDirectory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        tabAddEmployee = new JLabel("Add New Employee");
        tabAddEmployee.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabAddEmployee.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tabFireEmployee = new JLabel("Offboard Employee");
        tabFireEmployee.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabFireEmployee.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Initial Interactive State Initialization
        setTabActive(tabDirectory);
        setTabInactive(tabAddEmployee);
        setTabInactive(tabFireEmployee);

        tabsHeader.add(tabDirectory);
        tabsHeader.add(tabAddEmployee);
        tabsHeader.add(tabFireEmployee);
        mainContainerCard.add(tabsHeader, BorderLayout.NORTH);

        // --- 4. CARD LAYOUT PANELS SETUP ---
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(COLOR_CARD);

        // BUILD INDIVIDUAL VIEW CARDS
        initDirectoryCard();
        initAddEmployeeCard();
        initFireEmployeeCard();

        mainContainerCard.add(cardsPanel, BorderLayout.CENTER);
        add(mainContainerCard, BorderLayout.CENTER);

        // --- 5. TAB MOUSE CLICK LISTENERS ---
        tabDirectory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setTabActive(tabDirectory);
                setTabInactive(tabAddEmployee);
                setTabInactive(tabFireEmployee);
                cardLayout.show(cardsPanel, "DIRECTORY");
            }
        });

        tabAddEmployee.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setTabActive(tabAddEmployee);
                setTabInactive(tabDirectory);
                setTabInactive(tabFireEmployee);
                cardLayout.show(cardsPanel, "ADD");
            }
        });

        tabFireEmployee.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setTabActive(tabFireEmployee);
                setTabInactive(tabDirectory);
                setTabInactive(tabAddEmployee);
                cardLayout.show(cardsPanel, "FIRE");
            }
        });
    }

    // --- CARD 1: DIRECTORY & LIVE SEARCH FILTER PANEL ---
    private void initDirectoryCard() {
        JPanel directoryPanel = new JPanel(new BorderLayout());
        directoryPanel.setBackground(COLOR_CARD);
        directoryPanel.setBorder(new EmptyBorder(25, 30, 30, 30));

        // Top Filter Row Container
        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setBackground(COLOR_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Search Inputs Initialization
        txtSearchName = createStyledTextField();
        txtSearchID = createStyledTextField();
        cmbSearchDept = new JComboBox<>(new String[]{"All Departments", "IT Department", "HR Department", "Operations", "Finance", "Administration", "Marketing", "Legal"});
        cmbSearchDept.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbSearchDept.setBackground(COLOR_CARD);
        cmbSearchDept.setPreferredSize(new Dimension(0, 40));

        // Add Filters to Layout System
        gbc.gridx = 0; filterRow.add(createFieldWrapper("Search Name", txtSearchName), gbc);
        gbc.gridx = 1; filterRow.add(createFieldWrapper("Search ID", txtSearchID), gbc);
        gbc.gridx = 2; filterRow.add(createFieldWrapper("Department Filter", cmbSearchDept), gbc);

        // Result Metrics Counter Dashboard Element
        lblTotalCount = new JLabel("Total Results: 0");
        lblTotalCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalCount.setForeground(COLOR_ACCENT_BLUE);
        gbc.gridx = 3; gbc.weightx = 0.2; gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(0, 0, 20, 0);
        filterRow.add(lblTotalCount, gbc);

        directoryPanel.add(filterRow, BorderLayout.NORTH);

        // Modern Customized Data Table Construction
        String[] columns = {"ID", "Full Name", "Department", "Position", "Contact No"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        employeeTable = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(rowSorter);

        // UI Styling of Table Rows and Headers
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeTable.setRowHeight(35);
        employeeTable.setGridColor(Color.decode("#F1F5F9"));
        employeeTable.setSelectionBackground(Color.decode("#F0F9FF"));
        employeeTable.setSelectionForeground(COLOR_TEXT_MAIN);
        employeeTable.setShowVerticalLines(false);

        JTableHeader tableHeader = employeeTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(Color.decode("#F8FAFC"));
        tableHeader.setForeground(COLOR_TEXT_MUTED);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));

        // Aligning Data Center/Left with Custom Renderers
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        employeeTable.setDefaultRenderer(Object.class, cellRenderer);

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(new LineBorder(Color.decode("#E2E8F0"), 1, true));
        scrollPane.getViewport().setBackground(COLOR_CARD);
        
        directoryPanel.add(scrollPane, BorderLayout.CENTER);
        cardsPanel.add(directoryPanel, "DIRECTORY");

        // Attaching Dynamic Document Search Logic
        DocumentListener searchListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { executeSearchFilter(); }
            public void removeUpdate(DocumentEvent e) { executeSearchFilter(); }
            public void changedUpdate(DocumentEvent e) { executeSearchFilter(); }
        };
        txtSearchName.getDocument().addDocumentListener(searchListener);
        txtSearchID.getDocument().addDocumentListener(searchListener);
        cmbSearchDept.addActionListener(e -> executeSearchFilter());
    }

    // --- CARD 2: ADD EMPLOYEE ARCHITECTURE ---
    private void initAddEmployeeCard() {
        JPanel addPanel = new JPanel(new BorderLayout());
        addPanel.setBackground(COLOR_CARD);
        addPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel caption = new JLabel("Onboard a New Corporate Profile");
        caption.setFont(new Font("Segoe UI", Font.BOLD, 18));
        caption.setForeground(COLOR_TEXT_MAIN);
        caption.setBorder(new EmptyBorder(0, 0, 20, 0));
        addPanel.add(caption, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 30, 25));
        formGrid.setBackground(COLOR_CARD);

        formGrid.add(createFieldWrapper("Full Name", txtAddName = createStyledFormTextField()));
        formGrid.add(createFieldWrapper("Email Address", txtAddEmail = createStyledFormTextField()));
        formGrid.add(createFieldWrapper("Contact Number", txtAddPhone = createStyledFormTextField()));
        formGrid.add(createFieldWrapper("Position Description", txtAddPosition = createStyledFormTextField()));

        String[] depts = {"IT Department", "HR Department", "Operations", "Finance", "Administration", "Marketing", "Legal"};
        cmbAddDept = new JComboBox<>(depts);
        cmbAddDept.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbAddDept.setBackground(COLOR_CARD);
        cmbAddDept.setPreferredSize(new Dimension(0, 40));
        formGrid.add(createFieldWrapper("Assigned Corporate Department", cmbAddDept));

        addPanel.add(formGrid, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 25));
        footer.setBackground(COLOR_CARD);
        btnSubmitAdd = new JButton("Onboard Employee");
        styleButton(btnSubmitAdd, COLOR_BTN_DARK, Color.WHITE);
        btnSubmitAdd.setPreferredSize(new Dimension(180, 42));
        btnSubmitAdd.addActionListener(e -> processAddEmployee());
        footer.add(btnSubmitAdd);
        addPanel.add(footer, BorderLayout.SOUTH);

        cardsPanel.add(addPanel, "ADD");
    }

    // --- CARD 3: TERMINATE / FIRE EMPLOYEE CARD ---
    private void initFireEmployeeCard() {
        JPanel firePanel = new JPanel(new BorderLayout());
        firePanel.setBackground(COLOR_CARD);
        firePanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel caption = new JLabel("Employee Separation Management");
        caption.setFont(new Font("Segoe UI", Font.BOLD, 18));
        caption.setForeground(COLOR_TEXT_MAIN);
        caption.setBorder(new EmptyBorder(0, 0, 20, 0));
        firePanel.add(caption, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel();
        centerContainer.setBackground(COLOR_CARD);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        txtFireID = createStyledFormTextField();
        txtFireID.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JPanel idWrapper = createFieldWrapper("Target Employee System ID (e.g. 101, 102)", txtFireID);
        idWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtFireReason = new JTextArea(6, 20);
        txtFireReason.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtFireReason.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        txtFireReason.setLineWrap(true);
        txtFireReason.setWrapStyleWord(true);
        JPanel reasonWrapper = createFieldWrapper("Formal Reason for Corporate Offboarding/Separation", txtFireReason);
        reasonWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerContainer.add(idWrapper);
        centerContainer.add(Box.createVerticalStrut(20));
        centerContainer.add(reasonWrapper);
        firePanel.add(centerContainer, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 25));
        footer.setBackground(COLOR_CARD);
        btnSubmitFire = new JButton("Revoke System Credentials");
        styleButton(btnSubmitFire, COLOR_DANGER, Color.WHITE);
        btnSubmitFire.setPreferredSize(new Dimension(220, 42));
        btnSubmitFire.addActionListener(e -> processFireEmployee());
        footer.add(btnSubmitFire);
        firePanel.add(footer, BorderLayout.SOUTH);

        cardsPanel.add(firePanel, "FIRE");
    }

    // --- FILTER ENGINE LOGIC ---
    private void executeSearchFilter() {
        String nameQuery = txtSearchName.getText().trim().toLowerCase();
        String idQuery = txtSearchID.getText().trim().toLowerCase();
        String selectedDept = (String) cmbSearchDept.getSelectedItem();

        RowFilter<DefaultTableModel, Object> combinedFilter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String id = entry.getStringValue(0).toLowerCase();
                String name = entry.getStringValue(1).toLowerCase();
                String dept = entry.getStringValue(2);

                boolean matchesName = name.contains(nameQuery);
                boolean matchesID = id.contains(idQuery);
                boolean matchesDept = selectedDept.equals("All Departments") || dept.equalsIgnoreCase(selectedDept);

                return matchesName && matchesID && matchesDept;
            }
        };
        rowSorter.setRowFilter(combinedFilter);
        
        // Update Live Counter System metric UI
        int currentMatches = rowSorter.getViewRowCount();
        lblTotalCount.setText("Total Results: " + currentMatches);
    }

    // --- FORM PROCESS ACTIONS ---
    private void processAddEmployee() {
        String name = txtAddName.getText().trim();
        String email = txtAddEmail.getText().trim();
        String phone = txtAddPhone.getText().trim();
        String position = txtAddPosition.getText().trim();
        String dept = (String) cmbAddDept.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || position.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fulfill all contextual required text fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Generate ID based on row count
        int customNewID = tableModel.getRowCount() + 1;
        tableModel.addRow(new Object[]{String.valueOf(customNewID), name, dept, position, phone});
        
        // Success Dialog UI & Clean-up
        JOptionPane.showMessageDialog(this, name + " registered successfully under corporate registry ID: " + customNewID, "Onboard Success", JOptionPane.INFORMATION_MESSAGE);
        txtAddName.setText(""); txtAddEmail.setText(""); txtAddPhone.setText(""); txtAddPosition.setText("");
        executeSearchFilter();
        cardLayout.show(cardsPanel, "DIRECTORY");
        setTabActive(tabDirectory); setTabInactive(tabAddEmployee);
    }

    private void processFireEmployee() {
        String targetID = txtFireID.getText().trim();
        String logs = txtFireReason.getText().trim();

        if (targetID.isEmpty() || logs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Input credentials and exit parameters to finalize offboarding.", "Verification Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean profileLocated = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).toString().equalsIgnoreCase(targetID)) {
                String empName = tableModel.getValueAt(i, 1).toString();
                
                int choice = JOptionPane.showConfirmDialog(this, 
                        "Confirm separation actions for target ID " + targetID + " (" + empName + ")? This configuration deletes system authorizations.", 
                        "Destructive Separation Event Warning", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                
                if (choice == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(i);
                    JOptionPane.showMessageDialog(this, "Profile and references removed safely from primary tracking directories.", "Account Deactivated", JOptionPane.INFORMATION_MESSAGE);
                    txtFireID.setText(""); txtFireReason.setText("");
                    executeSearchFilter();
                    cardLayout.show(cardsPanel, "DIRECTORY");
                    setTabActive(tabDirectory); setTabInactive(tabFireEmployee);
                }
                profileLocated = true;
                break;
            }
        }

        if (!profileLocated) {
            JOptionPane.showMessageDialog(this, "No active records found corresponding to target ID: " + targetID, "Identity Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- TAB RENDERING HELPERS ---
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

    private JPanel createFieldWrapper(String labelText, JComponent inputComponent) {
        JPanel wrapper = new JPanel();
        wrapper.setBackground(COLOR_CARD);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        return textField;
    }

    private JTextField createStyledFormTextField() {
        JTextField f = createStyledTextField();
        f.setEditable(true); // Enables active user inputs inside forms
        return f;
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    // --- MOCK DATABASE INITIALIZATION (Matches Image Dataset) ---
    private void loadSampleData() {
        tableModel.addRow(new Object[]{"1", "Jade Marco Ayop", "IT Department", "Software Developer", "0917-123-4567"});
        tableModel.addRow(new Object[]{"2", "Asa Marie Cathel", "HR Department", "HR Manager", "0918-987-6543"});
        tableModel.addRow(new Object[]{"3", "Remar Baer", "Operations", "Project Coordinator", "0922-555-0011"});
        tableModel.addRow(new Object[]{"4", "Yuuri Tizon", "IT Department", "System Analyst", "0915-111-2233"});
        tableModel.addRow(new Object[]{"5", "Admin User", "Administration", "HR Manager", "0912-345-6789"});
        tableModel.addRow(new Object[]{"6", "Liam Henderson", "IT Department", "Software Engineer", "09171112223"});
        tableModel.addRow(new Object[]{"7", "Noah Santiago", "HR Department", "HR Specialist", "09182223334"});
        tableModel.addRow(new Object[]{"8", "Oliver Bennett", "Finance", "Accountant", "09193334445"});
        tableModel.addRow(new Object[]{"9", "Elijah Dumagat", "Marketing", "Content Creator", "09204445556"});
        tableModel.addRow(new Object[]{"10", "James Villafuerte", "Operations", "Logistics Manager", "09215556667"});
        
        executeSearchFilter(); // Triggers base calculation for dynamic metrics UI text update
    }
}