package vn.ttapp.dao;

import vn.ttapp.model.Booking;
import vn.ttapp.model.BookingItem;
import vn.ttapp.model.SeatHold;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import vn.ttapp.config.Db;

public class BookingDao {

    private String genBookingCode() {
        String d = java.time.LocalDate.now().toString().replace("-", "");
        String rnd = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "TT" + d + rnd;
    }

    /**
     * Tạo booking DRAFT + items và gắn các SeatHold vào booking (booking_id)
     * trong 1 transaction.
     */
    public Booking createDraftWithItems(
            String contactEmail,
            String contactPhone,
            int tripId,
            BigDecimal subtotal,
            List<BookingItem> items,
            List<SeatHold> holds
    ) throws Exception {

        try (Connection cn = Db.getConnection()) {
            cn.setAutoCommit(false);
            try {
                String bookingCode = genBookingCode();
                int bookingId;

                // 1) Insert Booking (DRAFT)
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO Booking(booking_code, trip_id, buyer_email, buyer_phone, "
                        + "subtotal, discount, total_amount, status, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, 0, ?, 'DRAFT', SYSUTCDATETIME()); "
                        + "SELECT CAST(SCOPE_IDENTITY() AS INT);")) {
                    ps.setString(1, bookingCode);
                    ps.setInt(2, tripId);
                    ps.setString(3, contactEmail);
                    ps.setString(4, contactPhone);
                    ps.setInt(5, subtotal.intValue());  // nếu cột là DECIMAL thì chuyển sang setBigDecimal
                    ps.setInt(6, subtotal.intValue());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        bookingId = rs.getInt(1);
                    }
                }

                // 2) Insert Items -> BookingSeat
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO BookingSeat(booking_id, trip_id, carriage_id, seat_id, seat_code, seat_class, price) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    for (BookingItem bi : items) {
                        ps.setInt(1, bookingId);
                        ps.setInt(2, bi.getTripId());
                        // nếu bạn chưa có carriageId trong BookingItem, có thể set NULL bằng:
                        if (bi.getSeatCode() != null) {
                            // carriage_id: nếu chưa có, setNull
                            ps.setNull(3, Types.INTEGER);
                        } else {
                            ps.setNull(3, Types.INTEGER);
                        }
                        ps.setInt(4, bi.getSeatId());
                        ps.setString(5, bi.getSeatCode());
                        ps.setString(6, String.valueOf(bi.getSeatClassId())); // hoặc truyền tên hạng nếu bạn lưu string
                        ps.setInt(7, bi.getAmount().intValue()); // nếu cột price là DECIMAL -> dùng setBigDecimal
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // 3) Gắn các hold vào booking (tránh dùng lại)
                if (holds != null && !holds.isEmpty()) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE SeatHold SET booking_id=? "
                            + "WHERE seat_hold_id=? AND booking_id IS NULL")) {
                        for (SeatHold h : holds) {
                            ps.setInt(1, bookingId);
                            ps.setInt(2, h.seatHoldId);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                cn.commit();

                // 4) Map kết quả
                Booking b = new Booking();
                b.bookingId = bookingId;
                b.bookingCode = bookingCode;
                b.tripId = tripId;
                b.buyerEmail = contactEmail;
                b.buyerPhone = contactPhone;
                b.subtotal = subtotal.intValue();
                b.discount = 0;
                b.totalAmount = subtotal.intValue();
                b.status = vn.ttapp.model.BookingStatus.PENDING; // hoặc giữ DRAFT tùy flow
                b.createdAt = OffsetDateTime.now();
                return b;

            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public Booking findById(Long bookingId) throws SQLException {
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(
                "SELECT booking_id, booking_code, trip_id, buyer_name, buyer_email, buyer_phone, "
                + "subtotal, discount, total_amount, status, created_at "
                + "FROM Booking WHERE booking_id=?")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Booking b = new Booking();
                b.bookingId = rs.getInt("booking_id");
                b.bookingCode = rs.getString("booking_code");
                b.tripId = rs.getInt("trip_id");
                b.buyerName = rs.getString("buyer_name");
                b.buyerEmail = rs.getString("buyer_email");
                b.buyerPhone = rs.getString("buyer_phone");
                b.subtotal = rs.getInt("subtotal");
                b.discount = rs.getInt("discount");
                b.totalAmount = rs.getInt("total_amount");
                b.status = vn.ttapp.model.BookingStatus.valueOf(rs.getString("status"));
                b.createdAt = rs.getObject("created_at", java.time.OffsetDateTime.class);
                return b;
            }
        }
    }
}
