package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import vn.ttapp.model.User;
import vn.ttapp.dao.PaymentConfigDao;
import vn.ttapp.model.PaymentConfig;

@WebServlet("/confirm-booking")
public class ConfirmBookingServlet extends HttpServlet {

    private static final BigDecimal SERVICE_FEE = new BigDecimal("10000");
    private static final BigDecimal INSURANCE_PER_PAX = new BigDecimal("1000");
    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    /* ============== Helpers ============== */
    private static Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal parseMoneyOrNull(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim().replaceAll("[^0-9.\\-]", "");
        if (s.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseDobOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), DOB_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private static void badRequest(HttpServletResponse res, String msg) throws IOException {
        res.setStatus(400);
        res.setContentType("text/plain; charset=UTF-8");
        res.getWriter().write(msg);
    }

    private static String placeholders(int n) {
        return (n <= 0) ? "" : String.join(",", Collections.nCopies(n, "?"));
    }

    private static String[] pickParams(HttpServletRequest req, String a, String b) {
        String[] v = req.getParameterValues(a);
        if (v == null || v.length == 0) {
            v = req.getParameterValues(b);
        }
        return v;
    }

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

    private static Map<Integer, Integer> loadSeatClassIds(Connection cn, List<Integer> seatIds) throws SQLException {
        Map<Integer, Integer> map = new HashMap<>();
        if (seatIds.isEmpty()) {
            return map;
        }

        String sql = "SELECT seat_id, seat_class_id FROM dbo.Seat WHERE seat_id IN (" + placeholders(seatIds.size()) + ")";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            for (Integer id : seatIds) {
                ps.setInt(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt(1), rs.getInt(2));
                }
            }
        }
        return map;
    }

    private static UUID currentUserId(HttpServletRequest req) {
        HttpSession ss = req.getSession(false);
        if (ss == null) {
            return null;
        }

        Object au = ss.getAttribute("authUser");
        if (au instanceof User u && u.getUserId() != null) {
            return u.getUserId();
        }

        Object uid = ss.getAttribute("userId");
        if (uid instanceof UUID) {
            return (UUID) uid;
        }
        if (uid instanceof String s && !s.isBlank()) {
            try {
                return UUID.fromString(s.trim());
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static class Holder<T> {

        T value;

        Holder(T v) {
            value = v;
        }
    }

    private static void fillContactFromSessionIfMissing(HttpServletRequest req, Holder<String> email, Holder<String> phone) {
        HttpSession ss = req.getSession(false);
        if (ss == null) {
            return;
        }
        Object au = ss.getAttribute("authUser");
        if (au instanceof User u) {
            if ((email.value == null || email.value.isBlank()) && u.getEmail() != null) {
                email.value = u.getEmail();
            }
            if ((phone.value == null || phone.value.isBlank()) && u.getPhone() != null) {
                phone.value = u.getPhone();
            }
        }
    }

    /* ============== Main ============== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // 0) bắt buộc login
        UUID userId = currentUserId(req);
        if (userId == null) {
            String next = req.getRequestURL().toString();
            String qs = req.getQueryString();
            if (qs != null && !qs.isBlank()) {
                next += "?" + qs;
            }
            res.sendRedirect(req.getContextPath() + "/auth/login?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8));
            return;
        }

        // 1) đọc & validate cơ bản
        Integer tripIdBox = parseIntOrNull(req.getParameter("tripId"));
        if (tripIdBox == null) {
            badRequest(res, "Thiếu hoặc sai tripId");
            return;
        }
        int tripId = tripIdBox;

        Holder<String> phoneH = new Holder<>(req.getParameter("phone"));
        Holder<String> emailH = new Holder<>(req.getParameter("email"));
        fillContactFromSessionIfMissing(req, emailH, phoneH);

        String[] seatIdsStr = compact(pickParams(req, "seatId", "seatId[]"));
        if (seatIdsStr == null || seatIdsStr.length == 0) {
            badRequest(res, "Không có ghế nào được chọn");
            return;
        }

        // KHỬ TRÙNG LẶP GHẾ (tránh tự đụng UQ)
        LinkedHashSet<Integer> seatIdSet = new LinkedHashSet<>();
        for (String s : seatIdsStr) {
            Integer v = parseIntOrNull(s);
            if (v != null) {
                seatIdSet.add(v);
            }
        }
        if (seatIdSet.isEmpty()) {
            badRequest(res, "Danh sách ghế không hợp lệ");
            return;
        }
        List<Integer> seatIds = new ArrayList<>(seatIdSet);

        String[] seatClassStr = compact(pickParams(req, "seatClassId", "seatClassId[]"));
        String[] pricesStr = compact(pickParams(req, "price", "price[]"));
        String[] fullnames = pickParams(req, "fullname[]", "fullname");
        String[] dobs = pickParams(req, "dob[]", "dob");
        String[] idnums = pickParams(req, "idNumber[]", "idNumber");

        Connection cn = null;
        boolean ok = false;

        try {
            cn = vn.ttapp.config.Db.getConnection();
            cn.setAutoCommit(false);

            // 2) user tồn tại
            try (PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM dbo.Users WHERE user_id=?")) {
                ps.setObject(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ServletException("User không tồn tại: " + userId);
                    }
                }
            }

            // 3) thông tin trip
            int routeId;
            LocalDate travelDate;
            Timestamp departAt;
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT route_id, depart_at FROM dbo.Trip WHERE trip_id = ?")) {
                ps.setInt(1, tripId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        badRequest(res, "Trip không tồn tại");
                        return;
                    }
                    routeId = rs.getInt("route_id");
                    departAt = rs.getTimestamp("depart_at");
                    travelDate = departAt.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
                }
            }

            // 4) seat class map
            Map<Integer, Integer> classBySeat = new HashMap<>();
            if (seatClassStr != null) {
                int n = Math.min(seatIds.size(), seatClassStr.length);
                int i = 0;
                for (Integer sid : seatIds) {
                    if (i >= n) {
                        break;
                    }
                    Integer sc = parseIntOrNull(seatClassStr[i]);
                    if (sc != null) {
                        classBySeat.put(sid, sc);
                    }
                    i++;
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

            // 5) giá vé
            List<BigDecimal> prices = new ArrayList<>(seatIds.size());
            Map<Integer, BigDecimal> priceByClass = new HashMap<>();
            for (int i = 0; i < seatIds.size(); i++) {
                int sid = seatIds.get(i);
                int scid = classBySeat.get(sid);
                BigDecimal p = (pricesStr != null && pricesStr.length > i) ? parseMoneyOrNull(pricesStr[i]) : null;
                if (p == null || p.compareTo(BigDecimal.ZERO) <= 0) {
                    p = priceByClass.get(scid);
                    if (p == null) {
                        try (PreparedStatement fps = cn.prepareStatement("""
                            SELECT TOP (1) base_price
                            FROM dbo.FareRule
                            WHERE route_id=? AND seat_class_id=? AND effective_from<=?
                                  AND (effective_to IS NULL OR effective_to>=?)
                            ORDER BY effective_from DESC
                        """)) {
                            fps.setInt(1, routeId);
                            fps.setInt(2, scid);
                            fps.setDate(3, java.sql.Date.valueOf(travelDate));
                            fps.setDate(4, java.sql.Date.valueOf(travelDate));
                            try (ResultSet frs = fps.executeQuery()) {
                                if (!frs.next()) {
                                    badRequest(res, "Không tìm thấy giá cho seatId=" + sid);
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

            BigDecimal subtotal = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal insurance = INSURANCE_PER_PAX.multiply(BigDecimal.valueOf(seatIds.size()));
            BigDecimal total = subtotal.add(insurance).add(SERVICE_FEE);

            // 6) tạo booking (HOLD +5')
            long bookingId;
            try (PreparedStatement ps = cn.prepareStatement("""
                INSERT INTO dbo.Booking(user_id, contact_email, contact_phone, status, subtotal, discount_total, total_amount, hold_expires_at)
                VALUES (?, ?, ?, 'HOLD', ?, 0, ?, DATEADD(MINUTE, 5, SYSUTCDATETIME()))
            """, Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, userId);
                ps.setString(2, emailH.value);
                ps.setString(3, phoneH.value);
                ps.setBigDecimal(4, subtotal);
                ps.setBigDecimal(5, total);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    bookingId = rs.getLong(1);
                }
            }

            // 7) passenger (tuỳ form có gửi)
            if (fullnames != null && fullnames.length > 0) {
                try (PreparedStatement ps = cn.prepareStatement("""
                    INSERT INTO dbo.Passenger(booking_id, full_name, birth_date, id_number, phone, email)
                    VALUES (?, ?, ?, ?, ?, ?)
                """)) {
                    for (int i = 0; i < fullnames.length; i++) {
                        ps.setLong(1, bookingId);
                        ps.setString(2, fullnames[i]);
                        LocalDate dob = (dobs != null && dobs.length > i) ? parseDobOrNull(dobs[i]) : null;
                        if (dob != null) {
                            ps.setDate(3, java.sql.Date.valueOf(dob));
                        } else {
                            ps.setNull(3, Types.DATE);
                        }
                        ps.setString(4, (idnums != null && idnums.length > i) ? idnums[i] : null);
                        ps.setString(5, phoneH.value);
                        ps.setString(6, emailH.value);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // 8) booking item
            try (PreparedStatement ps = cn.prepareStatement("""
                INSERT INTO dbo.BookingItem(booking_id, trip_id, seat_id, seat_class_id, segment, passenger_id, base_price, discount_amount, amount)
                VALUES (?, ?, ?, ?, 'OUTBOUND', NULL, ?, 0, ?)
            """)) {
                for (int i = 0; i < seatIds.size(); i++) {
                    BigDecimal price = prices.get(i);
                    ps.setLong(1, bookingId);
                    ps.setInt(2, tripId);
                    ps.setInt(3, seatIds.get(i));
                    ps.setInt(4, classBySeat.get(seatIds.get(i)));
                    ps.setBigDecimal(5, price);
                    ps.setBigDecimal(6, price);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 9) seat lock an toàn (tránh UQ_SeatLock)
            final String updateLockSql = """
                UPDATE L
                   SET booking_id = ?,
                       locked_at  = SYSUTCDATETIME(),
                       expires_at = DATEADD(MINUTE, 5, SYSUTCDATETIME()),
                       status     = 'LOCKED'
                FROM dbo.SeatLock AS L WITH (UPDLOCK, HOLDLOCK)
                WHERE L.trip_id = ? AND L.seat_id = ?
                  AND (L.status <> 'LOCKED' OR L.expires_at <= SYSUTCDATETIME())
            """;
            final String insertLockSql = """
                INSERT INTO dbo.SeatLock(trip_id, seat_id, booking_id, locked_at, expires_at, status)
                SELECT ?, ?, ?, SYSUTCDATETIME(), DATEADD(MINUTE, 5, SYSUTCDATETIME()), 'LOCKED'
                WHERE NOT EXISTS (
                    SELECT 1 FROM dbo.SeatLock WITH (UPDLOCK, HOLDLOCK)
                    WHERE trip_id = ? AND seat_id = ?
                      AND status = 'LOCKED' AND expires_at > SYSUTCDATETIME()
                )
            """;

            for (Integer seatId : seatIds) {
                int updated;
                try (PreparedStatement psU = cn.prepareStatement(updateLockSql)) {
                    psU.setLong(1, bookingId);
                    psU.setInt(2, tripId);
                    psU.setInt(3, seatId);
                    updated = psU.executeUpdate();
                }
                if (updated == 0) {
                    int inserted;
                    try (PreparedStatement psI = cn.prepareStatement(insertLockSql)) {
                        psI.setInt(1, tripId);
                        psI.setInt(2, seatId);
                        psI.setLong(3, bookingId);
                        psI.setInt(4, tripId);
                        psI.setInt(5, seatId);
                        inserted = psI.executeUpdate();
                    }
                    if (inserted == 0) {
                        cn.rollback();
                        req.setAttribute("error", "Ghế " + seatId + " đang được giữ bởi giao dịch khác. Vui lòng chọn ghế khác.");
                        req.getRequestDispatcher("/WEB-INF/views/customer/checkout.jsp").forward(req, res);
                        return;
                    }
                }
            }

            // 10) payment txn
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

            // 11) cấu hình thanh toán & biến QR cho JSP
            PaymentConfig cfg = PaymentConfigDao.getActiveConfig(cn);
            if (cfg == null) {
                cn.rollback();
                throw new ServletException("Chưa cấu hình PaymentConfig (is_active=1). Vui lòng thêm record vào dbo.PaymentConfig.");
            }

            // memo gợi ý: TT-{bookingId}
            String memo = "TT-" + bookingId;

            // Chuỗi data đơn giản: bankCode|accountNo|amount|memo (JSP sẽ encode khi gọi api.qrserver)
            String qrData = String.join("|",
                    Optional.ofNullable(cfg.getBankCode()).orElse(""),
                    Optional.ofNullable(cfg.getAccountNo()).orElse(""),
                    total.toPlainString(),
                    memo
            );

            // Tính countdownSec còn lại theo hold_expires_at (ở đây mặc định 300 giây nếu không muốn query lại)
            int countdownSec = 300;

            cn.commit();
            ok = true;

            // Gắn attribute cho JSP
            req.setAttribute("bookingId", bookingId);
            req.setAttribute("paymentId", paymentId);
            req.setAttribute("amount", total);
            req.setAttribute("memo", memo);

            req.setAttribute("bankCode", cfg.getBankCode());
            req.setAttribute("bankName", cfg.getBankName());
            req.setAttribute("binCode", cfg.getBinCode());
            req.setAttribute("accountNo", cfg.getAccountNo());
            req.setAttribute("accountName", cfg.getAccountName());

            req.setAttribute("qrData", qrData);
            req.setAttribute("qrImageUrl", null);       // nếu bạn có ảnh QR sẵn, set URL tại đây
            req.setAttribute("countdownSec", countdownSec);

            req.getRequestDispatcher("/WEB-INF/views/customer/payment_qr.jsp").forward(req, res);

        } catch (Exception e) {
            if (cn != null) try {
                cn.rollback();
            } catch (Exception ignore) {
            }
            throw new ServletException(e);
        } finally {
            if (cn != null) try {
                cn.setAutoCommit(true);
                cn.close();
            } catch (Exception ignore) {
            }
        }
    }
}
