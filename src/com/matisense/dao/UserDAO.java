package com.matisense.dao;

import com.matisense.model.User;
import com.matisense.exception.MatisenseException;
import com.matisense.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations
 * Implements CRUD operations and encapsulates database logic
 */
public class UserDAO {
    private final DatabaseConfig dbConfig;
    
    /**
     * Constructor
     */
    public UserDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Create a new user
     * @param user User object to create
     * @return Created user with ID
     * @throws MatisenseException if creation fails
     */
    public User create(User user) throws MatisenseException {
        if (!user.isValid()) {
            throw new MatisenseException("Invalid user data");
        }
        
        String sql = "INSERT INTO users (username, password, full_name, address, gender, email, " +
                    "contact_number, birthday, user_role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getAddress());
            pstmt.setString(5, user.getGender());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, user.getContactNumber());
            pstmt.setString(8, user.getBirthday());
            pstmt.setString(9, user.getUserRole().getDisplayName());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new MatisenseException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                    return user;
                } else {
                    throw new MatisenseException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new MatisenseException("Username or email already exists", "DUPLICATE_ENTRY");
            }
            throw new MatisenseException("Error creating user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update password for user
     * @param userId User ID
     * @param newPassword New password
     * @return true if updated successfully
     * @throws MatisenseException if update fails
     */
    public boolean updatePassword(int userId, String newPassword) throws MatisenseException {
        String sql = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error updating password: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find user by ID
     * @param id User ID
     * @return User object or null if not found
     * @throws MatisenseException if search fails
     */
    public User findById(int id) throws MatisenseException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error finding user by ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find user by username
     * @param username Username
     * @return User object or null if not found
     * @throws MatisenseException if search fails
     */
    public User findByUsername(String username) throws MatisenseException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error finding user by username: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find user by email
     * @param email Email
     * @return User object or null if not found
     * @throws MatisenseException if search fails
     */
    public User findByEmail(String email) throws MatisenseException {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error finding user by email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Authenticate user
     * @param username Username
     * @param password Password
     * @return User object if authenticated, null otherwise
     * @throws MatisenseException if authentication fails
     */
    public User authenticate(String username, String password) throws MatisenseException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error authenticating user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update user
     * @param user User object to update
     * @return Updated user
     * @throws MatisenseException if update fails
     */
    public User update(User user) throws MatisenseException {
        if (!user.isValid() || user.getId() <= 0) {
            throw new MatisenseException("Invalid user data for update");
        }
        
        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, address = ?, " +
                    "gender = ?, email = ?, contact_number = ?, birthday = ?, user_role = ?, " +
                    "updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getAddress());
            pstmt.setString(5, user.getGender());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, user.getContactNumber());
            pstmt.setString(8, user.getBirthday());
            pstmt.setString(9, user.getUserRole().getDisplayName());
            pstmt.setInt(10, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new MatisenseException("Updating user failed, no rows affected.");
            }
            
            return user;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new MatisenseException("Username or email already exists", "DUPLICATE_ENTRY");
            }
            throw new MatisenseException("Error updating user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete user by ID
     * @param id User ID
     * @return true if deleted, false otherwise
     * @throws MatisenseException if deletion fails
     */
    public boolean delete(int id) throws MatisenseException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error deleting user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all users
     * @return List of all users
     * @throws MatisenseException if retrieval fails
     */
    public List<User> findAll() throws MatisenseException {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
            return users;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding all users: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get users by role
     * @param role User role
     * @return List of users with specified role
     * @throws MatisenseException if retrieval fails
     */
    public List<User> findByRole(User.UserRole role) throws MatisenseException {
        String sql = "SELECT * FROM users WHERE user_role = ? ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role.getDisplayName());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            
            return users;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding users by role: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map ResultSet to User object
     * @param rs ResultSet
     * @return User object
     * @throws SQLException if mapping fails
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setAddress(rs.getString("address"));
        user.setGender(rs.getString("gender"));
        user.setEmail(rs.getString("email"));
        user.setContactNumber(rs.getString("contact_number"));
        user.setBirthday(rs.getString("birthday"));
        user.setUserRole(User.UserRole.fromString(rs.getString("user_role")));
        
        // Set timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }
}
