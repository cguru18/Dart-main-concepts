package com.studentmanagement.dao;

import com.studentmanagement.model.User;
import com.studentmanagement.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User operations
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private final DatabaseConnection dbConnection;

    public UserDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (admission_number, email, full_name, password_hash, role) VALUES (?, ?, ?, ?, ?)";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getAdmissionNumber());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getFullName());
                stmt.setString(4, user.getPasswordHash());
                stmt.setString(5, user.getRole().name());

                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            user.setId(generatedKeys.getInt(1));
                        }
                    }
                }
                
                logger.info("User saved successfully: {}", user.getAdmissionNumber());
                return user;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public Optional<User> findByAdmissionNumber(String admissionNumber) throws SQLException {
        String sql = "SELECT * FROM users WHERE admission_number = ? AND is_active = TRUE";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, admissionNumber);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return Optional.empty();
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? AND is_active = TRUE";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return Optional.empty();
    }

    public List<User> findByRole(User.Role role) throws SQLException {
        String sql = "SELECT * FROM users WHERE role = ? AND is_active = TRUE ORDER BY full_name";
        List<User> users = new ArrayList<>();
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, role.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return users;
    }

    public User update(User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, full_name = ?, password_hash = ?, role = ?, updated_at = ? WHERE id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getFullName());
                stmt.setString(3, user.getPasswordHash());
                stmt.setString(4, user.getRole().name());
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(6, user.getId());

                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    user.setUpdatedAt(LocalDateTime.now());
                    logger.info("User updated successfully: {}", user.getAdmissionNumber());
                } else {
                    throw new SQLException("No user found with ID: " + user.getId());
                }
                
                return user;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public boolean delete(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = FALSE, updated_at = ? WHERE id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(2, userId);

                int rowsAffected = stmt.executeUpdate();
                boolean deleted = rowsAffected > 0;
                
                if (deleted) {
                    logger.info("User soft deleted successfully: ID {}", userId);
                }
                
                return deleted;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public boolean existsByAdmissionNumber(String admissionNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE admission_number = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, admissionNumber);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return false;
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return false;
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users WHERE is_active = TRUE ORDER BY full_name";
        List<User> users = new ArrayList<>();
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setAdmissionNumber(rs.getString("admission_number"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        user.setActive(rs.getBoolean("is_active"));
        
        return user;
    }
}