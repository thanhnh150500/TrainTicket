package vn.ttapp.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import vn.ttapp.model.Trip;
import vn.ttapp.model.Carriage;
import vn.ttapp.model.SeatView;
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

    // ==== DTOs ====
    public static record SeatDto(
            int id, String code, Integer row, Integer col,
            Long price, String status, String holdExpiresAt,
            Integer carriageId, String carriageCode) {

    }

    // ĐÃ thêm id ở đầu tham số
    public static record CoachDto(
            int id, int no, String name, int seatCount, List<Long> rangePrice) {

    }

    public static record Payload(
            int tripId, Map<String, Object> coach,
            List<SeatDto> seats, List<CoachDto> coaches, String now) {

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String tripIdStr = req.getParameter("tripId");
        if (tripIdStr == null || tripIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing tripId");
            return;
        }

        try {
            int tripId = Integer.parseInt(tripIdStr);
            Trip trip = tripService.findById(tripId);
            if (trip == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Trip not found");
                return;
            }

            // ===== Toa của train =====
            List<Carriage> cars = carService.findByTrain(trip.getTrainId());
            cars.sort(Comparator
                    .comparingInt((Carriage c) -> c.getSortOrder() != null ? c.getSortOrder() : 9999)
                    .thenComparing(Carriage::getCode, naturalComparator()));
            if (cars.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No carriage for this trip");
                return;
            }

            // Map coachNo ↔ carriageId
            Map<Integer, Integer> coachNo2CarId = new LinkedHashMap<>();
            Map<Integer, Integer> carId2CoachNo = new HashMap<>();
            int seq = 1;
            for (Carriage c : cars) {
                coachNo2CarId.put(seq, c.getCarriageId());
                carId2CoachNo.put(c.getCarriageId(), seq);
                seq++;
            }

            // ===== Chọn toa hiện tại =====
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
            Integer carriageIdObj = coachNo2CarId.get(coachNo);
            if (carriageIdObj == null) {
                carriageIdObj = coachNo2CarId.get(1);
            }
            final int carriageId = carriageIdObj;

            // ===== Ghế toàn train (availability theo trip) =====
            int trainId = trip.getTrainId();
            List<SeatView> allSeatViews = seatService.getSeatMapWithAvailabilityForTrain(tripId, trainId);
            if (allSeatViews == null) {
                allSeatViews = List.of();
            }

            // De-dup nếu query trả trùng dòng
            Map<Integer, SeatView> unique = new LinkedHashMap<>();
            for (SeatView v : allSeatViews) {
                unique.putIfAbsent(v.seatId, v);
            }
            allSeatViews = new ArrayList<>(unique.values());

            // Sắp xếp ổn định để FE vẽ đẹp
            allSeatViews.sort(
                    Comparator.comparingInt((SeatView v) -> v.carriageId)
                            .thenComparing(v -> v.rowNo, nullsLastNatural())
                            .thenComparing(v -> v.colNo, nullsLastNatural())
                            .thenComparing(v -> v.seatCode, naturalComparator())
                            .thenComparingInt(v -> v.seatId)
            );

            // Coaches DTO: seatCount + khoảng giá
            List<CoachDto> coaches = new ArrayList<>(cars.size());
            for (Carriage c : cars) {
                int no = carId2CoachNo.get(c.getCarriageId());
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

                // 🔧 SỬA Ở ĐÂY: truyền đủ 5 tham số theo đúng thứ tự (id, no, name, seatCount, range)
                coaches.add(new CoachDto(
                        c.getCarriageId(), // id = carriageId thật
                        no, // no = số thứ tự toa (1..n)
                        c.getCode() != null ? c.getCode() : ("Toa " + no),
                        cnt,
                        range
                ));
            }

            // seats[]: trả toàn bộ ghế; FE lọc theo carriageId
            List<SeatDto> seats = new ArrayList<>(allSeatViews.size());
            Map<String, Integer> seatClassCountForCurrentCoach = new HashMap<>();

            for (SeatView v : allSeatViews) {
                String status = v.available ? "FREE" : (v.lockExpiresAt != null ? "HELD" : "SOLD");
                seats.add(new SeatDto(
                        v.seatId, v.seatCode, v.rowNo, v.colNo,
                        v.price != null ? v.price.longValue() : null,
                        status,
                        v.lockExpiresAt != null ? v.lockExpiresAt.toInstant().toString() : null,
                        v.carriageId, v.carriageCode
                ));
                if (v.carriageId == carriageId && v.seatClassName != null) {
                    seatClassCountForCurrentCoach.merge(v.seatClassName, 1, Integer::sum);
                }
            }

            // Header coach
            String coachName = cars.stream()
                    .filter(c -> c.getCarriageId() == carriageId)
                    .map(Carriage::getCode)
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

            // ===== Xuất JSON =====
            Payload out = new Payload(tripId, coach, seats, coaches, Instant.now().toString());

            resp.setContentType("application/json; charset=UTF-8");
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0L);
            mapper.writeValue(resp.getWriter(), out);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tripId format");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    // ==== comparators ====
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
