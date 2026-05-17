package newPanel;

import theme.SystemTheme;
import dbquery.LeaveQuery;
import dataObject.LeaveRequestRow;
import dataObject.LeaveRequestEntity;
import logic.LeaveRequestLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestApproval extends JPanel {

    private final Color COLOR_BG = SystemTheme.CARD_BG;
    private final Color COLOR_CARD = Color.WHITE;
    private final Color COLOR_TEXT_MAIN = SystemTheme.TEXT_MAIN;
    private final Color COLOR_TEXT_MUTED = SystemTheme.TEXT_MUTED;
    private final Color COLOR_TEXT_NORMAL = SystemTheme.TEXT_COLOR;
    private final Color COLOR_ACCENT = SystemTheme.TEXT_INDICATOR;
    private final Color COLOR_SUCCESS = SystemTheme.BTN_YES;
    private final Color COLOR_DANGER = SystemTheme.BTN_NO;
    private final Color COLOR_REFRESH = SystemTheme.BTN_REFRESH;

    private JLabel tabPending, tabApproved, tabDenied;
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    private String activeTabKey = "PENDING";

    private JTable tablePending, tableApproved, tableDenied;
    private DefaultTableModel modelPending, modelApproved, modelDenied;

    private final List<LeaveRequestRow> allPending = new ArrayList<>();
    private final List<LeaveRequestRow> allApproved = new ArrayList<>();
    private final List<LeaveRequestRow> allDenied = new ArrayList<>();

    private static final int ROWS_PER_PAGE = 5;
    private int pagePending = 1, pageApproved = 1, pageDenied = 1;

    private JLabel lblPagePending, lblPageApproved, lblPageDenied;
    private JButton btnPrevPending, btnNextPending;
    private JButton btnPrevApproved, btnNextApproved;
    private JButton btnPrevDenied, btnNextDenied;

    private JTextField txtSearchName, txtSearchEmpID;
    private JLabel lblMetricsTracker;

    private Runnable onStatusChanged;

    public LeaveRequestApproval() {
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

        JLabel titleLabel = new JLabel("Leave Requests");
        titleLabel.setFont(SystemTheme.HEADER_TEXT);
        titleLabel.setForeground(COLOR_TEXT_MAIN);

        JLabel breadcrumb = new JLabel("Dashboard / Leave Approvals");
        breadcrumb.setFont(SystemTheme.NORMAL_TEXT);
        breadcrumb.setForeground(COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(25));
        add(headerPanel, BorderLayout.NORTH);

        // Card frame
        JPanel primaryCardFrame = new JPanel(new BorderLayout());
        primaryCardFrame.setBackground(COLOR_CARD);
        primaryCardFrame.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));

        // Tab strip
        JPanel stripHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        stripHeader.setBackground(COLOR_CARD);
        stripHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));

        tabPending = new JLabel("Pending Requests");
        tabApproved = new JLabel("Approved History");
        tabDenied = new JLabel("Denied Archive");

        Font tabFont = SystemTheme.BOLD_TEXT;
        for (JLabel tab : new JLabel[]{tabPending, tabApproved, tabDenied}) {
            tab.setFont(tabFont);
            tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        setTabState(tabPending, true);
        setTabState(tabApproved, false);
        setTabState(tabDenied, false);

        stripHeader.add(tabPending);
        stripHeader.add(tabApproved);
        stripHeader.add(tabDenied);
        primaryCardFrame.add(stripHeader, BorderLayout.NORTH);

        // Filter bar
        JPanel utilityFilterBar = new JPanel(new BorderLayout());
        utilityFilterBar.setBackground(COLOR_CARD);
        utilityFilterBar.setBorder(new EmptyBorder(20, 30, 10, 30));

        JPanel searchInputsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        searchInputsPanel.setBackground(COLOR_CARD);

        txtSearchName = createStyledInputTextField();
        txtSearchName.setPreferredSize(new Dimension(220, 40));
        txtSearchEmpID = createStyledInputTextField();
        txtSearchEmpID.setPreferredSize(new Dimension(160, 40));

        searchInputsPanel.add(createFieldContainerWrapper("Search Employee Name", txtSearchName));
        searchInputsPanel.add(createFieldContainerWrapper("Filter Employee ID", txtSearchEmpID));
        utilityFilterBar.add(searchInputsPanel, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(SystemTheme.BOLD_TEXT);
        btnRefresh.setBackground(COLOR_REFRESH);
        btnRefresh.setForeground(COLOR_TEXT_NORMAL);
        btnRefresh.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(110, 38));
        btnRefresh.addActionListener(e -> loadFromDatabase());

        JPanel metricsContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 18));
        metricsContainer.setBackground(COLOR_CARD);
        lblMetricsTracker = new JLabel("Pending Queue: 0 entries found");
        lblMetricsTracker.setFont(SystemTheme.BOLD_TEXT);
        lblMetricsTracker.setForeground(COLOR_ACCENT);
        metricsContainer.add(lblMetricsTracker);
        metricsContainer.add(btnRefresh);
        utilityFilterBar.add(metricsContainer, BorderLayout.EAST);

        JPanel processingCenterPanel = new JPanel(new BorderLayout());
        processingCenterPanel.setBackground(COLOR_CARD);
        processingCenterPanel.add(utilityFilterBar, BorderLayout.NORTH);

        // Tables
        String[] cols = {"Request ID", "Emp ID", "Name", "Type", "Start Date", "End Date", "Status"};
        modelPending = createStrictTableModel(cols);
        tablePending = buildConfiguredTable(modelPending);

        modelApproved = createStrictTableModel(cols);
        tableApproved = buildConfiguredTable(modelApproved);

        modelDenied = createStrictTableModel(cols);
        tableDenied = buildConfiguredTable(modelDenied);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(COLOR_CARD);

        cardsPanel.add(createPaginatedTabContainer(tablePending, "PENDING", true), "PENDING");
        cardsPanel.add(createPaginatedTabContainer(tableApproved, "APPROVED", false), "APPROVED");
        cardsPanel.add(createPaginatedTabContainer(tableDenied, "DENIED", false), "DENIED");

        processingCenterPanel.add(cardsPanel, BorderLayout.CENTER);
        primaryCardFrame.add(processingCenterPanel, BorderLayout.CENTER);
        add(primaryCardFrame, BorderLayout.CENTER);

        // Live search
        DocumentListener liveSearch = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                runGlobalFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runGlobalFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                runGlobalFilters();
            }
        };
        txtSearchName.getDocument().addDocumentListener(liveSearch);
        txtSearchEmpID.getDocument().addDocumentListener(liveSearch);

        // Tab clicks
        tabPending.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchActiveView("PENDING", tabPending, tabApproved, tabDenied);
            }
        });
        tabApproved.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchActiveView("APPROVED", tabApproved, tabPending, tabDenied);
            }
        });
        tabDenied.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchActiveView("DENIED", tabDenied, tabPending, tabApproved);
            }
        });
    }

    // Load Database
    public void loadFromDatabase() {
        allPending.clear();
        allApproved.clear();
        allDenied.clear();

        for (LeaveRequestEntity entity : LeaveQuery.getRequestsByStatus("Pending")) {
            allPending.add(LeaveRequestLogic.toRow(entity));
        }

        for (LeaveRequestEntity entity : LeaveQuery.getRequestsByStatus("Approved")) {
            allApproved.add(LeaveRequestLogic.toRow(entity));
        }

        for (LeaveRequestEntity entity : LeaveQuery.getRequestsByStatus("Disapproved")) {
            allDenied.add(LeaveRequestLogic.toRow(entity));
        }

        pagePending = pageApproved = pageDenied = 1;
        refreshTableSlice("PENDING");
        refreshTableSlice("APPROVED");
        refreshTableSlice("DENIED");
    }

    // Approve / Disapprove
    private void evalSelectionState(boolean approve) {
        int viewRow = tablePending.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave request row to process.",
                    "Selection Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<LeaveRequestRow> filtered = getFilteredDataset(allPending);
        int globalIdx = (pagePending - 1) * ROWS_PER_PAGE + viewRow;
        if (globalIdx >= filtered.size()) {
            return;
        }

        LeaveRequestRow selected = filtered.get(globalIdx);
        String newStatus = approve ? "Approved" : "Disapproved";

        boolean ok = LeaveQuery.updateStatus(selected.getRequestId(), newStatus);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update status in the database. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calculate days of the selected request
        // We need the raw dates — add a helper query for this
        if (approve) {
            // Only deduct credits upon APPROVAL
            int[] days = LeaveQuery.getLeaveDays(selected.getRequestId());
            if (days != null) {
                LeaveQuery.deductCredits(
                        Integer.parseInt(selected.getEmpId()),
                        selected.getLeaveType(),
                        days[0]
                );
            }
        }
        // On disapprove: do nothing to credits — they were never touched

        allPending.remove(selected);
        LeaveRequestRow moved = selected.withStatus(newStatus);

        if (approve) {
            allApproved.add(moved);
            JOptionPane.showMessageDialog(this,
                    "Request #" + selected.getRequestId() + " for "
                    + selected.getFullName() + " has been Approved.",
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            allDenied.add(moved);
            JOptionPane.showMessageDialog(this,
                    "Request #" + selected.getRequestId() + " for "
                    + selected.getFullName() + " has been Disapproved.",
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        }

        refreshTableSlice("PENDING");
        refreshTableSlice("APPROVED");
        refreshTableSlice("DENIED");

        if (onStatusChanged != null) {
            onStatusChanged.run();
        }
    }

    // Pagination
    private JPanel createPaginatedTabContainer(JTable table, String tabKey, boolean includeActions) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_CARD);
        wrapper.setBorder(new EmptyBorder(5, 30, 25, 30));

        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(new LineBorder(Color.decode("#E2E8F0"), 1, true));
        pane.getViewport().setBackground(COLOR_CARD);
        wrapper.add(pane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(COLOR_CARD);
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel paginationStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        paginationStrip.setBackground(COLOR_CARD);

        JButton btnPrev = new JButton(" < ");
        JButton btnNext = new JButton(" > ");
        JLabel lblPage = new JLabel("Page 1 of 1");
        lblPage.setFont(SystemTheme.BOLD_TEXT);
        lblPage.setForeground(COLOR_TEXT_MUTED);
        stylePaginationButton(btnPrev);
        stylePaginationButton(btnNext);

        if (tabKey.equals("PENDING")) {
            btnPrevPending = btnPrev;
            btnNextPending = btnNext;
            lblPagePending = lblPage;
            btnPrev.addActionListener(e -> {
                if (pagePending > 1) {
                    pagePending--;
                    refreshTableSlice("PENDING");
                }
            });
            btnNext.addActionListener(e -> {
                if (pagePending < getMaxPages("PENDING")) {
                    pagePending++;
                    refreshTableSlice("PENDING");
                }
            });
        } else if (tabKey.equals("APPROVED")) {
            btnPrevApproved = btnPrev;
            btnNextApproved = btnNext;
            lblPageApproved = lblPage;
            btnPrev.addActionListener(e -> {
                if (pageApproved > 1) {
                    pageApproved--;
                    refreshTableSlice("APPROVED");
                }
            });
            btnNext.addActionListener(e -> {
                if (pageApproved < getMaxPages("APPROVED")) {
                    pageApproved++;
                    refreshTableSlice("APPROVED");
                }
            });
        } else {
            btnPrevDenied = btnPrev;
            btnNextDenied = btnNext;
            lblPageDenied = lblPage;
            btnPrev.addActionListener(e -> {
                if (pageDenied > 1) {
                    pageDenied--;
                    refreshTableSlice("DENIED");
                }
            });
            btnNext.addActionListener(e -> {
                if (pageDenied < getMaxPages("DENIED")) {
                    pageDenied++;
                    refreshTableSlice("DENIED");
                }
            });
        }

        paginationStrip.add(btnPrev);
        paginationStrip.add(lblPage);
        paginationStrip.add(btnNext);
        footerPanel.add(paginationStrip, BorderLayout.WEST);

        if (includeActions) {
            JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
            actionGroup.setBackground(COLOR_CARD);

            JButton btnDeny = new JButton("Disapprove");
            JButton btnApprove = new JButton("Approve");
            styleActionButton(btnDeny, COLOR_DANGER);
            styleActionButton(btnApprove, COLOR_SUCCESS);
            btnDeny.addActionListener(e -> evalSelectionState(false));
            btnApprove.addActionListener(e -> evalSelectionState(true));

            actionGroup.add(btnDeny);
            actionGroup.add(btnApprove);
            footerPanel.add(actionGroup, BorderLayout.EAST);
        }

        wrapper.add(footerPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private void refreshTableSlice(String tabKey) {
        DefaultTableModel model;
        List<LeaveRequestRow> source;
        int page;
        JLabel pageLabel;
        JButton prevBtn, nextBtn;

        if (tabKey.equals("PENDING")) {
            model = modelPending;
            source = getFilteredDataset(allPending);
            page = pagePending;
            pageLabel = lblPagePending;
            prevBtn = btnPrevPending;
            nextBtn = btnNextPending;
        } else if (tabKey.equals("APPROVED")) {
            model = modelApproved;
            source = getFilteredDataset(allApproved);
            page = pageApproved;
            pageLabel = lblPageApproved;
            prevBtn = btnPrevApproved;
            nextBtn = btnNextApproved;
        } else {
            model = modelDenied;
            source = getFilteredDataset(allDenied);
            page = pageDenied;
            pageLabel = lblPageDenied;
            prevBtn = btnPrevDenied;
            nextBtn = btnNextDenied;
        }

        model.setRowCount(0);

        int total = source.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) total / ROWS_PER_PAGE));

        if (page > maxPages) {
            page = maxPages;
            if (tabKey.equals("PENDING")) {
                pagePending = page;
            } else if (tabKey.equals("APPROVED")) {
                pageApproved = page;
            } else {
                pageDenied = page;
            }
        }

        int start = (page - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, total);

        for (int i = start; i < end; i++) {
            model.addRow(source.get(i).toTableRow());
        }

        pageLabel.setText("Page " + page + " of " + maxPages);
        prevBtn.setEnabled(page > 1);
        nextBtn.setEnabled(page < maxPages);

        updateMetricsDisplay();
    }

    private List<LeaveRequestRow> getFilteredDataset(List<LeaveRequestRow> source) {
        String nameFilter = txtSearchName.getText().trim().toLowerCase();
        String idFilter = txtSearchEmpID.getText().trim().toLowerCase();
        List<LeaveRequestRow> result = new ArrayList<>();
        for (LeaveRequestRow row : source) {
            if (row.getFullName().toLowerCase().contains(nameFilter)
                    && row.getEmpId().toLowerCase().contains(idFilter)) {
                result.add(row);
            }
        }
        return result;
    }

    private int getMaxPages(String tabKey) {
        List<LeaveRequestRow> src = tabKey.equals("PENDING") ? allPending
                : tabKey.equals("APPROVED") ? allApproved : allDenied;
        return Math.max(1, (int) Math.ceil((double) getFilteredDataset(src).size() / ROWS_PER_PAGE));
    }

    private void runGlobalFilters() {
        pagePending = pageApproved = pageDenied = 1;
        refreshTableSlice("PENDING");
        refreshTableSlice("APPROVED");
        refreshTableSlice("DENIED");
    }

    private void switchActiveView(String cardID, JLabel active, JLabel a, JLabel b) {
        activeTabKey = cardID;
        setTabState(active, true);
        setTabState(a, false);
        setTabState(b, false);
        cardLayout.show(cardsPanel, cardID);
        refreshTableSlice(cardID);
    }

    private void updateMetricsDisplay() {
        List<LeaveRequestRow> src = activeTabKey.equals("PENDING") ? allPending
                : activeTabKey.equals("APPROVED") ? allApproved : allDenied;
        String label = activeTabKey.equals("PENDING") ? "Pending Queue"
                : activeTabKey.equals("APPROVED") ? "Approved Registry" : "Archived Denials";
        lblMetricsTracker.setText(label + ": " + getFilteredDataset(src).size() + " entries found");
    }

    private JTable buildConfiguredTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(SystemTheme.NORMAL_TEXT);
        table.setRowHeight(38);
        table.setGridColor(Color.decode("#F1F5F9"));
        table.setSelectionBackground(Color.decode("#F0F9FF"));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(SystemTheme.BOLD_TEXT);
        header.setBackground(Color.decode("#F8FAFC"));
        header.setForeground(COLOR_TEXT_MUTED);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (c == 6 && v != null) {
                    setFont(SystemTheme.BOLD_TEXT);
                    String val = v.toString();
                    if (val.equalsIgnoreCase("Approved")) {
                        setForeground(COLOR_SUCCESS);
                    } else if (val.equalsIgnoreCase("Disapproved")) {
                        setForeground(COLOR_DANGER);
                    } else {
                        setForeground(COLOR_ACCENT);
                    }
                } else {
                    setForeground(COLOR_TEXT_MAIN);
                    setFont(SystemTheme.NORMAL_TEXT);
                }
                return this;
            }
        });
        return table;
    }

    private DefaultTableModel createStrictTableModel(String[] headers) {
        return new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private void setTabState(JLabel label, boolean active) {
        if (active) {
            label.setForeground(COLOR_TEXT_MAIN);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_TEXT_MAIN),
                    new EmptyBorder(18, 5, 12, 5)));
        } else {
            label.setForeground(COLOR_TEXT_MUTED);
            label.setBorder(new EmptyBorder(18, 5, 15, 5));
        }
    }

    private JPanel createFieldContainerWrapper(String labelText, JComponent field) {
        JPanel wrap = new JPanel();
        wrap.setBackground(COLOR_CARD);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(SystemTheme.BOLD_TEXT);
        lbl.setForeground(COLOR_TEXT_MAIN);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(lbl);
        wrap.add(field);
        return wrap;
    }

    private JTextField createStyledInputTextField() {
        JTextField tf = new JTextField();
        tf.setFont(SystemTheme.NORMAL_TEXT);
        tf.setBackground(COLOR_CARD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        return tf;
    }

    private void styleActionButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(SystemTheme.BOLD_TEXT);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 38));
    }

    private void stylePaginationButton(JButton b) {
        b.setBackground(Color.decode("#F1F5F9"));
        b.setForeground(COLOR_TEXT_MAIN);
        b.setFont(SystemTheme.SMALL_TEXT_BOLD);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(Color.decode("#CBD5E1"), 1, true));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(42, 32));
    }

    public void setOnStatusChanged(Runnable callback) {
        this.onStatusChanged = callback;
    }
}
