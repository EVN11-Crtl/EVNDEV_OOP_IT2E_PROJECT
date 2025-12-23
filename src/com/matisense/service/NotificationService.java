package com.matisense.service;

import com.matisense.dao.NotificationDAO;
import com.matisense.dao.UserDAO;
import com.matisense.exception.MatisenseException;
import com.matisense.model.Announcement;
import com.matisense.model.Notification;
import com.matisense.model.Report;
import com.matisense.model.User;

/**
 * Notification Service for managing system notifications
 * Implements notification creation and management logic
 * Service layer na namamahala sa paglikha at pag-update ng mga notification sa
 * buong sistema.
 */
public class NotificationService {
    private final NotificationDAO notificationDAO;
    private final UserDAO userDAO;

    /**
     * Constructor
     * 
     * Nag-iinitialize ng NotificationDAO at UserDAO na gagamitin sa mga operasyon.
     */
    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Create notification for new report submission (for admin)
     * 
     * @param report Submitted report
     * @throws MatisenseException if notification creation fails
     * 
     *                            Gumagawa ng notification para sa lahat ng admin
     *                            tuwing may bagong report na naisusumite.
     */
    public void notifyNewReportSubmission(Report report) throws MatisenseException {
        try {
            // Get all admin users
            java.util.List<User> admins = userDAO.findByRole(User.UserRole.ADMIN);

            for (User admin : admins) {
                String title = report.getReportType() != null ? report.getReportType() : "Report";
                String location = report.getLocation() != null ? report.getLocation() : "Unspecified";
                String description = report.getDescription() != null ? report.getDescription()
                        : "No description provided.";

                // Detailed message for admins about the new report
                String message = String.format(
                        "New report submitted by Resident #%d%n" +
                                "Type: %s%n" +
                                "Location: %s%n%n" +
                                "Description:%n%s",
                        report.getResidentId(),
                        report.getReportType(),
                        location,
                        description);

                // Store detailed report data in the notification record
                Notification notification = new Notification(
                        admin.getId(),
                        title,
                        message,
                        Notification.NotificationType.REPORT);
                notification.setRelatedId(report.getId());
                notificationDAO.create(notification);
            }
        } catch (MatisenseException e) {
            throw new MatisenseException("Error creating report submission notifications: " + e.getMessage(), e);
        }
    }

    /**
     * Create notification for report status update (for resident)
     * 
     * @param report    Updated report
     * @param oldStatus Previous status
     * @throws MatisenseException if notification creation fails
     * 
     *                            Pinapadalhan ang resident ng notification kapag
     *                            nagbago ang status ng kanyang report.
     */
    public void notifyReportStatusUpdate(Report report, Report.ReportStatus oldStatus) throws MatisenseException {
        try {
            String title = report.getReportType() != null ? report.getReportType() : "Report";
            String location = report.getLocation() != null ? report.getLocation() : "Unspecified";
            String description = report.getDescription() != null ? report.getDescription() : "No description provided.";

            // Detailed message for resident about status change
            String message = String.format(
                    "Your report status has been updated.%n" +
                            "Type: %s%n" +
                            "Location: %s%n" +
                            "Old Status: %s%n" +
                            "New Status: %s%n%n" +
                            "Description:%n%s",
                    report.getReportType(),
                    location,
                    oldStatus != null ? oldStatus.getDisplayName() : "-",
                    report.getStatus() != null ? report.getStatus().getDisplayName() : "-",
                    description);

            // Store detailed report data in the notification record
            Notification notification = new Notification(
                    report.getResidentId(),
                    title,
                    message,
                    Notification.NotificationType.REPORT);
            notification.setRelatedId(report.getId());
            notificationDAO.create(notification);
        } catch (MatisenseException e) {
            throw new MatisenseException("Error creating report status update notification: " + e.getMessage(), e);
        }
    }

    /**
     * Create notification for new announcement (for all residents)
     * 
     * @param announcement New announcement
     * @throws MatisenseException if notification creation fails
     * 
     *                            Nagpapadala ng notification sa lahat ng resident
     *                            kapag may bagong announcement.
     */
    public void notifyNewAnnouncement(Announcement announcement) throws MatisenseException {
        try {
            // Get all resident users
            java.util.List<User> residents = userDAO.findByRole(User.UserRole.RESIDENT);

            for (User resident : residents) {
                Notification notification = new Notification(
                        resident.getId(),
                        "New Announcement",
                        String.format("New announcement: %s", announcement.getTitle()),
                        Notification.NotificationType.ANNOUNCEMENT);
                notification.setRelatedId(announcement.getId());
                notificationDAO.create(notification);
            }
        } catch (MatisenseException e) {
            throw new MatisenseException("Error creating announcement notifications: " + e.getMessage(), e);
        }
    }

    /**
     * Create system notification for all users
     * 
     * @param title   Notification title
     * @param message Notification message
     * @throws MatisenseException if notification creation fails
     * 
     *                            Gumagawa ng generic/system notification para sa
     *                            lahat ng users.
     */
    public void notifyAllUsers(String title, String message) throws MatisenseException {
        try {
            notificationDAO.createForAllUsers(title, message, Notification.NotificationType.SYSTEM, null);
        } catch (MatisenseException e) {
            throw new MatisenseException("Error creating system notifications: " + e.getMessage(), e);
        }
    }

    /**
     * Get unread notification count for user
     * 
     * @param userId User ID
     * @return Unread count
     * @throws MatisenseException if count fails
     * 
     *                            Kinukuha kung ilan pang notifications ang hindi
     *                            nababasa ng isang user.
     */
    public int getUnreadCount(int userId) throws MatisenseException {
        return notificationDAO.getUnreadCount(userId);
    }

    /**
     * Mark notification as read
     * 
     * @param notificationId Notification ID
     * @return true if successful, false otherwise
     * @throws MatisenseException if update fails
     * 
     *                            Tinatawag kapag gusto nating i-update sa database
     *                            na nabasa na ang isang notification.
     */
    public boolean markAsRead(int notificationId) throws MatisenseException {
        return notificationDAO.markAsRead(notificationId);
    }
}
