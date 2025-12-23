package com.matisense.ui;

import com.matisense.model.User;
import com.matisense.model.Notification;
import com.matisense.model.Report;
import com.matisense.dao.NotificationDAO;
import com.matisense.dao.ReportDAO;
import com.matisense.exception.MatisenseException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Notification Panel for viewing and managing notifications
 * Implements notification viewing and management for both admin and resident
 * users
 * Panel ng notifications kung saan nakikita at mina-manage ng admin at resident
 * ang mga abiso.
 */
public class NotificationPanel extends JFrame {
    private static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(140, 32);
    private static final Color TABLE_HEADER = new Color(227, 242, 253);
    private static final Color LIGHT_BG = new Color(245, 247, 250);
    private User currentUser;
    private NotificationDAO notificationDAO;
    private ReportDAO reportDAO;

    // UI Components
    private JTable notificationsTable;
    private DefaultTableModel notificationsTableModel;
    private JButton markAsReadButton;
    private JButton markAllAsReadButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JLabel unreadCountLabel;

    /**
     * Constructor
     * 
     * @param currentUser Currently logged-in user
     * 
     *                    Ginagamit para buksan ang notification window ng
     *                    kasalukuyang user (admin o resident).
     */
    public NotificationPanel(User currentUser) {
        this.currentUser = currentUser;
        this.notificationDAO = new NotificationDAO();
        this.reportDAO = new ReportDAO();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
        loadNotifications();
        updateUnreadCount();
    }

    /**
     * Initialize UI components
     * 
     * Inihahanda ang mga table, button, at label na gagamitin sa notification
     * window.
     */
    private void initializeComponents() {
        notificationsTableModel = new DefaultTableModel(new String[] {
                "ID", "Title", "Message", "Type", "Read", "Created At"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        notificationsTable = new JTable(notificationsTableModel);
        notificationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationsTable.getTableHeader().setReorderingAllowed(false);
        styleTable(notificationsTable);

        // Configure table columns
        notificationsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        notificationsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        notificationsTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        notificationsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        notificationsTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        notificationsTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        markAsReadButton = new JButton("Mark as Read");
        markAllAsReadButton = new JButton("Mark All as Read");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");

        applyButtonTheme(markAsReadButton, markAllAsReadButton, deleteButton, refreshButton, closeButton);
        unreadCountLabel = new JLabel();
        unreadCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void applyButtonTheme(JButton... buttons) {
        for (JButton button : buttons) {
            if (button != null) {
                button.setFocusPainted(false);
                button.setPreferredSize(DEFAULT_BUTTON_SIZE);
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
                button.setOpaque(true);
            }
        }
    }

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

    private JPanel buildDetailPanel(Notification notification) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(460, 260));

        // Always use the stored notification title and message so it matches
        // what is saved in the database
        String titleToShow = notification.getTitle();
        String messageText = notification.getMessage();

        String[][] data;

        // For report notifications, try to show complete report details in the
        // upper table using the related Report record
        if (notification.isReportNotification() && notification.getRelatedId() != null) {
            String reportId = String.valueOf(notification.getRelatedId());
            String reportType = "";
            String location = "";
            String reportStatus = "";
            String reportCreated = "";
            String reportUpdated = "";

            try {
                Report report = reportDAO.findById(notification.getRelatedId());
                if (report != null) {
                    reportId = String.valueOf(report.getId());
                    if (report.getReportType() != null) {
                        reportType = report.getReportType();
                    }
                    if (report.getLocation() != null) {
                        location = report.getLocation();
                    }
                    if (report.getStatus() != null) {
                        reportStatus = report.getStatus().getDisplayName();
                    }
                    if (report.getFormattedCreatedAt() != null) {
                        reportCreated = report.getFormattedCreatedAt();
                    }
                    if (report.getFormattedUpdatedAt() != null) {
                        reportUpdated = report.getFormattedUpdatedAt();
                    }
                }
            } catch (MatisenseException ignored) {
                // Fall back to notification-only details if report loading fails
            }

            data = new String[][] {
                    { "Notification ID", String.valueOf(notification.getId()) },
                    { "Title", titleToShow },
                    { "Type", notification.getNotificationType().getDisplayName() },
                    { "Status", notification.isRead() ? "Read" : "Unread" },
                    { "Created", notification.getFormattedCreatedAt() },
                    { "Report ID", reportId },
                    { "Report Type", reportType },
                    { "Location", location },
                    { "Report Status", reportStatus },
                    { "Report Created", reportCreated },
                    { "Report Updated", reportUpdated }
            };
        } else {
            data = new String[][] {
                    { "Notification ID", String.valueOf(notification.getId()) },
                    { "Title", titleToShow },
                    { "Type", notification.getNotificationType().getDisplayName() },
                    { "Status", notification.isRead() ? "Read" : "Unread" },
                    { "Created", notification.getFormattedCreatedAt() }
            };
        }

        String[] columnNames = { "Field", "Value" };
        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(440, 140));

        JTextArea messageArea = new JTextArea(messageText != null ? messageText : "");
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setBorder(BorderFactory.createTitledBorder("Message"));
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(440, 110));

