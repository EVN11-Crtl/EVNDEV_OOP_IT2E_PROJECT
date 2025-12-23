package com.matisense.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base class for all model entities
 * Implements common functionality and enforces abstraction
 */
public abstract class BaseEntity {
    protected int id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with ID
     * @param id Entity ID
     */
    public BaseEntity(int id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get entity ID
     * @return Entity ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set entity ID
     * @param id Entity ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get creation date
     * @return Creation date
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set creation date
     * @param createdAt Creation date
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get last update date
     * @return Last update date
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Set last update date
     * @param updatedAt Last update date
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Update the timestamp
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get formatted creation date
     * @return Formatted date string
     */
    public String getFormattedCreatedAt() {
        if (createdAt != null) {
            return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"));
        }
        return "N/A";
    }
    
    /**
     * Get formatted update date
     * @return Formatted date string
     */
    public String getFormattedUpdatedAt() {
        if (updatedAt != null) {
            return updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"));
        }
        return "N/A";
    }
    
    /**
     * Abstract method to get entity type
     * @return Entity type as string
     */
    public abstract String getEntityType();
    
    /**
     * Abstract method for validation
     * @return true if entity is valid, false otherwise
     */
    public abstract boolean isValid();
    
    @Override
    public String toString() {
        return getEntityType() + "[id=" + id + ", createdAt=" + getFormattedCreatedAt() + "]";
    }
}
