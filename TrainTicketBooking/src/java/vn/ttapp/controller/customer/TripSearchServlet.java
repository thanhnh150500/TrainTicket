package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import vn.ttapp.dao.SearchContext;
import vn.ttapp.dao.StationDao;
import vn.ttapp.dao.TripListDao;
import vn.ttapp.model.Trip;
import vn.ttapp.model.TripCardVm;
import vn.ttapp.model.DayTabVm;
import vn.ttapp.service.TripService;
import vn.ttapp.service.TripService.SearchResult;

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

    /* ---------- helpers ---------- */
    // yyyy-MM-dd hoặc dd/MM/yyyy
    private LocalDate parseDateLenient(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        List<DateTimeFormatter> fmts = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
        for (DateTimeFormatter f : fmts) {
            try {
                return LocalDate.parse(s, f);
            } catch (DateTimeParseException ignore) {
            }
        }
        return null;
    }

    // HH:mm, HH:mm:ss, H:mm
    private LocalTime parseTimeLenient(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        List<DateTimeFormatter> fmts = List.of(
                DateTimeFormatter.ISO_LOCAL_TIME,
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm")
        );
        for (DateTimeFormatter f : fmts) {
            try {
                return LocalTime.parse(s, f);
            } catch (DateTimeParseException ignore) {
            }
        }
        return null;
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trimOrNull(String s) {
        return (s == null) ? null : s.trim();
    }

    private static String normalizeTripType(String t) {
        if (t == null) {
            return "ONEWAY";
        }
        String x = t.trim().toUpperCase(Locale.ROOT);
        if (x.startsWith("ROUND")) {
            return "ROUNDTRIP";
        }
        return "ONEWAY";
    }

    /* ---------- main ---------- */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Tên người dùng nhập (để lưu session / hiển thị)
        final String originNameRaw = trimOrNull(request.getParameter("originStation"));
        final String destNameRaw = trimOrNull(request.getParameter("destStation"));

        // Hidden ID (ưu tiên)
        final Integer originIdFromForm = parseIntOrNull(request.getParameter("originId"));
        final Integer destIdFromForm = parseIntOrNull(request.getParameter("destId"));

        final String tripType = normalizeTripType(trimOrNull(request.getParameter("tripType")));
        final String departRaw = trimOrNull(request.getParameter("departDate"));
        final String returnRaw = trimOrNull(request.getParameter("returnDate"));
        final String timeRaw = trimOrNull(request.getParameter("departTime"));
        final String paxRaw = trimOrNull(request.getParameter("pax"));

        HttpSession ss = request.getSession(true);
        ss.setAttribute("lastOrigin", originNameRaw);
        ss.setAttribute("lastDest", destNameRaw);
        ss.setAttribute("lastTripType", tripType);
        ss.setAttribute("lastDepart", departRaw);
        ss.setAttribute("lastReturn", returnRaw);

        try {
            // 1) Validate cơ bản
            if ((originNameRaw == null && originIdFromForm == null)
                    || (destNameRaw == null && destIdFromForm == null)
                    || departRaw == null) {
                backWithError(request, response, "Vui lòng nhập ga đi, ga đến và ngày đi.");
                return;
            }

            if (originNameRaw != null && destNameRaw != null
                    && originNameRaw.equalsIgnoreCase(destNameRaw)) {
                backWithError(request, response, "Ga đi và ga đến phải khác nhau.");
                return;
            }

            // 2) Resolve Station IDs (ưu tiên hidden ID)
            Integer originId = originIdFromForm;
            Integer destId = destIdFromForm;

            if (originId == null && originNameRaw != null) {
                originId = stationDao.findIdByNameExact(originNameRaw);
                if (originId == null) {
                    originId = stationDao.findIdByNameLoose(originNameRaw);
                }
            }
            if (destId == null && destNameRaw != null) {
                destId = stationDao.findIdByNameExact(destNameRaw);
                if (destId == null) {
                    destId = stationDao.findIdByNameLoose(destNameRaw);
                }
            }

            if (originId == null || destId == null) {
                backWithError(request, response, "Không tìm thấy ga đi/ga đến trong hệ thống.");
                return;
            }

            // Lấy lại tên chuẩn từ DB (nếu cần) để hiển thị ổn định
            String originName = (originNameRaw != null) ? originNameRaw : stationDao.findNameById(originId);
            String destName = (destNameRaw != null) ? destNameRaw : stationDao.findNameById(destId);

            // 3) Parse ngày giờ
            LocalDate departDate = parseDateLenient(departRaw);
            if (departDate == null) {
                backWithError(request, response, "Ngày đi không hợp lệ. Hãy dùng định dạng yyyy-MM-dd hoặc dd/MM/yyyy.");
                return;
            }

            LocalDate returnDate = null;
            if ("ROUNDTRIP".equals(tripType)) {
                if (returnRaw == null || returnRaw.isBlank()) {
                    backWithError(request, response, "Bạn chọn khứ hồi. Vui lòng chọn ngày về.");
                    return;
                }
                returnDate = parseDateLenient(returnRaw);
                if (returnDate == null) {
                    backWithError(request, response, "Ngày về không hợp lệ. Hãy dùng định dạng yyyy-MM-dd hoặc dd/MM/yyyy.");
                    return;
                }
                if (returnDate.isBefore(departDate)) {
                    backWithError(request, response, "Ngày về phải sau hoặc bằng ngày đi.");
                    return;
                }
            }

            LocalTime departTime = null;
            if (timeRaw != null && !timeRaw.isBlank()) {
                departTime = parseTimeLenient(timeRaw);
                if (departTime == null) {
                    backWithError(request, response, "Giờ đi không hợp lệ. Hãy dùng định dạng HH:mm hoặc HH:mm:ss.");
                    return;
                }
            }

            int pax = 1;
            if (paxRaw != null && !paxRaw.isBlank()) {
                try {
                    pax = Math.max(1, Integer.parseInt(paxRaw));
                } catch (NumberFormatException ignore) {
                    pax = 1;
                }
            }

            // 4) Tìm chuyến
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
            if ("ROUNDTRIP".equals(tripType) && sr.inbound != null && !sr.inbound.isEmpty()) {
                chosenInbound = sr.inbound.stream().min(Comparator.comparing(Trip::getDepartAt));
            }

            // 5) SearchContext
            SearchContext ctx = new SearchContext();
            ctx.setTripType(tripType);
            ctx.setOriginStationId(originId);
            ctx.setDestStationId(destId);
            ctx.setOriginName(originName);
            ctx.setDestName(destName);
            ctx.setDepartDate(departDate);
            ctx.setDepartTime(departTime);
            ctx.setReturnDate(returnDate);
            ctx.setPax(pax);

            ss.setAttribute("searchCtx", ctx);
            ss.setAttribute("chosenOutboundTripId", chosenOutbound.get().getTripId());
            ss.setAttribute("chosenInboundTripId", chosenInbound.map(Trip::getTripId).orElse(null));

            // 6) Dữ liệu trang danh sách chuyến
            LocalDate viewDate = departDate;
            List<TripCardVm> trips = tripListDao.queryTripsForDate(originId, destId, viewDate);
            List<DayTabVm> days = tripListDao.buildDayTabs(request, viewDate, originId, destId);

            DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            request.setAttribute("routeTitle", originName + " → " + destName);
            request.setAttribute("routeOriginCode", originName);
            request.setAttribute("routeDestCode", destName);
            request.setAttribute("activeDateLabel", DMY.format(viewDate));
            request.setAttribute("searchDate", viewDate);
            request.setAttribute("days", days);
            request.setAttribute("trips", trips);

            request.setAttribute("prevDateUrl",
                    request.getContextPath() + "/trips?originId=" + originId
                    + "&destId=" + destId + "&date=" + viewDate.minusDays(1));
            request.setAttribute("nextDateUrl",
                    request.getContextPath() + "/trips?originId=" + originId
                    + "&destId=" + destId + "&date=" + viewDate.plusDays(1));

            // 7) Forward
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
}
