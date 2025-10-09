package vn.ttapp.dao;


import vn.ttapp.config.Db;
import vn.ttapp.model.User;
import java.sql.*;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author New User
 */
public class UserDao {
    public User findByEmail(String email) throws SQLException {
        String sql = """
                     SELECT CAST(user_id AS NVARCHAR(36)) AS user_id, email, password, full_name, is_active
                     FROM dbo.Users WHERE email = ?
        """;
        try (Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User u = new User();
                u.setUserId(rs.getString("user_id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setFullName(rs.getString("full_name"));
                u.setActive(rs.getBoolean("is_active"));
                return u;
            }
        }
    }
        
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Users WHERE email = ?";
        try(Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return  rs.next();
            }
        }
    }
    
    public String create(String email, String hash, String fullName) throws SQLException {
        String sql = "INSERT INTO dbo.Users(email, password, full_name) VALUES(?,?,?)";
        try (Connection c = Db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, hash);
                ps.setString(3, fullName);
                ps.executeUpdate();
        }
        User u = findByEmail(email);
        return u != null ? u.getUserId() : null;
    }
}
