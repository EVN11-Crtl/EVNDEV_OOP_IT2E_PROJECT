package com.matisense.dao;

import com.matisense.model.Announcement;
import com.matisense.exception.MatisenseException;
import com.matisense.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Announcement operations
 * Implements CRUD operations and encapsulates database logic
 */
public class AnnouncementDAO {
    private final DatabaseConfig dbConfig;
    
    /**
     * Constructor
     */
    public AnnouncementDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Create a new announcement
     * @param announcement Announcement object to create
     * @return Created announcement with ID
     * @throws MatisenseException if creation fails
     */
    public Announcement create(Announcement announcement) throws MatisenseException {
        if (!announcement.isValid()) {
            throw new MatisenseException("Invalid announcement data");
        }
        
        String sql = "INSERT INTO announcements (admin_id, title, content) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, announcement.getAdminId());
            pstmt.setString(2, announcement.getTitle());
            pstmt.setString(3, announcement.getContent());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new MatisenseException("Creating announcement failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    announcement.setId(generatedKeys.getInt(1));
                    return announcement;
                } else {
                    throw new MatisenseException("Creating announcement failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error creating announcement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find announcement by ID
     * @param id Announcement ID
     * @return Announcement object or null if not found
     * @throws MatisenseException if search fails
     */
    public Announcement findById(int id) throws MatisenseException {
        String sql = "SELECT * FROM announcements WHERE announcement_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnnouncement(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error finding announcement by ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all announcements
     * @return List of all announcements
     * @throws MatisenseException if retrieval fails
     */
    public List<Announcement> findAll() throws MatisenseException {
        String sql = "SELECT * FROM announcements ORDER BY created_at DESC";
        List<Announcement> announcements = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                announcements.add(mapResultSetToAnnouncement(rs));
            }
            
            return announcements;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding all announcements: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get recent announcements (limit)
     * @param limit Maximum number of announcements
     * @return List of recent announcements
     * @throws MatisenseException if retrieval fails
     */
    public List<Announcement> findRecent(int limit) throws MatisenseException {
        String sql = "SELECT * FROM announcements ORDER BY created_at DESC LIMIT ?";
        List<Announcement> announcements = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapResultSetToAnnouncement(rs));
                }
            }
            
            return announcements;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding recent announcements: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get announcements by admin ID
     * @param adminId Admin ID
     * @return List of announcements by admin
     * @throws MatisenseException if retrieval fails
     */
    public List<Announcement> findByAdminId(int adminId) throws MatisenseException {
        String sql = "SELECT * FROM announcements WHERE admin_id = ? ORDER BY created_at DESC";
        List<Announcement> announcements = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, adminId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapResultSetToAnnouncement(rs));
                }
            }
            
            return announcements;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding announcements by admin: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update announcement
     * @param announcement Announcement object to update
     * @return Updated announcement
     * @throws MatisenseException if update fails
     */
    public Announcement update(Announcement announcement) throws MatisenseException {
        if (!announcement.isValid() || announcement.getId() <= 0) {
            throw new MatisenseException("Invalid announcement data for update");
        }
        
        String sql = "UPDATE announcements SET admin_id = ?, title = ?, content = ?, " +
                    "updated_at = CURRENT_TIMESTAMP WHERE announcement_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, announcement.getAdminId());
            pstmt.setString(2, announcement.getTitle());
            pstmt.setString(3, announcement.getContent());
            pstmt.setInt(4, announcement.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new MatisenseException("Updating announcement failed, no rows affected.");
            }
            
            return announcement;
        } catch (SQLException e) {
            throw new MatisenseException("Error updating announcement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete announcement by ID
     * @param id Announcement ID
     * @return true if deleted, false otherwise
     * @throws MatisenseException if deletion fails
     */
    public boolean delete(int id) throws MatisenseException {
        String sql = "DELETE FROM announcements WHERE announcement_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error deleting announcement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get total count of announcements
     * @return Total count
     * @throws MatisenseException if count fails
     */
    public int getCount() throws MatisenseException {
        String sql = "SELECT COUNT(*) FROM announcements";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error counting announcements: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map ResultSet to Announcement object
     * @param rs ResultSet
     * @return Announcement object
     * @throws SQLException if mapping fails
     */
    private Announcement mapResultSetToAnnouncement(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setId(rs.getInt("announcement_id"));
        announcement.setAdminId(rs.getInt("admin_id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));
        
        // Set timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            announcement.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            announcement.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return announcement;
    }
}
