package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.BookingItem;
import vn.ttapp.model.TripInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingItemDao {

    public void batchInsert(List<BookingItem> items) throws SQLException {
        String sql = """
            INSERT INTO dbo.BookingItem
              (booking_id, trip_id, seat_id, seat_class_id, segment, passenger_id, base_price, discount_amount, amount)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            for (BookingItem i : items) {
                ps.setLong(1, i.getBookingId());
                ps.setInt(2, i.getTripId());
                ps.setInt(3, i.getSeatId());
                ps.setInt(4, i.getSeatClassId());
                ps.setString(5, i.getSegment()); // 'OUTBOUND' / 'RETURN'

                if (i.getPassengerId() != null) {
                    ps.setLong(6, i.getPassengerId());
                } else {
                    ps.setNull(6, Types.BIGINT);
                }

                ps.setBigDecimal(7, i.getBasePrice());
                ps.setBigDecimal(8, i.getDiscountAmount());
                ps.setBigDecimal(9, i.getAmount());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /* =========================
       B) LẤY THÔNG TIN TRIP/TRAIN CỦA ĐƠN
       ========================= */
    public TripInfo getTripInfo(long bookingId) throws SQLException {
        String sql = """
            SELECT TOP 1
                tr.trip_id,
                t.code AS train_code,
                t.name AS train_name,
                tr.depart_at,
                tr.arrive_at
            FROM dbo.BookingItem bi
            JOIN dbo.Trip  tr ON tr.trip_id  = bi.trip_id
            JOIN dbo.Train t  ON t.train_id  = tr.train_id
            WHERE bi.booking_id = ?
            ORDER BY bi.booking_item_id ASC
        """;

        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                TripInfo ti = new TripInfo();
                ti.setTripId(rs.getInt("trip_id"));
                ti.setTrainCode(rs.getString("train_code"));
                ti.setTrainName(rs.getString("train_name"));
                ti.setDepartAt(rs.getTimestamp("depart_at"));
                ti.setArriveAt(rs.getTimestamp("arrive_at"));
                return ti;
            }
        }
    }

    /* =========================
       C) DANH SÁCH CHI TIẾT GHẾ / GIÁ CỦA ĐƠN
       ========================= */
    public List<BookingItem> listDetails(long bookingId) throws SQLException {
        String sql = """
            SELECT 
                bi.booking_item_id,
                s.code        AS seat_code,
                c.code        AS carriage_code,
                sc.name       AS seat_class_name,
                bi.base_price,
                bi.discount_amount,
                bi.amount
            FROM dbo.BookingItem bi
            JOIN dbo.Seat      s  ON s.seat_id       = bi.seat_id
            JOIN dbo.Carriage  c  ON c.carriage_id   = s.carriage_id
            JOIN dbo.SeatClass sc ON sc.seat_class_id= s.seat_class_id
            WHERE bi.booking_id = ?
            ORDER BY ISNULL(c.sort_order, 9999), s.code
        """;

        List<BookingItem> list = new ArrayList<>();
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingItem bi = new BookingItem();
                    bi.setBookingItemId(rs.getLong("booking_item_id"));
                    bi.setSeatCode(rs.getString("seat_code"));
                    bi.setCarriageCode(rs.getString("carriage_code"));
                    bi.setSeatClassName(rs.getString("seat_class_name"));
                    bi.setBasePrice(rs.getBigDecimal("base_price"));
                    bi.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    bi.setAmount(rs.getBigDecimal("amount"));
                    list.add(bi);
                }
            }
        }
        return list;
    }
}
