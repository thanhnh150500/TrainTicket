package vn.ttapp.controller.customer;

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

    /* ---------- helpers ---------- */
    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }
    
     /** Parse ngày cho phép 2 format: yyyy-MM-dd (HTML date) và dd/MM/yyyy (người dùng quen thuộc) */
    private LocalDate parseDateLenient(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        var fmts = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE, // yyyy-MM-dd
                DateTimeFormatter.ofPattern("dd/MM/yyyy") // dd/MM/yyyy
        );
        for (var f : fmts) {
            try {
                return LocalDate.parse(s, f);
            } catch (DateTimeParseException ignore) {
            }
        }
        return null;
    }
    
    /** Parse giờ cho phép: HH:mm[:ss], H:mm, HH:mm */
    private LocalTime parseTimeLenient(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        var fmts = List.of(
                DateTimeFormatter.ISO_LOCAL_TIME, // HH:mm[:ss]
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm")
        );
        for (var f : fmts) {
            try {
                return LocalTime.parse(s, f);
            } catch (DateTimeParseException ignore) {
            }
        }
        return null;
    }
    
    /** Parse Integer an toàn (null nếu không phải số) */
    private static Integer parseIntSafe(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        final String typeRaw = nz(request.getParameter("tripType"));
        final String originNm = nz(request.getParameter("originStation"));
        final String destNm = nz(request.getParameter("destStation"));
        final String departRaw = nz(request.getParameter("departDate"));
        final String returnRaw = nz(request.getParameter("returnDate"));
        final String timeRaw = nz(request.getParameter("departTime"));
        final String paxRaw = nz(request.getParameter("pax"));

        // hidden IDs (ưu tiên)
        Integer originIdParam = parseIntSafe(request.getParameter("originId"));
        Integer destIdParam = parseIntSafe(request.getParameter("destId"));

        // chuẩn hoá tripType
        String tripType = switch (typeRaw.toUpperCase()) {
            case "ROUNDTRIP", "ROUND", "RT" ->
                "ROUNDTRIP";
            default ->
                "ONEWAY";
        };

        // lưu để giữ input khi quay lại /home
        HttpSession ss = request.getSession(true);
        ss.setAttribute("lastOrigin", originNm);
        ss.setAttribute("lastDest", destNm);
        ss.setAttribute("lastTripType", tripType);
        ss.setAttribute("lastDepart", departRaw);
        ss.setAttribute("lastReturn", returnRaw);

        try {
            // Ưu tiên id ẩn; nếu chưa có thì tìm theo tên ga 
            Integer originId = (originIdParam != null && originIdParam > 0) ? originIdParam : null;
            Integer destId = (destIdParam != null && destIdParam > 0) ? destIdParam : null;
                    
            if (originId == null && !originNm.isBlank()) {
                originId = stationDao.findIdByNameExact(originNm);
                if (originId == null) {
                    originId = stationDao.findIdByNameLoose(originNm);
                }
            }
            if (destId == null && !destNm.isBlank()) {
                destId = stationDao.findIdByNameExact(destNm);
                if (destId == null) {
                    destId = stationDao.findIdByNameLoose(destNm);
                }
            }
            
            // validate ga
            if (originId == null || destId == null) {
                backWithError(request, response, "Không tìm thấy ga đi/ga đến trong hệ thống.");
                return;
            }
            if (originId.equals(destId)) {
                backWithError(request, response, "Ga đi và ga đến phải khác nhau.");
                return;
            }

            // --- validate ngày/giờ ---
            LocalDate departDate = parseDateLenient(departRaw);
            if (departDate == null) {
                backWithError(request, response, "Ngày đi không hợp lệ. Dùng yyyy-MM-dd hoặc dd/MM/yyyy.");
                return;
            }
            
            LocalDate returnDate = null;
            if ("ROUNDTRIP".equals(tripType)) {
                if (returnRaw.isBlank()) {
                    backWithError(request, response, "Bạn chọn khứ hồi. Vui lòng chọn ngày về.");
                    return;
                }
                returnDate = parseDateLenient(returnRaw);
                if (returnDate == null) {
                    backWithError(request, response, "Ngày về không hợp lệ. Dùng yyyy-MM-dd hoặc dd/MM/yyyy.");
                    return;
                }
                if (returnDate.isBefore(departDate)) {
                    backWithError(request, response, "Ngày về phải sau hoặc bằng ngày đi.");
                    return;
                }
            }
            // giờ đi có thể không bắt buộc
            LocalTime departTime = timeRaw.isBlank() ? null : parseTimeLenient(timeRaw);
            
            int pax = 1;
            try {
                if (!paxRaw.isBlank()) {
                    pax = Math.max(1, Integer.parseInt(paxRaw));
                }
            } catch (NumberFormatException ignore) {
            }

            // --- tìm chuyến ---
            SearchResult sr = tripService.searchTripsByStationIds(
                    tripType, originId, destId, departDate, departTime, returnDate, null);
            
            // chọn chuyến gợi ý chuyến sớm nhấts
            Optional<Trip> chosenOutbound
                    = (sr.outbound == null || sr.outbound.isEmpty())
                    ? Optional.empty()
                    : sr.outbound.stream().min(Comparator.comparing(Trip::getDepartAt));

            Optional<Trip> chosenInbound = Optional.empty();
            if ("ROUNDTRIP".equals(tripType) && sr.inbound != null && !sr.inbound.isEmpty()) {
                chosenInbound = sr.inbound.stream().min(Comparator.comparing(Trip::getDepartAt));
            }

            // --- build context + dữ liệu list ---
            SearchContext ctx = new SearchContext();
            ctx.setTripType(tripType);
            ctx.setOriginStationId(originId);
            ctx.setDestStationId(destId);
            ctx.setOriginName(originNm);
            ctx.setDestName(destNm);
            ctx.setDepartDate(departDate);
            ctx.setDepartTime(departTime);
            ctx.setReturnDate(returnDate);
            ctx.setPax(pax);

            ss.setAttribute("searchCtx", ctx);
            ss.setAttribute("chosenOutboundTripId", chosenOutbound.map(Trip::getTripId).orElse(null));
            ss.setAttribute("chosenInboundTripId", chosenInbound.map(Trip::getTripId).orElse(null));
            
            // Dữ liệu lấy cho triplistjsp
            LocalDate viewDate = departDate;
            List<TripCardVm> trips = tripListDao.queryTripsForDate(originId, destId, viewDate);
            List<DayTabVm> days = tripListDao.buildDayTabs(request, viewDate, originId, destId);

            DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            request.setAttribute("routeTitle",
                    (originNm.isBlank() ? "" : originNm) + " → " + (destNm.isBlank() ? "" : destNm));
            request.setAttribute("activeDateLabel", DMY.format(viewDate));
            request.setAttribute("searchDate", viewDate);
            request.setAttribute("days", days);
            request.setAttribute("trips", trips);

            // cờ để JSP hiển thị thông báo rỗng
            request.setAttribute("emptyOutbound", (sr.outbound == null || sr.outbound.isEmpty()));
            request.setAttribute("emptyInbound", "ROUNDTRIP".equals(tripType)
                    && (sr.inbound == null || sr.inbound.isEmpty()));
            
            
            request.setAttribute("prevDateUrl",
                    request.getContextPath() + "/trips?originId=" + originId
                    + "&destId=" + destId + "&date=" + viewDate.minusDays(1));
            request.setAttribute("nextDateUrl",
                    request.getContextPath() + "/trips?originId=" + originId
                    + "&destId=" + destId + "&date=" + viewDate.plusDays(1));

            request.getRequestDispatcher("/WEB-INF/views/customer/triplist.jsp").forward(request, response);

        } catch (Exception ex) {
            ex.printStackTrace(); // log server
            HttpSession ss2 = request.getSession(true);
            String msg = ex.getClass().getSimpleName();
            if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
                msg += ": " + ex.getMessage();
            }
            ss2.setAttribute("error", "Lỗi tìm kiếm: " + msg);
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }

    /* redirect về /home với thông báo lỗi */
    private void backWithError(HttpServletRequest req, HttpServletResponse resp, String msg)
            throws IOException {
        req.getSession(true).setAttribute("error", msg);
        resp.sendRedirect(req.getContextPath() + "/home");
    }
}
