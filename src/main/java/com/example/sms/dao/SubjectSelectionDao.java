package com.example.sms.dao;

import com.example.sms.model.Subject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class SubjectSelectionDao {
    private final DataSource dataSource;

    public SubjectSelectionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Set<Subject> getSubjectsForUser(long userId) throws SQLException {
        String sql = "SELECT subject FROM subject_selection WHERE user_id=?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                Set<Subject> set = EnumSet.noneOf(Subject.class);
                while (rs.next()) {
                    set.add(Subject.valueOf(rs.getString("subject")));
                }
                return set;
            }
        }
    }

    public void replaceSubjects(long userId, Set<Subject> subjects) throws SQLException {
        String delete = "DELETE FROM subject_selection WHERE user_id=?";
        String insert = "INSERT INTO subject_selection(user_id, subject) VALUES(?,?)";
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement(delete)) {
                del.setLong(1, userId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = c.prepareStatement(insert)) {
                for (Subject s : subjects) {
                    ins.setLong(1, userId);
                    ins.setString(2, s.name());
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            c.commit();
        }
    }
}
