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
import vn.ttapp.model.SeatView;          // ✅ dùng SeatView riêng
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

    // ======= DTO cho JSON trả về =======
    public static record CoachDto(int no, String name, int seatCount, List<Long> rangePrice) {

    }

    public static record SeatDto(int id, String code, Integer row, Integer col,
            Long price, String status, String holdExpiresAt) {

    }

    public static record Payload(int tripId, Map<String, Object> coach,
            List<SeatDto> seats, List<CoachDto> coaches, String now) {

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String tripIdStr = req.getParameter("tripId");
        if (tripIdStr == null) {
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

            // Lấy toàn bộ toa thuộc train của trip
            List<Carriage> cars = new ArrayList<>();
            for (Carriage c : carService.findAll()) {
                if (Objects.equals(c.getTrainId(), trip.getTrainId())) {
                    cars.add(c);
                }
            }
            cars.sort(Comparator
                    .comparingInt(Carriage::getSortOrder)
                    .thenComparing(Carriage::getCode, naturalComparator()));

            if (cars.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No carriage for this trip");
                return;
            }

            // Map coachNo <-> carriageId
            Map<Integer, Integer> coachNo2CarId = new LinkedHashMap<>();
            Map<Integer, Integer> carId2CoachNo = new HashMap<>();
            int no = 1;
            for (Carriage c : cars) {
                coachNo2CarId.put(no, c.getCarriageId());
                carId2CoachNo.put(c.getCarriageId(), no);
                no++;
            }

            // Chọn toa hiện tại
            Integer coachNo = null;
            if (req.getParameter("coachNo") != null) {
                coachNo = Integer.valueOf(req.getParameter("coachNo"));
            }
            if (coachNo == null || !coachNo2CarId.containsKey(coachNo)) {
                coachNo = 1;
            }
            final int carriageId = coachNo2CarId.get(coachNo);

            // ✅ Lấy danh sách SeatView (đã có status/price) từ service
            List<SeatView> allSeatViews = seatService.getSeatMapWithAvailability(tripId);

            // ===== coaches[] =====
            List<CoachDto> coaches = new ArrayList<>();
            for (Carriage c : cars) {
                int cn = carId2CoachNo.get(c.getCarriageId());
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
                coaches.add(new CoachDto(
                        cn,
                        c.getCode() != null ? c.getCode() : ("Toa " + cn),
                        cnt,
                        range
                ));
            }

            // ===== seats[] của toa hiện tại =====
            List<SeatDto> seats = new ArrayList<>();
            for (SeatView v : allSeatViews) {
                if (Objects.equals(v.carriageId, carriageId)) {               // getter: v.getCarriageId()
                    String status = v.available ? "FREE" // getter: v.isAvailable()
                            : (v.lockExpiresAt != null ? "HELD" : "SOLD");   // getter: v.getLockExpiresAt()
                    seats.add(new SeatDto(
                            v.seatId, // getter: v.getSeatId()
                            v.seatCode, // getter: v.getSeatCode()
                            v.rowNo, // getter: v.getRowNo()
                            v.colNo, // getter: v.getColNo()
                            v.price != null ? v.price.longValue() : null, // getter: v.getPrice()
                            status,
                            v.lockExpiresAt != null // getter: v.getLockExpiresAt()
                                    ? v.lockExpiresAt.toInstant().toString()
                                    : null
                    ));
                }
            }
            // Sắp theo mã ghế “tự nhiên”: 1,2,3,10,...
            seats.sort(Comparator.comparing(SeatDto::code, naturalComparator()));

            // coach hiện tại
            String coachName = cars.stream()
                    .filter(c -> c.getCarriageId() == carriageId)
                    .map(Carriage::getCode).findFirst().orElse("Toa " + coachNo);
            Map<String, Object> coach = new LinkedHashMap<>();
            coach.put("no", coachNo);
            coach.put("name", coachName);
            coach.put("seatClass", "UNKNOWN"); // nếu có field seatClass, map thêm tại đây

            Payload out = new Payload(
                    tripId,
                    coach,
                    seats,
                    coaches,
                    Instant.now().toString()
            );

            resp.setContentType("application/json; charset=UTF-8");
            resp.setHeader("Cache-Control", "no-store");
            mapper.writeValue(resp.getWriter(), out);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tripId format");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    // Comparator so sánh chuỗi theo số tự nhiên (A1 < A2 < A10)
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
                int c = (na && nb) ? Integer.compare(Integer.parseInt(sa), Integer.parseInt(sb))
                        : sa.compareToIgnoreCase(sb);
                if (c != 0) {
                    return c;
                }
            }
            return ma.find() ? 1 : (mb.find() ? -1 : 0);
        };
    }
}
