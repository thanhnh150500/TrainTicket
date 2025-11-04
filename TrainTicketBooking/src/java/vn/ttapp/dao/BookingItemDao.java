package vn.ttapp.dao;

import vn.ttapp.model.BookingItem;
import vn.ttapp.config.Db;

import java.sql.*;
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
                ps.setString(5, i.getSegment()); // 'OUTBOUND'/'RETURN'
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
}
