package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Booking;
import vn.ttapp.model.BookingItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.List;

public interface BookingDao {

    Booking createDraftWithItems(
            String contactEmail,
            String contactPhone,
            int tripId,
            BigDecimal subtotal,
            List<BookingItem> items
    ) throws Exception;

    Booking findById(Long bookingId) throws Exception;

    public static class Impl implements BookingDao {

        @Override
        public Booking createDraftWithItems(String contactEmail, String contactPhone,
                int tripId, BigDecimal subtotal, List<BookingItem> items)
                throws Exception {

            Connection cn = null;
            boolean ok = false;
            try {
                cn = Db.getConnection();
                cn.setAutoCommit(false);

                // 1) Insert Booking (DRAFT)
                long bookingId;
                try (PreparedStatement ps = cn.prepareStatement("""
                    INSERT INTO dbo.Booking (user_id, contact_email, contact_phone, status,
                                             subtotal, discount_total, total_amount)
                    VALUES (NULL, ?, ?, 'DRAFT', ?, 0, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, contactEmail);
                    ps.setString(2, contactPhone);
                    ps.setBigDecimal(3, subtotal);
                    ps.setBigDecimal(4, subtotal);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        bookingId = rs.getLong(1);
                    }
                }

                // 2) Insert BookingItem
                try (PreparedStatement ps = cn.prepareStatement("""
                    INSERT INTO dbo.BookingItem(booking_id, trip_id, seat_id, seat_class_id,
                                                segment, passenger_id, base_price, discount_amount, amount)
                    VALUES (?, ?, ?, ?, ?, NULL, ?, 0, ?)
                """)) {
                    for (BookingItem bi : items) {
                        ps.setLong(1, bookingId);
                        ps.setInt(2, bi.getTripId());
                        ps.setInt(3, bi.getSeatId());
                        ps.setInt(4, bi.getSeatClassId());
                        ps.setString(5, bi.getSegment());
                        ps.setBigDecimal(6, bi.getBasePrice());
                        ps.setBigDecimal(7, bi.getAmount());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                cn.commit();
                ok = true;

                // 3) Build Booking model trả về
                Booking b = new Booking();
                b.setBookingId(bookingId);
                b.setContactEmail(contactEmail);
                b.setContactPhone(contactPhone);
                b.setSubtotal(subtotal);
                b.setDiscountTotal(BigDecimal.ZERO);
                b.setTotalAmount(subtotal);
                b.setStatus("DRAFT");
                b.setCreatedAt(OffsetDateTime.now());
                return b;

            } catch (Exception ex) {
                if (cn != null) {
                    cn.rollback();
                }
                throw ex;
            } finally {
                if (cn != null) {
                    try {
                        cn.setAutoCommit(true);
                        cn.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        @Override
        public Booking findById(Long bookingId) throws Exception {
            String sql = """
                SELECT booking_id, contact_email, contact_phone, subtotal, discount_total,
                       total_amount, status, created_at, updated_at
                FROM dbo.Booking WHERE booking_id = ?
            """;
            try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setLong(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    Booking b = new Booking();
                    b.setBookingId(rs.getLong("booking_id"));
                    b.setContactEmail(rs.getString("contact_email"));
                    b.setContactPhone(rs.getString("contact_phone"));
                    b.setSubtotal(rs.getBigDecimal("subtotal"));
                    b.setDiscountTotal(rs.getBigDecimal("discount_total"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setStatus(rs.getString("status"));
                    return b;
                }
            }
        }
    }
}
