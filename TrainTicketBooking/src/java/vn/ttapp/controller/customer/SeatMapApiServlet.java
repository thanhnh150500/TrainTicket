package vn.ttapp.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import vn.ttapp.model.Trip;
import vn.ttapp.model.Carriage;
import vn.ttapp.model.SeatView;
import vn.ttapp.model.User;
import vn.ttapp.service.TripService;
import vn.ttapp.service.CarriageService;
import vn.ttapp.service.SeatService;

@WebServlet(name = "SeatMapApiServlet", urlPatterns = {"/api/seatmap"})
public class SeatMapApiServlet extends HttpServlet {

    private final TripService tripService = new TripService();
    private final CarriageService carService = new CarriageService();
    private final SeatService seatService = new SeatService();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /* ===== DTOs ===== */
    public static record SeatDto(
            Integer id, String code, Integer row, Integer col,
            Long price,
            String status, // FREE | LOCKED | LOCKED_BY_ME | BOOKED
            Long remainSec, // còn bao nhiêu giây nếu đang lock
            String holdExpiresAt, // ISO-8601 string nếu đang lock
            Boolean lockedByMe, // true nếu do user hiện tại giữ
            Integer carriageId, String carriageCode,
            Integer seatClassId, String seatClassCode, String seatClassName) {

    }

    // JS đang cần có id + no + name
    public static record CoachDto(
            Integer id, Integer no, String name, Integer seatCount, List<Long> rangePrice) {

    }

    public static record Payload(
            Integer tripId, Map<String, Object> coach,
            List<SeatDto> seats, List<CoachDto> coaches, String now) {

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Luôn tắt cache để các tab nhận trạng thái mới nhất
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0L);
        resp.setContentType("application/json; charset=UTF-8");

        String tripIdStr = req.getParameter("tripId");
        if (tripIdStr == null || tripIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing tripId");
            return;
        }

