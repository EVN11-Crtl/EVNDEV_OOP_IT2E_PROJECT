package com.matisense.ui;

import com.matisense.model.User;
import com.matisense.model.Report;
import com.matisense.model.Announcement;
import com.matisense.dao.ReportDAO;
import com.matisense.dao.AnnouncementDAO;
import com.matisense.dao.UserDAO;
import com.matisense.exception.MatisenseException;
import com.matisense.service.NotificationService;
import com.matisense.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Resident Dashboard for submitting reports and viewing announcements
 * Implements resident functionality with report submission and announcement viewing
 */
public class ResidentDashboard extends JFrame {
    private static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(150, 36);
    private static final Color PRIMARY_COLOR = new Color(21, 101, 192);
    private static final Color LIGHT_BG = new Color(245, 247, 250);
    private static final Color TABLE_HEADER = new Color(227, 242, 253);
    private User currentUser;
    private ReportDAO reportDAO;
    private AnnouncementDAO announcementDAO;
    private UserDAO userDAO;
    private NotificationService notificationService;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private JTable myReportsTable;
    private DefaultTableModel myReportsTableModel;
    private JTable announcementsTable;
    private DefaultTableModel announcementsTableModel;
    
    // Report submission components
    private JComboBox<String> reportTypeComboBox;
    private JTextField locationField;
    private JTextArea descriptionArea;
    private JButton submitReportButton;
    private JButton clearButton;
    
    // Buttons
    private JButton refreshButton;
    private JButton notificationsButton;
    private JButton logoutButton;
    private JLabel welcomeLabel;
    
    // Profile components
    private JTextField profileUsernameField;
    private JTextField profileFullNameField;
    private JTextArea profileAddressArea;
    private JComboBox<String> profileGenderComboBox;
    private JTextField profileEmailField;
    private JTextField profileContactField;
    private JTextField profileBirthdayField;
    private JButton profileSaveButton;
    
    private static class DashboardData {
        private final List<ReportRow> reportRows = new ArrayList<>();
        private final List<AnnouncementRow> announcementRows = new ArrayList<>();
    }
    
    private static class ReportRow {
        private final Object[] rowData;
        
        private ReportRow(int id, String type, String location, String descriptionPreview,
                          String status, String createdAt) {
            this.rowData = new Object[]{id, type, location, descriptionPreview, status, createdAt};
        }
    }
    
    private static class AnnouncementRow {
        private final Object[] rowData;
        
        private AnnouncementRow(String title, String preview, String createdAt) {
            this.rowData = new Object[]{title, preview, createdAt};
        }
    }
    
    /**
     * Constructor
     * @param currentUser Currently logged-in resident user
     */
    public ResidentDashboard(User currentUser) {
        this.currentUser = currentUser;
        this.reportDAO = new ReportDAO();
        this.announcementDAO = new AnnouncementDAO();
        this.notificationService = new NotificationService();
        this.userDAO = new UserDAO();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
        loadDashboardDataWithLoader("Loading your data...");
        loadProfileInfo();
    }
    
    /**
     * Build a consistent detail panel with table and body content
     */
    private JPanel buildDetailPanel(String[][] data, String bodyTitle, String bodyContent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(460, 260));
        