        panel.add(tableScroll);
        panel.add(Box.createVerticalStrut(8));
        panel.add(messageScroll);
        return panel;
    }

    /**
     * Setup layout
     * 
     * Inaayos ang pagkaka-layout ng header, notification table, at mga action
     * buttons sa frame.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Notifications - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(unreadCountLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(markAsReadButton);
        buttonPanel.add(markAllAsReadButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(notificationsTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Setup event handlers
     * 
     * Dinidikit ang actions ng mga button at double-click sa table para gumana ang
     * features.
     */
    private void setupEventHandlers() {
        markAsReadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markSelectedAsRead();
            }
        });

        markAllAsReadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAllAsRead();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedNotification();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadNotifications();
                updateUnreadCount();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Double-click to view details
        notificationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewNotificationDetails();
                }
            }
        });
    }

    /**
     * Configure frame properties
     * 
     * Nagseset ng title, default close operation, at size ng notification window.
     */
    private void configureFrame() {
        setTitle("Notifications - Matisense Community Report System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
    }

    /**
     * Load notifications into table
     * 
     * Kinukuha ang notifications ng kasalukuyang user at ipinapakita sa table.
     */
    private void loadNotifications() {
        try {
            List<Notification> notifications = notificationDAO.findByUserId(currentUser.getId());
            notificationsTableModel.setRowCount(0);

            for (Notification notification : notifications) {
                // Use the stored title and message preview directly so it matches
                // the content saved in the notifications table
                String title = notification.getTitle();
                String messagePreview = notification.getTruncatedMessage(80);

                Object[] row = {
                        notification.getId(),
                        title,
                        messagePreview,
                        notification.getNotificationType().getDisplayName(),
                        notification.isRead() ? "Yes" : "No",
                        notification.getFormattedCreatedAt()
                };

                notificationsTableModel.addRow(row);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Update unread count display
     * 
     * Ina-update ang bilang ng unread notifications at binabago ang kulay ng label
     * depende sa count.
     */
    private void updateUnreadCount() {
        try {
            int unreadCount = notificationDAO.getUnreadCount(currentUser.getId());
            unreadCountLabel.setText("Unread: " + unreadCount);

            if (unreadCount > 0) {
                unreadCountLabel.setForeground(Color.RED);
            } else {
                unreadCountLabel.setForeground(Color.BLACK);
            }
        } catch (MatisenseException e) {
            unreadCountLabel.setText("Unread count unavailable");
        }
    }

    /**
     * Mark selected notification as read
     * 
     * Ginagamit kapag may isang napiling notification na gustong itatak bilang
     * nabasa.
     */
    private void markSelectedAsRead() {
        int selectedRow = notificationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification to mark as read",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int notificationId = (Integer) notificationsTableModel.getValueAt(selectedRow, 0);
        Boolean isRead = "Yes".equals(notificationsTableModel.getValueAt(selectedRow, 4));

        if (isRead) {
            JOptionPane.showMessageDialog(this, "This notification is already marked as read",
                    "Already Read", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            boolean success = notificationDAO.markAsRead(notificationId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Notification marked as read",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadNotifications();
                updateUnreadCount();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to mark notification as read",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error marking notification as read: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mark all notifications as read
     * 
     * Minamark lahat ng notifications ng user bilang nabasa sa isang click.
     */
    private void markAllAsRead() {
        try {
            boolean success = notificationDAO.markAllAsReadForUser(currentUser.getId());
            if (success) {
                JOptionPane.showMessageDialog(this, "All notifications marked as read",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadNotifications();
                updateUnreadCount();
            } else {
                JOptionPane.showMessageDialog(this, "No notifications to mark as read",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error marking all notifications as read: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete selected notification
     * 
     * Binubura ang napiling notification matapos kumpirmahin ng user.
     */
    private void deleteSelectedNotification() {
        int selectedRow = notificationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int notificationId = (Integer) notificationsTableModel.getValueAt(selectedRow, 0);
        String title = (String) notificationsTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this notification?\n\"" + title + "\"",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = notificationDAO.delete(notificationId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Notification deleted successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadNotifications();
                    updateUnreadCount();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete notification",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (MatisenseException e) {
                JOptionPane.showMessageDialog(this, "Error deleting notification: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * View notification details
     * 
     * Ipinapakita ang buong detalye ng napiling notification sa isang dialog.
     */
    private void viewNotificationDetails() {
        int selectedRow = notificationsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int notificationId = (Integer) notificationsTableModel.getValueAt(selectedRow, 0);

        try {
            Notification notification = notificationDAO.findById(notificationId);
            if (notification != null) {
                JPanel detailsPanel = buildDetailPanel(notification);

                JOptionPane.showMessageDialog(this, detailsPanel,
                        "Notification Details", JOptionPane.INFORMATION_MESSAGE);

                if (!notification.isRead()) {
                    notificationDAO.markAsRead(notificationId);
                    loadNotifications();
                    updateUnreadCount();
                }
            }
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Error loading notification details: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
