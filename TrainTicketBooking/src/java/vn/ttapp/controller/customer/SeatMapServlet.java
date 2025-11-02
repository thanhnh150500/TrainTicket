package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
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

    /**
     * ViewModel đưa sang JSP
     */
    public static class Vm {

        public Trip trip;
        public List<Trip> sameDayTrips;
        public List<Carriage> carriages;
        public Map<Integer, List<SeatView>> seatsByCarriage; // CarriageId -> list ghế
        public Map<Integer, String> seatStatus;              // SeatId -> FREE/LOCKED/BOOKED
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

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

            // (Tuỳ chọn) các chuyến cùng ngày — hiện để 1 item
            List<Trip> sameDay = Collections.singletonList(trip);

            // Lấy danh sách toa thuộc train của trip, sort theo sort_order rồi mã toa “tự nhiên”
            List<Carriage> cars = new ArrayList<>();
            for (Carriage c : carService.findAll()) {
                if (Objects.equals(c.getTrainId(), trip.getTrainId())) {
                    cars.add(c);
                }
            }
            cars.sort(Comparator
                    .comparingInt(Carriage::getSortOrder)
                    .thenComparing(Carriage::getCode, naturalComparator()));

            // Ghế + trạng thái (để binding lên header ngay khi mở)
            List<SeatView> seatViews = seatService.getSeatMapWithAvailability(tripId);
            Map<Integer, List<SeatView>> byCar = new HashMap<>();
            Map<Integer, String> statusMap = new HashMap<>();
            for (SeatView v : seatViews) {
                byCar.computeIfAbsent(v.carriageId, k -> new ArrayList<>()).add(v);
                statusMap.put(
                        v.seatId,
                        v.available ? "FREE" : (v.lockExpiresAt != null ? "LOCKED" : "BOOKED")
                );
            }
            // sort ghế theo mã (A1 < A2 < A10)
            for (List<SeatView> list : byCar.values()) {
                list.sort(Comparator.comparing(sv -> sv.seatCode, naturalComparator()));
            }

            Vm vm = new Vm();
            vm.trip = trip;
            vm.sameDayTrips = sameDay;
            vm.carriages = cars;
            vm.seatsByCarriage = byCar;
            vm.seatStatus = statusMap;

            // (Tuỳ chọn) giờ đi/đến để gán lên thẻ đầu tàu
            req.setAttribute("departFmt", trip.getDepartAt().toLocalTime().toString());
            req.setAttribute("arriveFmt", trip.getArriveAt().toLocalTime().toString());

            req.setAttribute("vm", vm);
            req.getRequestDispatcher("/WEB-INF/views/customer/seatmap.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tripId");
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
                int c = na && nb
                        ? Integer.compare(Integer.parseInt(sa), Integer.parseInt(sb))
                        : sa.compareToIgnoreCase(sb);
                if (c != 0) {
                    return c;
                }
            }
            return ma.find() ? 1 : (mb.find() ? -1 : 0);
        };
    }
}
