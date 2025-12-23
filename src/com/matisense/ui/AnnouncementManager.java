
package com.matisense.ui;

import com.matisense.dao.AnnouncementDAO;
import com.matisense.exception.MatisenseException;
import com.matisense.model.Announcement;
import com.matisense.model.User;
import com.matisense.service.NotificationService;
import com.matisense.util.ValidationUtil;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Announcement Manager for admin announcement operations
 * Implements CRUD operations for announcements
 */
public class AnnouncementManager extends JFrame {
    private final User currentUser;
    private final AnnouncementDAO announcementDAO;
    private final NotificationService notificationService;

    // UI Components
    private JTable announcementsTable;
    private DefaultTableModel announcementsTableModel;
    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton closeButton;

    /**
     * Constructor
     * 
     * @param currentUser Currently logged-in admin user
     */
    public AnnouncementManager(User currentUser) {
        this.currentUser = currentUser;
        this.announcementDAO = new AnnouncementDAO();
        this.notificationService = new NotificationService();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
        loadAnnouncements();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        announcementsTableModel = new DefaultTableModel(new String[] {
                "ID", "Title", "Content Preview", "Created At", "Updated At"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        announcementsTable = new JTable(announcementsTableModel);
        announcementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        announcementsTable.getTableHeader().setReorderingAllowed(false);

        // Configure table columns
        announcementsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        announcementsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        announcementsTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        announcementsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        announcementsTable.getColumnModel().getColumn(4).setPreferredWidth(150);

        createButton = new JButton("Create New");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");

    }

    /**
     * Setup layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        // Main panel with table
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(announcementsTable), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        createButton.addActionListener(e -> createNewAnnouncement());

        editButton.addActionListener(e -> editSelectedAnnouncement());

        deleteButton.addActionListener(e -> deleteSelectedAnnouncement());

        refreshButton.addActionListener(e -> loadAnnouncements());

        closeButton.addActionListener(e -> dispose());

        // Double-click to edit
        announcementsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedAnnouncement();
                }
            }
        });
    }

    /**
     * Configure frame properties
     */
    private void configureFrame() {
        setTitle("Announcement Manager - Matisense Community Report System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    /**
     * Load announcements into table
     */
    private void loadAnnouncements() {
        try {
            List<Announcement> announcements = announcementDAO.findAll();
            announcementsTableModel.setRowCount(0);

            for (Announcement announcement : announcements) {
                Object[] row = {
                        announcement.getId(),
                        announcement.getTitle(),
                        announcement.getTruncatedContent(100),
                        announcement.getFormattedCreatedAt(),
                        announcement.getFormattedUpdatedAt()
                };

                announcementsTableModel.addRow(row);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error loading announcements: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Create new announcement
     */
    private void createNewAnnouncement() {
        showAnnouncementDialog(null);
    }

    /**
     * Edit selected announcement
     */
    private void editSelectedAnnouncement() {
        int selectedRow = announcementsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an announcement to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int announcementId = (Integer) announcementsTableModel.getValueAt(selectedRow, 0);

        try {
            Announcement announcement = announcementDAO.findById(announcementId);
            if (announcement != null) {
                showAnnouncementDialog(announcement);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error loading announcement: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show dialog for creating or editing announcement
     */
    private void showAnnouncementDialog(Announcement announcementToEdit) {
        boolean isEditMode = announcementToEdit != null;

        JDialog dialog = new JDialog(this, isEditMode ? "Edit Announcement" : "Create Announcement", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
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
                JOptionPane.showMessageDialog(dialog, "Title is required", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtil.isValidLength(title, 200)) {
                JOptionPane.showMessageDialog(dialog, "Title must be 200 characters or less", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtil.isNotEmpty(content)) {
                JOptionPane.showMessageDialog(dialog, "Content is required", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (isEditMode) {
                    announcementToEdit.setTitle(title);
                    announcementToEdit.setContent(content);
                    announcementDAO.update(announcementToEdit);
                    JOptionPane.showMessageDialog(dialog, "Announcement updated successfully",
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
                    JOptionPane.showMessageDialog(dialog, "Announcement created successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                loadAnnouncements();
                dialog.dispose();
            } catch (MatisenseException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving announcement: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * Delete selected announcement
     */
    private void deleteSelectedAnnouncement() {
        int selectedRow = announcementsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an announcement to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int announcementId = (Integer) announcementsTableModel.getValueAt(selectedRow, 0);
        String title = (String) announcementsTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the announcement:\n\"" + title + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = announcementDAO.delete(announcementId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Announcement deleted successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAnnouncements();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete announcement",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (MatisenseException e) {
                JOptionPane.showMessageDialog(this, "Error deleting announcement: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
