package com.matisense.model;

import com.matisense.util.ValidationUtil;

/**
 * Announcement model class representing admin announcements
 * Extends BaseEntity and implements encapsulation
 */
public class Announcement extends BaseEntity {
    private int adminId;
    private String title;
    private String content;
    
    /**
     * Default constructor
     */
    public Announcement() {
        super();
    }
    
    /**
     * Constructor with basic info
     * @param adminId Admin ID
     * @param title Title
     * @param content Content
     */
    public Announcement(int adminId, String title, String content) {
        super();
        this.adminId = adminId;
        this.title = title;
        this.content = content;
    }
    
    /**
     * Full constructor
     * @param id Announcement ID
     * @param adminId Admin ID
     * @param title Title
     * @param content Content
     */
    public Announcement(int id, int adminId, String title, String content) {
        super(id);
        this.adminId = adminId;
        this.title = title;
        this.content = content;
    }
    
    // Getters and Setters
    
    public int getAdminId() {
        return adminId;
    }
    
    public void setAdminId(int adminId) {
        this.adminId = adminId;
        updateTimestamp();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        updateTimestamp();
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        updateTimestamp();
    }
    
    /**
     * Get truncated content for preview
     * @param maxLength Maximum length
     * @return Truncated content
     */
    public String getTruncatedContent(int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
    
    /**
     * Validate announcement data
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid() {
        return adminId > 0 &&
               ValidationUtil.isNotEmpty(title) &&
               ValidationUtil.isNotEmpty(content) &&
               ValidationUtil.isValidLength(title, 200);
    }
    
    @Override
    public String getEntityType() {
        return "Announcement";
    }
    
    @Override
    public String toString() {
        return String.format("Announcement[id=%d, adminId=%d, title='%s']", 
                           id, adminId, title);
    }
}
