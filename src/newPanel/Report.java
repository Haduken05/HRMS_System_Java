package newPanel;

// ── ADD these imports at the top of Report.java alongside the existing ones ──
import com.toedter.calendar.JDateChooser;

import theme.SystemTheme;
import dataObject.LeaveRequestEntity;
import dataObject.AttendanceRecord;
import dbquery.LeaveQuery;
import dbquery.AttendanceQuery;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Locale;

public class Report extends JPanel {

    private static final Color BG = SystemTheme.CARD_BG;
    private static final Color CARD = Color.WHITE;
    private static final Color TEXT_MAIN = SystemTheme.TEXT_MAIN;
    private static final Color TEXT_MUTED = SystemTheme.TEXT_MUTED;
    private static final Color TEXT_REFRESH = SystemTheme.TEXT_COLOR;
    private static final Color BORDER_CLR = SystemTheme.BORDER_COLOR;
    private static final Color HEADER_BG = SystemTheme.PRIMARY_COLOR;
    private static final Color BTN_REFRESH = SystemTheme.BTN_REFRESH;

    private static final Color APPROVED_COLOR = SystemTheme.APPROVED_COLOR;
    private static final Color APPROVED_BG = SystemTheme.APPROVED_BG;
    private static final Color PENDING_COLOR = SystemTheme.PENDING_COLOR;
    private static final Color PENDING_BG = SystemTheme.PENDING_BG;
    private static final Color TODAY_RING = SystemTheme.CALENDAR_TODAY_RING;
    private static final Color CAL_HEADER_BG = SystemTheme.BTN_DARK;
    private static final Color CAL_HEADER_FG = SystemTheme.TEXT_COLOR;
    private static final Color WEEKEND_BG = SystemTheme.CALENDAR_WEEKEND_BG;
    private static final Color CELL_HOVER_BG = SystemTheme.CALENDAR_CELL_HOVER;

    private YearMonth currentMonth = YearMonth.now();

    private final Map<Integer, List<LeaveRequestEntity>> approvedByDay = new HashMap<>();
    private final Map<Integer, List<LeaveRequestEntity>> pendingByDay = new HashMap<>();

    private JLabel lblMonthYear;
    private CalendarGrid calendarGrid;
    private JLabel tabAttendance, tabCalendar;
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    private JPanel tooltipPopup;
    private JLabel activeTabRef;

    private DefaultTableModel attModel;
    private TableRowSorter<DefaultTableModel> attSorter;
    private JDateChooser dcAttFrom, dcAttTo;
    private JComboBox<String> cmbAttDept;
    private JLabel lblAttCount;

    private static final String[] ATT_DEPARTMENTS = {
        "All Departments",
        "IT Department", "HR Department",
        "Operation Department", "Marketing Department"
    };

    private static final SimpleDateFormat DB_FMT
            = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FMT
            = new SimpleDateFormat("h:mm a");
    private static final SimpleDateFormat DATE_FMT
            = new SimpleDateFormat("MMM d, yyyy");

    public Report() {
        initComponents();
        loadMonth();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(1000, 700));
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // Page header
        JPanel header = new JPanel();
        header.setBackground(BG);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Reports");
        title.setFont(SystemTheme.HEADER_TEXT);
        title.setForeground(TEXT_MAIN);