        try {
            final int tripId = Integer.parseInt(tripIdStr);
            Trip trip = tripService.findById(tripId);
            if (trip == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Trip not found");
                return;
            }

            /* ===== Lấy danh sách toa và đánh số coachNo ổn định ===== */
            List<Carriage> cars = carService.findByTrain(trip.getTrainId());
            cars.sort(Comparator
                    .comparingInt((Carriage c) -> c.getSortOrder() != null ? c.getSortOrder() : 9999)
                    .thenComparing(Carriage::getCode, naturalComparator()));
            if (cars.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No carriage for this trip");
                return;
            }

            Map<Integer, Integer> coachNo2CarId = new LinkedHashMap<>();
            Map<Integer, Integer> carId2CoachNo = new HashMap<>();
            int seq = 1;
            for (Carriage c : cars) {
                coachNo2CarId.put(seq, c.getCarriageId());
                carId2CoachNo.put(c.getCarriageId(), seq);
                seq++;
            }

            int coachNo = 1;
            String coachNoParam = req.getParameter("coachNo");
            if (coachNoParam != null && !coachNoParam.isBlank()) {
                try {
                    int parsed = Integer.parseInt(coachNoParam);
                    if (coachNo2CarId.containsKey(parsed)) {
                        coachNo = parsed;
                    }
                } catch (NumberFormatException ignore) {
                }
            }
            Integer carriageIdObj = coachNo2CarId.getOrDefault(coachNo, coachNo2CarId.get(1));
            final int carriageId = carriageIdObj;

            /* ===== Lấy toàn bộ ghế (có metadata) ===== */
            int trainId = trip.getTrainId();
            List<SeatView> allSeatViews = seatService.getSeatMapWithAvailabilityForTrain(tripId, trainId);
            if (allSeatViews == null) {
                allSeatViews = List.of();
            }

            // unique theo seatId
            Map<Integer, SeatView> unique = new LinkedHashMap<>();
            for (SeatView v : allSeatViews) {
                unique.putIfAbsent(v.seatId, v);
            }
            allSeatViews = new ArrayList<>(unique.values());

            // sort ổn định
            allSeatViews.sort(
                    Comparator.comparingInt((SeatView v) -> v.carriageId)
                            .thenComparing(v -> v.rowNo, nullsLastNatural())
                            .thenComparing(v -> v.colNo, nullsLastNatural())
                            .thenComparing(v -> v.seatCode, naturalComparator())
                            .thenComparingInt(v -> v.seatId)
            );

            /* ===== Đọc trạng thái thật từ DB: BOOKED & LOCKED ===== */
            UUID currentUser = currentUserId(req);

            // booked seats
            Set<Integer> bookedSeatIds = new HashSet<>();
            // active locks: seatId -> (lockUserId, expiresAt)
            Map<Integer, UUID> lockUserBySeat = new HashMap<>();
            Map<Integer, Timestamp> lockExpireBySeat = new HashMap<>();

            try (Connection cn = vn.ttapp.config.Db.getConnection()) {
                // BOOKED
                try (PreparedStatement ps = cn.prepareStatement("""
                        SELECT bi.seat_id
                        FROM dbo.BookingItem bi
                        JOIN dbo.Booking b ON b.booking_id = bi.booking_id
                        WHERE bi.trip_id = ? AND b.status IN ('PAID','CONFIRMED')
                    """)) {
                    ps.setInt(1, tripId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            bookedSeatIds.add(rs.getInt(1));
                        }
                    }
                }

                // LOCKED (còn hạn)
                try (PreparedStatement ps = cn.prepareStatement("""
                        SELECT L.seat_id, B.user_id, L.expires_at
                        FROM dbo.SeatLock L
                        LEFT JOIN dbo.Booking B ON B.booking_id = L.booking_id
                        WHERE L.trip_id = ? AND L.status = 'LOCKED' AND L.expires_at > SYSUTCDATETIME()
                    """)) {
                    ps.setInt(1, tripId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int seatId = rs.getInt(1);
                            UUID uid = null;
                            Object uo = rs.getObject(2);
                            if (uo instanceof UUID) {
                                uid = (UUID) uo;
                            } else if (uo instanceof String s && !s.isBlank()) {
                                try {
                                    uid = UUID.fromString(s.trim());
                                } catch (Exception ignore) {
                                }
                            }
                            Timestamp exp = rs.getTimestamp(3);
                            lockUserBySeat.put(seatId, uid);
                            lockExpireBySeat.put(seatId, exp);
                        }
                    }
                }
            }

            /* ===== Build coaches DTO ===== */
            List<CoachDto> coaches = new ArrayList<>(cars.size());
            for (Carriage c : cars) {
                Integer no = carId2CoachNo.getOrDefault(c.getCarriageId(), 0);
                long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
                int cnt = 0;
                for (SeatView v : allSeatViews) {
                    if (v.carriageId == c.getCarriageId()) {
                        cnt++;
                        if (v.price != null) {
                            long p = v.price.longValue();
                            if (p < min) {
                                min = p;
                            }
                            if (p > max) {
                                max = p;
                            }
                        }
                    }
                }
                List<Long> range = (min == Long.MAX_VALUE) ? List.of() : List.of(min, max);
                String name = (c.getCode() != null && !c.getCode().isBlank()) ? c.getCode() : ("Toa " + no);
                coaches.add(new CoachDto(c.getCarriageId(), no, name, cnt, range));
            }

            /* ===== seats[] hợp nhất trạng thái ===== */
            List<SeatDto> seats = new ArrayList<>(allSeatViews.size());
            Map<String, Integer> seatClassCountForCurrentCoach = new HashMap<>();

            long nowMs = System.currentTimeMillis();

            for (SeatView v : allSeatViews) {
                String status;
                boolean lockedByMe = false;
                Long remainSec = null;
                String holdExpiresAt = null;

                if (bookedSeatIds.contains(v.seatId)) {
                    status = "BOOKED";
                } else if (lockExpireBySeat.containsKey(v.seatId)) {
                    Timestamp exp = lockExpireBySeat.get(v.seatId);
                    UUID lockUser = lockUserBySeat.get(v.seatId);
                    lockedByMe = (currentUser != null && currentUser.equals(lockUser));
                    status = lockedByMe ? "LOCKED_BY_ME" : "LOCKED";
                    if (exp != null) {
                        long rem = Math.max(0L, (exp.getTime() - nowMs) / 1000L);
                        remainSec = rem;
                        holdExpiresAt = exp.toInstant().toString();
                    }
                } else {
                    status = "FREE";
                }

                seats.add(new SeatDto(
                        v.seatId,
                        v.seatCode,
                        v.rowNo,
                        v.colNo,
                        v.price != null ? v.price.longValue() : null,
                        status,
                        remainSec,
                        holdExpiresAt,
                        lockedByMe,
                        v.carriageId,
                        v.carriageCode,
                        v.seatClassId,
                        v.seatClassCode,
                        v.seatClassName
                ));

                if (v.carriageId == carriageId && v.seatClassName != null) {
                    seatClassCountForCurrentCoach.merge(v.seatClassName, 1, Integer::sum);
                }
            }

            // Header coach
            String coachName = cars.stream()
                    .filter(c -> c.getCarriageId() == carriageId)
                    .map(Carriage::getCode)
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank())
                    .findFirst()
                    .orElse("Toa " + coachNo);

            String seatClassForCoach = seatClassCountForCurrentCoach.isEmpty()
                    ? "UNKNOWN"
                    : seatClassCountForCurrentCoach.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("UNKNOWN");

            Map<String, Object> coach = new LinkedHashMap<>();
            coach.put("no", coachNo);
            coach.put("name", coachName);
            coach.put("seatClass", seatClassForCoach);

            /* ===== Xuất JSON ===== */
            Payload out = new Payload(tripId, coach, seats, coaches, Instant.now().toString());
            mapper.writeValue(resp.getWriter(), out);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tripId format");
        } catch (Exception e) {
            e.printStackTrace(); // log để debug
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "ok", false,
                    "error", "Internal error",
                    "message", e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage())
            ));
        }
    }

    /* ===== helpers ===== */
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

    private static Comparator<String> naturalComparator() {
        final Pattern p = Pattern.compile("(\\d+)|(\\D+)");
        return (a, b) -> {
            if (a == null) {
                return (b == null) ? 0 : -1;
            }
            if (b == null) {
                return 1;
            }
            var ma = p.matcher(a);
            var mb = p.matcher(b);
            while (ma.find() && mb.find()) {
                String sa = ma.group(), sb = mb.group();
                boolean na = sa.chars().allMatch(Character::isDigit);
                boolean nb = sb.chars().allMatch(Character::isDigit);
                int c = (na && nb)
                        ? Integer.compare(Integer.parseInt(sa), Integer.parseInt(sb))
                        : sa.compareToIgnoreCase(sb);
                if (c != 0) {
                    return c;
                }
            }
            return ma.find() ? 1 : (mb.find() ? -1 : 0);
        };
    }

    private static Comparator<Integer> nullsLastNatural() {
        return (a, b) -> {
            if (a == null) {
                return (b == null ? 0 : 1);
            }
            if (b == null) {
                return -1;
            }
            return Integer.compare(a, b);
        };
    }
}
