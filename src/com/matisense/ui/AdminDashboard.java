package com.matisense.ui;

import com.matisense.dao.AnnouncementDAO;
import com.matisense.dao.ReportDAO;
import com.matisense.dao.UserDAO;
import com.matisense.exception.MatisenseException;
import com.matisense.model.Announcement;
import com.matisense.model.Report;
import com.matisense.model.User;
import com.matisense.service.NotificationService;
import com.matisense.util.ValidationUtil;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Admin dashboard with reports and announcements management.
 */
public class AdminDashboard extends JFrame {
    private static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(150, 36);
    private static final Color PRIMARY_COLOR = new Color(21, 101, 192);
    private static final Color TABLE_HEADER = new Color(227, 242, 253);

    private final User currentUser;
    private final ReportDAO reportDAO;
    private final UserDAO userDAO;
    private final AnnouncementDAO announcementDAO;
    private final NotificationService notificationService;

    private JTabbedPane tabbedPane;
    private JTable reportsTable;
    private DefaultTableModel reportsTableModel;
    private JTable announcementsTable;
    private DefaultTableModel announcementsTableModel;
    private JButton refreshButton;
    private JButton updateStatusButton;
    private JButton notificationsButton;
    private JButton logoutButton;
    private JButton annRefreshButton;
    private JButton annCreateButton;
    private JButton annEditButton;
    private JButton annDeleteButton;
    private JButton annCloseButton;
    private JLabel welcomeLabel;
    private JLabel statsLabel;

    private static class DashboardData {
        private final List<ReportRow> reportRows = new ArrayList<>();
        private int pendingCount;
        private int inReviewCount;
        private int approvedCount;
        private int inProgressCount;
        private int resolvedCount;
        private int completedCount;
    }

    private static class ReportRow {
        private final Object[] rowData;

        private ReportRow(int reportId, String residentName, String reportType,
                String location, String descriptionPreview,
                String status, String createdAt) {
            this.rowData = new Object[] {
                    reportId,
                    residentName,
                    reportType,
                    location,
                    descriptionPreview,
                    status,
                    createdAt
            };
        }
    }

    private static class AnnouncementRow {
        private final Object[] rowData;

        private AnnouncementRow(int id, String title, String preview,
                String createdAt, String updatedAt) {
            this.rowData = new Object[] { id, title, preview, createdAt, updatedAt };
        }
    }

    public AdminDashboard(User currentUser) {
        this.currentUser = currentUser;
        this.reportDAO = new ReportDAO();
        this.userDAO = new UserDAO();
        this.announcementDAO = new AnnouncementDAO();
        this.notificationService = new NotificationService();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
        refreshDashboardDataWithLoader("Loading dashboard data...");
    }

    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tabbedPane.setBackground(Color.WHITE);

        reportsTableModel = new DefaultTableModel(new String[] {
                "Report ID", "Resident", "Type", "Location", "Description", "Status", "Created At"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportsTable = new JTable(reportsTableModel);
        reportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportsTable.getTableHeader().setReorderingAllowed(false);
        styleTable(reportsTable);
        reportsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        reportsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        reportsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        reportsTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        reportsTable.getColumnModel().getColumn(4).setPreferredWidth(260);
        reportsTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        reportsTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        announcementsTableModel = new DefaultTableModel(new String[] {
                "ID", "Title", "Preview", "Created At", "Updated At"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        announcementsTable = new JTable(announcementsTableModel);
        announcementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        announcementsTable.getTableHeader().setReorderingAllowed(false);
        styleTable(announcementsTable);

        refreshButton = new JButton("Refresh");
        updateStatusButton = new JButton("Update Status");
        notificationsButton = new JButton("Notifications");
        logoutButton = new JButton("Logout");
        annRefreshButton = new JButton("Refresh");
        annCreateButton = new JButton("Create New");
        annEditButton = new JButton("Edit");
        annDeleteButton = new JButton("Delete");
        annCloseButton = new JButton("Back to Reports");

        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Admin)");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 15));
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(233, 247, 255));

        applyButtonSizing(refreshButton, updateStatusButton, notificationsButton, logoutButton,
                annRefreshButton, annCreateButton, annEditButton, annDeleteButton, annCloseButton);
        applyButtonTheme(refreshButton, updateStatusButton, notificationsButton, logoutButton,
                annRefreshButton, annCreateButton, annEditButton, annDeleteButton, annCloseButton);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        headerPanel.setBackground(PRIMARY_COLOR);
        welcomeLabel.setForeground(Color.WHITE);

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.add(welcomeLabel);
        textStack.add(Box.createVerticalStrut(4));
        textStack.add(statsLabel);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtons.setOpaque(false);
        headerButtons.add(notificationsButton);
        headerButtons.add(logoutButton);

        headerPanel.add(textStack, BorderLayout.WEST);
        headerPanel.add(headerButtons, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(230, 232, 236))));
        JPanel reportsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        reportsButtonPanel.setOpaque(false);
        reportsButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        reportsButtonPanel.add(refreshButton);
        reportsButtonPanel.add(updateStatusButton);
        reportsPanel.add(reportsButtonPanel, BorderLayout.NORTH);
        reportsPanel.add(new JScrollPane(reportsTable), BorderLayout.CENTER);
        tabbedPane.addTab("Reports Management", reportsPanel);

