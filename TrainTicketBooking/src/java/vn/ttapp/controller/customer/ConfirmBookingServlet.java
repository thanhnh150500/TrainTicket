package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/confirm-booking")
public class ConfirmBookingServlet extends HttpServlet {

    private static final BigDecimal SERVICE_FEE = new BigDecimal("10000");
    private static final BigDecimal INSURANCE_PER_PAX = new BigDecimal("1000");
    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    /* ======================= Helpers ======================= */
    private static Integer parseIntOrNull(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseMoneyOrNull(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty()) {
            return null;
        }
        s = s.replaceAll("[^0-9.\\-]", ""); // loại dấu phẩy, ký hiệu đ
        if (s.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseDobOrNull(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s, DOB_FMT);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static void badRequest(HttpServletResponse res, String msg) throws IOException {
        res.setStatus(400);
        res.setContentType("text/plain; charset=UTF-8");
        res.getWriter().write(msg);
    }

    private static String placeholders(int n) {
        if (n <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sb.toString();
    }

    // đọc mảng theo 2 tên (vd: "seatId" và "seatId[]")
    private static String[] pickParams(HttpServletRequest req, String a, String b) {
        String[] v = req.getParameterValues(a);
        if (v == null || v.length == 0) {
            v = req.getParameterValues(b);
        }
        return v;
    }

    // loại chuỗi rỗng/null
    private static String[] compact(String[] arr) {
        if (arr == null) {
            return null;
        }
        List<String> out = new ArrayList<>();
        for (String s : arr) {
            if (s != null && !s.trim().isEmpty()) {
                out.add(s.trim());
            }
        }
        return out.toArray(new String[0]);
    }

    // Tải seat_class_id từ bảng Seat cho danh sách seat_id
    private static Map<Integer, Integer> loadSeatClassIds(Connection cn, List<Integer> seatIds) throws SQLException {
        Map<Integer, Integer> map = new HashMap<>();
        if (seatIds.isEmpty()) {
            return map;
        }

        StringBuilder sb = new StringBuilder("SELECT seat_id, seat_class_id FROM dbo.Seat WHERE seat_id IN (");
        for (int i = 0; i < seatIds.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        sb.append(')');

        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            int idx = 1;
            for (Integer id : seatIds) {
                ps.setInt(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt(1), rs.getInt(2));
                }
            }
        }
        return map;
    }

    // Check ghế bị LOCK còn hạn / đã PAID
    private static boolean hasActiveConflicts(Connection cn, int tripId, List<Integer> seatIds) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) {
            return false;
        }

        String sqlLock = "SELECT COUNT(1) FROM dbo.SeatLock WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE trip_id = ? AND seat_id IN (" + placeholders(seatIds.size()) + ") "
                + "AND status = 'LOCKED' AND expires_at > SYSUTCDATETIME()";
        try (PreparedStatement ps = cn.prepareStatement(sqlLock)) {
            int idx = 1;
            ps.setInt(idx++, tripId);
            for (Integer sid : seatIds) {
                ps.setInt(idx++, sid);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        String sqlPaid = "SELECT COUNT(1) FROM dbo.BookingItem bi "
                + "JOIN dbo.Booking b ON b.booking_id = bi.booking_id "
                + "WHERE bi.trip_id = ? AND bi.seat_id IN (" + placeholders(seatIds.size()) + ") "
                + "AND b.status = 'PAID'";
        try (PreparedStatement ps = cn.prepareStatement(sqlPaid)) {
            int idx = 1;
            ps.setInt(idx++, tripId);
            for (Integer sid : seatIds) {
                ps.setInt(idx++, sid);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /* ======================= Main ======================= */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // ---------- Đọc & validate cơ bản ----------
        Integer tripIdBox = parseIntOrNull(req.getParameter("tripId"));
        if (tripIdBox == null) {
            badRequest(res, "Thiếu hoặc sai tripId");
            return;
        }
        int tripId = tripIdBox;

        String phone = req.getParameter("phone");
        String email = req.getParameter("email");

        // Chấp nhận cả tên có [] và không []
        String[] seatIdsStr = compact(pickParams(req, "seatId", "seatId[]"));
        String[] seatClassIdStr = compact(pickParams(req, "seatClassId", "seatClassId[]")); // có thể thiếu
        String[] pricesStr = compact(pickParams(req, "price", "price[]"));             // có thể thiếu

        String[] fullnames = pickParams(req, "fullname[]", "fullname");
        String[] dobs = pickParams(req, "dob[]", "dob");
        String[] idnums = pickParams(req, "idNumber[]", "idNumber");

        if (seatIdsStr == null || seatIdsStr.length == 0) {
            badRequest(res, "Không có ghế nào được chọn");
            return;
        }

        // ---------- Chuẩn hoá seatIds ----------
        List<Integer> seatIds = new ArrayList<>();
        for (String s : seatIdsStr) {
            Integer v = parseIntOrNull(s);
            if (v != null) {
                seatIds.add(v);
            }
        }
        if (seatIds.isEmpty()) {
            badRequest(res, "Danh sách ghế không hợp lệ");
            return;
        }

        Connection cn = null;
        boolean ok = false;

        try {
            cn = vn.ttapp.config.Db.getConnection();
            cn.setAutoCommit(false);

            // Lấy routeId & travelDate của trip
            int routeId;
            LocalDate travelDate;
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT route_id, CAST(depart_at AS date) AS d FROM dbo.Trip WHERE trip_id = ?")) {
                ps.setInt(1, tripId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        badRequest(res, "Trip không tồn tại");
                        return;
                    }
                    routeId = rs.getInt("route_id");
                    travelDate = rs.getDate("d").toLocalDate();
                }
            }

            // ---------- Suy seatClassId khi thiếu ----------
            Map<Integer, Integer> classBySeat = new HashMap<>();
            if (seatClassIdStr != null) {
                int n = Math.min(seatIds.size(), seatClassIdStr.length);
                for (int i = 0; i < n; i++) {
                    Integer sc = parseIntOrNull(seatClassIdStr[i]);
                    if (sc != null) {
                        classBySeat.put(seatIds.get(i), sc);
                    }
                }
            }
            if (classBySeat.size() < seatIds.size()) {
                Map<Integer, Integer> fromDb = loadSeatClassIds(cn, seatIds);
                for (Integer sid : seatIds) {
                    if (!classBySeat.containsKey(sid)) {
                        Integer sc = fromDb.get(sid);
                        if (sc == null) {
                            badRequest(res, "Không tìm thấy hạng ghế cho seatId=" + sid);
                            return;
                        }
                        classBySeat.put(sid, sc);
                    }
                }
            }

            // ---------- Lắp mảng đồng bộ + fallback giá ----------
            List<Integer> seatClassIds = new ArrayList<>(seatIds.size());
            List<BigDecimal> prices = new ArrayList<>(seatIds.size());
            Map<Integer, BigDecimal> priceByClass = new HashMap<>();

            for (int i = 0; i < seatIds.size(); i++) {
                int sid = seatIds.get(i);
                int scid = classBySeat.get(sid);
                seatClassIds.add(scid);

                BigDecimal p = (pricesStr != null && pricesStr.length > i) ? parseMoneyOrNull(pricesStr[i]) : null;
                if (p == null || p.compareTo(BigDecimal.ZERO) <= 0) {
                    p = priceByClass.get(scid);
                    if (p == null) {
                        try (PreparedStatement fps = cn.prepareStatement("""
                            SELECT TOP (1) base_price
                            FROM dbo.FareRule
                            WHERE route_id=? AND seat_class_id=?
                              AND effective_from<=? AND (effective_to IS NULL OR effective_to>=?)
                            ORDER BY effective_from DESC
                        """)) {
                            fps.setInt(1, routeId);
                            fps.setInt(2, scid);
                            fps.setDate(3, java.sql.Date.valueOf(travelDate));
                            fps.setDate(4, java.sql.Date.valueOf(travelDate));
                            try (ResultSet frs = fps.executeQuery()) {
                                if (!frs.next()) {
                                    badRequest(res, "Không tìm thấy giá cho hạng ghế (seatId=" + sid + ")");
                                    return;
                                }
                                p = frs.getBigDecimal(1);
                                priceByClass.put(scid, p);
                            }
                        }
                    }
                }
                prices.add(p);
            }

            // ---------- Tính tiền ----------
            BigDecimal subtotal = BigDecimal.ZERO;
            for (BigDecimal p : prices) {
                subtotal = subtotal.add(p);
            }
            BigDecimal insurance = INSURANCE_PER_PAX.multiply(BigDecimal.valueOf(seatIds.size()));
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal total = subtotal.add(insurance).add(SERVICE_FEE).subtract(discount);

            // ---------- Check conflict ----------
            if (hasActiveConflicts(cn, tripId, seatIds)) {
                cn.rollback();
                req.setAttribute("error", "Một hoặc nhiều ghế đã bị giữ/đã bán. Vui lòng chọn ghế khác.");
                req.getRequestDispatcher("/WEB-INF/views/customer/checkout.jsp").forward(req, res);
                return;
            }

            // ---------- Tạo booking (HOLD +5') ----------
            UUID userId = UUID.randomUUID();
            long bookingId;
            try (PreparedStatement ps = cn.prepareStatement("""
                INSERT INTO dbo.Booking(user_id, contact_email, contact_phone, status, subtotal, discount_total, total_amount, hold_expires_at)
                VALUES (?, ?, ?, 'HOLD', ?, ?, ?, DATEADD(MINUTE, 5, SYSUTCDATETIME()))
            """, Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, userId);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setBigDecimal(4, subtotal);
                ps.setBigDecimal(5, discount);
                ps.setBigDecimal(6, total);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    bookingId = rs.getLong(1);
                }
            }

            // ---------- Passengers ----------
            if (fullnames != null && fullnames.length > 0) {
                try (PreparedStatement ps = cn.prepareStatement("""
                    INSERT INTO dbo.Passenger(booking_id, full_name, birth_date, id_number, phone, email)
                    VALUES (?, ?, ?, ?, ?, ?)
                """)) {
                    for (int i = 0; i < fullnames.length; i++) {
                        String fn = fullnames[i];
                        String idn = (idnums != null && idnums.length > i) ? idnums[i] : null;
                        LocalDate dob = (dobs != null && dobs.length > i) ? parseDobOrNull(dobs[i]) : null;

                        ps.setLong(1, bookingId);
                        ps.setString(2, fn);
                        if (dob != null) {
                            ps.setDate(3, java.sql.Date.valueOf(dob));
                        } else {
                            ps.setNull(3, Types.DATE);
                        }
                        ps.setString(4, idn);
                        ps.setString(5, phone);
                        ps.setString(6, email);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // ---------- Booking items ----------
            try (PreparedStatement ps = cn.prepareStatement("""
                INSERT INTO dbo.BookingItem(booking_id, trip_id, seat_id, seat_class_id, segment, passenger_id, base_price, discount_amount, amount)
                VALUES (?, ?, ?, ?, 'OUTBOUND', ?, ?, 0, ?)
            """)) {
                for (int i = 0; i < seatIds.size(); i++) {
                    int seatId = seatIds.get(i);
                    int seatClassId = seatClassIds.get(i);
                    BigDecimal price = prices.get(i);

                    Long passengerId = null; // có thể map 1-1 nếu cần
                    ps.setLong(1, bookingId);
                    ps.setInt(2, tripId);
                    ps.setInt(3, seatId);
                    ps.setInt(4, seatClassId);
                    if (passengerId != null) {
                        ps.setLong(5, passengerId);
                    } else {
                        ps.setNull(5, Types.BIGINT);
                    }
                    ps.setBigDecimal(6, price);
                    ps.setBigDecimal(7, price);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // ---------- Seat locks (5 phút) ----------
            try (PreparedStatement ps = cn.prepareStatement("""
                INSERT INTO dbo.SeatLock(trip_id, seat_id, booking_id, locked_at, expires_at, status)
                VALUES (?, ?, ?, SYSUTCDATETIME(), DATEADD(MINUTE, 5, SYSUTCDATETIME()), 'LOCKED')
            """)) {
                for (Integer seatId : seatIds) {
                    ps.setInt(1, tripId);
                    ps.setInt(2, seatId);
                    ps.setLong(3, bookingId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // ---------- Payment INITIATED ----------
            long paymentId;
            String idem = UUID.randomUUID().toString();
            try (PreparedStatement ps = cn.prepareStatement("""
                INSERT INTO dbo.PaymentTransaction(booking_id, method, amount, currency, status, idempotency_key)
                VALUES (?, 'VIETQR', ?, 'VND', 'INITIATED', ?)
            """, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, bookingId);
                ps.setBigDecimal(2, total);
                ps.setString(3, idem);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    paymentId = rs.getLong(1);
                }
            }

            cn.commit();
            ok = true;

            // Sang trang thanh toán (QR + countdown)
            req.setAttribute("bookingId", bookingId);
            req.setAttribute("paymentId", paymentId);
            req.setAttribute("total", total);
            req.getRequestDispatcher("/WEB-INF/views/customer/payment_qr.jsp").forward(req, res);

        } catch (Exception e) {
            if (cn != null) try {
                cn.rollback();
            } catch (Exception ignore) {
            }
            throw new ServletException(e);
        } finally {
            if (cn != null) try {
                if (!ok) {
                    cn.rollback();
                }
                cn.setAutoCommit(true);
                cn.close();
            } catch (Exception ignore) {
            }
        }
    }
}
