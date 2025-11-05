package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.User;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import vn.ttapp.model.Role;

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

    public List<String> getRoleCodes(UUID userId) throws SQLException {
        final String sql = """
            SELECT r.code
            FROM dbo.UserRoles ur
            JOIN dbo.Roles r ON ur.role_id = r.role_id
            WHERE ur.user_id = ?
        """;

        List<String> roles = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            // Một số driver JDBC (MSSQL) chấp nhận UUID trực tiếp, một số khác cần String
            try {
                ps.setObject(1, userId);
            } catch (Throwable ignore) {
                ps.setString(1, userId.toString());
            }
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
     * Trả về danh sách Role (roleId, code, name) cho userId
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
            try {
                ps.setObject(1, userId);
            } catch (Throwable ignore) {
                ps.setString(1, userId.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role r = new Role();
                    try {
                        r.setRoleId(rs.getInt("role_id"));
                    } catch (Throwable ignore) {
                        // ignore
                    }
                    r.setCode(rs.getString("code"));
                    r.setName(rs.getString("name"));
                    roles.add(r);
                }
            }
        }
        return roles;
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
    
    
    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        // Lấy từ user_id_str (đã được CAST) hoặc user_id (tùy theo SQL)
        String uidStr = null;
        try {
            uidStr = rs.getString("user_id_str");
        } catch (SQLException ignore) {
        }
        if (uidStr == null) {
            uidStr = rs.getString("user_id");
        }
        if (uidStr != null) {
            try {
                u.setUserId(UUID.fromString(uidStr));
            } catch (IllegalArgumentException ignore) {
                // leave null
            }
        }
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setActive(rs.getBoolean("is_active"));
        return u;
    }
    private Role mapRole(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setRoleId(rs.getInt("role_id"));
        r.setCode(rs.getString("code"));
        r.setName(rs.getString("name"));
        return r;
    }
    // NOTE: removed duplicate String-based findRolesByUserId; use UUID version above.
    
    // =======================================================
    // ==> (CÁC HÀM MỚI CHO ADMIN)
    // =======================================================

    /**
     * Lấy tất cả user VÀ vai trò của họ
     */
    public List<User> findAllWithRoles(String roleType) throws SQLException {
        List<User> userList = new ArrayList<>();
        
        String sql = """
            SELECT 
                u.user_id, CAST(u.user_id AS NVARCHAR(36)) AS user_id_str, 
                u.email, u.full_name, u.is_active, u.password_hash,
                r.role_id, r.code, r.name
            FROM dbo.Users u
            LEFT JOIN dbo.UserRoles ur ON u.user_id = ur.user_id
            LEFT JOIN dbo.Roles r ON ur.role_id = r.role_id
        """;
        
        // (Xử lý 2 tab)
        if ("CUSTOMER".equals(roleType)) {
            sql += " WHERE (r.code = 'CUSTOMER' OR r.code IS NULL)";
        } else {
            sql += " WHERE (r.code != 'CUSTOMER' AND r.code IS NOT NULL)";
        }
        sql += " ORDER BY u.created_at DESC";

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            User currentUser = null;
            String lastUserId = "";

            while (rs.next()) {
                String currentUserId = rs.getString("user_id_str");
                
                if (!currentUserId.equals(lastUserId)) {
                    // (Đây là 1 user mới)
                    currentUser = new User();
                    try {
                        currentUser.setUserId(UUID.fromString(currentUserId));
                    } catch (IllegalArgumentException ignore) {
                        // leave null if parsing fails
                    }
                    currentUser.setEmail(rs.getString("email"));
                    currentUser.setFullName(rs.getString("full_name"));
                    currentUser.setActive(rs.getBoolean("is_active"));
                    // (Lấy password_hash để hàm map không bị lỗi)
                    currentUser.setPasswordHash(rs.getString("password_hash")); 
                    currentUser.setRoles(new ArrayList<>());
                    
                    userList.add(currentUser);
                    lastUserId = currentUserId;
                }
                
                // (Thêm Role vào user hiện tại)
                if (rs.getInt("role_id") != 0) {
                    Role r = mapRole(rs);
                    currentUser.getRoles().add(r);
                }
            }
        }
        return userList;
    }
    
    /**
     * Lấy 1 User và vai trò (Dùng cho Admin Edit)
     */
    public User findUserWithRoles(String userId) throws SQLException {
        User user = null;
        String sql = """
            SELECT 
                CAST(u.user_id AS NVARCHAR(36)) AS user_id_str, 
                u.email, u.password_hash, u.full_name, u.is_active,
                r.role_id, r.code, r.name
            FROM dbo.Users u
            LEFT JOIN dbo.UserRoles ur ON u.user_id = ur.user_id
            LEFT JOIN dbo.Roles r ON ur.role_id = r.role_id
            WHERE u.user_id = ?
        """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(userId));
             
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (user == null) {
                        user = map(rs); // Dùng map cũ
                    }
                    if (rs.getInt("role_id") != 0) {
                        if (user.getRoles() == null) user.setRoles(new ArrayList<>());
                        user.getRoles().add(mapRole(rs));
                    }
                }
            }
        }
        return user;
    }
    
    /**
     * Tạo User (bởi Admin)
     */
    public String adminCreateUser(User user, Connection conn) throws SQLException {
        String sql = """
            INSERT INTO dbo.Users(email, full_name, password_hash, is_active, updated_at) 
            OUTPUT INSERTED.user_id
            VALUES(?,?,?,?, SYSUTCDATETIME())
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, user.getEmail());
            ps.setNString(2, user.getFullName());
            ps.setNString(3, user.getPasswordHash()); // (Hash đã được set ở Service)
            ps.setBoolean(4, user.isActive());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1).toString(); // Trả về String UUID
                }
            }
        }
        return null;
    }
    
    /**
     * Cập nhật User (bởi Admin)
     */
    public void adminUpdateUser(User user, Connection conn) throws SQLException {
        // (Nếu mật khẩu rỗng/null, không cập nhật cột password_hash)
        String sql;
        if (user.getPasswordHash()!= null && !user.getPasswordHash().isBlank()) {
             sql = "UPDATE dbo.Users SET email=?, full_name=?, password_hash=?, is_active=?, updated_at=SYSUTCDATETIME() WHERE user_id=?";
        } else {
             sql = "UPDATE dbo.Users SET email=?, full_name=?, is_active=?, updated_at=SYSUTCDATETIME() WHERE user_id=?";
        }
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, user.getEmail());
            ps.setNString(2, user.getFullName());

            UUID uid = user.getUserId();
            if (user.getPasswordHash()!= null && !user.getPasswordHash().isBlank()) {
                ps.setNString(3, user.getPasswordHash());
                ps.setBoolean(4, user.isActive());
                ps.setObject(5, uid);
            } else {
                ps.setBoolean(3, user.isActive());
                ps.setObject(4, uid);
            }
            ps.executeUpdate();
        }
    }
    
    /**
     * Xóa hết role cũ, thêm role mới (Transaction)
     */
    public void adminUpdateRoles(UUID userId, List<Integer> roleIds, Connection conn) throws SQLException {
        // 1. Xóa
        String deleteSql = "DELETE FROM dbo.UserRoles WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setObject(1, userId);
            ps.executeUpdate();
        }
        
        // 2. Thêm mới (nếu có)
        if (roleIds != null && !roleIds.isEmpty()) {
            String insertSql = "INSERT INTO dbo.UserRoles (user_id, role_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Integer roleId : roleIds) {
                    ps.setObject(1, userId);
                    ps.setInt(2, roleId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }
}