        JPanel announcementsPanel = new JPanel(new BorderLayout());
        announcementsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(230, 232, 236))));
        JPanel annButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        annButtonPanel.setOpaque(false);
        annButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        annButtonPanel.add(annRefreshButton);
        annButtonPanel.add(annCreateButton);
        annButtonPanel.add(annEditButton);
        annButtonPanel.add(annDeleteButton);
        annButtonPanel.add(annCloseButton);
        announcementsPanel.add(annButtonPanel, BorderLayout.NORTH);
        announcementsPanel.add(new JScrollPane(announcementsTable), BorderLayout.CENTER);
        tabbedPane.addTab("Announcements Management", announcementsPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> refreshDashboardDataWithLoader("Refreshing reports..."));

        updateStatusButton.addActionListener(e -> updateReportStatus());
        notificationsButton.addActionListener(e -> openNotifications());
        logoutButton.addActionListener(e -> logout());

        reportsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewReportDetails();
                }
            }
        });

        annRefreshButton.addActionListener(e -> loadAnnouncements());
        annCreateButton.addActionListener(e -> showAnnouncementDialog(null));
        annEditButton.addActionListener(e -> editSelectedAnnouncement());
        annDeleteButton.addActionListener(e -> deleteSelectedAnnouncement());
        annCloseButton.addActionListener(e -> tabbedPane.setSelectedIndex(0));

        announcementsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedAnnouncement();
                }
            }
        });
    }

    private void configureFrame() {
        setTitle("Admin Dashboard - Matisense Community Report System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void refreshDashboardDataWithLoader(String message) {
        final LoadingDialog loadingDialog = new LoadingDialog(this, message);
        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            private Exception loadException;

            @Override
            protected DashboardData doInBackground() {
                try {
                    return fetchDashboardData();
                } catch (Exception e) {
                    loadException = e;
                    return null;
                }
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                if (loadException != null) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Error loading dashboard data: " + loadException.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    DashboardData data = get();
                    if (data != null) {
                        applyDashboardData(data);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Error applying dashboard data: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    private DashboardData fetchDashboardData() throws MatisenseException {
        DashboardData data = new DashboardData();
        List<Report> reports = reportDAO.findAll();

        for (Report report : reports) {
            String residentName = getResidentName(report.getResidentId());
            String description = report.getDescription() != null ? report.getDescription() : "";
            String preview = description.length() > 120 ? description.substring(0, 120) + "..." : description;

            data.reportRows.add(new ReportRow(
                    report.getId(),
                    residentName,
                    report.getReportType(),
                    report.getLocation(),
                    preview,
                    report.getStatus().getDisplayName(),
                    report.getFormattedCreatedAt()));

            switch (report.getStatus()) {
                case PENDING -> data.pendingCount++;
                case IN_REVIEW -> data.inReviewCount++;
                case APPROVED -> data.approvedCount++;
                case IN_PROGRESS -> data.inProgressCount++;
                case RESOLVED -> data.resolvedCount++;
                case COMPLETED -> data.completedCount++;
                default -> {
                }
            }
        }

        return data;
    }

    private String getResidentName(int residentId) {
        try {
            User user = userDAO.findById(residentId);
            return user != null ? user.getFullName() : "Resident #" + residentId;
        } catch (MatisenseException e) {
            return "Resident #" + residentId;
        }
    }

    private void applyDashboardData(DashboardData data) {
        reportsTableModel.setRowCount(0);
        for (ReportRow row : data.reportRows) {
            reportsTableModel.addRow(row.rowData);
        }
        updateStatsLabel(data);
        loadAnnouncements();
    }

    private void updateStatsLabel(DashboardData data) {
        statsLabel.setText(String.format(
                "Pending: %d | In Review: %d | Approved: %d | In Progress: %d | Resolved: %d | Completed: %d",
                data.pendingCount,
                data.inReviewCount,
                data.approvedCount,
                data.inProgressCount,
                data.resolvedCount,
                data.completedCount));
    }

    private void updateReportStatus() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a report to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reportId = (int) reportsTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) reportsTableModel.getValueAt(selectedRow, 5);

        Report.ReportStatus[] statuses = Report.ReportStatus.values();
        String[] options = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            options[i] = statuses[i].getDisplayName();
        }

        String selectedStatus = (String) JOptionPane.showInputDialog(this,
                "Select new status:",
                "Update Report Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                currentStatus);

        if (selectedStatus == null || selectedStatus.equals(currentStatus)) {
            return;
        }

        Report.ReportStatus newStatus = Report.ReportStatus.fromString(selectedStatus);
        try {
            // Load the full report to capture details and old status before updating
            Report report = reportDAO.findById(reportId);
            if (report == null) {
                JOptionPane.showMessageDialog(this, "Unable to load report details for notification.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Report.ReportStatus oldStatus = report.getStatus();

            boolean updated = reportDAO.updateStatus(reportId, newStatus);
            if (updated) {
                // Sync in-memory report status with the new value for accurate notifications
                report.setStatus(newStatus);

                // Notify the resident that their report status has changed
                try {
                    notificationService.notifyReportStatusUpdate(report, oldStatus);
                } catch (MatisenseException notifyEx) {
                    JOptionPane.showMessageDialog(this,
                            "Status updated but resident notification failed: " + notifyEx.getMessage(),
                            "Notification Warning", JOptionPane.WARNING_MESSAGE);
                }
                JOptionPane.showMessageDialog(this, "Report status updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshDashboardDataWithLoader("Updating statistics...");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update report status.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this,
                    "Error updating report status: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewReportDetails() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int reportId = (int) reportsTableModel.getValueAt(selectedRow, 0);
        try {
            Report report = reportDAO.findById(reportId);
            if (report == null) {
                JOptionPane.showMessageDialog(this, "Unable to load report details.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User resident = userDAO.findById(report.getResidentId());
            String[][] data = {
                    { "Report ID", String.valueOf(report.getId()) },
                    { "Resident", resident != null ? resident.getFullName() : "Unknown" },
                    { "Type", report.getReportType() },
                    { "Location", report.getLocation() },
                    { "Status", report.getStatus().getDisplayName() },
                    { "Created", report.getFormattedCreatedAt() },
                    { "Updated", report.getFormattedUpdatedAt() }
            };

            JPanel detailsPanel = buildDetailPanel(data, "Description",
                    report.getDescription() != null ? report.getDescription() : "");

            JOptionPane.showMessageDialog(this, detailsPanel,
                    "Report Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading report details: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openNotifications() {
        NotificationPanel notificationPanel = new NotificationPanel(currentUser);
        notificationPanel.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        }
    }

    private void loadAnnouncements() {
        try {
            List<Announcement> announcements = announcementDAO.findAll();
            announcementsTableModel.setRowCount(0);
            for (Announcement announcement : announcements) {
                announcementsTableModel.addRow(new AnnouncementRow(
                        announcement.getId(),
                        announcement.getTitle(),
                        announcement.getTruncatedContent(80),
                        announcement.getFormattedCreatedAt(),
                        announcement.getFormattedUpdatedAt()).rowData);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading announcements: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedAnnouncement() {
        int selectedRow = announcementsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an announcement to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int announcementId = (int) announcementsTableModel.getValueAt(selectedRow, 0);
        try {
            Announcement announcement = announcementDAO.findById(announcementId);
            if (announcement != null) {
                showAnnouncementDialog(announcement);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading announcement: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAnnouncement() {
        int selectedRow = announcementsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an announcement to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int announcementId = (int) announcementsTableModel.getValueAt(selectedRow, 0);
        String title = (String) announcementsTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete the announcement: \"" + title + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = announcementDAO.delete(announcementId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this,
                            "Announcement deleted successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAnnouncements();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete announcement.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (MatisenseException e) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting announcement: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAnnouncementDialog(Announcement announcementToEdit) {
        boolean isEditMode = announcementToEdit != null;
        JDialog dialog = new JDialog(this,
                isEditMode ? "Edit Announcement" : "Create Announcement", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        String initialTitle = "";
        String initialContent = "";
        if (isEditMode && announcementToEdit != null) {
            initialTitle = announcementToEdit.getTitle();
            initialContent = announcementToEdit.getContent();
        }

        JTextField titleField = new JTextField(initialTitle, 30);
        JTextArea contentArea = new JTextArea(initialContent, 10, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Content:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane contentScroll = new JScrollPane(contentArea);
        formPanel.add(contentScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(isEditMode ? "Update" : "Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (!ValidationUtil.isNotEmpty(title)) {
                JOptionPane.showMessageDialog(dialog, "Title is required.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtil.isValidLength(title, 200)) {
                JOptionPane.showMessageDialog(dialog, "Title must be 200 characters or less.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtil.isNotEmpty(content)) {
                JOptionPane.showMessageDialog(dialog, "Content is required.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (isEditMode) {
                    announcementToEdit.setTitle(title);
                    announcementToEdit.setContent(content);
                    announcementDAO.update(announcementToEdit);
                    JOptionPane.showMessageDialog(dialog, "Announcement updated successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Announcement newAnnouncement = new Announcement(currentUser.getId(), title, content);
                    Announcement created = announcementDAO.create(newAnnouncement);
                    try {
                        notificationService.notifyNewAnnouncement(created);
                    } catch (MatisenseException notifyEx) {
                        JOptionPane.showMessageDialog(dialog,
                                "Announcement saved but notifications failed: " + notifyEx.getMessage(),
                                "Notification Warning", JOptionPane.WARNING_MESSAGE);
                    }
                    JOptionPane.showMessageDialog(dialog, "Announcement created successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                loadAnnouncements();
                dialog.dispose();
            } catch (MatisenseException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error saving announcement: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void applyButtonSizing(JButton... buttons) {
        for (JButton button : buttons) {
            if (button != null) {
                button.setPreferredSize(DEFAULT_BUTTON_SIZE);
                button.setMinimumSize(DEFAULT_BUTTON_SIZE);
                button.setMaximumSize(DEFAULT_BUTTON_SIZE);
            }
        }
    }

    private void applyButtonTheme(JButton... buttons) {
        for (JButton button : buttons) {
            if (button != null) {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setFont(new Font("Arial", Font.PLAIN, 13));
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createLineBorder(new Color(205, 205, 205)));
            }
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
        header.setBackground(TABLE_HEADER);
        header.setForeground(Color.DARK_GRAY);
        header.setFont(new Font("Arial", Font.BOLD, 12));

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
    }

    private JPanel buildDetailPanel(String[][] data, String bodyTitle, String bodyContent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(480, 280));

        String[] columnNames = { "Field", "Value" };
        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(320);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(460, Math.min(160, table.getRowCount() * 28)));

        JTextArea bodyArea = new JTextArea(bodyContent != null ? bodyContent : "");
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setEditable(false);
        bodyArea.setBorder(BorderFactory.createTitledBorder(bodyTitle));
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setPreferredSize(new Dimension(460, 110));

        panel.add(tableScroll);
        panel.add(Box.createVerticalStrut(8));
        panel.add(bodyScroll);
        return panel;
    }
}
