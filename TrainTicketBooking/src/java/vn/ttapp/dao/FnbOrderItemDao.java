/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.dao;

import vn.ttapp.model.FnbOrderItem;
import vn.ttapp.model.FnbItemRevenue;
import vn.ttapp.config.Db;
import java.sql.Timestamp;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet; // (Thêm)
import java.util.ArrayList; // (Thêm)
import java.util.List; // (Thêm)
import vn.ttapp.config.Db; // (Thêm)


public class FnbOrderItemDao {

    /**
     * Thêm một FnbOrderItem vào DB.
     * Phương thức này YÊU CẦU một Connection (để dùng trong transaction).
     */
    public int insert(FnbOrderItem item, Connection conn) throws SQLException {
        String sql = """
            INSERT INTO dbo.FnbOrderItem
            (order_id, item_id, quantity, unit_price, amount)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, item.getOrderId());
            ps.setInt(2, item.getItemId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getAmount());

            return ps.executeUpdate();
        }
    }
    
    // =====================================================================
    // ==> (THÊM 2 HÀM MỚI)
    // =====================================================================
    
    // (Hàm helper mới)
    private FnbOrderItem map(ResultSet rs) throws SQLException {
        FnbOrderItem item = new FnbOrderItem();
        item.setOrderItemId(rs.getLong("order_item_id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setItemId(rs.getInt("item_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        item.setAmount(rs.getDouble("amount"));

        // (Trường JOIN mới)
        try {
            item.setItemName(rs.getString("item_name"));
            item.setItemImageUrl(rs.getString("item_image_url"));
        } catch (SQLException e) {
            // Bỏ qua nếu không JOIN
        }
        return item;
    }

    /**
     * Lấy danh sách các món ăn (đã join tên món) theo 1 đơn hàng
     */
    public List<FnbOrderItem> findDetailedByOrderId(long orderId) throws SQLException {
        List<FnbOrderItem> list = new ArrayList<>();
        String sql = """
            SELECT oi.*, 
                   i.name AS item_name, 
                   i.image_url AS item_image_url
            FROM dbo.FnbOrderItem oi
            JOIN dbo.FnbItem i ON oi.item_id = i.item_id
            WHERE oi.order_id = ?
        """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy doanh thu theo món (tổng quantity và tổng amount) cho tất cả đơn hàng đã thanh toán thành công.
     * Trả về danh sách các món đã bán (total_amount > 0), sắp theo tổng doanh thu giảm dần.
     */
    public List<FnbItemRevenue> findRevenueAllTime() throws SQLException {
        List<FnbItemRevenue> list = new ArrayList<>();
        String sql = """
            SELECT oi.item_id, i.name AS item_name, SUM(oi.quantity) AS total_qty, SUM(oi.amount) AS total_amount
            FROM dbo.FnbOrderItem oi
            JOIN dbo.FnbOrder o ON oi.order_id = o.order_id
            JOIN dbo.FnbItem i ON oi.item_id = i.item_id
            WHERE o.payment_status = 'SUCCESS' AND o.order_status <> 'CANCELED'
            GROUP BY oi.item_id, i.name
            HAVING SUM(oi.amount) > 0
            ORDER BY SUM(oi.amount) DESC
        """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FnbItemRevenue r = new FnbItemRevenue();
                r.setItemId(rs.getInt("item_id"));
                r.setItemName(rs.getString("item_name"));
                r.setTotalQuantity(rs.getInt("total_qty"));
                r.setTotalAmount(rs.getDouble("total_amount"));
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Lấy doanh thu tổng theo ngày trong khoảng from..to (bao gồm) cho các đơn đã thanh toán.
     * Trả về danh sách ngày có doanh thu (ngày không có sẽ không xuất hiện — caller có thể fill zeros nếu cần).
     */
    public List<vn.ttapp.model.FnbDailyRevenue> findDailyRevenue(java.sql.Timestamp from, java.sql.Timestamp to) throws SQLException {
        List<vn.ttapp.model.FnbDailyRevenue> list = new ArrayList<>();
        String sql = """
            SELECT CAST(o.created_at AS DATE) AS day, SUM(o.total_amount) AS total_amount
            FROM dbo.FnbOrder o
            WHERE o.payment_status = 'SUCCESS' AND o.order_status <> 'CANCELED'
              AND o.created_at BETWEEN ? AND ?
            GROUP BY CAST(o.created_at AS DATE)
            ORDER BY day ASC
        """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, from);
            ps.setTimestamp(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vn.ttapp.model.FnbDailyRevenue r = new vn.ttapp.model.FnbDailyRevenue();
                    java.sql.Date d = rs.getDate("day");
                    if (d != null) r.setDay(d.toLocalDate());
                    r.setTotalAmount(rs.getDouble("total_amount"));
                    list.add(r);
                }
            }
        }
        return list;
    }
}