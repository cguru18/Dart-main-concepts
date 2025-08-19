package com.example.sms.dao;

import com.example.sms.model.Score;
import com.example.sms.model.Subject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class ScoreDao {
    private final DataSource dataSource;
    public ScoreDao(DataSource dataSource) { this.dataSource = dataSource; }

    public List<Score> findByExam(long examId) throws SQLException {
        String sql = "SELECT id, exam_id, user_id, subject, marks FROM scores WHERE exam_id=?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Score> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public Map<Long, Integer> totalByStudentForExam(long examId) throws SQLException {
        String sql = "SELECT user_id, SUM(marks) AS total FROM scores WHERE exam_id=? GROUP BY user_id";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Long, Integer> map = new HashMap<>();
                while (rs.next()) map.put(rs.getLong("user_id"), rs.getInt("total"));
                return map;
            }
        }
    }

    private Score map(ResultSet rs) throws SQLException {
        Score s = new Score();
        s.setId(rs.getLong("id"));
        s.setExamId(rs.getLong("exam_id"));
        s.setUserId(rs.getLong("user_id"));
        s.setSubject(Subject.valueOf(rs.getString("subject")));
        s.setMarks(rs.getInt("marks"));
        return s;
    }

    // Per-subject marks for a given student and exam
    public Map<Subject, Integer> scoresBySubjectForUser(long examId, long userId) throws SQLException {
        String sql = "SELECT subject, marks FROM scores WHERE exam_id=? AND user_id=?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, examId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Subject, Integer> map = new EnumMap<>(Subject.class);
                while (rs.next()) {
                    map.put(Subject.valueOf(rs.getString("subject")), rs.getInt("marks"));
                }
                return map;
            }
        }
    }

    // Number of distinct students per subject for an exam
    public Map<Subject, Integer> studentCountBySubject(long examId) throws SQLException {
        String sql = "SELECT subject, COUNT(DISTINCT user_id) AS cnt FROM scores WHERE exam_id=? GROUP BY subject";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Subject, Integer> map = new EnumMap<>(Subject.class);
                while (rs.next()) {
                    map.put(Subject.valueOf(rs.getString("subject")), rs.getInt("cnt"));
                }
                return map;
            }
        }
    }

    // User's position within a subject for an exam
    public int positionForUserBySubject(long examId, long userId, Subject subject) throws SQLException {
        String sqlMarks = "SELECT marks FROM scores WHERE exam_id=? AND subject=? AND user_id=?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement psMarks = c.prepareStatement(sqlMarks)) {
            psMarks.setLong(1, examId);
            psMarks.setString(2, subject.name());
            psMarks.setLong(3, userId);
            try (ResultSet rs = psMarks.executeQuery()) {
                if (!rs.next()) return 0; // no marks -> no position
                int myMarks = rs.getInt("marks");
                String sqlPos = "SELECT COUNT(*) + 1 AS pos FROM scores WHERE exam_id=? AND subject=? AND marks > ?";
                try (PreparedStatement psPos = c.prepareStatement(sqlPos)) {
                    psPos.setLong(1, examId);
                    psPos.setString(2, subject.name());
                    psPos.setInt(3, myMarks);
                    try (ResultSet rsPos = psPos.executeQuery()) {
                        if (rsPos.next()) return rsPos.getInt("pos");
                    }
                }
            }
        }
        return 0;
    }
}