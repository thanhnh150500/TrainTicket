package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Optional;
import java.util.List;

import vn.ttapp.dao.SearchContext;
import vn.ttapp.dao.StationDao;
import vn.ttapp.model.Trip;
import vn.ttapp.service.TripService;
import vn.ttapp.service.TripService.SearchResult;

// cho list page
import vn.ttapp.dao.TripListDao;
import vn.ttapp.model.TripCardVm;
import vn.ttapp.model.DayTabVm;

@WebServlet(name = "TripSearchServlet", urlPatterns = {"/tripsearch"})
public class TripSearchServlet extends HttpServlet {

    private final TripService tripService = new TripService();
    private final StationDao stationDao = new StationDao();
    private final TripListDao tripListDao = new TripListDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //test
        boolean debug = "1".equals(request.getParameter("debug"));

        final String originName = trimOrNull(request.getParameter("originStation"));
        final String destName = trimOrNull(request.getParameter("destStation"));
        final String typeRaw = trimOrNull(request.getParameter("tripType"));
        final String departRaw = trimOrNull(request.getParameter("departDate"));
        final String returnRaw = trimOrNull(request.getParameter("returnDate"));
        final String timeRaw = trimOrNull(request.getParameter("departTime"));
        final String tripType = (typeRaw == null || typeRaw.isBlank()) ? "ONEWAY" : typeRaw;

        HttpSession ss = request.getSession(true);
        ss.setAttribute("lastOrigin", originName);
        ss.setAttribute("lastDest", destName);
        ss.setAttribute("lastTripType", tripType);
        ss.setAttribute("lastDepart", departRaw);
        ss.setAttribute("lastReturn", returnRaw);

        try {
            // 1) Validate
            if (originName == null || destName == null || departRaw == null) {
                backWithError(request, response, "Vui lòng nhập ga đi, ga đến và ngày đi.");
                return;
            }
            if (originName.equalsIgnoreCase(destName)) {
                backWithError(request, response, "Ga đi và ga đến phải khác nhau.");
                return;
            }

            // 2) Resolve Station IDs
            Integer originId = stationDao.findIdByNameExact(originName);
            Integer destId = stationDao.findIdByNameExact(destName);
            if (originId == null || destId == null) {
                backWithError(request, response, "Không tìm thấy ga đi/ga đến trong hệ thống.");
                return;
            }

            // 3) Parse date/time
            LocalDate departDate;
            LocalDate returnDate = null;
            LocalTime departTime = null;

            try {
                departDate = LocalDate.parse(departRaw);
            } catch (DateTimeParseException ex) {
                backWithError(request, response, "Ngày đi không hợp lệ (yyyy-MM-dd).");
                return;
            }

            if ("ROUNDTRIP".equalsIgnoreCase(tripType)) {
                if (returnRaw == null || returnRaw.isBlank()) {
                    backWithError(request, response, "Bạn chọn khứ hồi. Vui lòng chọn ngày về.");
                    return;
                }
                try {
                    returnDate = LocalDate.parse(returnRaw);
                } catch (DateTimeParseException ex) {
                    backWithError(request, response, "Ngày về không hợp lệ (yyyy-MM-dd).");
                    return;
                }
                if (returnDate.isBefore(departDate)) {
                    backWithError(request, response, "Ngày về phải sau hoặc bằng ngày đi.");
                    return;
                }
            }

            if (timeRaw != null && !timeRaw.isBlank()) {
                try {
                    departTime = LocalTime.parse(timeRaw);
                } catch (DateTimeParseException ex) {
                    backWithError(request, response, "Giờ đi không hợp lệ (HH:mm).");
                    return;
                }
            }

            // 4) Kiểm tra có chuyến hay không
            SearchResult sr = tripService.searchTripsByStationIds(
                    tripType, originId, destId, departDate, departTime, returnDate, null);

            Optional<Trip> chosenOutbound = (sr.outbound == null || sr.outbound.isEmpty())
                    ? Optional.empty()
                    : sr.outbound.stream().min(Comparator.comparing(Trip::getDepartAt));
            if (chosenOutbound.isEmpty()) {
                backWithError(request, response, "Không có chuyến phù hợp cho chiều đi.");
                return;
            }

            Optional<Trip> chosenInbound = Optional.empty();
            if ("ROUNDTRIP".equalsIgnoreCase(tripType) && sr.inbound != null && !sr.inbound.isEmpty()) {
                chosenInbound = sr.inbound.stream().min(Comparator.comparing(Trip::getDepartAt));
            }

            // 5) Lưu ngữ cảnh tìm kiếm
            SearchContext ctx = new SearchContext();
            ctx.setTripType(tripType);
            ctx.setOriginStationId(originId);
            ctx.setDestStationId(destId);
            ctx.setOriginName(originName);
            ctx.setDestName(destName);
            ctx.setDepartDate(departDate);
            ctx.setDepartTime(departTime);
            ctx.setReturnDate(returnDate);

            ss.setAttribute("searchCtx", ctx);
            ss.setAttribute("chosenOutboundTripId", chosenOutbound.get().getTripId());
            ss.setAttribute("chosenInboundTripId", chosenInbound.map(Trip::getTripId).orElse(null));

            // 6) Lấy danh sách chuyến cho ngày đang xem và forward sang /trips
            LocalDate viewDate = departDate;
            List<TripCardVm> trips = tripListDao.queryTripsForDate(originId, destId, viewDate);
            List<DayTabVm> days = tripListDao.buildDayTabs(request, viewDate, originId, destId);

            request.setAttribute("routeTitle", originName + " → " + destName);
            request.setAttribute("searchDate", viewDate);
            request.setAttribute("days", days);
            request.setAttribute("trips", trips);

            request.setAttribute("prevDateUrl",
                    request.getContextPath() + "/trips?originId=" + originId
                    + "&destId=" + destId + "&date=" + viewDate.minusDays(1));
            request.setAttribute("nextDateUrl",
                    request.getContextPath() + "/trips?originId=" + originId
                    + "&destId=" + destId + "&date=" + viewDate.plusDays(1));

            request.getRequestDispatcher("/WEB-INF/views/customer/triplist.jsp").forward(request, response);

        } catch (Exception ex) {
            ex.printStackTrace();
            backWithError(request, response, "Đã xảy ra lỗi khi tìm kiếm.");
        }
    }

    private void backWithError(HttpServletRequest req, HttpServletResponse resp, String msg)
            throws IOException {
        req.getSession(true).setAttribute("error", msg);
        resp.sendRedirect(req.getContextPath() + "/home");
    }

    private static String trimOrNull(String s) {
        return (s == null) ? null : s.trim();
    }
}
