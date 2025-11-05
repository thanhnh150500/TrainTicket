/*

Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license

Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
*/
package vn.ttapp.dao;

import vn.ttapp.model.Trip; // <-- THÊM IMPORT
import vn.ttapp.model.User;
import vn.ttapp.config.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // <-- THÊM IMPORT

public class TripStaffDao {

// (Hàm helper để map User, tránh lặp code)
private User mapUser(ResultSet rs) throws SQLException {
    User u = new User();
    // (SỬA) Lấy đúng kiểu String UUID từ DB
    // User.userId is a UUID in the model; try to read as UUID, fallback to String
    try {
        // JDBC 4.2 may support getObject with UUID.class
        java.util.UUID uuid = null;
        try {
            uuid = rs.getObject("user_id", java.util.UUID.class);
        } catch (Throwable ignore) {
            // fallback to string parse
            String s = rs.getString("user_id");
            if (s != null) uuid = java.util.UUID.fromString(s);
        }
        if (uuid != null) u.setUserId(uuid);
    } catch (Exception ignore) {
        // leave null if cannot parse
    }
    u.setFullName(rs.getString("full_name"));
    u.setEmail(rs.getString("email"));
    
    // (Bổ sung các trường còn thiếu từ SQL)
    try {
        u.setActive(rs.getBoolean("is_active"));
    } catch (Exception e) {
        // Bỏ qua nếu các cột này không được SELECT (ví dụ: trong findByTripId cũ)
    }
    return u;
}

// Lấy tất cả staff F&B (Dùng cho trang Manager)
public List<User> findAllStaffFNB() throws SQLException {
    String sql = """
                   SELECT CAST(u.user_id AS NVARCHAR(36)) AS user_id, u.email, u.full_name, u.phone, u.address, u.is_active
                   FROM Users u
                   INNER JOIN UserRoles ur ON u.user_id = ur.user_id
                   INNER JOIN Roles r ON ur.role_id = r.role_id
                   WHERE r.code = 'STAFF_FNB' AND u.is_active = 1
                 """;
    try (Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
        List<User> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapUser(rs)); // (Dùng hàm helper)
        }
        return list;
    }
}

// Lấy staff theo trip (Dùng cho trang Manager)
public List<User> findByTripId(int tripId) throws SQLException {
    // (SỬA) Thêm các cột để dùng chung mapUser
    String sql = """
                   SELECT CAST(u.user_id AS NVARCHAR(36)) AS user_id, u.full_name, u.email, u.phone, u.address, u.is_active
                   FROM TripStaff ts
                   INNER JOIN Users u ON ts.user_id = u.user_id
                   WHERE ts.trip_id = ?
                 """;
    try (Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, tripId);
        try (ResultSet rs = ps.executeQuery()) {
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapUser(rs)); // (Dùng hàm helper)
            }
            return list;
        }
    }
}

// Gán staff cho trip (xóa cũ + thêm mới) - (Dùng cho trang Manager)
public void assignStaffToTrip(int tripId, List<String> staffIds, String staffRole) throws SQLException {
    
    // (SỬA) Chỉ xóa staff có vai trò staffRole, không xóa hết
    String deleteSql = "DELETE FROM TripStaff WHERE trip_id = ? AND staff_role = ?";
    String insertSql = "INSERT INTO TripStaff (trip_id, user_id, staff_role) VALUES (?, ?, ?)";

    // (SỬA) Viết lại logic transaction cho đúng
    Connection c = null;
    try {
        c = Db.getConnection();
        c.setAutoCommit(false); // Bắt đầu transaction

        // 1. Xóa
        try (PreparedStatement del = c.prepareStatement(deleteSql)) {
            del.setInt(1, tripId);
            del.setString(2, staffRole); // (VD: chỉ xóa STAFF_FNB)
            del.executeUpdate();
        }

        // 2. Thêm mới
        try (PreparedStatement ins = c.prepareStatement(insertSql)) {
            for (String uid : staffIds) {
                ins.setInt(1, tripId);
                
                // (SỬA LỖI) Chuyển String UUID sang Object UUID cho SQL Server
                ins.setObject(2, UUID.fromString(uid)); 
                
                ins.setString(3, staffRole);
                ins.addBatch();
            }
            ins.executeBatch();
        }
        
        c.commit(); // Lưu transaction
        
    } catch (SQLException e) {
        if (c != null) c.rollback(); // Hủy nếu lỗi
        throw e; // Báo lỗi ra ngoài
    } finally {
        if (c != null) {
            c.setAutoCommit(true);
            c.close();
        }
    }
}

// =====================================================================
// ==> HÀM MỚI (Dùng cho StaffOrderServlet)
// =====================================================================

// (Hàm helper để map Trip, dùng model Trip bạn đã cung cấp)
private Trip mapTrip(ResultSet rs) throws SQLException {
    Trip trip = new Trip();
    trip.setTripId(rs.getInt("trip_id"));
    
    Timestamp departTs = rs.getTimestamp("depart_at");
    if(departTs != null) trip.setDepartAt(departTs.toLocalDateTime());
    
    Timestamp arriveTs = rs.getTimestamp("arrive_at");
    if(arriveTs != null) trip.setArriveAt(arriveTs.toLocalDateTime());
    
    trip.setStatus(rs.getString("status"));
    
    trip.setTrainCode(rs.getString("train_code"));
    trip.setRouteCode(rs.getString("route_code"));
    trip.setOriginName(rs.getString("origin_name"));
    trip.setDestName(rs.getString("dest_name"));
    
    return trip;
}

/**
 * (HÀM MỚI)
 * Tìm các chuyến tàu ĐANG CHẠY (status='RUNNING')
 * mà nhân viên (staffId) được phân công.
 * (Nhận vào String userId, vì model User của bạn dùng String)
 */
public List<Trip> findActiveTripsByStaff(String staffId) throws SQLException {
    List<Trip> list = new ArrayList<>();
    
    String sql = """
        SELECT 
            t.trip_id, t.depart_at, t.arrive_at, t.status,
            tr.code AS train_code,
            r.code AS route_code,
            s_origin.name AS origin_name,
            s_dest.name AS dest_name
        FROM dbo.TripStaff ts
        JOIN dbo.Trip t ON ts.trip_id = t.trip_id
        JOIN dbo.Train tr ON t.train_id = tr.train_id
        JOIN dbo.Route r ON t.route_id = r.route_id
        JOIN dbo.Station s_origin ON r.origin_station_id = s_origin.station_id
        JOIN dbo.Station s_dest ON r.dest_station_id = s_dest.station_id
        WHERE 
            ts.user_id = ? 
            AND t.status = 'RUNNING'  -- (Chỉ lấy chuyến đang chạy)
        ORDER BY t.depart_at
    """;
    
    try (Connection conn = Db.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        // Chuyển String UUID sang Object UUID
        ps.setObject(1, UUID.fromString(staffId)); 
        
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTrip(rs));
            }
        }
    }
    return list;
}

/**
 * (HÀM MỚI)
 * Kiểm tra nhanh xem staff (staffId) có được phân công
 * vào một chuyến cụ thể (tripId) hay không.
 * (Dùng để bảo mật doPost)
 */
public boolean isStaffAssignedToTrip(String staffId, int tripId) throws SQLException {
    String sql = "SELECT 1 FROM TripStaff WHERE user_id = ? AND trip_id = ?";
    try (Connection conn = Db.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setObject(1, UUID.fromString(staffId)); // Chuyển String sang UUID
        ps.setInt(2, tripId);
        
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next(); // true nếu tìm thấy
        }
    }
}


}