/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.dao;

import vn.ttapp.model.FnbOrder;
import vn.ttapp.model.Trip; // <-- (Thêm) Model Trip
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import vn.ttapp.config.Db;

public class FnbOrderDao {

    /**
     * Thêm một FnbOrder vào DB và TRẢ VỀ ID vừa tạo.
     * (Hàm này của bạn đã đúng)
     */
    public long insert(FnbOrder order, Connection conn) throws SQLException {
        String sql = """
            INSERT INTO dbo.FnbOrder 
            (trip_id, seat_label, order_status, total_amount, payment_method, payment_status, 
             created_by_user_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, SYSUTCDATETIME(), SYSUTCDATETIME())
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, order.getTripId());
            ps.setNString(2, order.getSeatLabel());
            ps.setNString(3, order.getOrderStatus().name()); // Lấy tên Enum
            ps.setDouble(4, order.getTotalAmount());
            ps.setString(5, order.getPaymentMethod());
            ps.setNString(6, order.getPaymentStatus().name()); // Lấy tên Enum
            ps.setObject(7, order.getCreatedByUserId()); // Dùng setObject cho UUID

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Tạo FnbOrder thất bại, không có hàng nào được thêm.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1); // Trả về order_id
                } else {
                    throw new SQLException("Tạo FnbOrder thất bại, không lấy được ID.");
                }
            }
        }
    }

    /**
     * (HÀM HELPER)
     * Chuyển đổi ResultSet thành FnbOrder object
     */
    private FnbOrder map(ResultSet rs) throws SQLException {
        FnbOrder order = new FnbOrder();
        order.setOrderId(rs.getLong("order_id"));
        order.setTripId(rs.getInt("trip_id"));
        order.setSeatLabel(rs.getString("seat_label"));
        
        // (SỬA) Kiểm tra NULL trước khi dùng valueOf (để phòng hờ)
        String orderStatusStr = rs.getString("order_status");
        if (orderStatusStr != null) {
            order.setOrderStatus(FnbOrder.OrderStatus.valueOf(orderStatusStr));
        }
        
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setPaymentMethod(rs.getString("payment_method"));
        
        // (SỬA) Kiểm tra NULL trước khi dùng valueOf (để phòng hờ)
        String paymentStatusStr = rs.getString("payment_status");
        if (paymentStatusStr != null) {
            order.setPaymentStatus(FnbOrder.PaymentStatus.valueOf(paymentStatusStr));
        }
        
        long paymentId = rs.getLong("payment_id");
        if (!rs.wasNull()) order.setPaymentId(paymentId);
        
        order.setCreatedByUserId(UUID.fromString(rs.getString("created_by_user_id")));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        
        try {
            order.setCreatedByStaffName(rs.getString("staff_full_name"));
        } catch (SQLException e) { /* Bỏ qua nếu không JOIN */ }
        return order;
    }

    /**
     * (HÀM HELPER MỚI CHO BÁO CÁO/QUẢN LÝ)
     * Map dữ liệu Trip (đã join) vào một đối tượng Trip
     */
    private Trip mapTrip(ResultSet rs) throws SQLException {
        Trip trip = new Trip();
        trip.setTripId(rs.getInt("trip_id"));
        
        Timestamp departTs = rs.getTimestamp("depart_at");
        if(departTs != null) trip.setDepartAt(departTs.toLocalDateTime());
        // Set a human-readable departure string to avoid using fmt:formatDate on LocalDateTime in JSP
        try {
            if (departTs != null) {
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM");
                trip.setDepartAtStr(departTs.toLocalDateTime().format(fmt));
            }
        } catch (Exception e) {
            // ignore formatting errors
        }
        
        trip.setTrainCode(rs.getString("train_code"));
        trip.setRouteCode(rs.getString("route_code"));
        trip.setOriginName(rs.getString("origin_name"));
        trip.setDestName(rs.getString("dest_name"));
        return trip;
    }

    /**
     * (HÀM CHO MANAGER)
     * Lấy tất cả đơn hàng (đã join) trong khoảng thời gian.
     */
    public List<FnbOrder> findAllDetailedByDateRange(Timestamp from, Timestamp to) throws SQLException {
        List<FnbOrder> list = new ArrayList<>();
        String sql = """
            SELECT 
                o.*, 
                CAST(o.created_by_user_id AS NVARCHAR(36)) AS created_by_user_id,
                u.full_name AS staff_full_name,
                t.depart_at,
                tr.code AS train_code,
                r.code AS route_code,
                s_origin.name AS origin_name,
                s_dest.name AS dest_name
            FROM dbo.FnbOrder o
            JOIN dbo.Users u ON o.created_by_user_id = u.user_id
            JOIN dbo.Trip t ON o.trip_id = t.trip_id
            JOIN dbo.Train tr ON t.train_id = tr.train_id
            JOIN dbo.Route r ON t.route_id = r.route_id
            JOIN dbo.Station s_origin ON r.origin_station_id = s_origin.station_id
            JOIN dbo.Station s_dest ON r.dest_station_id = s_dest.station_id
            WHERE 
                o.created_at BETWEEN ? AND ?
            ORDER BY o.created_at DESC
        """;
        
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, from);
            ps.setTimestamp(2, to);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // =======================================================
                    // ==> (SỬA LỖI) BẠN BỊ THIẾU 3 DÒNG NÀY
                    // =======================================================
                    FnbOrder order = map(rs); 
                    Trip trip = mapTrip(rs); 
                    order.setTrip(trip); // <-- DÒNG NÀY BỊ THIẾU
                    list.add(order);
                    // =======================================================
                }
            }
        }
        return list;
    }

    /**
     * (HÀM CHO STAFF)
     * Lấy danh sách đơn hàng (đã join) THEO CHUYẾN TÀU và THEO STAFF
     */
    public List<FnbOrder> findDetailedOrdersByTripAndStaff(int tripId, UUID staffId) throws SQLException {
        List<FnbOrder> list = new ArrayList<>();
        String sql = """
            SELECT o.*, 
                   CAST(o.created_by_user_id AS NVARCHAR(36)) AS created_by_user_id,
                   u.full_name AS staff_full_name
            FROM dbo.FnbOrder o
            JOIN dbo.Users u ON o.created_by_user_id = u.user_id
            WHERE o.trip_id = ? AND o.created_by_user_id = ?
            ORDER BY o.created_at DESC
        """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, tripId);
            ps.setObject(2, staffId); // (Lọc theo staffId)
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs)); // (Dùng hàm map)
                }
            }
        }
        return list;
    }
    
    /**
     * (HÀM CẦN CHO Cập nhật)
     */
    public boolean updateStatus(long orderId, String orderStatus, String paymentStatus) throws SQLException {
        String sql = """
            UPDATE dbo.FnbOrder
            SET order_status = ?, payment_status = ?, updated_at = SYSUTCDATETIME()
            WHERE order_id = ?
        """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, orderStatus);
            ps.setNString(2, paymentStatus);
            ps.setLong(3, orderId);
            return ps.executeUpdate() > 0;
        }
    }
    
    // (Hàm cần cho Service)
    public FnbOrder findDetailedById(long orderId) throws SQLException {
        String sql = """
            SELECT o.*, 
                   CAST(o.created_by_user_id AS NVARCHAR(36)) AS created_by_user_id,
                   u.full_name AS staff_full_name
            FROM dbo.FnbOrder o
            JOIN dbo.Users u ON o.created_by_user_id = u.user_id
            WHERE o.order_id = ?
        """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs); // (Dùng map ở trên)
                }
            }
        }
        return null;
    }
}