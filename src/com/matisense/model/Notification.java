package com.matisense.model;

import com.matisense.util.ValidationUtil;
import java.time.LocalDateTime;

/**
 * Notification model class representing system notifications
 * Implements encapsulation and validation
 */
public class Notification {
    private int id;
    private int userId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private Integer relatedId;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Enum for notification types
     */
    public enum NotificationType {
        REPORT("Report"),
        ANNOUNCEMENT("Announcement"),
        SYSTEM("System");
        
        private final String displayName;
        
        NotificationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static NotificationType fromString(String type) {
            if (type != null) {
                for (NotificationType nt : NotificationType.values()) {
                    if (nt.displayName.equalsIgnoreCase(type) || nt.name().equalsIgnoreCase(type)) {
                        return nt;
                    }
                }
            }
            return SYSTEM; // Default
        }
    }
    
    /**
     * Default constructor
     */
    public Notification() {
        this.isRead = false;
        this.notificationType = NotificationType.SYSTEM;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with basic info
     * @param userId User ID
     * @param title Title
     * @param message Message
     * @param notificationType Type of notification
     */
    public Notification(int userId, String title, String message, NotificationType notificationType) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Full constructor
     * @param id Notification ID
     * @param userId User ID
     * @param title Title
     * @param message Message
     * @param notificationType Type of notification
     * @param relatedId Related entity ID
     * @param isRead Read status
     */
    public Notification(int id, int userId, String title, String message, 
                       NotificationType notificationType, Integer relatedId, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.relatedId = relatedId;
        this.isRead = isRead;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public Integer getRelatedId() {
        return relatedId;
    }
    
    public void setRelatedId(Integer relatedId) {
        this.relatedId = relatedId;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Update timestamp
     */
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Validate notification data
     * @return true if valid
     */
    public boolean isValid() {
        return userId > 0 &&
               ValidationUtil.isNotEmpty(title) &&
               ValidationUtil.isNotEmpty(message) &&
               ValidationUtil.isValidLength(title, 200);
    }
    
    /**
     * Mark as read
     */
    public void markAsRead() {
        this.isRead = true;
        updateTimestamp();
    }
    
    /**
     * Mark as unread
     */
    public void markAsUnread() {
        this.isRead = false;
        updateTimestamp();
    }
    
    /**
     * Check if notification is for a report
     * @return true if report notification, false otherwise
     */
    public boolean isReportNotification() {
        return notificationType == NotificationType.REPORT;
    }
    
    /**
     * Check if notification is for an announcement
     * @return true if announcement notification, false otherwise
     */
    public boolean isAnnouncementNotification() {
        return notificationType == NotificationType.ANNOUNCEMENT;
    }
    
    /**
     * Check if notification is system notification
     * @return true if system notification, false otherwise
     */
    public boolean isSystemNotification() {
        return notificationType == NotificationType.SYSTEM;
    }
    
    /**
     * Get truncated message for preview
     * @param maxLength Maximum length
     * @return Truncated message
     */
    public String getTruncatedMessage(int maxLength) {
        if (message == null) {
            return "";
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...";
    }
    
    /**
     * Get formatted created at date
     * @return Formatted date string
     */
    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "";
        }
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    @Override
    public String toString() {
        return String.format("Notification[id=%d, userId=%d, title='%s', type='%s', read=%s]", 
                           id, userId, title, notificationType.getDisplayName(), isRead);
    }
}
