package com.matisense.model;

import com.matisense.util.ValidationUtil;

/**
 * User model class representing system users
 * Extends BaseEntity and implements encapsulation
 */
public class User extends BaseEntity {
    private String username;
    private String password;
    private String fullName;
    private String address;
    private String gender;
    private String email;
    private String contactNumber;
    private String birthday;
    private UserRole userRole;
    
    /**
     * Enum for user roles
     */
    public enum UserRole {
        ADMIN("Admin"),
        RESIDENT("Resident");
        
        private final String displayName;
        
        UserRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static UserRole fromString(String role) {
            if (role != null) {
                for (UserRole ur : UserRole.values()) {
                    if (ur.displayName.equalsIgnoreCase(role) || ur.name().equalsIgnoreCase(role)) {
                        return ur;
                    }
                }
            }
            return RESIDENT; // Default
        }
    }
    
    /**
     * Default constructor
     */
    public User() {
        super();
        this.userRole = UserRole.RESIDENT;
    }
    
    /**
     * Constructor with basic info
     * @param username Username
     * @param password Password
     * @param fullName Full name
     * @param email Email
     */
    public User(String username, String password, String fullName, String email) {
        super();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.userRole = UserRole.RESIDENT;
    }
    
    /**
     * Full constructor
     */
    public User(int id, String username, String password, String fullName, String address, 
                String gender, String email, String contactNumber, String birthday, UserRole userRole) {
        super(id);
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
        this.gender = gender;
        this.email = email;
        this.contactNumber = contactNumber;
        this.birthday = birthday;
        this.userRole = userRole;
    }
    
    // Getters and Setters with validation
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
        updateTimestamp();
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
        updateTimestamp();
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
        updateTimestamp();
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
        updateTimestamp();
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
        updateTimestamp();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        updateTimestamp();
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
        updateTimestamp();
    }
    
    public String getBirthday() {
        return birthday;
    }
    
    public void setBirthday(String birthday) {
        this.birthday = birthday;
        updateTimestamp();
    }
    
    public UserRole getUserRole() {
        return userRole;
    }
    
    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
        updateTimestamp();
    }
    
    /**
     * Check if user is admin
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return userRole == UserRole.ADMIN;
    }
    
    /**
     * Check if user is resident
     * @return true if resident, false otherwise
     */
    public boolean isResident() {
        return userRole == UserRole.RESIDENT;
    }
    
    /**
     * Validate user data
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid() {
        return ValidationUtil.isNotEmpty(username) &&
               ValidationUtil.isNotEmpty(password) &&
               ValidationUtil.isNotEmpty(fullName) &&
               ValidationUtil.isNotEmpty(email) &&
               ValidationUtil.isValidEmail(email) &&
               ValidationUtil.isValidUsername(username) &&
               ValidationUtil.isValidPassword(password) &&
               (gender == null || ValidationUtil.isValidGender(gender)) &&
               (contactNumber == null || ValidationUtil.isValidPhone(contactNumber)) &&
               (birthday == null || ValidationUtil.isValidDate(birthday));
    }
    
    @Override
    public String getEntityType() {
        return "User";
    }
    
    @Override
    public String toString() {
        return String.format("User[id=%d, username='%s', fullName='%s', email='%s', role='%s']", 
                           id, username, fullName, email, userRole.getDisplayName());
    }
}
