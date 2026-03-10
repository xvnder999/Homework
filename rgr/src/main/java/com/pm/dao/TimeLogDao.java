package com.pm.dao;

import com.pm.model.TimeLog;
import com.pm.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TimeLogDao {

    private static final Logger LOGGER = Logger.getLogger(TimeLogDao.class.getName());

    public List<TimeLog> findByTask(int taskId) throws SQLException {
        String sql = "SELECT tl.id, tl.task_id, tl.user_id, u.username, tl.minutes, tl.note, tl.logged_at " +
                     "FROM time_logs tl JOIN users u ON u.id = tl.user_id " +
                     "WHERE tl.task_id = ? ORDER BY tl.logged_at DESC";
        List<TimeLog> list = new ArrayList<>();
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int totalByTask(int taskId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(minutes), 0) FROM time_logs WHERE task_id = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int totalByProject(int projectId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tl.minutes), 0) FROM time_logs tl " +
                     "JOIN tasks t ON t.id = tl.task_id WHERE t.project_id = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public TimeLog insert(int taskId, int userId, int minutes, String note) throws SQLException {
        String sql = "INSERT INTO time_logs (task_id, user_id, minutes, note) VALUES (?, ?, ?, ?) RETURNING id, logged_at";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            ps.setInt(3, minutes);
            ps.setString(4, note);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LOGGER.info("Logged " + minutes + " min for task " + taskId);
                    TimeLog tl = new TimeLog();
                    tl.setId(rs.getInt(1));
                    tl.setTaskId(taskId);
                    tl.setUserId(userId);
                    tl.setMinutes(minutes);
                    tl.setNote(note);
                    tl.setLoggedAt(rs.getTimestamp(2));
                    return tl;
                }
            }
        }
        return null;
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM time_logs WHERE id = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private TimeLog mapRow(ResultSet rs) throws SQLException {
        TimeLog tl = new TimeLog();
        tl.setId(rs.getInt("id"));
        tl.setTaskId(rs.getInt("task_id"));
        tl.setUserId(rs.getInt("user_id"));
        tl.setUsername(rs.getString("username"));
        tl.setMinutes(rs.getInt("minutes"));
        tl.setNote(rs.getString("note"));
        tl.setLoggedAt(rs.getTimestamp("logged_at"));
        return tl;
    }
}
