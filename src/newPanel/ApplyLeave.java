package newPanel;

import com.toedter.calendar.JDateChooser;
import dbquery.LeaveQuery;
import logic.LeaveLogic;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class ApplyLeave extends JPanel {

    private static final Color BG = Color.decode("#F8FAFC");
    private static final Color CARD = Color.WHITE;
    private static final Color FIELD_BG = Color.decode("#E2E8F0");
    private static final Color TEXT_MAIN = Color.decode("#0F172A");
    private static final Color TEXT_MUTED = Color.decode("#475569");
    private static final Color BORDER_CLR = Color.decode("#CBD5E1");
    private static final Color BTN_SUBMIT = Color.decode("#475569");
    private static final Color BTN_CANCEL = Color.decode("#F1F5F9");
    private static final Color BADGE_VL = Color.decode("#DCFCE7");
    private static final Color BADGE_SL = Color.decode("#FEF9C3");
    private static final Color BADGE_TEXT = Color.decode("#166534");

    private static final String UPLOAD_DIR = "uploads/leave_docs/";

    private int currentEmpId = -1;
    private String uploadedPath = null;

    private JTextField txtEmpID, txtNameEmployee;
    private JComboBox<String> cmbLeaveType;
    private JDateChooser dcFrom, dcTo;
    private JButton btnCancel, btnSubmit;

    private JLabel lblVLCredits, lblSLCredits;

    // Drop zone
    private JPanel dropZone;
    private JLabel dropHint;
    private JLabel previewLabel;
    private JLabel fileNameLabel;
    private JButton btnClearFile;

    public ApplyLeave() {
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(800, 700));
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        //HEADER
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(BG);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Leave Request");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_MAIN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel breadcrumb = new JLabel("Dashboard / Apply Leave");
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        breadcrumb.setForeground(TEXT_MUTED);
        breadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgeRow.setBackground(BG);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblVLCredits = createBadge("VL: — days", BADGE_VL);
        lblSLCredits = createBadge("SL: — days", BADGE_SL);

        badgeRow.add(lblVLCredits);
        badgeRow.add(lblSLCredits);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(18));
        headerPanel.add(badgeRow);
        headerPanel.add(Box.createVerticalStrut(18));

        add(headerPanel, BorderLayout.NORTH);

        //CONTENT
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        contentGrid.setBackground(BG);

        //LEFT: form fields
        JPanel leftColumn = new JPanel();
        leftColumn.setBackground(BG);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        leftColumn.add(createFieldLabel("Employee ID"));
        txtEmpID = createStyledTextField();
        leftColumn.add(txtEmpID);
        leftColumn.add(Box.createVerticalStrut(14));

        leftColumn.add(createFieldLabel("Employee Name"));
        txtNameEmployee = createStyledTextField();
        leftColumn.add(txtNameEmployee);
        leftColumn.add(Box.createVerticalStrut(14));

        leftColumn.add(createFieldLabel("Leave Type"));
        String[] leaveTypes = {
            "Select Leave Type",
            "Vacation Leave (VL)",
            "Sick Leave (SL)"
        };
        cmbLeaveType = createStyledComboBox(leaveTypes);
        leftColumn.add(cmbLeaveType);
        leftColumn.add(Box.createVerticalStrut(14));

        leftColumn.add(createFieldLabel("From Date"));
        dcFrom = createStyledDateChooser();
        leftColumn.add(dcFrom);
        leftColumn.add(Box.createVerticalStrut(14));

        leftColumn.add(createFieldLabel("To Date"));
        dcTo = createStyledDateChooser();
        leftColumn.add(dcTo);

        // DATE RESTRICTIONS
        java.util.Calendar minFrom = java.util.Calendar.getInstance();
        minFrom.set(java.util.Calendar.HOUR_OF_DAY, 0);
        minFrom.set(java.util.Calendar.MINUTE, 0);
        minFrom.set(java.util.Calendar.SECOND, 0);
        minFrom.set(java.util.Calendar.MILLISECOND, 0);

        dcFrom.setMinSelectableDate(minFrom.getTime());

        dcFrom.addPropertyChangeListener("date", evt -> {
            java.util.Date selectedFrom = dcFrom.getDate();
            if (selectedFrom != null) {
                dcTo.setMinSelectableDate(selectedFrom);

                if (dcTo.getDate() != null && dcTo.getDate().before(selectedFrom)) {
                    dcTo.setDate(null);
                }
            }
        });

        //LEAVE TYPE
        cmbLeaveType.addActionListener(e -> {
            String raw = (String) cmbLeaveType.getSelectedItem();
            String code = mapLeaveType(raw);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);

            if ("VL".equals(code) || "SL".equals(code)) {
                cal.add(java.util.Calendar.DAY_OF_MONTH, 2); // must be at least 2 days away
            }

            dcFrom.setMinSelectableDate(cal.getTime());

            if (dcFrom.getDate() != null && dcFrom.getDate().before(cal.getTime())) {
                dcFrom.setDate(null);
                dcTo.setDate(null);
            }
        });

        dcTo.addPropertyChangeListener("date", evt -> {
            java.util.Date from = dcFrom.getDate();
            java.util.Date to = dcTo.getDate();
            if (from == null || to == null) {
                return;
            }
            if (to.before(from)) {
                return;
            }

            long daysRequested = LeaveLogic.daysBetween(from, to) + 1;
            String leaveTypeRaw = (String) cmbLeaveType.getSelectedItem();
            String leaveCode = mapLeaveType(leaveTypeRaw);
            if (leaveCode == null) {
                return;
            }

            int credits = -1;
            String creditLabel = "";
            if (leaveCode.equals("VL")) {
                credits = LeaveQuery.getVLCredits(currentEmpId);
                creditLabel = "Vacation Leave";
            } else if (leaveCode.equals("SL")) {
                credits = LeaveQuery.getSLCredits(currentEmpId);
                creditLabel = "Sick Leave";
            } else {
                return;
            }

            if (credits >= 0 && daysRequested > credits) {
                JOptionPane.showMessageDialog(
                        ApplyLeave.this,
                        "<html>You only have <b>" + credits + " " + creditLabel
                        + " day(s)</b> remaining,<br>but your selected dates span "
                        + daysRequested + " day(s).<br><br>"
                        + "Please shorten your leave period.</html>",
                        "Insufficient Credits",
                        JOptionPane.WARNING_MESSAGE);

                dcTo.setDate(null);
            }
        });

        // RIGHT: upload / preview
        JPanel rightColumn = new JPanel(new BorderLayout(0, 6));
        rightColumn.setBackground(BG);

        JPanel uploadLabelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        uploadLabelRow.setBackground(BG);
        uploadLabelRow.add(createFieldLabel("Supporting Document"));
        rightColumn.add(uploadLabelRow, BorderLayout.NORTH);

        rightColumn.add(buildDropZone(), BorderLayout.CENTER);

        contentGrid.add(leftColumn);
        contentGrid.add(rightColumn);

        //BOTTOM
        JPanel bottomArea = new JPanel();
        bottomArea.setBackground(BG);
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel rulesBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        rulesBox.setBackground(FIELD_BG);
        rulesBox.setBorder(new LineBorder(BORDER_CLR, 1, true));
        rulesBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel rulesText = new JLabel("<html><ul>"
                + "<li>Vacation Leave and Sick Leave must be filed 2 days before the leave.</li>"
                + "<li>Sick Leave filed after the leave requires a medical certificate if 2+ days absent.</li>"
                + "<li>Leave days are deducted from your credits upon submission.</li>"
                + "</ul></html>");
        rulesText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rulesText.setForeground(TEXT_MAIN);
        rulesBox.add(rulesText);

        bottomArea.add(rulesBox);
        bottomArea.add(Box.createVerticalStrut(20));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonRow.setBackground(BG);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCancel = new JButton("Cancel");
        styleButton(btnCancel, BTN_CANCEL, TEXT_MAIN);
        btnCancel.setBorder(new LineBorder(BORDER_CLR, 1, true));

        btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, BTN_SUBMIT, Color.WHITE);

        buttonRow.add(btnCancel);
        buttonRow.add(btnSubmit);
        bottomArea.add(buttonRow);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(BG);
        centerWrapper.add(contentGrid, BorderLayout.CENTER);
        centerWrapper.add(bottomArea, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);

        wireActions();
    }

    //  Drop Zone
    private JPanel buildDropZone() {

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(FIELD_BG);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        dropHint = new JLabel(
                "<html><center>📎 Drag &amp; drop file here<br>"
                + "or <u>click to browse</u><br><br>"
                + "<font color='#94A3B8'>PDF, JPG, PNG — max 5 MB</font></center></html>",
                SwingConstants.CENTER);
        dropHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        dropHint.setForeground(TEXT_MUTED);
        dropHint.setCursor(new Cursor(Cursor.HAND_CURSOR));

        //PREVIEW
        previewLabel = new JLabel("", SwingConstants.CENTER);
        previewLabel.setBackground(FIELD_BG);
        previewLabel.setOpaque(true);

        CardLayout dropCard = new CardLayout();
        JPanel dropStack = new JPanel(dropCard);
        dropStack.setBackground(FIELD_BG);
        dropStack.add(dropHint, "hint");
        dropStack.add(previewLabel, "preview");

        wrapper.add(dropStack, BorderLayout.CENTER);

        JPanel southStrip = new JPanel(new BorderLayout(8, 0));
        southStrip.setBackground(FIELD_BG);
        southStrip.setBorder(new EmptyBorder(10, 4, 2, 4));
        southStrip.setVisible(false);

        fileNameLabel = new JLabel("", SwingConstants.LEFT);
        fileNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fileNameLabel.setForeground(TEXT_MUTED);

        btnClearFile = new JButton("Remove File");
        btnClearFile.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClearFile.setForeground(Color.decode("#DC2626"));
        btnClearFile.setBackground(Color.decode("#FEF2F2"));
        btnClearFile.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#FECACA"), 1, true),
                new EmptyBorder(6, 16, 6, 16)));
        btnClearFile.setFocusPainted(false);
        btnClearFile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearFile.setOpaque(true);

        southStrip.add(fileNameLabel, BorderLayout.CENTER);
        southStrip.add(btnClearFile, BorderLayout.EAST);

        wrapper.add(southStrip, BorderLayout.SOUTH);

        // INTERACTIONS
        dropHint.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openFileChooser(dropCard, dropStack, southStrip);
            }
        });

        new DropTarget(wrapper, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        handleFile(files.get(0), dropCard, dropStack, southStrip);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                wrapper.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.decode("#6366F1"), 2, true),
                        new EmptyBorder(12, 12, 12, 12)));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                wrapper.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_CLR, 1, true),
                        new EmptyBorder(12, 12, 12, 12)));
            }
        });

        // Remove button
        btnClearFile.addActionListener(e -> clearFile(dropCard, dropStack, southStrip));

        return wrapper;
    }

    //  File handling
    private void openFileChooser(CardLayout card, JPanel stack, JPanel southStrip) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Documents & Images (PDF, JPG, PNG)", "pdf", "jpg", "jpeg", "png"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            handleFile(chooser.getSelectedFile(), card, stack, southStrip);
        }
    }

    private void handleFile(File file, CardLayout card, JPanel stack, JPanel southStrip) {
        // Size check (5 MB)
        if (file.length() > 5L * 1024 * 1024) {
            JOptionPane.showMessageDialog(this,
                    "File is too large. Maximum size is 5 MB.", "File Too Large",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = file.getName().toLowerCase();
        boolean isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
        boolean isPdf = name.endsWith(".pdf");

        if (!isImage && !isPdf) {
            JOptionPane.showMessageDialog(this,
                    "Only PDF, JPG, and PNG files are supported.", "Unsupported File",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Copy to uploads directory
        try {
            Path uploadDir = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadDir);
            String destName = System.currentTimeMillis() + "_" + file.getName();
            Path dest = uploadDir.resolve(destName);
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            uploadedPath = dest.toAbsolutePath().toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to save uploaded file:\n" + ex.getMessage(),
                    "Upload Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build preview
        previewLabel.setText("");
        previewLabel.setIcon(null);

        if (isImage) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {

                    Image scaled = img.getScaledInstance(320, 260, Image.SCALE_SMOOTH);
                    previewLabel.setIcon(new ImageIcon(scaled));
                }
            } catch (IOException ex) {
                previewLabel.setText("<html><center><font color='#94A3B8'>Preview unavailable</font></center></html>");
            }
        } else {

            previewLabel.setText(
                    "<html><center>"
                    + "<div style='font-size:52px'>📄</div>"
                    + "<br><span style='color:#64748B;font-size:14px'>PDF Document</span>"
                    + "</center></html>");
        }

        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);

        card.show(stack, "preview");
        fileNameLabel.setText(file.getName());
        southStrip.setVisible(true);

        revalidate();
        repaint();
    }

    private void clearFile(CardLayout card, JPanel stack, JPanel southStrip) {
        uploadedPath = null;
        previewLabel.setIcon(null);
        previewLabel.setText("");
        fileNameLabel.setText("");
        southStrip.setVisible(false);
        card.show(stack, "hint");
        revalidate();
        repaint();
    }

    //  Button actions
    private void wireActions() {
        btnCancel.addActionListener(e -> resetForm());

        btnSubmit.addActionListener(e -> submitLeave());
    }

    private void submitLeave() {
        if (currentEmpId < 0) {
            JOptionPane.showMessageDialog(this, "Employee session not loaded.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String leaveTypeRaw = (String) cmbLeaveType.getSelectedItem();
        String leaveCode = mapLeaveType(leaveTypeRaw);
        if (leaveCode == null) {
            JOptionPane.showMessageDialog(this, "Please select a leave type.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date fromDate = dcFrom.getDate();
        java.util.Date toDate = dcTo.getDate();

        // Validate
        String error = LeaveLogic.validate(currentEmpId, leaveCode, fromDate, toDate, uploadedPath);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Submit to DB
        boolean ok = LeaveQuery.submitRequest(currentEmpId, leaveCode, fromDate, toDate, uploadedPath);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to submit request. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Deduct credits
        if (leaveCode.equals("VL") || leaveCode.equals("SL")) {
            long days = LeaveLogic.daysBetween(fromDate, toDate) + 1;
            LeaveQuery.deductCredits(currentEmpId, leaveCode, (int) days);
        }

        JOptionPane.showMessageDialog(this,
                "Leave request submitted successfully!\nStatus: Pending approval.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        refreshCreditBadges();
        resetForm();
    }

    private void resetForm() {
        cmbLeaveType.setSelectedIndex(0);
        dcFrom.setDate(null);
        dcTo.setDate(null);
        uploadedPath = null;
        previewLabel.setIcon(null);
        previewLabel.setText("");
        fileNameLabel.setText("");

        btnClearFile.doClick();
    }

    //  Credits
    private void refreshCreditBadges() {
        if (currentEmpId < 0) {
            return;
        }
        int vl = LeaveQuery.getVLCredits(currentEmpId);
        int sl = LeaveQuery.getSLCredits(currentEmpId);
        lblVLCredits.setText("  VL: " + (vl >= 0 ? vl : "—") + " day(s) remaining  ");
        lblSLCredits.setText("  SL: " + (sl >= 0 ? sl : "—") + " day(s) remaining  ");
    }

    //  Public API
    public void setEmployee(int id, String name) {
        currentEmpId = id;

        txtEmpID.setText(String.valueOf(id));
        txtEmpID.setEditable(false);

        txtNameEmployee.setText(name);
        txtNameEmployee.setEditable(false);

        refreshCreditBadges();
    }

    //  Helpers
    private String mapLeaveType(String display) {
        if (display == null) {
            return null;
        }
        if (display.contains("VL")) {
            return "VL";
        }
        if (display.contains("SL")) {
            return "SL";
        }
        if (display.contains("EL")) {
            return "EL";
        }
        return null;
    }

    private JLabel createBadge(String text, Color bg) {
        JLabel badge = new JLabel("  " + text + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBackground(bg);
        badge.setForeground(BADGE_TEXT);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BBF7D0"), 1, true),
                new EmptyBorder(3, 6, 3, 6)));
        return badge;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_MAIN);
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(FIELD_BG);
        tf.setForeground(TEXT_MAIN);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        tf.setPreferredSize(new Dimension(0, 40));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(FIELD_BG);
        cb.setForeground(TEXT_MAIN);
        cb.setPreferredSize(new Dimension(0, 40));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
    }

    private JDateChooser createStyledDateChooser() {
        JDateChooser dc = new JDateChooser();
        dc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(FIELD_BG);
        dc.setPreferredSize(new Dimension(0, 40));
        dc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        dc.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField tf = (JTextField) dc.getDateEditor().getUiComponent();
        tf.setBackground(FIELD_BG);
        tf.setForeground(TEXT_MAIN);
        tf.setBorder(new EmptyBorder(0, 10, 0, 0));
        return dc;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(160, 45));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}