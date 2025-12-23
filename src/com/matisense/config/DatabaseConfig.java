package com.matisense.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;

/**
 * Database configuration and connection manager for MySQL
 * Implements singleton pattern for database connection management
 */
public class DatabaseConfig {
    private static DatabaseConfig instance;
    private Connection connection;
    private static final String DB_TIMEZONE = ZoneId.systemDefault().getId();
    private static final String DB_URL_TEMPLATE =
        "jdbc:mysql://localhost:3306/matisense_community?useSSL=false&serverTimezone=%s&useLegacyDatetimeCode=false";
    // MySQL connection configuration - update these values for your environment
    private static final String DB_URL = String.format(DB_URL_TEMPLATE, DB_TIMEZONE);
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private DatabaseConfig() {
        // Private constructor for singleton pattern
    }
    
    /**
     * Get singleton instance of DatabaseConfig
     * @return DatabaseConfig instance
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                initializeDatabase();
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC driver not found", e);
            }
        }
        return connection;
    }
    
    /**
     * Initialize database with schema
     */
    private void initializeDatabase() {
        try {
            // Read and execute schema from file
            String schema = readSchemaFromFile();
            if (schema != null && !schema.isEmpty()) {
                executeSchemaStatements(schema);
            }
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
    
    /**
     * Read database schema from SQL file
     * @return Schema SQL string
     */
    private String readSchemaFromFile() {
        StringBuilder schema = new StringBuilder();
        try (InputStream is = openSchemaInputStream()) {
            if (is == null) {
                return getBasicSchema();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }
                    schema.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading schema file: " + e.getMessage());
            return getBasicSchema();
        }
        return schema.toString();
    }

    private InputStream openSchemaInputStream() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("database_schema.sql");
        if (is != null) {
            return is;
        }
        try {
            Path schemaPath = Paths.get("database_schema.sql");
            if (Files.exists(schemaPath)) {
                return new FileInputStream(schemaPath.toFile());
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * Execute schema statements sequentially to support MySQL strict mode.
     */
    private void executeSchemaStatements(String schema) throws SQLException {
        String[] statements = schema.split(";");
        try (Statement stmt = connection.createStatement()) {
            for (String rawStatement : statements) {
                String sql = rawStatement.trim();
                if (sql.isEmpty()) {
                    continue;
                }
                stmt.execute(sql);
            }
        }
    }
    
    /**
     * Get basic database schema as fallback for MySQL/MariaDB
     * @return Basic schema SQL
     */
    private String getBasicSchema() {
        return """
            CREATE TABLE IF NOT EXISTS users (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                full_name VARCHAR(100) NOT NULL,
                address TEXT NOT NULL,
                gender VARCHAR(10),
                email VARCHAR(100) UNIQUE NOT NULL,
                contact_number VARCHAR(20),
                birthday DATE,
                user_role VARCHAR(20) NOT NULL DEFAULT 'Resident',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS reports (
                report_id INT AUTO_INCREMENT PRIMARY KEY,
                resident_id INT NOT NULL,
                report_type VARCHAR(50) NOT NULL,
                location TEXT NOT NULL,
                description TEXT NOT NULL,
                status VARCHAR(20) DEFAULT 'Pending',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (resident_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS announcements (
                announcement_id INT AUTO_INCREMENT PRIMARY KEY,
                admin_id INT NOT NULL,
                title VARCHAR(200) NOT NULL,
                content TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS notifications (
                notification_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                title VARCHAR(200) NOT NULL,
                message TEXT NOT NULL,
                notification_type VARCHAR(20) NOT NULL,
                related_id INT,
                is_read BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
            
            INSERT IGNORE INTO users (username, password, full_name, address, gender, email, contact_number, birthday, user_role)
            VALUES ('admin', 'adminadmin123', 'System Administrator', 'Admin Office', 'Other', 'admin@matisense.com', '1234567890', '1990-01-01', 'Admin');
            """;
    }
    
    /**
     * Close database connection
     */
    public synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
