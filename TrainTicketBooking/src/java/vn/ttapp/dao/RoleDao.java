/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO này dùng để lấy danh sách Vai trò (Roles)
 */
public class RoleDao {

    private Role map(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setRoleId(rs.getInt("role_id"));
        r.setCode(rs.getString("code"));
        r.setName(rs.getString("name"));
        return r;
    }

    /**
     * Lấy tất cả vai trò
     */
    public List<Role> findAll() throws SQLException {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT role_id, code, name FROM dbo.Roles ORDER BY role_id";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }
    
    /**
     * Lấy các vai trò "Nội bộ" (Admin, Manager, Staff)
     */
    public List<Role> findInternalRoles() throws SQLException {
         List<Role> list = new ArrayList<>();
        String sql = "SELECT role_id, code, name FROM dbo.Roles WHERE code != 'CUSTOMER' ORDER BY role_id";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }
}
