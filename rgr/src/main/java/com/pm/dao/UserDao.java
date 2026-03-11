package com.pm.dao;

import com.pm.model.User;
import com.pm.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDao {

    public User findByUsername(String username) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id,username,email,role,confirmed FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id,username,email,role,confirmed FROM users WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id,username,email,role,confirmed FROM users ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public User insert(String username, String email, String hash) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users(username,email,password_hash) VALUES(?,?,?) RETURNING id")) {
            ps.setString(1, username); ps.setString(2, email); ps.setString(3, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt(1)); u.setUsername(username);
                    u.setEmail(email); u.setRole("user"); u.setConfirmed(false);
                    return u;
                }
            }
        }
        return null;
    }

    public String createConfirmToken(int userId) throws SQLException {
        String token = UUID.randomUUID().toString().replace("-","");
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "INSERT INTO confirm_tokens(user_id,token) VALUES(?,?)")) {
            ps.setInt(1, userId); ps.setString(2, token); ps.executeUpdate();
        }
        return token;
    }

    public boolean confirmByToken(String token) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT user_id FROM confirm_tokens WHERE token=?")) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                int uid = rs.getInt(1);
                try (PreparedStatement u2 = c.prepareStatement(
                        "UPDATE users SET confirmed=true WHERE id=?")) {
                    u2.setInt(1, uid); u2.executeUpdate();
                }
                try (PreparedStatement d = c.prepareStatement(
                        "DELETE FROM confirm_tokens WHERE token=?")) {
                    d.setString(1, token); d.executeUpdate();
                }
                return true;
            }
        }
    }

    public void updateRole(int id, String role) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "UPDATE users SET role=? WHERE id=?")) {
            ps.setString(1, role); ps.setInt(2, id); ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id")); u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email")); u.setRole(rs.getString("role"));
        u.setConfirmed(rs.getBoolean("confirmed"));
        return u;
    }
}
