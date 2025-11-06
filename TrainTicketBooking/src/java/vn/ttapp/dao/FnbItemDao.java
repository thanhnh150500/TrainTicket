/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.FnbItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FnbItemDao {

    private FnbItem map(ResultSet rs) throws SQLException {
        FnbItem x = new FnbItem();
        x.setItemId(rs.getInt("item_id"));
        x.setCategoryId(rs.getInt("category_id"));
        x.setCode(rs.getString("code"));
        x.setName(rs.getString("name"));
        x.setPrice(rs.getDouble("price"));
        x.setImageUrl(rs.getString("image_url")); // <-- Cập nhật
        x.setActive(rs.getBoolean("is_active"));
        
        // Thử lấy category_name nếu có join
        try {
             x.setCategoryName(rs.getString("category_name"));
        } catch (SQLException e) {
            // Bỏ qua nếu cột không tồn tại (trong câu query không join)
        }
        return x;
    }

    public List<FnbItem> findAll() {
        List<FnbItem> list = new ArrayList<>();
        // Trả về cả tên danh mục (category_name) để JSP có thể hiển thị
        String sql = "SELECT i.*, c.name AS category_name " +
                     "FROM dbo.FnbItem i " +
                     "LEFT JOIN dbo.FnbCategory c ON c.category_id = i.category_id " +
                     "ORDER BY i.category_id, i.name";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                 // Dùng hàm map đã sửa
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<FnbItem> findAllActive() {
        List<FnbItem> list = new ArrayList<>();
        // Thêm "WHERE i.is_active=1"
        String sql = """
            SELECT i.*, c.name AS category_name
            FROM dbo.FnbItem i
            LEFT JOIN dbo.FnbCategory c ON c.category_id = i.category_id
            WHERE i.is_active=1
            ORDER BY i.category_id, i.name
        """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Tận dụng hàm map bạn đã có
                list.add(map(rs)); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public FnbItem findById(int id) throws SQLException {
        String sql = """
                     SELECT i.*, c.name AS category_name
                     FROM dbo.FnbItem i
                     LEFT JOIN dbo.FnbCategory c ON c.category_id = i.category_id
                     WHERE i.item_id = ?
                 """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean codeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.FnbItem WHERE code = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insert(FnbItem x) throws SQLException {
        // <-- Cập nhật SQL (thêm image_url)
        String sql = """
                     INSERT INTO dbo.FnbItem(category_id, code, name, price, is_active, image_url)
                     VALUES(?,?,?,?,?,?)
                 """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (x.getCategoryId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, x.getCategoryId());
            }
            ps.setString(2, x.getCode());
            ps.setNString(3, x.getName());
            ps.setDouble(4, x.getPrice());
            ps.setBoolean(5, x.isActive());
            ps.setNString(6, x.getImageUrl()); // <-- Cập nhật
            return ps.executeUpdate();
        }
    }

    public int update(FnbItem x) throws SQLException {
         // <-- Cập nhật SQL (thêm image_url)
        String sql = """
                     UPDATE dbo.FnbItem
                     SET category_id=?, code=?, name=?, price=?, is_active=?, image_url=?
                     WHERE item_id=?
                 """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (x.getCategoryId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, x.getCategoryId());
            }
            ps.setString(2, x.getCode());
            ps.setNString(3, x.getName());
            ps.setDouble(4, x.getPrice());
            ps.setBoolean(5, x.isActive());
            ps.setNString(6, x.getImageUrl()); // <-- Cập nhật
            ps.setInt(7, x.getItemId());      // <-- Cập nhật
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM dbo.FnbItem WHERE item_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}