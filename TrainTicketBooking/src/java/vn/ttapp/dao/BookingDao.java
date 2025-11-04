package vn.ttapp.dao;

import vn.ttapp.model.Booking;
import vn.ttapp.model.BookingItem;
import vn.ttapp.model.BookingSummary;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface BookingDao {

    Booking createDraftWithItems(
            String contactEmail,
            String contactPhone,
            int tripId,
            BigDecimal subtotal,
            List<BookingItem> items
    ) throws Exception;

    Booking findById(Long bookingId) throws Exception;

    long countByUser(UUID userId) throws Exception;

    List<BookingSummary> listSummariesByUser(UUID userId, String status,
            int page, int pageSize) throws Exception;

    // ====================== Implementation ======================
    public static class Impl implements BookingDao {

        @Override
        public Booking createDraftWithItems(String contactEmail, String contactPhone,
                int tripId, BigDecimal subtotal, List<BookingItem> items)
                throws Exception {
            Connection cn = null;
            try {
                cn = vn.ttapp.config.Db.getConnection();
                cn.setAutoCommit(false);

                long bookingId;

                // 1) Insert Booking (DRAFT)
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
                        if (!rs.next()) {
                            throw new SQLException("No generated key for Booking.");
                        }
                        bookingId = rs.getLong(1);
                    }
                }

                // 2) Insert BookingItem
                try (PreparedStatement ps = cn.prepareStatement("""
                    INSERT INTO dbo.BookingItem (booking_id, trip_id, seat_id, seat_class_id,
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
                if (cn != null) try {
                    cn.rollback();
                } catch (Exception ignore) {
                }
                throw ex;
            } finally {
                if (cn != null) try {
                    cn.setAutoCommit(true);
                    cn.close();
                } catch (Exception ignore) {
                }
            }
        }

        @Override
        public Booking findById(Long bookingId) throws Exception {
            String sql = """
                SELECT booking_id, user_id, contact_email, contact_phone, subtotal, discount_total,
                       total_amount, status, created_at, updated_at, hold_expires_at, paid_at
                FROM dbo.Booking
                WHERE booking_id = ?
            """;
            try (Connection cn = vn.ttapp.config.Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setLong(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    Booking b = new Booking();
                    b.setBookingId(rs.getLong("booking_id"));
                    Object uidObj = rs.getObject("user_id"); // có thể NULL
                    if (uidObj instanceof UUID u) {
                        b.setUserId(u);
                    }
                    b.setContactEmail(rs.getString("contact_email"));
                    b.setContactPhone(rs.getString("contact_phone"));
                    b.setSubtotal(rs.getBigDecimal("subtotal"));
                    b.setDiscountTotal(rs.getBigDecimal("discount_total"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setStatus(rs.getString("status"));

                    Timestamp c = rs.getTimestamp("created_at");
                    if (c != null) {
                        b.setCreatedAt(c.toInstant().atOffset(java.time.ZoneOffset.UTC));
                    }
                    Timestamp u = rs.getTimestamp("updated_at");
                    if (u != null) {
                        b.setUpdatedAt(u.toInstant().atOffset(java.time.ZoneOffset.UTC));
                    }
                    Timestamp h = rs.getTimestamp("hold_expires_at");
                    if (h != null) {
                        b.setHoldExpiresAt(h.toInstant().atOffset(java.time.ZoneOffset.UTC));
                    }
                    Timestamp p = rs.getTimestamp("paid_at");
                    if (p != null) {
                        b.setPaidAt(p.toInstant().atOffset(java.time.ZoneOffset.UTC));
                    }
                    return b;
                }
            }
        }

        @Override
        public long countByUser(UUID userId) throws Exception {
            String sql = "SELECT COUNT(*) FROM dbo.Booking WHERE user_id = ?";
            try (Connection cn = vn.ttapp.config.Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setObject(1, userId); // MSSQL JDBC hỗ trợ UUID
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }

        @Override
        public List<BookingSummary> listSummariesByUser(UUID userId, String status,
                int page, int pageSize) throws Exception {
            if (page < 1) {
                page = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }
            int offset = (page - 1) * pageSize;

            // Sử dụng đúng bảng/alias theo schema: Seat, Trip, Train; cột Seat.code
            StringBuilder sb = new StringBuilder("""
                SELECT 
                    b.booking_id,
                    MIN(CONVERT(varchar(10), tr.trip_id)) AS trip_code,   -- hiển thị trip_id như mã
                    MIN(t.code)                           AS train_name,  -- mã tàu (nếu muốn tên thì MIN(t.name))
                    STRING_AGG(s.code, ', ')              AS seat_codes,  -- ghép danh sách ghế
                    COUNT(1)                              AS item_count,
                    b.total_amount,
                    b.status,
                    b.created_at,
                    b.paid_at
                FROM dbo.Booking b
                JOIN dbo.BookingItem bi ON bi.booking_id = b.booking_id
                JOIN dbo.Seat s         ON s.seat_id     = bi.seat_id
                JOIN dbo.Trip tr        ON tr.trip_id    = bi.trip_id
                JOIN dbo.Train t        ON t.train_id    = tr.train_id
                WHERE b.user_id = ?
            """);
            if (status != null && !status.isBlank()) {
                sb.append(" AND b.status = ? ");
            }
            sb.append("""
                GROUP BY b.booking_id, b.total_amount, b.status, b.created_at, b.paid_at
                ORDER BY b.created_at DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """);

            try (Connection cn = vn.ttapp.config.Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sb.toString())) {
                int i = 1;
                ps.setObject(i++, userId);
                if (status != null && !status.isBlank()) {
                    ps.setString(i++, status);
                }
                ps.setInt(i++, offset);
                ps.setInt(i++, pageSize);

                List<BookingSummary> list = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BookingSummary bs = new BookingSummary();
                        bs.setBookingId(rs.getLong("booking_id"));
                        bs.setTripCode(rs.getString("trip_code"));
                        bs.setTrainName(rs.getString("train_name"));
                        bs.setSeatCodes(rs.getString("seat_codes"));
                        bs.setItemCount(rs.getInt("item_count"));
                        bs.setTotalAmount(rs.getBigDecimal("total_amount"));
                        bs.setStatus(rs.getString("status"));

                        Timestamp cts = rs.getTimestamp("created_at");
                        if (cts != null) {
                            bs.setCreatedAt(cts.toInstant().atOffset(java.time.ZoneOffset.UTC));
                        }
                        Timestamp pts = rs.getTimestamp("paid_at");
                        if (pts != null) {
                            bs.setPaidAt(pts.toInstant().atOffset(java.time.ZoneOffset.UTC));
                        }

                        list.add(bs);
                    }
                }
                return list;
            }
        }
    }
}
