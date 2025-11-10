package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Role;
import vn.ttapp.model.User;

import java.sql.*;
import java.util.*;

/**
 * UserDao: - Truy vấn user và vai trò (Roles) cho đăng nhập/quản trị. - Tương
 * thích SQL Server: ưu tiên JDBC 4.2 (UUID native), fallback String. - Khi xử
 * lý quan hệ 1-n (user-roles), ORDER BY để gom nhóm ổn định.
 */
public class UserDao {

    /* ----------------- Helpers UUID ----------------- */
    private static void bindUuid(PreparedStatement ps, int idx, UUID id) throws SQLException {
        try {
            ps.setObject(idx, id); // Driver MSSQL mới hỗ trợ tốt
        } catch (Throwable ignore) {
            ps.setString(idx, id != null ? id.toString() : null);
        }
    }

    private static UUID readUuid(ResultSet rs, String col) throws SQLException {
        try {
            UUID u = rs.getObject(col, UUID.class);
            if (u != null) {
                return u;
            }
        } catch (Throwable ignore) {
            /* fallback */ }
        String s = rs.getString(col);
        return (s == null) ? null : UUID.fromString(s);
    }

    /* ----------------- BASIC (login/profile) ----------------- */
    /**
     * Tìm user theo email (đăng nhập).
     */
    public User findByEmail(String email) throws SQLException {
        final String sql = """
            SELECT user_id, email, password_hash, full_name, phone, address,
                   is_active, created_at, updated_at
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
                u.setUserId(readUuid(rs, "user_id"));
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

    /**
     * Tìm user theo userId.
     */
    public User findById(UUID userId) throws SQLException {
        final String sql = """
            SELECT user_id, email, password_hash, full_name, phone, address,
                   is_active, created_at, updated_at
            FROM dbo.Users
            WHERE user_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindUuid(ps, 1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User u = new User();
                u.setUserId(readUuid(rs, "user_id"));
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

    /**
     * Email đã tồn tại?
     */
    public boolean emailExists(String email) throws SQLException {
        final String sql = "SELECT 1 FROM dbo.Users WHERE email = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Tạo user cơ bản, trả về UUID.
     */
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
        // Fallback: đọc lại theo email (gần như không cần nếu OUTPUT đã có)
        User u = findByEmail(email);
        return u != null ? u.getUserId() : null;
    }

    /**
     * Đổi mật khẩu (hash đã chuẩn bị ở Service).
     */
    public void updatePasswordHash(UUID userId, String newHash) throws SQLException {
        final String sql = """
            UPDATE dbo.Users
            SET password_hash = ?, updated_at = SYSUTCDATETIME()
            WHERE user_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            bindUuid(ps, 2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật hồ sơ cơ bản.
     */
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
            bindUuid(ps, 4, userId);
            ps.executeUpdate();
        }
    }

    /* ----------------- ROLES (đọc cho Auth) ----------------- */
    /**
     * Lấy danh sách mã role của user.
     */
    public Set<String> getRoleCodes(UUID userId) throws SQLException {
        final String sql = """
            SELECT r.code
            FROM dbo.UserRoles ur
            JOIN dbo.Roles r ON ur.role_id = r.role_id
            WHERE ur.user_id = ?
        """;
        Set<String> roles = new HashSet<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindUuid(ps, 1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("code");
                    if (code != null) {
                        roles.add(code.trim());
                    }
                }
            }
        }
        return roles;
    }

    /**
     * Lấy đầy đủ Role (roleId, code, name) cho một user.
     */
    public List<Role> findRolesByUserId(UUID userId) throws SQLException {
        final String sql = """
            SELECT r.role_id, r.code, r.name
            FROM dbo.UserRoles ur
            JOIN dbo.Roles r ON ur.role_id = r.role_id
            WHERE ur.user_id = ?
        """;
        List<Role> roles = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindUuid(ps, 1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapRole(rs));
                }
            }
        }
        return roles;
    }

    /* ----------------- ADMIN PAGES ----------------- */
    /**
     * Lấy tất cả user kèm roles.
     *
     * @param roleType "CUSTOMER" → chỉ KH (hoặc null role); khác → non-customer
     */
    public List<User> findAllWithRoles(String roleType) throws SQLException {
        String sql = """
            SELECT 
                u.user_id,
                CAST(u.user_id AS NVARCHAR(36)) AS user_id_str,
                u.email, u.full_name, u.is_active, u.password_hash,
                r.role_id, r.code, r.name
            FROM dbo.Users u
            LEFT JOIN dbo.UserRoles ur ON u.user_id = ur.user_id
            LEFT JOIN dbo.Roles r ON ur.role_id = r.role_id
        """;
        if ("CUSTOMER".equals(roleType)) {
            sql += " WHERE (r.code = 'CUSTOMER' OR r.code IS NULL)";
        } else {
            sql += " WHERE (r.code != 'CUSTOMER' AND r.code IS NOT NULL)";
        }
        sql += " ORDER BY u.user_id, r.role_id"; // gom nhóm ổn định

        List<User> result = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            User current = null;
            String lastUserId = "";

            while (rs.next()) {
                String curId = rs.getString("user_id_str");
                if (!curId.equals(lastUserId)) {
                    current = new User();
                    try {
                        current.setUserId(UUID.fromString(curId));
                    } catch (IllegalArgumentException ignore) {
                    }
                    current.setEmail(rs.getString("email"));
                    current.setFullName(rs.getString("full_name"));
                    current.setActive(rs.getBoolean("is_active"));
                    current.setPasswordHash(rs.getString("password_hash"));
                    current.setRoles(new ArrayList<>());
                    result.add(current);
                    lastUserId = curId;
                }
                int roleId = rs.getInt("role_id");
                if (!rs.wasNull() && current != null) {
                    current.getRoles().add(mapRole(rs));
                }
            }
        }
        return result;
    }

    /**
     * Lấy một user + toàn bộ roles (cho form Admin Edit).
     */
    public User findUserWithRoles(String userId) throws SQLException {
        User user = null;
        final String sql = """
            SELECT 
                u.user_id, CAST(u.user_id AS NVARCHAR(36)) AS user_id_str,
                u.email, u.password_hash, u.full_name, u.is_active,
                r.role_id, r.code, r.name
            FROM dbo.Users u
            LEFT JOIN dbo.UserRoles ur ON u.user_id = ur.user_id
            LEFT JOIN dbo.Roles r ON ur.role_id = r.role_id
            WHERE u.user_id = ?
        """;
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindUuid(ps, 1, UUID.fromString(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (user == null) {
                        user = new User();
                        user.setUserId(readUuid(rs, "user_id"));
                        user.setEmail(rs.getString("email"));
                        user.setPasswordHash(rs.getString("password_hash"));
                        user.setFullName(rs.getString("full_name"));
                        user.setActive(rs.getBoolean("is_active"));
                        user.setRoles(new ArrayList<>());
                    }
                    int roleId = rs.getInt("role_id");
                    if (!rs.wasNull()) {
                        user.getRoles().add(mapRole(rs));
                    }
                }
            }
        }
        return user;
    }

    /**
     * Admin tạo user trong transaction, trả về UUID.
     */
    public UUID adminCreateUser(User user, Connection conn) throws SQLException {
        final String sql = """
            INSERT INTO dbo.Users(email, full_name, password_hash, is_active, created_at, updated_at)
            OUTPUT INSERTED.user_id
            VALUES(?, ?, ?, ?, SYSUTCDATETIME(), SYSUTCDATETIME())
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, user.getEmail());
            ps.setNString(2, user.getFullName());
            ps.setNString(3, user.getPasswordHash());
            ps.setBoolean(4, user.isActive());
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
        return null;
    }

    /**
     * Admin cập nhật user (nếu password_hash rỗng → không đổi mật khẩu).
     */
    public void adminUpdateUser(User user, Connection conn) throws SQLException {
        final boolean hasPwd = (user.getPasswordHash() != null && !user.getPasswordHash().isBlank());
        final String sql = hasPwd
                ? "UPDATE dbo.Users SET email=?, full_name=?, password_hash=?, is_active=?, updated_at=SYSUTCDATETIME() WHERE user_id=?"
                : "UPDATE dbo.Users SET email=?, full_name=?, is_active=?, updated_at=SYSUTCDATETIME() WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, user.getEmail());
            ps.setNString(2, user.getFullName());
            if (hasPwd) {
                ps.setNString(3, user.getPasswordHash());
                ps.setBoolean(4, user.isActive());
                bindUuid(ps, 5, user.getUserId());
            } else {
                ps.setBoolean(3, user.isActive());
                bindUuid(ps, 4, user.getUserId());
            }
            ps.executeUpdate();
        }
    }

    /**
     * Admin cập nhật toàn bộ roles cho user (xoá hết rồi chèn mới).
     */
    public void adminUpdateRoles(UUID userId, List<Integer> roleIds, Connection conn) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM dbo.UserRoles WHERE user_id = ?")) {
            bindUuid(del, 1, userId);
            del.executeUpdate();
        }
        if (roleIds != null && !roleIds.isEmpty()) {
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO dbo.UserRoles (user_id, role_id) VALUES (?, ?)")) {
                for (Integer rid : roleIds) {
                    bindUuid(ins, 1, userId);
                    ins.setInt(2, rid);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        }
    }

    /* ----------------- Private mappers ----------------- */
    private Role mapRole(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setRoleId(rs.getInt("role_id"));
        r.setCode(rs.getString("code"));
        r.setName(rs.getString("name"));
        return r;
    }
}
