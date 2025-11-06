/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.FnbCategory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FnbCategoryDao {

    private FnbCategory map(ResultSet rs) throws SQLException {
        return new FnbCategory(
                rs.getInt("category_id"),
                rs.getString("name"),
                rs.getBoolean("is_active")
        );
    }

    public List<FnbCategory> findAll() throws SQLException {
        String sql = "SELECT * FROM dbo.FnbCategory ORDER BY category_id";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<FnbCategory> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    public FnbCategory findById(int id) throws SQLException {
        String sql = "SELECT * FROM dbo.FnbCategory WHERE category_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int insert(String name, boolean active) throws SQLException {
        String sql = "INSERT INTO dbo.FnbCategory(name, is_active) VALUES(?, ?)";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setNString(1, name);
            ps.setBoolean(2, active);
            return ps.executeUpdate();
        }
    }

    public int update(FnbCategory cat) throws SQLException {
        String sql = "UPDATE dbo.FnbCategory SET name=?, is_active=? WHERE category_id=?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setNString(1, cat.getName());
            ps.setBoolean(2, cat.isActive());
            ps.setInt(3, cat.getCategoryId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM dbo.FnbCategory WHERE category_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
