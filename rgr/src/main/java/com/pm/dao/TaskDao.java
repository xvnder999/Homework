package com.pm.dao;

import com.pm.model.Task;
import com.pm.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TaskDao {

    private static final Logger LOGGER = Logger.getLogger(TaskDao.class.getName());

    private static final String SELECT_BASE =
        "SELECT t.id, t.project_id, p.name AS project_name, t.assignee_id, " +
        "u.username AS assignee_name, t.title, t.description, t.status, t.priority, " +
        "t.due_date, t.created_at, COALESCE(SUM(tl.minutes), 0) AS total_minutes " +
        "FROM tasks t " +
        "JOIN projects p ON p.id = t.project_id " +
        "LEFT JOIN users u ON u.id = t.assignee_id " +
        "LEFT JOIN time_logs tl ON tl.task_id = t.id ";

    public List<Task> findByProject(int projectId) throws SQLException {
        String sql = SELECT_BASE + "WHERE t.project_id = ? GROUP BY t.id, p.name, u.username ORDER BY t.created_at DESC";
        return query(sql, ps -> ps.setInt(1, projectId));
    }

    public Task findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE t.id = ? GROUP BY t.id, p.name, u.username";
        List<Task> list = query(sql, ps -> ps.setInt(1, id));
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Task> search(int projectId, String keyword, String status, String priority) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE t.project_id = ? ");
        if (keyword != null && !keyword.isBlank()) sql.append("AND LOWER(t.title) LIKE LOWER(?) ");
        if (status  != null && !status.isBlank())  sql.append("AND t.status = ? ");
        if (priority != null && !priority.isBlank()) sql.append("AND t.priority = ? ");
        sql.append("GROUP BY t.id, p.name, u.username ORDER BY t.created_at DESC");

        return query(sql.toString(), ps -> {
            int i = 1;
            ps.setInt(i++, projectId);
            if (keyword != null && !keyword.isBlank()) ps.setString(i++, "%" + keyword + "%");
            if (status  != null && !status.isBlank())  ps.setString(i++, status);
            if (priority != null && !priority.isBlank()) ps.setString(i, priority);
        });
    }

    public List<Task> searchAll(String keyword, String status, String priority, int userId, boolean isAdmin) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE 1=1 ");
        if (!isAdmin) sql.append("AND p.owner_id = ").append(userId).append(" ");
        if (keyword != null && !keyword.isBlank()) sql.append("AND LOWER(t.title) LIKE LOWER(?) ");
        if (status  != null && !status.isBlank())  sql.append("AND t.status = ? ");
        if (priority != null && !priority.isBlank()) sql.append("AND t.priority = ? ");
        sql.append("GROUP BY t.id, p.name, u.username ORDER BY t.created_at DESC");

        return query(sql.toString(), ps -> {
            int i = 1;
            if (keyword != null && !keyword.isBlank()) ps.setString(i++, "%" + keyword + "%");
            if (status  != null && !status.isBlank())  ps.setString(i++, status);
            if (priority != null && !priority.isBlank()) ps.setString(i, priority);
        });
    }

    public Task insert(int projectId, Integer assigneeId, String title, String description,
                       String priority, String dueDateStr) throws SQLException {
        String sql = "INSERT INTO tasks (project_id, assignee_id, title, description, priority, due_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            if (assigneeId != null) ps.setInt(2, assigneeId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, title);
            ps.setString(4, description);
            ps.setString(5, priority);
            if (dueDateStr != null && !dueDateStr.isBlank()) {
                ps.setDate(6, Date.valueOf(dueDateStr));
            } else {
                ps.setNull(6, Types.DATE);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    LOGGER.info("Created task id=" + id);
                    return findById(id);
                }
            }
        }
        return null;
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(int id, String title, String description, String priority,
                          String status, Integer assigneeId, String dueDateStr) throws SQLException {
        String sql = "UPDATE tasks SET title=?, description=?, priority=?, status=?, assignee_id=?, due_date=? WHERE id=?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, priority);
            ps.setString(4, status);
            if (assigneeId != null) ps.setInt(5, assigneeId); else ps.setNull(5, Types.INTEGER);
            if (dueDateStr != null && !dueDateStr.isBlank()) {
                ps.setDate(6, Date.valueOf(dueDateStr));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id=?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Task> query(String sql, PSS setter) throws SQLException {
        List<Task> list = new ArrayList<>();
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setProjectId(rs.getInt("project_id"));
        t.setProjectName(rs.getString("project_name"));
        int aId = rs.getInt("assignee_id");
        t.setAssigneeId(rs.wasNull() ? null : aId);
        t.setAssigneeName(rs.getString("assignee_name"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setStatus(rs.getString("status"));
        t.setPriority(rs.getString("priority"));
        t.setDueDate(rs.getDate("due_date"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setTotalMinutes(rs.getInt("total_minutes"));
        return t;
    }

    @FunctionalInterface
    interface PSS {
        void set(PreparedStatement ps) throws SQLException;
    }
}
