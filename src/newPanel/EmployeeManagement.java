package newPanel;

import logic.EmployeeValidation;
import dataObject.Employee;
import dbquery.EmployeeQuery;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class EmployeeManagement extends JPanel {

    private final Color COLOR_BG = Color.decode("#F8FAFC");
    private final Color COLOR_CARD = Color.WHITE;
    private final Color COLOR_TEXT_MAIN = Color.decode("#0F172A");
    private final Color COLOR_TEXT_MUTED = Color.decode("#64748B");
    private final Color COLOR_ACCENT = Color.decode("#0EA5E9");
    private final Color COLOR_BTN_DARK = Color.decode("#1E293B");
    private final Color COLOR_DANGER = Color.decode("#EF4444");

    private JLabel tabDirectory, tabAddEmployee, tabOffboard;
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    private JTextField txtSearchName, txtSearchID;
    private JComboBox<String> cmbSearchDept;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel lblTotalCount;

    private JTextField txtAddName, txtAddPhone;
    private JComboBox<String> cmbAddDept, cmbAddPosition;
    private JRadioButton radEmployee, radSupervisor;
    private ButtonGroup roleGroup;
    private JButton btnSubmitAdd;

    private JTextField txtFireID;
    private JTextArea txtFireReason;
    private JButton btnSubmitFire;
    private JLabel lblFirePreview;

    private static final String[] DEPARTMENTS = {
        "IT Department", "HR Department", "Operation Department", "Marketing Department"
    };

    public EmployeeManagement() {
        initComponents();
        loadFromDatabase();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(1000, 700));
        setBackground(COLOR_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
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

        // Main card
        JPanel mainCard = new JPanel(new BorderLayout());
        mainCard.setBackground(COLOR_CARD);
        mainCard.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));

        // Tab strip
        JPanel tabsHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        tabsHeader.setBackground(COLOR_CARD);
        tabsHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));

        tabDirectory = makeTab("Employee Directory");
        tabAddEmployee = makeTab("Add New Employee");
        tabOffboard = makeTab("Offboard Employee");

        setTabActive(tabDirectory);
        setTabInactive(tabAddEmployee);
        setTabInactive(tabOffboard);

        tabsHeader.add(tabDirectory);
        tabsHeader.add(tabAddEmployee);
        tabsHeader.add(tabOffboard);
        mainCard.add(tabsHeader, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(COLOR_CARD);

        buildDirectoryCard();
        buildAddCard();
        buildOffboardCard();

        mainCard.add(cardsPanel, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        // Tab listeners
        tabDirectory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchTab("DIRECTORY", tabDirectory, tabAddEmployee, tabOffboard);
            }
        });
        tabAddEmployee.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchTab("ADD", tabAddEmployee, tabDirectory, tabOffboard);
            }
        });
        tabOffboard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchTab("OFFBOARD", tabOffboard, tabDirectory, tabAddEmployee);
            }
        });
    }

    private void buildDirectoryCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_CARD);
        panel.setBorder(new EmptyBorder(25, 30, 30, 30));

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setBackground(COLOR_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtSearchName = createStyledTextField();
        txtSearchID = createStyledTextField();

        String[] deptOptions = new String[DEPARTMENTS.length + 1];
        deptOptions[0] = "All Departments";
        System.arraycopy(DEPARTMENTS, 0, deptOptions, 1, DEPARTMENTS.length);
        cmbSearchDept = new JComboBox<>(deptOptions);
        styleComboBox(cmbSearchDept);

        gbc.gridx = 0;
        filterRow.add(createFieldWrapper("Search Name", txtSearchName), gbc);
        gbc.gridx = 1;
        filterRow.add(createFieldWrapper("Search Employee ID", txtSearchID), gbc);
        gbc.gridx = 2;
        filterRow.add(createFieldWrapper("Department", cmbSearchDept), gbc);

        lblTotalCount = new JLabel("Total: 0");
        lblTotalCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalCount.setForeground(COLOR_ACCENT);
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(0, 0, 20, 0);
        filterRow.add(lblTotalCount, gbc);

        panel.add(filterRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Department", "Position", "Contact No.", "Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        employeeTable = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(rowSorter);
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeTable.setRowHeight(35);
        employeeTable.setGridColor(Color.decode("#F1F5F9"));
        employeeTable.setSelectionBackground(Color.decode("#F0F9FF"));
        employeeTable.setSelectionForeground(COLOR_TEXT_MAIN);
        employeeTable.setShowVerticalLines(false);

        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(Color.decode("#F8FAFC"));
        header.setForeground(COLOR_TEXT_MUTED);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        employeeTable.setDefaultRenderer(Object.class, cellRenderer);

        JScrollPane scroll = new JScrollPane(employeeTable);
        scroll.setBorder(new LineBorder(Color.decode("#E2E8F0"), 1, true));
        scroll.getViewport().setBackground(COLOR_CARD);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        bottomBar.setBackground(COLOR_CARD);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(Color.decode("#F1F5F9"));
        btnRefresh.setForeground(COLOR_TEXT_MAIN);
        btnRefresh.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(110, 36));
        btnRefresh.addActionListener(e -> loadFromDatabase());
        bottomBar.add(btnRefresh);
        panel.add(bottomBar, BorderLayout.SOUTH);

        cardsPanel.add(panel, "DIRECTORY");

        DocumentListener liveSearch = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        };
        txtSearchName.getDocument().addDocumentListener(liveSearch);
        txtSearchID.getDocument().addDocumentListener(liveSearch);
        cmbSearchDept.addActionListener(e -> applyFilters());
    }

    // CARD 2: Add Employee
    private void buildAddCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_CARD);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel caption = new JLabel("Onboard a New Employee");
        caption.setFont(new Font("Segoe UI", Font.BOLD, 18));
        caption.setForeground(COLOR_TEXT_MAIN);
        caption.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(caption, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 30, 25));
        formGrid.setBackground(COLOR_CARD);

        txtAddName = createStyledFormTextField();
        txtAddPhone = createStyledFormTextField();
        
        cmbAddDept = new JComboBox<>(DEPARTMENTS);
        styleComboBox(cmbAddDept);

        cmbAddPosition = new JComboBox<>();
        styleComboBox(cmbAddPosition);

        radEmployee = new JRadioButton("Employee");
        radEmployee.setSelected(true);
        radEmployee.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        radEmployee.setForeground(COLOR_TEXT_MAIN);
        radEmployee.setBackground(COLOR_CARD);
        radEmployee.setFocusPainted(false);

        radSupervisor = new JRadioButton("Supervisor");
        radSupervisor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        radSupervisor.setForeground(COLOR_TEXT_MAIN);
        radSupervisor.setBackground(COLOR_CARD);
        radSupervisor.setFocusPainted(false);

        roleGroup = new ButtonGroup();
        roleGroup.add(radEmployee);
        roleGroup.add(radSupervisor);

        JPanel radioContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        radioContainer.setBackground(COLOR_CARD);
        radioContainer.add(radEmployee);
        radioContainer.add(radSupervisor);

        cmbAddDept.addActionListener(e -> updatePositionsCombo());
        radEmployee.addActionListener(e -> updatePositionsCombo());
        radSupervisor.addActionListener(e -> updatePositionsCombo());

        updatePositionsCombo();

        formGrid.add(createFieldWrapper("Full Name", txtAddName));
        formGrid.add(createFieldWrapper("Contact No.", txtAddPhone));
        formGrid.add(createFieldWrapper("Department", cmbAddDept));
        formGrid.add(createFieldWrapper("Role", radioContainer));
        formGrid.add(createFieldWrapper("Position", cmbAddPosition));

        panel.add(formGrid, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 25));
        footer.setBackground(COLOR_CARD);
        btnSubmitAdd = new JButton("Add Employee");
        styleButton(btnSubmitAdd, COLOR_BTN_DARK, Color.WHITE);
        btnSubmitAdd.setPreferredSize(new Dimension(160, 42));
        btnSubmitAdd.addActionListener(e -> processAddEmployee());
        footer.add(btnSubmitAdd);
        panel.add(footer, BorderLayout.SOUTH);

        cardsPanel.add(panel, "ADD");
    }

    // Dynamic Form Population Rules Matrix
    private void updatePositionsCombo() {
        if (cmbAddDept == null || cmbAddPosition == null) return;
        
        String selectedDept = (String) cmbAddDept.getSelectedItem();
        boolean isSupervisor = radSupervisor.isSelected();
        
        cmbAddPosition.removeAllItems();

        if (selectedDept == null) return;

        switch (selectedDept) {
            case "IT Department":
                if (isSupervisor) {
                    cmbAddPosition.addItem("IT Team Lead");
                    cmbAddPosition.addItem("IT Manager");
                    cmbAddPosition.addItem("IT Director");
                } else {
                    cmbAddPosition.addItem("Tech Support");
                    cmbAddPosition.addItem("Software Developer");
                    cmbAddPosition.addItem("QA Engineer");
                }
                break;
            case "HR Department":
                if (isSupervisor) {
                    cmbAddPosition.addItem("HR Manager");
                    cmbAddPosition.addItem("HR Director");
                } else {
                    cmbAddPosition.addItem("HR Assistant");
                    cmbAddPosition.addItem("Recruiter");
                    cmbAddPosition.addItem("Payroll Specialist");
                }
                break;
            case "Operation Department":
                if (isSupervisor) {
                    cmbAddPosition.addItem("Operations Manager");
                    cmbAddPosition.addItem("Operations Director");
                } else {
                    cmbAddPosition.addItem("Logistics Coordinator");
                    cmbAddPosition.addItem("Operations Assistant");
                    cmbAddPosition.addItem("Inventory Clerk");
                }
                break;
            case "Marketing Department":
                if (isSupervisor) {
                    cmbAddPosition.addItem("Marketing Manager");
                    cmbAddPosition.addItem("Marketing Director");
                } else {
                    cmbAddPosition.addItem("Content Writer");
                    cmbAddPosition.addItem("Graphic Designer");
                    cmbAddPosition.addItem("Social Media Specialist");
                }
                break;
            default:
                cmbAddPosition.addItem("Staff Member");
                break;
        }
    }

    private void buildOffboardCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_CARD);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel caption = new JLabel("Offboard Employee");
        caption.setFont(new Font("Segoe UI", Font.BOLD, 18));
        caption.setForeground(COLOR_TEXT_MAIN);
        caption.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(caption, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(COLOR_CARD);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        txtFireID = createStyledFormTextField();
        txtFireID.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        lblFirePreview = new JLabel(" ");
        lblFirePreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFirePreview.setForeground(COLOR_ACCENT);
        lblFirePreview.setBorder(new EmptyBorder(6, 2, 0, 0));
        lblFirePreview.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel idWrapper = createFieldWrapper("Employee ID to Offboard", txtFireID);
        idWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtFireID.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { resolveEmployeeName(); }
            @Override public void removeUpdate(DocumentEvent e) { resolveEmployeeName(); }
            @Override public void changedUpdate(DocumentEvent e) { resolveEmployeeName(); }
        });

        txtFireReason = new JTextArea(6, 20);
        txtFireReason.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtFireReason.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        txtFireReason.setLineWrap(true);
        txtFireReason.setWrapStyleWord(true);

        JPanel reasonWrapper = createFieldWrapper("Reason for Offboarding", txtFireReason);
        reasonWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(idWrapper);
        center.add(lblFirePreview);
        center.add(Box.createVerticalStrut(20));
        center.add(reasonWrapper);
        panel.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 25));
        footer.setBackground(COLOR_CARD);
        btnSubmitFire = new JButton("Offboard Employee");
        styleButton(btnSubmitFire, COLOR_DANGER, Color.WHITE);
        btnSubmitFire.setPreferredSize(new Dimension(185, 42));
        btnSubmitFire.addActionListener(e -> processOffboard());
        footer.add(btnSubmitFire);
        panel.add(footer, BorderLayout.SOUTH);

        cardsPanel.add(panel, "OFFBOARD");
    }

    public void loadFromDatabase() {
        tableModel.setRowCount(0);
        List<Employee> employees = EmployeeQuery.getAllEmployees();
        for (Employee emp : employees) {
            tableModel.addRow(new Object[]{
                emp.empId, emp.fullName, emp.department, emp.position, emp.contactNo, emp.role
            });
        }
        applyFilters();
    }

    private void processAddEmployee() {
        String name = txtAddName.getText().trim();
        String phone = txtAddPhone.getText().trim();
        String position = (String) cmbAddPosition.getSelectedItem();
        String dept = (String) cmbAddDept.getSelectedItem();
        String role = radSupervisor.isSelected() ? "Supervisor" : "Employee";

        String error = EmployeeValidation.validateAdd(name, phone, position, dept, role);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newId = EmployeeQuery.insertEmployee(name, dept, position, phone, role);
        if (newId < 0) {
            JOptionPane.showMessageDialog(this, "Failed to add employee.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, name + " has been added successfully.\nEmployee ID: " + newId + "\nDefault password: 1234", "Employee Added", JOptionPane.INFORMATION_MESSAGE);

        txtAddName.setText("");
        txtAddPhone.setText("");
        cmbAddDept.setSelectedIndex(0);
        radEmployee.setSelected(true);
        updatePositionsCombo();

        loadFromDatabase();
        switchTab("DIRECTORY", tabDirectory, tabAddEmployee, tabOffboard);
    }

    private void processOffboard() {
        String idText = txtFireID.getText().trim();
        String reason = txtFireReason.getText().trim();

        String error = EmployeeValidation.validateOffboard(idText, reason);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int empId = EmployeeValidation.parseId(idText);
        Employee target = EmployeeQuery.getById(empId);
        if (target == null) {
            JOptionPane.showMessageDialog(this, "No employee found with ID: " + empId, "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>You are about to offboard:<br><br><b>" + target.fullName + "</b> (ID: " + empId + ")<br>Department: " + target.department + "<br>Position: " + target.position + "<br><br>Their record will be <b>archived</b> before removal.<br>This action <b>cannot be undone</b>. Continue?</html>",
                "Confirm Offboarding", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = EmployeeQuery.archiveAndDelete(empId, reason);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to offboard employee.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, target.fullName + " has been offboarded.", "Offboarding Complete", JOptionPane.INFORMATION_MESSAGE);

        txtFireID.setText("");
        txtFireReason.setText("");
        lblFirePreview.setText(" ");

        loadFromDatabase();
        switchTab("DIRECTORY", tabDirectory, tabOffboard, tabAddEmployee);
    }

    private void resolveEmployeeName() {
        String text = txtFireID.getText().trim();
        if (text.isEmpty()) {
            lblFirePreview.setText(" ");
            return;
        }
        try {
            int id = Integer.parseInt(text);
            Employee emp = EmployeeQuery.getById(id);
            if (emp != null) {
                lblFirePreview.setText("✓ " + emp.fullName + " — " + emp.department + " / " + emp.position);
                lblFirePreview.setForeground(COLOR_ACCENT);
            } else {
                lblFirePreview.setText("X No employee found with this ID");
                lblFirePreview.setForeground(COLOR_DANGER);
            }
        } catch (NumberFormatException ex) {
            lblFirePreview.setText("X ID must be a number");
            lblFirePreview.setForeground(COLOR_DANGER);
        }
    }

    private void applyFilters() {
        String nameQ = txtSearchName.getText().trim().toLowerCase();
        String idQ = txtSearchID.getText().trim().toLowerCase();
        String dept = (String) cmbSearchDept.getSelectedItem();

        rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                String rowId = entry.getStringValue(0).toLowerCase();
                String rowName = entry.getStringValue(1).toLowerCase();
                String rowDept = entry.getStringValue(2);
                return rowName.contains(nameQ) && rowId.contains(idQ) && (dept.equals("All Departments") || rowDept.equalsIgnoreCase(dept));
            }
        });
        lblTotalCount.setText("Total: " + rowSorter.getViewRowCount());
    }

    private void switchTab(String card, JLabel active, JLabel a, JLabel b) {
        setTabActive(active);
        setTabInactive(a);
        setTabInactive(b);
        cardLayout.show(cardsPanel, card);
    }

    private JLabel makeTab(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private void setTabActive(JLabel label) {
        label.setForeground(COLOR_TEXT_MAIN);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_TEXT_MAIN),
                new EmptyBorder(15, 5, 12, 5)));
    }

    private void setTabInactive(JLabel label) {
        label.setForeground(COLOR_TEXT_MUTED);
        label.setBorder(new EmptyBorder(15, 5, 15, 5));
    }

    private JPanel createFieldWrapper(String labelText, JComponent input) {
        JPanel wrap = new JPanel();
        wrap.setBackground(COLOR_CARD);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_MAIN);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(lbl);
        wrap.add(input);
        return wrap;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(COLOR_CARD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        tf.setPreferredSize(new Dimension(0, 40));
        return tf;
    }

    private JTextField createStyledFormTextField() {
        JTextField tf = createStyledTextField();
        tf.setEditable(true);
        return tf;
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(COLOR_CARD);
        cb.setPreferredSize(new Dimension(0, 40));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
    }
}