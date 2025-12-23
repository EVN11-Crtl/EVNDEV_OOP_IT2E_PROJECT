package com.matisense.dao;

import com.matisense.model.Report;
import com.matisense.exception.MatisenseException;
import com.matisense.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Report operations
 * Implements CRUD operations and encapsulates database logic
 */
public class ReportDAO {
    private final DatabaseConfig dbConfig;
    
    /**
     * Constructor
     */
    public ReportDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Create a new report
     * @param report Report object to create
     * @return Created report with ID
     * @throws MatisenseException if creation fails
     */
    public Report create(Report report) throws MatisenseException {
        if (!report.isValid()) {
            throw new MatisenseException("Invalid report data");
        }
        
        String sql = "INSERT INTO reports (resident_id, report_type, location, description, status) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, report.getResidentId());
            pstmt.setString(2, report.getReportType());
            pstmt.setString(3, report.getLocation());
            pstmt.setString(4, report.getDescription());
            pstmt.setString(5, report.getStatus().getDisplayName());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new MatisenseException("Creating report failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    report.setId(generatedKeys.getInt(1));
                    return report;
                } else {
                    throw new MatisenseException("Creating report failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error creating report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find report by ID
     * @param id Report ID
     * @return Report object or null if not found
     * @throws MatisenseException if search fails
     */
    public Report findById(int id) throws MatisenseException {
        String sql = "SELECT * FROM reports WHERE report_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReport(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error finding report by ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all reports
     * @return List of all reports
     * @throws MatisenseException if retrieval fails
     */
    public List<Report> findAll() throws MatisenseException {
        String sql = "SELECT * FROM reports ORDER BY created_at DESC";
        List<Report> reports = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
            
            return reports;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding all reports: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get reports by resident ID
     * @param residentId Resident ID
     * @return List of reports by resident
     * @throws MatisenseException if retrieval fails
     */
    public List<Report> findByResidentId(int residentId) throws MatisenseException {
        String sql = "SELECT * FROM reports WHERE resident_id = ? ORDER BY created_at DESC";
        List<Report> reports = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, residentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
            
            return reports;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding reports by resident: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get reports by status
     * @param status Report status
     * @return List of reports with specified status
     * @throws MatisenseException if retrieval fails
     */
    public List<Report> findByStatus(Report.ReportStatus status) throws MatisenseException {
        String sql = "SELECT * FROM reports WHERE status = ? ORDER BY created_at DESC";
        List<Report> reports = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.getDisplayName());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
            
            return reports;
        } catch (SQLException e) {
            throw new MatisenseException("Error finding reports by status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update report
     * @param report Report object to update
     * @return Updated report
     * @throws MatisenseException if update fails
     */
    public Report update(Report report) throws MatisenseException {
        if (!report.isValid() || report.getId() <= 0) {
            throw new MatisenseException("Invalid report data for update");
        }
        
        String sql = "UPDATE reports SET resident_id = ?, report_type = ?, location = ?, " +
                    "description = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE report_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, report.getResidentId());
            pstmt.setString(2, report.getReportType());
            pstmt.setString(3, report.getLocation());
            pstmt.setString(4, report.getDescription());
            pstmt.setString(5, report.getStatus().getDisplayName());
            pstmt.setInt(6, report.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new MatisenseException("Updating report failed, no rows affected.");
            }
            
            return report;
        } catch (SQLException e) {
            throw new MatisenseException("Error updating report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update report status
     * @param reportId Report ID
     * @param status New status
     * @return true if updated, false otherwise
     * @throws MatisenseException if update fails
     */
    public boolean updateStatus(int reportId, Report.ReportStatus status) throws MatisenseException {
        String sql = "UPDATE reports SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE report_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.getDisplayName());
            pstmt.setInt(2, reportId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error updating report status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete report by ID
     * @param id Report ID
     * @return true if deleted, false otherwise
     * @throws MatisenseException if deletion fails
     */
    public boolean delete(int id) throws MatisenseException {
        String sql = "DELETE FROM reports WHERE report_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new MatisenseException("Error deleting report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get report count by status
     * @param status Report status
     * @return Count of reports with specified status
     * @throws MatisenseException if count fails
     */
    public int getCountByStatus(Report.ReportStatus status) throws MatisenseException {
        String sql = "SELECT COUNT(*) FROM reports WHERE status = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.getDisplayName());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new MatisenseException("Error counting reports by status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map ResultSet to Report object
     * @param rs ResultSet
     * @return Report object
     * @throws SQLException if mapping fails
     */
    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setId(rs.getInt("report_id"));
        report.setResidentId(rs.getInt("resident_id"));
        report.setReportType(rs.getString("report_type"));
        report.setLocation(rs.getString("location"));
        report.setDescription(rs.getString("description"));
        report.setStatus(Report.ReportStatus.fromString(rs.getString("status")));
        
        // Set timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            report.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            report.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return report;
    }
}
