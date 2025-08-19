package com.studentmanagement.util;

import com.studentmanagement.exception.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Database connection utility with connection pooling for XAMPP MySQL
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    // XAMPP default configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/student_management_db";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = ""; // XAMPP default empty password
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection pool settings
    private static final int MAX_POOL_SIZE = 20;
    private static final int MIN_POOL_SIZE = 5;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    private static DatabaseConnection instance;
    private final ConcurrentLinkedQueue<Connection> connectionPool;
    private final ScheduledExecutorService poolMaintainer;
    private volatile boolean isShutdown = false;

    private DatabaseConnection() {
        this.connectionPool = new ConcurrentLinkedQueue<>();
        this.poolMaintainer = Executors.newSingleThreadScheduledExecutor();
        
        try {
            Class.forName(DB_DRIVER);
            initializePool();
            startPoolMaintenance();
            logger.info("Database connection pool initialized successfully");
        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC driver not found", e);
            throw new DatabaseConnectionException("MySQL JDBC driver not found", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void initializePool() {
        for (int i = 0; i < MIN_POOL_SIZE; i++) {
            try {
                Connection connection = createNewConnection();
                connectionPool.offer(connection);
            } catch (SQLException e) {
                logger.error("Failed to create initial database connection", e);
                throw new DatabaseConnectionException("Failed to initialize connection pool", e);
            }
        }
    }

    private Connection createNewConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USERNAME);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("autoReconnect", "true");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        
        Connection connection = DriverManager.getConnection(DB_URL, props);
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection getConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("Connection pool is shutdown");
        }

        Connection connection = connectionPool.poll();
        
        if (connection == null || connection.isClosed() || !connection.isValid(5)) {
            if (connectionPool.size() < MAX_POOL_SIZE) {
                connection = createNewConnection();
                logger.debug("Created new database connection");
            } else {
                // Wait for a connection to become available
                long startTime = System.currentTimeMillis();
                while (connection == null && (System.currentTimeMillis() - startTime) < CONNECTION_TIMEOUT) {
                    try {
                        Thread.sleep(100);
                        connection = connectionPool.poll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrupted while waiting for connection", e);
                    }
                }
                
                if (connection == null) {
                    throw new SQLException("Connection timeout: Unable to obtain database connection");
                }
            }
        }

        return connection;
    }

    public void returnConnection(Connection connection) {
        if (connection != null && !isShutdown) {
            try {
                if (!connection.isClosed() && connection.isValid(5)) {
                    connectionPool.offer(connection);
                    logger.debug("Returned connection to pool");
                } else {
                    connection.close();
                    logger.debug("Closed invalid connection");
                }
            } catch (SQLException e) {
                logger.error("Error returning connection to pool", e);
                try {
                    connection.close();
                } catch (SQLException ex) {
                    logger.error("Error closing connection", ex);
                }
            }
        }
    }

    private void startPoolMaintenance() {
        poolMaintainer.scheduleAtFixedRate(() -> {
            try {
                maintainPool();
            } catch (Exception e) {
                logger.error("Error during pool maintenance", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private void maintainPool() {
        // Remove invalid connections
        connectionPool.removeIf(connection -> {
            try {
                if (connection.isClosed() || !connection.isValid(5)) {
                    connection.close();
                    return true;
                }
                return false;
            } catch (SQLException e) {
                logger.warn("Error checking connection validity", e);
                try {
                    connection.close();
                } catch (SQLException ex) {
                    logger.error("Error closing invalid connection", ex);
                }
                return true;
            }
        });

        // Ensure minimum pool size
        while (connectionPool.size() < MIN_POOL_SIZE && !isShutdown) {
            try {
                connectionPool.offer(createNewConnection());
            } catch (SQLException e) {
                logger.error("Failed to create connection during maintenance", e);
                break;
            }
        }

        logger.debug("Pool maintenance completed. Current pool size: {}", connectionPool.size());
    }

    public void shutdown() {
        isShutdown = true;
        poolMaintainer.shutdown();
        
        try {
            if (!poolMaintainer.awaitTermination(10, TimeUnit.SECONDS)) {
                poolMaintainer.shutdownNow();
            }
        } catch (InterruptedException e) {
            poolMaintainer.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Close all connections
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing connection during shutdown", e);
            }
        }
        
        logger.info("Database connection pool shutdown completed");
    }

    public int getPoolSize() {
        return connectionPool.size();
    }

    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && connection.isValid(5);
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}