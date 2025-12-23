package com.matisense.model;

import com.matisense.util.ValidationUtil;

/**
 * Report model class representing community reports
 * Extends BaseEntity and implements encapsulation
 */
public class Report extends BaseEntity {
    private int residentId;
    private String reportType;
    private String location;
    private String description;
    private ReportStatus status;
    
    /**
     * Enum for report status
     */
    public enum ReportStatus {
        PENDING("Pending"),
        IN_REVIEW("In Review"),
        APPROVED("Approved"),
        IN_PROGRESS("In Progress"),
        RESOLVED("Resolved"),
        COMPLETED("Completed");
        
        private final String displayName;
        
        ReportStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static ReportStatus fromString(String status) {
            if (status == null) {
                return PENDING;
            }
            
            String normalized = status.trim().toLowerCase();
            
            if (normalized.startsWith("pending")) {
                return PENDING;
            } else if (normalized.startsWith("in review")) {
                return IN_REVIEW;
            } else if (normalized.startsWith("approved")) {
                return APPROVED;
            } else if (normalized.startsWith("in progress")) {
                return IN_PROGRESS;
            } else if (normalized.startsWith("resolved")) {
                return RESOLVED;
            } else if (normalized.startsWith("completed")) {
                return COMPLETED;
            }
            
            // Fallback: try matching enum names directly
            for (ReportStatus rs : ReportStatus.values()) {
                if (rs.name().equalsIgnoreCase(status)) {
                    return rs;
                }
            }
            return PENDING;
        }
    }
    
    /**
     * Default constructor
     */
    public Report() {
        super();
        this.status = ReportStatus.PENDING;
    }
    
    /**
     * Constructor with basic info
     * @param residentId Resident ID
     * @param reportType Type of report
     * @param location Location
     * @param description Description
     */
    public Report(int residentId, String reportType, String location, String description) {
        super();
        this.residentId = residentId;
        this.reportType = reportType;
        this.location = location;
        this.description = description;
        this.status = ReportStatus.PENDING;
    }
    
    /**
     * Full constructor
     * @param id Report ID
     * @param residentId Resident ID
     * @param reportType Type of report
     * @param location Location
     * @param description Description
     * @param status Current status
     */
    public Report(int id, int residentId, String reportType, String location, 
                  String description, ReportStatus status) {
        super(id);
        this.residentId = residentId;
        this.reportType = reportType;
        this.location = location;
        this.description = description;
        this.status = status;
    }
    
    // Getters and Setters
    
    public int getResidentId() {
        return residentId;
    }
    
    public void setResidentId(int residentId) {
        this.residentId = residentId;
        updateTimestamp();
    }
    
    public String getReportType() {
        return reportType;
    }
    
    public void setReportType(String reportType) {
        this.reportType = reportType;
        updateTimestamp();
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
        updateTimestamp();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        updateTimestamp();
    }
    
    public ReportStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReportStatus status) {
        this.status = status;
        updateTimestamp();
    }
    
    /**
     * Set status by string
     * @param status Status string
     */
    public void setStatus(String status) {
        this.status = ReportStatus.fromString(status);
        updateTimestamp();
    }
    
    /**
     * Check if report is pending
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return status == ReportStatus.PENDING;
    }
    
    /**
     * Check if report is approved
     * @return true if approved, false otherwise
     */
    public boolean isApproved() {
        return status == ReportStatus.APPROVED;
    }
    
    /**
     * Check if report is resolved
     * @return true if resolved, false otherwise
     */
    public boolean isResolved() {
        return status == ReportStatus.RESOLVED;
    }
    
    /**
     * Check if report is completed
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return status == ReportStatus.COMPLETED;
    }
    
    /**
     * Validate report data
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid() {
        return residentId > 0 &&
               ValidationUtil.isNotEmpty(reportType) &&
               ValidationUtil.isNotEmpty(location) &&
               ValidationUtil.isNotEmpty(description) &&
               ValidationUtil.isValidReportType(reportType) &&
               status != null;
    }
    
    @Override
    public String getEntityType() {
        return "Report";
    }
    
    @Override
    public String toString() {
        return String.format("Report[id=%d, residentId=%d, type='%s', location='%s', status='%s']", 
                           id, residentId, reportType, location, status.getDisplayName());
    }
}
