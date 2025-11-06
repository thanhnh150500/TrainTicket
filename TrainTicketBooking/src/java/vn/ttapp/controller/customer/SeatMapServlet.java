package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import vn.ttapp.model.Trip;
import vn.ttapp.model.Carriage;
import vn.ttapp.model.SeatView;
import vn.ttapp.service.CarriageService;
import vn.ttapp.service.SeatService;
import vn.ttapp.service.TripService;

@WebServlet(name = "SeatMapPageServlet", urlPatterns = {"/seatmap"})
public class SeatMapServlet extends HttpServlet {

    private final TripService tripService = new TripService();
    private final CarriageService carService = new CarriageService();
    private final SeatService seatService = new SeatService();

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public static class Vm {

        public Trip trip;
        public List<Trip> sameDayTrips;
        public List<Carriage> carriages;
        public Map<Integer, List<SeatView>> seatsByCarriage;
        public Map<Integer, String> seatStatus;

        public Trip getTrip() {
            return trip;
        }

        public List<Trip> getSameDayTrips() {
            return sameDayTrips;
        }

        public List<Carriage> getCarriages() {
            return carriages;
        }

        public Map<Integer, List<SeatView>> getSeatsByCarriage() {
            return seatsByCarriage;
        }

        public Map<Integer, String> getSeatStatus() {
            return seatStatus;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!vn.ttapp.security.AuthUtil.isLoggedIn(req)) {
            // Nếu chưa đăng nhập → chuyển hướng về trang login
            resp.sendRedirect(req.getContextPath() + "/login?next=" + req.getRequestURI()
                    + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
            return;
        }

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

            List<Trip> sameDay = Collections.singletonList(trip);

            List<Carriage> cars = carService.findByTrain(trip.getTrainId());
            System.out.println("[SEATMAP] cars=" + cars.size() + " trainId=" + trip.getTrainId());
            for (Carriage c : cars) {
                System.out.println("  - car#" + c.getCarriageId() + " code=" + c.getCode() + " sort=" + c.getSortOrder());
            }

            List<SeatView> views = seatService.getSeatMapWithAvailabilityForTrain(trip.getTripId(), trip.getTrainId());
            System.out.println("[SEATMAP] seatViews=" + views.size());

            Map<Integer, List<SeatView>> byCar = new LinkedHashMap<>();
            for (Carriage c : cars) {
                byCar.put(c.getCarriageId(), new ArrayList<>());
            }

            Map<Integer, String> statusMap = new HashMap<>();
            for (SeatView v : views) {
                int carId = v.getCarriageId();
                byCar.computeIfAbsent(carId, k -> new ArrayList<>()).add(v);
                statusMap.put(
                        v.getSeatId(),
                        v.isAvailable() ? "FREE" : (v.getLockExpiresAt() != null ? "LOCKED" : "BOOKED")
                );
            }

            for (List<SeatView> list : byCar.values()) {
                list.sort(Comparator.comparing(SeatView::getSeatCode, naturalComparator()));
            }

            Vm vm = new Vm();
            vm.trip = trip;
            vm.sameDayTrips = sameDay;
            vm.carriages = cars;
            vm.seatsByCarriage = byCar;
            vm.seatStatus = statusMap;

            if (trip.getDepartAt() != null) {
                req.setAttribute("departFmt", trip.getDepartAt().toLocalTime().format(HM));
            }
            if (trip.getArriveAt() != null) {
                req.setAttribute("arriveFmt", trip.getArriveAt().toLocalTime().format(HM));
            }
            req.setAttribute("vm", vm);

            // debug nhanh nếu cần
            // System.out.printf("trip=%s cars=%d seats=%d%n", trip.getTrainCode(), cars.size(), views.size());
            req.getRequestDispatcher("/WEB-INF/views/customer/seatmap.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tripId format");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
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
