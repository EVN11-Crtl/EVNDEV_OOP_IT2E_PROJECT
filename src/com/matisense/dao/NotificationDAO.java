package com.matisense.dao;

import com.matisense.model.Notification;
import com.matisense.exception.MatisenseException;
import com.matisense.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Notification operations
 * Implements CRUD operations and encapsulates database logic
 */
public class NotificationDAO {
    private final DatabaseConfig dbConfig;
    
    /**
     * Constructor
     */
    public NotificationDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Create a new notification
     * @param notification Notification object to create
     * @return Created notification with ID
     * @throws MatisenseException if creation fails
     */
    public Notification create(Notification notification) throws MatisenseException {
        if (!notification.isValid()) {
            throw new MatisenseException("Invalid notification data");
        }
        
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, related_id, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getTitle());
            pstmt.setString(3, notification.getMessage());
            pstmt.setString(4, notification.getNotificationType().getDisplayName());
            
            if (notification.getRelatedId() != null) {
                pstmt.setInt(5, notification.getRelatedId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            pstmt.setBoolean(6, notification.isRead());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new MatisenseException("Creating notification failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                    return notification;
                } else {
                    throw new MatisenseException("Creating notification failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error creating notification: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find notification by ID
     * @param id Notification ID
     * @return Notification object or null if not found
     * @throws MatisenseException if search fails
     */
    public Notification findById(int id) throws MatisenseException {
        String sql = "SELECT * FROM notifications WHERE notification_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNotification(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error finding notification by ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get notifications by user ID
     * @param userId User ID
     * @return List of notifications for user
     * @throws MatisenseException if retrieval fails
     */
    public List<Notification> findByUserId(int userId) throws MatisenseException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
            
            return notifications;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding notifications by user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get unread notifications by user ID
     * @param userId User ID
     * @return List of unread notifications for user
     * @throws MatisenseException if retrieval fails
     */
    public List<Notification> findUnreadByUserId(int userId) throws MatisenseException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = false ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
            
            return notifications;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding unread notifications: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all notifications
     * @return List of all notifications
     * @throws MatisenseException if retrieval fails
     */
    public List<Notification> findAll() throws MatisenseException {
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
            
            return notifications;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding all notifications: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update notification
     * @param notification Notification object to update
     * @return Updated notification
     * @throws MatisenseException if update fails
     */
    public Notification update(Notification notification) throws MatisenseException {
        if (!notification.isValid() || notification.getId() <= 0) {
            throw new MatisenseException("Invalid notification data for update");
        }
        
        String sql = "UPDATE notifications SET user_id = ?, title = ?, message = ?, " +
                    "notification_type = ?, related_id = ?, is_read = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE notification_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getTitle());
            pstmt.setString(3, notification.getMessage());
            pstmt.setString(4, notification.getNotificationType().getDisplayName());
            
            if (notification.getRelatedId() != null) {
                pstmt.setInt(5, notification.getRelatedId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            pstmt.setBoolean(6, notification.isRead());
            pstmt.setInt(7, notification.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new MatisenseException("Updating notification failed, no rows affected.");
            }
            
            return notification;
        } catch (SQLException e) {
            throw new MatisenseException("Error updating notification: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mark notification as read
     * @param notificationId Notification ID
     * @return true if updated, false otherwise
     * @throws MatisenseException if update fails
     */
    public boolean markAsRead(int notificationId) throws MatisenseException {
        String sql = "UPDATE notifications SET is_read = true, updated_at = CURRENT_TIMESTAMP WHERE notification_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notificationId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error marking notification as read: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mark all notifications for user as read
     * @param userId User ID
     * @return true if updated, false otherwise
     * @throws MatisenseException if update fails
     */
    public boolean markAllAsReadForUser(int userId) throws MatisenseException {
        String sql = "UPDATE notifications SET is_read = true, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error marking all notifications as read: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete notification by ID
     * @param id Notification ID
     * @return true if deleted, false otherwise
     * @throws MatisenseException if deletion fails
     */
    public boolean delete(int id) throws MatisenseException {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error deleting notification: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get unread count for user
     * @param userId User ID
     * @return Count of unread notifications
     * @throws MatisenseException if count fails
     */
    public int getUnreadCount(int userId) throws MatisenseException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error counting unread notifications: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create notification for all users
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type
     * @param relatedId Related entity ID
     * @throws MatisenseException if creation fails
     */
    public void createForAllUsers(String title, String message, Notification.NotificationType type, Integer relatedId) 
            throws MatisenseException {
        // This would require getting all users and creating notifications for each
        // For simplicity, we'll implement a basic version
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, related_id, is_read) " +
                    "SELECT user_id, ?, ?, ?, ?, false FROM users";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, message);
            pstmt.setString(3, type.getDisplayName());
            
            if (relatedId != null) {
                pstmt.setInt(4, relatedId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MatisenseException("Error creating notifications for all users: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map ResultSet to Notification object
     * @param rs ResultSet
     * @return Notification object
     * @throws SQLException if mapping fails
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setNotificationType(Notification.NotificationType.fromString(rs.getString("notification_type")));
        
        Integer relatedId = rs.getInt("related_id");
        if (rs.wasNull()) {
            relatedId = null;
        }
        notification.setRelatedId(relatedId);
        
        notification.setRead(rs.getBoolean("is_read"));
        
        // Set timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            notification.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            notification.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return notification;
    }
}