        String[] columnNames = {"Field", "Value"};
        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(440, Math.min(140, table.getRowCount() * 28)));
        
        JTextArea bodyArea = new JTextArea(bodyContent != null ? bodyContent : "");
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setEditable(false);
        bodyArea.setBorder(BorderFactory.createTitledBorder(bodyTitle));
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setPreferredSize(new Dimension(440, 110));
        
        panel.add(tableScroll);
        panel.add(Box.createVerticalStrut(8));
        panel.add(bodyScroll);
        return panel;
    }
    
    /**
     * Apply consistent sizing to buttons
     */
    private void applyButtonSizing(JButton... buttons) {
        for (JButton button : buttons) {
            if (button != null) {
                button.setPreferredSize(DEFAULT_BUTTON_SIZE);
                button.setMinimumSize(DEFAULT_BUTTON_SIZE);
                button.setMaximumSize(DEFAULT_BUTTON_SIZE);
            }
        }
    }
    
    /**
     * Apply table styling for consistent look
     */
    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.DARK_GRAY);
        table.setOpaque(false);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(Color.DARK_GRAY);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LIGHT_BG);
                }
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                }
                return c;
            }
        });
    }
    
    /**
     * Apply accent theme to buttons
     */
    private void applyButtonTheme(JButton... buttons) {
        for (JButton button : buttons) {
            if (button != null) {
                button.setFocusPainted(false);
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
                button.setOpaque(true);
            }
        }
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tabbedPane.setBackground(Color.WHITE);
        
        // My Reports tab components
        myReportsTableModel = new DefaultTableModel(new String[]{
            "Report ID", "Type", "Location", "Description", "Status", "Created At"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        myReportsTable = new JTable(myReportsTableModel);
        myReportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myReportsTable.getTableHeader().setReorderingAllowed(false);
        styleTable(myReportsTable);
        
        // Announcements tab components
        announcementsTableModel = new DefaultTableModel(new String[]{
            "Title", "Content", "Created At"
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
        
        // Report submission components
        String[] reportTypes = {"Infrastructure Issue", "Security Concern", "Environmental Problem", 
                                "Noise Complaint", "Public Service Request", "Other"};
        reportTypeComboBox = new JComboBox<>(reportTypes);
        locationField = new JTextField(30);
        descriptionArea = new JTextArea(8, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        submitReportButton = new JButton("Submit Report");
        clearButton = new JButton("Clear Form");
        
        refreshButton = new JButton("Refresh");
        notificationsButton = new JButton("Notifications");
        logoutButton = new JButton("Logout");
        
        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        profileUsernameField = new JTextField(20);
        profileUsernameField.setEditable(false);
        profileFullNameField = new JTextField(20);
        profileAddressArea = new JTextArea(4, 20);
        profileAddressArea.setLineWrap(true);
        profileAddressArea.setWrapStyleWord(true);
        profileGenderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        profileEmailField = new JTextField(20);
        profileContactField = new JTextField(20);
        profileBirthdayField = new JTextField(20);
        profileSaveButton = new JButton("Save Profile");
        
        applyButtonSizing(submitReportButton, clearButton, refreshButton,
            notificationsButton, logoutButton, profileSaveButton);
        applyButtonTheme(submitReportButton, clearButton, refreshButton,
            notificationsButton, logoutButton, profileSaveButton);
    }
    
    /**
     * Setup layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(PRIMARY_COLOR);
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(notificationsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tabs
        createMyReportsTab();
        createSubmitReportTab();
        createAnnouncementsTab();
        createProfileTab();
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Create My Reports tab
     */
    private void createMyReportsTab() {
        JPanel reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(230, 232, 236))
        ));
        reportsPanel.add(new JScrollPane(myReportsTable), BorderLayout.CENTER);
        tabbedPane.addTab("My Reports", reportsPanel);
        
        // Configure table columns
        myReportsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        myReportsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        myReportsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        myReportsTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        myReportsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        myReportsTable.getColumnModel().getColumn(5).setPreferredWidth(150);
    }
    
    /**
     * Create Submit Report tab
     */
    private void createSubmitReportTab() {
        JPanel submitPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Report Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Report Type:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(reportTypeComboBox, gbc);
        
        // Location
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Location:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(locationField, gbc);
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitReportButton);
        buttonPanel.add(clearButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(buttonPanel, gbc);
        
        submitPanel.add(formPanel, BorderLayout.CENTER);
        tabbedPane.addTab("Submit Report", submitPanel);
    }
    
    /**
     * Create Announcements tab
     */
    private void createAnnouncementsTab() {
        JPanel announcementsPanel = new JPanel(new BorderLayout());
        announcementsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(230, 232, 236))
        ));
        announcementsPanel.add(new JScrollPane(announcementsTable), BorderLayout.CENTER);
        tabbedPane.addTab("Announcements", announcementsPanel);
        
        // Configure table columns
        announcementsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        announcementsTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        announcementsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
    }
    
    /**
     * Create Profile tab
     */
    private void createProfileTab() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        int row = 0;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(profileUsernameField, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(profileFullNameField, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(profileEmailField, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Contact Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(profileContactField, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        formPanel.add(profileGenderComboBox, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Birthday (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        formPanel.add(profileBirthdayField, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane addressScroll = new JScrollPane(profileAddressArea);
        formPanel.add(addressScroll, gbc);
        
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setOpaque(false);
        actionsPanel.add(profileSaveButton);
        
        profilePanel.add(formPanel, BorderLayout.CENTER);
        profilePanel.add(actionsPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("My Profile", profilePanel);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        submitReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitReport();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadDashboardDataWithLoader("Refreshing your data...");
            }
        });
        
        notificationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNotifications();
            }
        });
        
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        profileSaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfileChanges();
            }
        });
        
        // Double-click on report to view details
        myReportsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewReportDetails();
                }
            }
        });
        
        // Double-click on announcement to view full content
        announcementsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewAnnouncementDetails();
                }
            }
        });
    }
    
    /**
     * Configure frame properties
     */
    private void configureFrame() {
        setTitle("Resident Dashboard - Matisense Community Report System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    /**
     * Load dashboard data with visual feedback
     */
    private void loadDashboardDataWithLoader(String message) {
        final LoadingDialog loadingDialog = new LoadingDialog(this, message);
        SwingWorker<DashboardData, Void> worker = new SwingWorker<DashboardData, Void>() {
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
                    JOptionPane.showMessageDialog(ResidentDashboard.this,
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
                    JOptionPane.showMessageDialog(ResidentDashboard.this,
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
        
        List<Report> reports = reportDAO.findByResidentId(currentUser.getId());
        for (Report report : reports) {
            String description = report.getDescription() != null ? report.getDescription() : "";
            String preview = description.length() > 50 ? description.substring(0, 50) + "..." : description;
            data.reportRows.add(new ReportRow(
                report.getId(),
                report.getReportType(),
                report.getLocation(),
                preview,
                report.getStatus().getDisplayName(),
                report.getFormattedCreatedAt()
            ));
        }
        
        List<Announcement> announcements = announcementDAO.findRecent(50);
        for (Announcement announcement : announcements) {
            data.announcementRows.add(new AnnouncementRow(
                announcement.getTitle(),
                announcement.getTruncatedContent(100),
                announcement.getFormattedCreatedAt()
            ));
        }
        
        return data;
    }
    
    private void applyDashboardData(DashboardData data) {
        myReportsTableModel.setRowCount(0);
        for (ReportRow row : data.reportRows) {
            myReportsTableModel.addRow(row.rowData);
        }
        
        announcementsTableModel.setRowCount(0);
        for (AnnouncementRow row : data.announcementRows) {
            announcementsTableModel.addRow(row.rowData);
        }
    }
    
    /**
     * Submit a new report
     */
    private void submitReport() {
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        String location = locationField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        // Validation
        if (!ValidationUtil.isNotEmpty(location)) {
            JOptionPane.showMessageDialog(this, "Location is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!ValidationUtil.isNotEmpty(description)) {
            JOptionPane.showMessageDialog(this, "Description is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Report report = new Report(currentUser.getId(), reportType, location, description);
            Report createdReport = reportDAO.create(report);
            
            if (createdReport != null && createdReport.getId() > 0) {
                JOptionPane.showMessageDialog(this, "Report submitted successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                try {
                    notificationService.notifyNewReportSubmission(createdReport);
                } catch (MatisenseException notifyEx) {
                    JOptionPane.showMessageDialog(this,
                        "Report saved but admin notification failed: " + notifyEx.getMessage(),
                        "Notification Warning", JOptionPane.WARNING_MESSAGE);
                }
                clearForm();
                loadDashboardDataWithLoader("Refreshing your data...");
                tabbedPane.setSelectedIndex(0); // Switch to My Reports tab
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit report", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error submitting report: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clear the report submission form
     */
    private void clearForm() {
        reportTypeComboBox.setSelectedIndex(0);
        locationField.setText("");
        descriptionArea.setText("");
    }
    
    /**
     * View report details
     */
    private void viewReportDetails() {
        int selectedRow = myReportsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        int reportId = (Integer) myReportsTableModel.getValueAt(selectedRow, 0);
        
        try {
            Report report = reportDAO.findById(reportId);
            if (report != null) {
                String[][] data = {
                    {"Report ID", String.valueOf(report.getId())},
                    {"Type", report.getReportType()},
                    {"Location", report.getLocation()},
                    {"Status", report.getStatus().getDisplayName()},
                    {"Created", report.getFormattedCreatedAt()}
                };
                
                JPanel detailsPanel = buildDetailPanel(data, "Description", report.getDescription());
                JOptionPane.showMessageDialog(this, detailsPanel,
                    "Report Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error loading report details: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Load current user info into profile form
     */
    private void loadProfileInfo() {
        profileUsernameField.setText(currentUser.getUsername());
        profileFullNameField.setText(currentUser.getFullName());
        profileEmailField.setText(currentUser.getEmail());
        profileContactField.setText(currentUser.getContactNumber());
        profileGenderComboBox.setSelectedItem(currentUser.getGender() != null ? currentUser.getGender() : "Other");
        profileBirthdayField.setText(currentUser.getBirthday());
        profileAddressArea.setText(currentUser.getAddress());
    }
    
    /**
     * Persist profile changes
     */
    private void saveProfileChanges() {
        String fullName = profileFullNameField.getText().trim();
        String email = profileEmailField.getText().trim();
        String contact = profileContactField.getText().trim();
        String gender = (String) profileGenderComboBox.getSelectedItem();
        String birthday = profileBirthdayField.getText().trim();
        String address = profileAddressArea.getText().trim();
        
        if (!ValidationUtil.isNotEmpty(fullName)) {
            JOptionPane.showMessageDialog(this, "Full name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!ValidationUtil.isNotEmpty(email) || !ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Valid email is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (ValidationUtil.isNotEmpty(contact) && !ValidationUtil.isValidPhone(contact)) {
            JOptionPane.showMessageDialog(this, "Contact number is invalid", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setContactNumber(contact);
        currentUser.setGender(gender);
        currentUser.setBirthday(birthday);
        currentUser.setAddress(address);
        
        try {
            userDAO.update(currentUser);
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            JOptionPane.showMessageDialog(this, "Profile updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Failed to update profile: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * View announcement details
     */
    private void viewAnnouncementDetails() {
        int selectedRow = announcementsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        String title = (String) announcementsTableModel.getValueAt(selectedRow, 0);
        String contentPreview = (String) announcementsTableModel.getValueAt(selectedRow, 1);
        
        try {
            List<Announcement> announcements = announcementDAO.findAll();
            for (Announcement announcement : announcements) {
                if (announcement.getTitle().equals(title) &&
                    announcement.getTruncatedContent(100).equals(contentPreview)) {
                    
                    String[][] data = {
                        {"Title", announcement.getTitle()},
                        {"Created", announcement.getFormattedCreatedAt()},
                        {"Updated", announcement.getFormattedUpdatedAt()}
                    };
                    
                    JPanel detailsPanel = buildDetailPanel(data, "Content", announcement.getContent());
                    JOptionPane.showMessageDialog(this, detailsPanel,
                        "Announcement Details", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error loading announcement details: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Open notifications
     */
    private void openNotifications() {
        NotificationPanel notificationPanel = new NotificationPanel(currentUser);
        notificationPanel.setVisible(true);
    }
    
    /**
     * Logout user
     */
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", "Logout Confirmation", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    LoginForm loginForm = new LoginForm();
                    loginForm.setVisible(true);
                }
            });
        }
    }
}
