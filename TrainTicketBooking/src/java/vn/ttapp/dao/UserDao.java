package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.User;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;

public class UserDao {

    public User findByEmail(String email) throws SQLException {
        final String sql = """
            SELECT 
                user_id,
                email,
                password_hash,
                full_name,
                phone,
                address,
                is_active,
                created_at,
                updated_at
            FROM dbo.Users
            WHERE email = ?
        """;

        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                User u = new User();

                // JDBC 4.2 với SQL Server hỗ trợ getObject(UUID.class); fallback sang String nếu cần
                UUID uid;
                try {
                    uid = rs.getObject("user_id", UUID.class);
                } catch (Throwable ignore) {
                    uid = UUID.fromString(rs.getString("user_id"));
                }
                u.setUserId(uid);

                u.setEmail(rs.getString("email"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setFullName(rs.getString("full_name"));
                u.setPhone(rs.getString("phone"));
                u.setAddress(rs.getString("address"));
                u.setActive(rs.getBoolean("is_active"));

                Timestamp cAt = rs.getTimestamp("created_at");
                Timestamp uAt = rs.getTimestamp("updated_at");
                if (cAt != null) {
                    u.setCreatedAt(cAt.toInstant());
                }
                if (uAt != null) {
                    u.setUpdatedAt(uAt.toInstant());
                }

                return u;
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        final String sql = "SELECT 1 FROM dbo.Users WHERE email = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public UUID create(String email, String passwordHash, String fullName) throws SQLException {
        final String sql = """
            INSERT INTO dbo.Users (email, password_hash, full_name, is_active, created_at, updated_at)
            OUTPUT INSERTED.user_id
            VALUES (?, ?, ?, 1, SYSUTCDATETIME(), SYSUTCDATETIME())
        """;

        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try {
                        return rs.getObject(1, UUID.class);
                    } catch (Throwable ignore) {
                        return UUID.fromString(rs.getString(1));
                    }
                }
            }
        }
        // Fallback (hiếm khi cần) — đọc lại theo email
        User u = findByEmail(email);
        return u != null ? u.getUserId() : null;
    }

    public void updatePasswordHash(UUID userId, String newHash) throws SQLException {
        final String sql = """
            UPDATE dbo.Users
            SET password_hash = ?, updated_at = SYSUTCDATETIME()
            WHERE user_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setObject(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateProfile(UUID userId, String fullName, String phone, String address) throws SQLException {
        final String sql = """
            UPDATE dbo.Users
            SET full_name = ?, phone = ?, address = ?, updated_at = SYSUTCDATETIME()
            WHERE user_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setObject(4, userId);
            ps.executeUpdate();
        }
    }
}
