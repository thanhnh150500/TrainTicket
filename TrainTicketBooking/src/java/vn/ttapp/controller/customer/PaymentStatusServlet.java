package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@WebServlet("/payment/status")
public class PaymentStatusServlet extends HttpServlet {

    // KHÔNG lấy paid_at nếu DB chưa có cột này
    private static final String BOOKING_SQL = """
        SELECT b.booking_id, b.user_id, b.status, 
               b.subtotal, b.discount_total, b.total_amount, 
               b.hold_expires_at, b.created_at,
               MAX(t.payment_id) AS latest_payment_id
          FROM dbo.Booking b
          LEFT JOIN dbo.PaymentTransaction t ON t.booking_id = b.booking_id
         WHERE b.booking_id = ?
         GROUP BY b.booking_id, b.user_id, b.status, 
                  b.subtotal, b.discount_total, b.total_amount, 
                  b.hold_expires_at, b.created_at
    """;

    private static final String ITEMS_SQL = """
        SELECT bi.booking_item_id, bi.trip_id, bi.seat_id, bi.seat_class_id,
               bi.base_price, bi.discount_amount, bi.amount,
               s.code AS seat_code, 
               c.code AS carriage_code
          FROM dbo.BookingItem bi
          JOIN dbo.Seat s ON s.seat_id = bi.seat_id
          LEFT JOIN dbo.Carriage c ON c.carriage_id = s.carriage_id
         WHERE bi.booking_id = ?
         ORDER BY bi.booking_item_id
    """;

    private static final String PAYMENT_LAST_SQL = """
        SELECT TOP (1) payment_id, method, amount, currency, status, created_at
          FROM dbo.PaymentTransaction
         WHERE booking_id = ?
         ORDER BY payment_id DESC
    """;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        final String bid = req.getParameter("bookingId");
        if (bid == null || bid.isBlank()) {
            res.sendError(400, "Missing bookingId");
            return;
        }

        final long bookingId;
        try {
            bookingId = Long.parseLong(bid.trim());
        } catch (NumberFormatException e) {
            res.sendError(400, "Invalid bookingId");
            return;
        }

        try (Connection cn = vn.ttapp.config.Db.getConnection()) {
            cn.setAutoCommit(false);

            // 1) Load booking
            BigDecimal subtotal = BigDecimal.ZERO, discount = BigDecimal.ZERO, total = BigDecimal.ZERO;
            String bookingStatus = null;

            try (PreparedStatement ps = cn.prepareStatement(BOOKING_SQL)) {
                ps.setLong(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        cn.rollback();
                        res.sendError(404, "Booking not found");
                        return;
                    }
                    bookingStatus = rs.getString("status");
                    subtotal = rs.getBigDecimal("subtotal");
                    discount = rs.getBigDecimal("discount_total");
                    total = rs.getBigDecimal("total_amount");
                }
            }

            // 2) Nếu booking chưa PAID -> xác nhận thủ công (idempotent)
            if (!"PAID".equalsIgnoreCase(bookingStatus)) {
                // 2.1) Booking -> PAID
                try (PreparedStatement ps = cn.prepareStatement("""
                    UPDATE dbo.Booking
                       SET status = 'PAID'
                     WHERE booking_id = ? AND status <> 'PAID'
                """)) {
                    ps.setLong(1, bookingId);
                    ps.executeUpdate();
                }

                // 2.2) Xóa lock của booking này (tránh CHECK/UQ & không còn giữ ghế)
                try (PreparedStatement ps = cn.prepareStatement("""
                    DELETE FROM dbo.SeatLock WHERE booking_id = ?
                """)) {
                    ps.setLong(1, bookingId);
                    ps.executeUpdate();
                }

                // 2.3) PaymentTransaction -> SUCCESS (khớp CHECK constraint hiện tại)
                // Chỉ chuyển từ INITIATED/PENDING sang SUCCESS, tránh đụng FAILED/CANCELED
                try (PreparedStatement ps = cn.prepareStatement("""
                    UPDATE dbo.PaymentTransaction
                       SET status = 'SUCCESS'
                     WHERE booking_id = ?
                       AND status IN ('INITIATED','PENDING')
                """)) {
                    ps.setLong(1, bookingId);
                    ps.executeUpdate();
                }

                bookingStatus = "PAID";
            }

            // 3) Load items
            final List<Map<String, Object>> items = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(ITEMS_SQL)) {
                ps.setLong(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> it = new LinkedHashMap<>();
                        it.put("bookingItemId", rs.getLong("booking_item_id"));
                        it.put("tripId", rs.getInt("trip_id"));
                        it.put("seatId", rs.getInt("seat_id"));
                        it.put("seatClassId", rs.getInt("seat_class_id"));
                        it.put("seatCode", rs.getString("seat_code"));
                        it.put("carriageCode", rs.getString("carriage_code"));
                        it.put("basePrice", rs.getBigDecimal("base_price"));
                        it.put("discountAmount", rs.getBigDecimal("discount_amount"));
                        it.put("amount", rs.getBigDecimal("amount"));
                        items.add(it);
                    }
                }
            }

            // 4) Giao dịch thanh toán cuối (nếu có)
            Map<String, Object> payment = new LinkedHashMap<>();
            try (PreparedStatement ps = cn.prepareStatement(PAYMENT_LAST_SQL)) {
                ps.setLong(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        payment.put("paymentId", rs.getLong("payment_id"));
                        payment.put("method", rs.getString("method"));
                        payment.put("amount", rs.getBigDecimal("amount"));
                        payment.put("currency", rs.getString("currency"));
                        payment.put("status", rs.getString("status"));
                        Timestamp ts = rs.getTimestamp("created_at");
                        payment.put("createdAt", ts != null ? ts.toInstant() : null);
                    }
                }
            }

            cn.commit();

            // 5) Gửi sang JSP hiển thị trạng thái
            req.setAttribute("bookingId", bookingId);
            req.setAttribute("status", bookingStatus); // 'PAID'
            req.setAttribute("subtotal", subtotal);
            req.setAttribute("discount", discount);
            req.setAttribute("total", total);
            req.setAttribute("items", items);
            req.setAttribute("payment", payment);

            req.getRequestDispatcher("/WEB-INF/views/customer/payment_status.jsp").forward(req, res);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
