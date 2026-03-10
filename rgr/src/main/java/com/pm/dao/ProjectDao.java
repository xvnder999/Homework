package com.pm.dao;

import com.pm.model.Project;
import com.pm.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProjectDao {

    private static final Logger LOGGER = Logger.getLogger(ProjectDao.class.getName());

    private static final String SELECT_BASE =
        "SELECT p.id, p.owner_id, u.username AS owner_name, p.name, p.description, p.status, p.created_at, " +
        "COUNT(DISTINCT t.id) AS task_count, COALESCE(SUM(tl.minutes), 0) AS total_minutes " +
        "FROM projects p " +
        "JOIN users u ON u.id = p.owner_id " +
        "LEFT JOIN tasks t ON t.project_id = p.id " +
        "LEFT JOIN time_logs tl ON tl.task_id = t.id ";

    public List<Project> findByOwner(int ownerId) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.owner_id = ? GROUP BY p.id, u.username ORDER BY p.created_at DESC";
        return query(sql, ps -> ps.setInt(1, ownerId));
    }

    public List<Project> findAll() throws SQLException {
        String sql = SELECT_BASE + "GROUP BY p.id, u.username ORDER BY p.created_at DESC";
        return query(sql, ps -> {});
    }

    public Project findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id = ? GROUP BY p.id, u.username";
        List<Project> list = query(sql, ps -> ps.setInt(1, id));
        return list.isEmpty() ? null : list.get(0);
    }

    public Project insert(int ownerId, String name, String description) throws SQLException {
        String sql = "INSERT INTO projects (owner_id, name, description) VALUES (?, ?, ?) RETURNING id";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ps.setString(2, name);
            ps.setString(3, description);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    LOGGER.info("Created project id=" + id);
                    return findById(id);
                }
            }
        }
        return null;
    }

    public boolean update(int id, String name, String description, String status) throws SQLException {
        String sql = "UPDATE projects SET name=?, description=?, status=? WHERE id=?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, status);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM projects WHERE id=?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Project> query(String sql, PreparedStatementSetter setter) throws SQLException {
        List<Project> list = new ArrayList<>();
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getInt("id"));
        p.setOwnerId(rs.getInt("owner_id"));
        p.setOwnerName(rs.getString("owner_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setTaskCount(rs.getInt("task_count"));
        p.setTotalMinutes(rs.getInt("total_minutes"));
        return p;
    }

    @FunctionalInterface
    interface PreparedStatementSetter {
        void set(PreparedStatement ps) throws SQLException;
    }
}
