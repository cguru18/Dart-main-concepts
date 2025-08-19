package com.example.sms.dao;

import com.example.sms.model.Exam;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamDao {
    private final DataSource dataSource;

    public ExamDao(DataSource dataSource) { this.dataSource = dataSource; }

    public List<Exam> listAll() throws SQLException {
        String sql = "SELECT id, name, term, year, created_at FROM exams ORDER BY created_at DESC";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Exam> list = new ArrayList<>();
            while (rs.next()) {
                Exam e = new Exam();
                e.setId(rs.getLong("id"));
                e.setName(rs.getString("name"));
                e.setTerm(rs.getInt("term"));
                e.setYear(rs.getInt("year"));
                Timestamp ts = rs.getTimestamp("created_at");
                e.setCreatedAt(ts != null ? ts.toInstant() : Instant.now());
                list.add(e);
            }
            return list;
        }
    }

    public Optional<Exam> findById(long id) throws SQLException {
        String sql = "SELECT id, name, term, year, created_at FROM exams WHERE id=?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Exam e = new Exam();
                    e.setId(rs.getLong("id"));
                    e.setName(rs.getString("name"));
                    e.setTerm(rs.getInt("term"));
                    e.setYear(rs.getInt("year"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    e.setCreatedAt(ts != null ? ts.toInstant() : Instant.now());
                    return Optional.of(e);
                }
            }
        }
        return Optional.empty();
    }
}