        JLabel crumb = new JLabel("Dashboard / Reports");
        crumb.setFont(SystemTheme.NORMAL_TEXT);
        crumb.setForeground(TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(crumb);
        header.add(Box.createVerticalStrut(25));
        add(header, BorderLayout.NORTH);

        // Primary card
        JPanel primaryCard = new JPanel(new BorderLayout());
        primaryCard.setBackground(CARD);
        primaryCard.setBorder(new LineBorder(BORDER_CLR, 1, true));

        // Tab strip
        JPanel tabStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        tabStrip.setBackground(CARD);
        tabStrip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                Color.decode("#E2E8F0")));

        tabAttendance = makeTabLabel("Attendance Tracker");
        tabCalendar = makeTabLabel("Leave Calendar");

        setTabActive(tabCalendar);
        setTabInactive(tabAttendance);

        tabStrip.add(tabAttendance);
        tabStrip.add(tabCalendar);
        primaryCard.add(tabStrip, BorderLayout.NORTH);

        // Cards
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(CARD);

        cardsPanel.add(buildAttendanceCard(), "ATTENDANCE");
        cardsPanel.add(buildCalendarCard(), "CALENDAR");
        cardLayout.show(cardsPanel, "CALENDAR");
        activeTabRef = tabCalendar;

        primaryCard.add(cardsPanel, BorderLayout.CENTER);
        add(primaryCard, BorderLayout.CENTER);

        // Tab listeners
        tabAttendance.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setTabActive(tabAttendance);
                setTabInactive(tabCalendar);
                activeTabRef = tabAttendance;
                cardLayout.show(cardsPanel, "ATTENDANCE");
                hideTooltip();
            }
        });
        tabCalendar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setTabActive(tabCalendar);
                setTabInactive(tabAttendance);
                activeTabRef = tabCalendar;
                cardLayout.show(cardsPanel, "CALENDAR");
            }
        });
    }

    //  Attendance tab
    private JPanel buildAttendanceCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        dcAttFrom = new JDateChooser();
        dcAttTo = new JDateChooser();

        // FILTER BAR
        JPanel filterBar = new JPanel(new GridBagLayout());
        filterBar.setBackground(CARD);
        filterBar.setBorder(new EmptyBorder(0, 0, 18, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        styleAttDateChooser(dcAttFrom);
        styleAttDateChooser(dcAttTo);

        java.util.Date today = new java.util.Date();
        dcAttFrom.setDate(today);
        dcAttTo.setDate(today);
        dcAttTo.setMinSelectableDate(today);

        dcAttFrom.addPropertyChangeListener("date", evt -> {
            java.util.Date fromDate = dcAttFrom.getDate();
            if (fromDate != null) {

                if (dcAttTo.getDate() == null || dcAttTo.getDate().before(fromDate)) {
                    dcAttTo.setDate(fromDate);
                }
                dcAttTo.setMinSelectableDate(fromDate);
            } else {
                dcAttTo.setMinSelectableDate(null);
            }
            loadAttendanceTables();
        });

        dcAttTo.addPropertyChangeListener("date", evt -> {
            java.util.Date toDate = dcAttTo.getDate();
            java.util.Date fromDate = dcAttFrom.getDate();

            if (toDate != null && fromDate != null && toDate.before(fromDate)) {
                dcAttTo.setDate(fromDate);
                return;
            }
            loadAttendanceTables();
        });

        gbc.gridx = 0;
        filterBar.add(attFieldWrapper("From Date", dcAttFrom), gbc);
        gbc.gridx = 1;
        filterBar.add(attFieldWrapper("To Date", dcAttTo), gbc);

        cmbAttDept = new JComboBox<>(ATT_DEPARTMENTS);
        cmbAttDept.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbAttDept.setBackground(CARD);
        cmbAttDept.setPreferredSize(new Dimension(0, 36));
        gbc.gridx = 2;
        filterBar.add(attFieldWrapper("Department", cmbAttDept), gbc);

        lblAttCount = new JLabel("Total: 0");
        lblAttCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAttCount.setForeground(SystemTheme.TEXT_INDICATOR);
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        gbc.anchor = GridBagConstraints.SOUTH;
        filterBar.add(lblAttCount, gbc);

        panel.add(filterBar, BorderLayout.NORTH);

        // TABLE
        String[] cols = {"ID", "Name", "Department", "Date",
            "Time In", "Time Out", "Status", "OT hrs", "Remarks"};
        attModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(attModel);
        attSorter = new TableRowSorter<>(attModel);
        table.setRowSorter(attSorter);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setGridColor(Color.decode("#F1F5F9"));
        table.setSelectionBackground(Color.decode("#1E3A5F"));
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(HEADER_BG);
        th.setForeground(TEXT_MUTED);
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setBorder(new EmptyBorder(0, 8, 0, 8));
        center.setHorizontalAlignment(SwingConstants.CENTER);
        center.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int i = 0; i < cols.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        // Status column renderer
        table.getColumnModel().getColumn(6).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!sel) {
                    String s = value == null ? "" : value.toString();
                    switch (s) {
                        case "Present":
                            setForeground(Color.decode("#065F46"));
                            break;
                        case "Late":
                            setForeground(Color.decode("#92400E"));
                            break;
                        case "Half-Day":
                            setForeground(Color.decode("#9F1239"));
                            break;
                        case "After Hours":
                            setForeground(Color.decode("#4C1D95"));
                            break;
                        default:
                            setForeground(TEXT_MUTED);
                            break;
                    }
                } else {
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Color.decode("#E2E8F0"), 1, true));
        scroll.getViewport().setBackground(CARD);
        panel.add(scroll, BorderLayout.CENTER);

        // BOTTOM BAR
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(CARD);

        JButton btnLoad = new JButton("Load / Refresh");
        btnLoad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoad.setBackground(BTN_REFRESH);
        btnLoad.setForeground(TEXT_REFRESH);
        btnLoad.setBorder(new LineBorder(BORDER_CLR, 1, true));
        btnLoad.setFocusPainted(false);
        btnLoad.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLoad.setPreferredSize(new Dimension(130, 34));
        btnLoad.addActionListener(e -> loadAttendanceTables());

        JButton btnClear = new JButton("Clear Filters");
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setBackground(CARD);
        btnClear.setForeground(TEXT_MUTED);
        btnClear.setBorder(new LineBorder(BORDER_CLR, 1, true));
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setPreferredSize(new Dimension(110, 34));
        btnClear.addActionListener(e -> {
            dcAttFrom.setDate(new java.util.Date());
            dcAttTo.setDate(new java.util.Date());
            cmbAttDept.setSelectedIndex(0);
            loadAttendanceTables();
        });

        bottomBar.add(btnClear);
        bottomBar.add(btnLoad);
        panel.add(bottomBar, BorderLayout.SOUTH);

        cmbAttDept.addActionListener(e -> loadAttendanceTables());

        // Initial load
        loadAttendanceTables();

        return panel;
    }

    private void loadAttendanceTables() {
        String from = null;
        String to = null;

        if (dcAttFrom.getDate() != null) {
            from = DB_FMT.format(dcAttFrom.getDate());
        }
        if (dcAttTo.getDate() != null) {
            to = DB_FMT.format(dcAttTo.getDate());
        }

        String dept = (String) cmbAttDept.getSelectedItem();
        if ("All Departments".equals(dept)) {
            dept = null;
        }

        List<AttendanceRecord> records = AttendanceQuery.getAttendanceReport(from, to, dept);

        attModel.setRowCount(0);
        for (AttendanceRecord r : records) {
            String timeInStr = r.timeIn != null ? TIME_FMT.format(r.timeIn) : "—";
            String timeOutStr = r.timeOut != null ? TIME_FMT.format(r.timeOut) : "—";
            String dateStr = DATE_FMT.format(java.sql.Date.valueOf(r.dateLogged));
            String otStr = r.otHours > 0 ? r.otHours + "h" : "—";
            String remarksStr = r.remarks != null ? r.remarks : "—";

            attModel.addRow(new Object[]{
                r.empId, r.fullName, r.department, dateStr,
                timeInStr, timeOutStr, r.status, otStr, remarksStr
            });
        }
        lblAttCount.setText("Total: " + attModel.getRowCount());
    }

    private void loadAttendanceFromFields(JTextField txtFrom, JTextField txtTo) {
    }

    private void loadAttendanceTable() {
    }

    private JPanel attFieldWrapper(String labelText, JComponent input) {
        JPanel wrap = new JPanel();
        wrap.setBackground(CARD);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(SystemTheme.BOLD_TEXT);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(lbl);
        wrap.add(input);
        return wrap;
    }

    private void styleAttDateChooser(JDateChooser dc) {
        dc.setDateFormatString("MMM d, yyyy");
        dc.setFont(SystemTheme.NORMAL_TEXT);
        dc.setBackground(CARD);
        dc.setPreferredSize(new Dimension(160, 38));
        dc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JTextField tf = (JTextField) dc.getDateEditor().getUiComponent();
        tf.setBackground(CARD);
        tf.setForeground(TEXT_MAIN);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(0, 10, 0, 10)));
    }

    //  Calendar tab
    private JPanel buildCalendarCard() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(CARD);
        outer.setBorder(new EmptyBorder(20, 30, 25, 30));

        outer.add(buildCalendarToolbar(), BorderLayout.NORTH);

        calendarGrid = new CalendarGrid();
        outer.add(calendarGrid, BorderLayout.CENTER);

        outer.add(buildLegend(), BorderLayout.SOUTH);

        return outer;
    }

    private JPanel buildCalendarToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CARD);
        bar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navRow.setBackground(CARD);

        JButton btnPrev = navArrowBtn("‹");
        JButton btnNext = navArrowBtn("›");

        lblMonthYear = new JLabel("", SwingConstants.LEFT);
        lblMonthYear.setFont(SystemTheme.LARGE_TEXT_BOLD);
        lblMonthYear.setForeground(TEXT_MAIN);
        updateMonthLabel();

        btnPrev.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthLabel();
            loadMonth();
        });
        btnNext.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonthLabel();
            loadMonth();
        });

        navRow.add(btnPrev);
        navRow.add(lblMonthYear);
        navRow.add(btnNext);
        bar.add(navRow, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(SystemTheme.BOLD_TEXT);
        btnRefresh.setBackground(BTN_REFRESH);
        btnRefresh.setForeground(TEXT_REFRESH);
        btnRefresh.setBorder(new LineBorder(BORDER_CLR, 1, true));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(100, 34));
        btnRefresh.addActionListener(e -> loadMonth());

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightBar.setBackground(CARD);
        rightBar.add(btnRefresh);
        bar.add(rightBar, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildLegend() {
        JPanel leg = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        leg.setBackground(CARD);
        leg.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                Color.decode("#E2E8F0")));
        leg.add(legendChip(APPROVED_BG, APPROVED_COLOR, "Approved leave"));
        leg.add(legendChip(PENDING_BG, PENDING_COLOR, "Pending leave"));
        JLabel todayChip = new JLabel("  Today  ");
        todayChip.setFont(SystemTheme.SMALL_TEXT_BOLD);
        todayChip.setBackground(CARD);
        todayChip.setForeground(TODAY_RING);
        todayChip.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(TODAY_RING, 2, true),
                new EmptyBorder(3, 8, 3, 8)));
        todayChip.setOpaque(true);
        leg.add(todayChip);
        return leg;
    }

    private JLabel legendChip(Color bg, Color fg, String text) {
        JLabel chip = new JLabel("  " + text + "  ");
        chip.setFont(SystemTheme.SMALL_TEXT_BOLD);
        chip.setBackground(bg);
        chip.setForeground(fg);
        chip.setOpaque(true);
        chip.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fg, 1, true),
                new EmptyBorder(3, 8, 3, 8)));
        return chip;
    }

    private JButton navArrowBtn(String label) {
        JButton btn = new JButton(label);
        btn.setFont(SystemTheme.LARGE_TEXT_BOLD);
        btn.setBackground(CARD);
        btn.setForeground(TEXT_MAIN);
        btn.setBorder(new LineBorder(BORDER_CLR, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(36, 34));
        return btn;
    }

    //  Data loading
    public void loadMonth() {
        approvedByDay.clear();
        pendingByDay.clear();

        List<LeaveRequestEntity> leaves
                = LeaveQuery.getLeavesForMonth(currentMonth.getYear(),
                        currentMonth.getMonthValue());

        for (LeaveRequestEntity e : leaves) {
            if (e.getStartDate() == null || e.getEndDate() == null) {
                continue;
            }

            LocalDate start = e.getStartDate().toLocalDate();
            LocalDate end = e.getEndDate().toLocalDate();

            // Walk every day of the leave range that falls in this month
            LocalDate cursor = start;
            while (!cursor.isAfter(end)) {
                if (cursor.getYear() == currentMonth.getYear()
                        && cursor.getMonthValue() == currentMonth.getMonthValue()) {

                    int day = cursor.getDayOfMonth();
                    Map<Integer, List<LeaveRequestEntity>> map
                            = e.getStatus().equalsIgnoreCase("Approved")
                            ? approvedByDay : pendingByDay;
                    map.computeIfAbsent(day, k -> new ArrayList<>()).add(e);
                }
                cursor = cursor.plusDays(1);
            }
        }

        if (calendarGrid != null) {
            calendarGrid.repaint();
        }
    }

    //  Tooltip popup
    private void showTooltip(int day, Point screenPt, Component invoker) {
        hideTooltip();

        List<LeaveRequestEntity> approved = approvedByDay.getOrDefault(day, List.of());
        List<LeaveRequestEntity> pending = pendingByDay.getOrDefault(day, List.of());
        if (approved.isEmpty() && pending.isEmpty()) {
            return;
        }

        tooltipPopup = new JPanel();
        tooltipPopup.setLayout(new BoxLayout(tooltipPopup, BoxLayout.Y_AXIS));
        tooltipPopup.setBackground(CARD);
        tooltipPopup.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(10, 14, 10, 14)));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        LocalDate date = LocalDate.of(currentMonth.getYear(),
                currentMonth.getMonthValue(), day);

        JLabel heading = new JLabel(date.format(fmt));
        heading.setFont(SystemTheme.BOLD_TEXT);
        heading.setForeground(TEXT_MAIN);
        heading.setBorder(new EmptyBorder(0, 0, 6, 0));
        tooltipPopup.add(heading);

        addTooltipRows(tooltipPopup, approved, APPROVED_COLOR, APPROVED_BG, "Approved");
        addTooltipRows(tooltipPopup, pending, PENDING_COLOR, PENDING_BG, "Pending");

        // Show as a lightweight popup
        JLayeredPane layered = SwingUtilities.getRootPane(invoker).getLayeredPane();
        Point rel = SwingUtilities.convertPoint(invoker,
                new Point(0, 0), layered);

        tooltipPopup.setSize(tooltipPopup.getPreferredSize());
        int px = screenPt.x - layered.getLocationOnScreen().x + 10;
        int py = screenPt.y - layered.getLocationOnScreen().y - 20;

        // Keep inside pane
        Dimension ps = tooltipPopup.getPreferredSize();
        if (px + ps.width > layered.getWidth()) {
            px = layered.getWidth() - ps.width - 4;
        }
        if (py + ps.height > layered.getHeight()) {
            py = layered.getHeight() - ps.height - 4;
        }
        if (px < 0) {
            px = 0;
        }
        if (py < 0) {
            py = 0;
        }

        tooltipPopup.setBounds(px, py, ps.width, ps.height);
        layered.add(tooltipPopup, JLayeredPane.POPUP_LAYER);
        layered.repaint();
    }

    private void addTooltipRows(JPanel popup,
            List<LeaveRequestEntity> list, Color fg, Color bg, String statusLabel) {
        for (LeaveRequestEntity e : list) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setBackground(CARD);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel badge = new JLabel(" " + statusLabel + " ");
            badge.setFont(SystemTheme.PANDAK_TEXT);
            badge.setBackground(bg);
            badge.setForeground(fg);
            badge.setOpaque(true);
            badge.setBorder(new LineBorder(fg, 1, true));

            JLabel name = new JLabel(e.getFullName()
                    + "  ·  " + e.getLeaveType());
            name.setFont(SystemTheme.NORMAL_TEXT);
            name.setForeground(TEXT_MAIN);

            row.add(badge);
            row.add(name);
            popup.add(row);
        }
    }

    private void hideTooltip() {
        if (tooltipPopup != null) {
            Container parent = tooltipPopup.getParent();
            if (parent != null) {
                parent.remove(tooltipPopup);
                parent.repaint();
            }
            tooltipPopup = null;
        }
    }

    //  Helpers
    private void updateMonthLabel() {
        lblMonthYear.setText(
                currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + "  " + currentMonth.getYear());
    }

    private JLabel makeTabLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SystemTheme.BOLD_TEXT);
        l.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return l;
    }

    private void setTabActive(JLabel l) {
        l.setForeground(TEXT_MAIN);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, TEXT_MAIN),
                new EmptyBorder(18, 5, 12, 5)));
    }

    private void setTabInactive(JLabel l) {
        l.setForeground(TEXT_MUTED);
        l.setBorder(new EmptyBorder(18, 5, 15, 5));
    }

    //  Custom Calendar Grid
    private class CalendarGrid extends JPanel {

        private static final int COLS = 7;
        private static final int ROWS = 7; // Up to 6 week rows
        private static final int HEADER_H = 38;
        private static final int MAX_DOTS = 3; // max employee dots per cell

        private int hoveredDay = -1;

        CalendarGrid() {
            setBackground(CARD);
            setPreferredSize(new Dimension(0, 460));

            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    int day = dayAtPoint(e.getPoint());
                    if (day != hoveredDay) {
                        hoveredDay = day;
                        repaint();
                        if (day > 0) {
                            showTooltip(day, e.getLocationOnScreen(), CalendarGrid.this);
                        } else {
                            hideTooltip();
                        }
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredDay = -1;
                    repaint();
                    hideTooltip();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cellW = w / COLS;
            int bodyH = h - HEADER_H;
            int cellH = bodyH / 6;

            // Day-of-week header row
            g2.setColor(CAL_HEADER_BG);
            g2.fillRoundRect(0, 0, w, HEADER_H, 8, 8);

            String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            g2.setColor(CAL_HEADER_FG);
            g2.setFont(SystemTheme.BOLD_TEXT);
            FontMetrics fmH = g2.getFontMetrics();
            for (int c = 0; c < COLS; c++) {
                int cx = c * cellW + cellW / 2;
                int cy = HEADER_H / 2 + fmH.getAscent() / 2 - 1;
                g2.drawString(days[c], cx - fmH.stringWidth(days[c]) / 2, cy);
            }

            // Build day grid
            LocalDate today = LocalDate.now();
            LocalDate firstDay = currentMonth.atDay(1);
            int startCol = firstDay.getDayOfWeek().getValue() % 7; // Sun=0
            int daysInMonth = currentMonth.lengthOfMonth();

            int day = 1;
            for (int row = 0; row < 6 && day <= daysInMonth; row++) {
                for (int col = 0; col < COLS && day <= daysInMonth; col++) {
                    if (row == 0 && col < startCol) {
                        continue;
                    }

                    int x = col * cellW;
                    int y = HEADER_H + row * cellH;

                    boolean isWeekend = (col == 0 || col == 6);
                    boolean isToday = (today.getYear() == currentMonth.getYear()
                            && today.getMonthValue() == currentMonth.getMonthValue()
                            && today.getDayOfMonth() == day);
                    boolean isHovered = (hoveredDay == day);

                    List<LeaveRequestEntity> appr = approvedByDay.getOrDefault(day, List.of());
                    List<LeaveRequestEntity> pend = pendingByDay.getOrDefault(day, List.of());
                    boolean hasLeave = !appr.isEmpty() || !pend.isEmpty();

                    // Cell background
                    if (isHovered && hasLeave) {
                        g2.setColor(CELL_HOVER_BG);
                    } else if (isWeekend) {
                        g2.setColor(WEEKEND_BG);
                    } else {
                        g2.setColor(CARD);
                    }
                    g2.fillRect(x, y, cellW, cellH);

                    // Cell border
                    g2.setColor(Color.decode("#E2E8F0"));
                    g2.drawRect(x, y, cellW - 1, cellH - 1);

                    // Today ring
                    if (isToday) {
                        g2.setColor(TODAY_RING);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(x + 2, y + 2, cellW - 5, cellH - 5, 8, 8);
                        g2.setStroke(new BasicStroke(1f));
                    }

                    // Day number
                    g2.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 14));
                    g2.setColor(isToday ? TODAY_RING : isWeekend ? TEXT_MUTED : TEXT_MAIN);
                    FontMetrics fmD = g2.getFontMetrics();
                    String dayStr = String.valueOf(day);
                    g2.drawString(dayStr,
                            x + cellW - fmD.stringWidth(dayStr) - 8,
                            y + fmD.getAscent() + 6);

                    // Leave indicators
                    if (hasLeave) {
                        int dotY = y + cellH - 28;
                        int dotSize = 8;
                        int dotGap = 5;
                        int padding = 6;

                        // Approved dots (green)
                        int dotX = x + padding;
                        int shown = 0;
                        for (LeaveRequestEntity e : appr) {
                            if (shown >= MAX_DOTS) {
                                break;
                            }
                            g2.setColor(APPROVED_COLOR);
                            g2.fillOval(dotX, dotY, dotSize, dotSize);
                            dotX += dotSize + dotGap;
                            shown++;
                        }

                        // Pending dots (amber)
                        for (LeaveRequestEntity e : pend) {
                            if (shown >= MAX_DOTS + appr.size()) {
                                break;
                            }
                            if (shown >= MAX_DOTS * 2) {
                                break;
                            }
                            g2.setColor(PENDING_COLOR);
                            g2.fillOval(dotX, dotY, dotSize, dotSize);
                            dotX += dotSize + dotGap;
                            shown++;
                        }

                        // Overflow label "+N more"
                        int total = appr.size() + pend.size();
                        if (total > MAX_DOTS) {
                            g2.setFont(SystemTheme.PANDAK_TEXT);
                            g2.setColor(TEXT_MUTED);
                            String more = "+" + (total - Math.min(shown, MAX_DOTS)) + " more";
                            g2.drawString(more, x + padding, y + cellH - 10);
                        }

                        // Name pill for the first person (if cell is tall enough)
                        if (cellH >= 70) {
                            LeaveRequestEntity first = !appr.isEmpty() ? appr.get(0) : pend.get(0);
                            Color pillBg = !appr.isEmpty() ? APPROVED_BG : PENDING_BG;
                            Color pillFg = !appr.isEmpty() ? APPROVED_COLOR : PENDING_COLOR;

                            String pillText = first.getFullName().split(" ")[0]; // first name only
                            g2.setFont(SystemTheme.PANDAK_TEXT);
                            FontMetrics fmP = g2.getFontMetrics();
                            int pw = fmP.stringWidth(pillText) + 10;
                            int ph = 16;
                            int pillX = x + 5;
                            int pillY = y + cellH - 48;

                            g2.setColor(pillBg);
                            g2.fillRoundRect(pillX, pillY, pw, ph, 8, 8);
                            g2.setColor(pillFg);
                            g2.drawRoundRect(pillX, pillY, pw, ph, 8, 8);
                            g2.drawString(pillText,
                                    pillX + 5,
                                    pillY + fmP.getAscent() + 2);

                            // "+N" badge if more than 1 person
                            if (total > 1) {
                                String badge = "+" + (total - 1);
                                g2.setFont(SystemTheme.PANDAK_TEXT);
                                FontMetrics fmB = g2.getFontMetrics();
                                int bx = pillX + pw + 4;
                                int bw = fmB.stringWidth(badge) + 8;
                                g2.setColor(Color.decode("#E2E8F0"));
                                g2.fillRoundRect(bx, pillY, bw, ph, 8, 8);
                                g2.setColor(TEXT_MUTED);
                                g2.drawString(badge, bx + 4, pillY + fmB.getAscent() + 2);
                            }
                        }
                    }

                    day++;
                }
            }

            g2.dispose();
        }

        /**
         * Returns the day-of-month at the given panel-local point, or -1.
         */
        private int dayAtPoint(Point p) {
            int w = getWidth();
            int cellW = w / COLS;
            int bodyH = getHeight() - HEADER_H;
            int cellH = bodyH / 6;

            if (p.y < HEADER_H) {
                return -1;
            }

            int col = p.x / cellW;
            int row = (p.y - HEADER_H) / cellH;
            if (col < 0 || col >= COLS || row < 0 || row >= 6) {
                return -1;
            }

            LocalDate firstDay = currentMonth.atDay(1);
            int startCol = firstDay.getDayOfWeek().getValue() % 7;
            int daysInMonth = currentMonth.lengthOfMonth();

            int day = row * COLS + col - startCol + 1;
            if (day < 1 || day > daysInMonth) {
                return -1;
            }
            return day;
        }
    }
}
