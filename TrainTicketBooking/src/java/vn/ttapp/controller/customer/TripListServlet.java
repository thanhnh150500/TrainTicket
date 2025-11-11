package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import vn.ttapp.dao.TripListDao;
import vn.ttapp.dao.StationDao;
import vn.ttapp.model.DayTabVm;
import vn.ttapp.model.TripCardVm;

@WebServlet(name = "TripListServlet", urlPatterns = {"/trips"})
public class TripListServlet extends HttpServlet {

    private final TripListDao tripListDao = new TripListDao();
    private final StationDao stationDao = new StationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // --- Lấy & kiểm tra tham số ---
        String originIdRaw = req.getParameter("originId");
        String destIdRaw = req.getParameter("destId");
        String dateRaw = req.getParameter("date");

        if (originIdRaw == null || destIdRaw == null || dateRaw == null) {
            backHome(req, resp, "Thiếu tham số xem chuyến (originId, destId, date).");
            return;
        }

        final int originId, destId;
        final LocalDate date;
        try {
            originId = Integer.parseInt(originIdRaw);
            destId = Integer.parseInt(destIdRaw);
            date = LocalDate.parse(dateRaw); // yyyy-MM-dd
        } catch (NumberFormatException | DateTimeParseException ex) {
            backHome(req, resp, "Tham số không hợp lệ.");
            return;
        }

        try {
            // --- Danh sách chuyến cho ngày đang xem ---
            List<TripCardVm> trips = tripListDao.listTripsByRouteAndDay(originId, destId, date, null);

            // --- Dải ngày (-1 → +7) & prev/next URL (dùng hàm trong DAO) ---
            List<DayTabVm> days = tripListDao.buildDayTabs(req, date, originId, destId);

            // --- Thông tin hiển thị header ---
            String originName = safeStationName(originId);
            String destName = safeStationName(destId);

            Locale vi = new Locale("vi");
            DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String activeDateLabel = date.format(DMY) + ", "
                    + date.getDayOfWeek().getDisplayName(TextStyle.FULL, vi); 
            
            // JSP
            req.setAttribute("routeTitle", originName + " → " + destName);
            req.setAttribute("routeOriginCode", originName);
            req.setAttribute("routeDestCode", destName);
            req.setAttribute("activeDateLabel", activeDateLabel);
            req.setAttribute("searchDate", date);
            req.setAttribute("days", days);
            req.setAttribute("trips", trips);

            req.getRequestDispatcher("/WEB-INF/views/customer/triplist.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            backHome(req, resp, "Không tải được danh sách chuyến.");
        }
    }

    // ===== Helpers =====
    private void backHome(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException {
        req.getSession(true).setAttribute("error", msg);
        resp.sendRedirect(req.getContextPath() + "/home");
    }

    /**
     * Lấy tên ga theo ID;
     * findById và getName().
     */
    private String safeStationName(int stationId) {
        try {
            //findNameById(int)
            String name = stationDao.findNameById(stationId);
            if (name != null && !name.isBlank()) {
                return name;
            }

            // Fallback
            var st = stationDao.findById(stationId);
            return (st != null && st.getName() != null) ? st.getName() : ("#" + stationId);
        } catch (Exception ignore) {
            return "#" + stationId;
        }
    }
}
