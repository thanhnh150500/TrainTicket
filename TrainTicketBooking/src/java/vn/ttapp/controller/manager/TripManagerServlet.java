package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import vn.ttapp.dao.RouteDao;
import vn.ttapp.dao.TrainDao;
import vn.ttapp.model.Route;
import vn.ttapp.model.Trip;
import vn.ttapp.model.Train;
import vn.ttapp.model.User;
import vn.ttapp.service.TripService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Quản lý chuyến tàu (Khu vực Manager/Admin).
 *
 * GET : /manager/trips?op=list|new|edit|view POST : /manager/trips
 * (op=save|delete)
 *
 * - Chặn truy cập: chỉ cho phép nếu session có cờ isManager hoặc isAdmin (đặt
 * bởi LoginServlet/AuthFilter). - Validate cơ bản và logic thời gian (arrive >
 * depart). - Gán staff F&B cho chuyến (service.assignStaff) sau khi tạo/cập
 * nhật thành công.
 */
@WebServlet(name = "TripManagerServlet", urlPatterns = {"/manager/trips"})
public class TripManagerServlet extends HttpServlet {

    private static final String VIEW_FORM = "/WEB-INF/views/manager/trip_form.jsp";
    private static final String VIEW_LIST = "/WEB-INF/views/manager/trip_list.jsp";
    private static final String VIEW_VIEW = "/WEB-INF/views/manager/trip_view.jsp";

    private final TripService service = new TripService();
    private final RouteDao routeDao = new RouteDao();
    private final TrainDao trainDao = new TrainDao();

    /* =========================
       Helpers
       ========================= */
    /**
     * Chỉ cho phép Manager/Admin (dựa trên cờ đặt ở LoginServlet/AuthFilter).
     */
    private static boolean isManagerOrAdmin(HttpSession ss) {
        if (ss == null) {
            return false;
        }
        Boolean isMgr = (Boolean) ss.getAttribute("isManager");
        Boolean isAdm = (Boolean) ss.getAttribute("isAdmin");
        return Boolean.TRUE.equals(isMgr) || Boolean.TRUE.equals(isAdm);
    }

    /**
     * Parse int an toàn (nullable).
     */
    private static Integer parseIntSafe(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse datetime-local "yyyy-MM-dd'T'HH:mm" an toàn (nullable).
     */
    private static LocalDateTime parseDateTimeLocal(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            // Input chuẩn từ <input type="datetime-local">: yyyy-MM-ddTHH:mm
            return LocalDateTime.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignore) {
            // fallback: nếu có giây, cắt còn phút
            try {
                int i = s.lastIndexOf(':');
                if (i > 0) {
                    String mmOnly = s.substring(0, i); // yyyy-MM-ddTHH:mm
                    return LocalDateTime.parse(mmOnly + ":00");
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    /**
     * Format LocalDateTime -> "yyyy-MM-ddTHH:mm" để bind lại vào input.
     */
    private static String toInputValue(LocalDateTime dt) {
        if (dt == null) {
            return "";
        }
        String s = dt.toString(); // ISO_LOCAL_DATE_TIME
        int lastColon = s.lastIndexOf(':');
        if (lastColon > 0) {
            return s.substring(0, lastColon);
        }
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }

    /**
     * Load routes/trains/staff tham chiếu cho form.
     */
    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<Route> routes = routeDao.findAll();
        List<Train> trains = trainDao.findAll();
        // Staff phục vụ F&B gán cho chuyến (tuỳ business của bạn)
        List<User> allStaff = Optional.ofNullable(service.getAllStaffFNB()).orElseGet(List::of);

        req.setAttribute("routes", routes);
        req.setAttribute("trains", trains);
        req.setAttribute("allStaff", allStaff);
    }

    /**
     * Forward tiện lợi.
     */
    private void fwd(HttpServletRequest req, HttpServletResponse res, String jsp) throws ServletException, IOException {
        req.getRequestDispatcher(jsp).forward(req, res);
    }

    /* =========================
       GET
       ========================= */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // Gate quyền
        HttpSession ss = req.getSession(false);
        if (!isManagerOrAdmin(ss)) {
            // Nếu chưa login/không đủ quyền, điều hướng về login và mang theo next
            res.sendRedirect(req.getContextPath() + "/auth/login?next=/manager/trips");
            return;
        }

        String op = Optional.ofNullable(req.getParameter("op")).orElse("list");

        try {
            switch (op) {
                case "new" -> {
                    loadRefs(req);
                    Trip t = new Trip();
                    req.setAttribute("t", t);
                    req.setAttribute("departAtInput", "");
                    req.setAttribute("arriveAtInput", "");
                    req.setAttribute("assignedStaffIds", List.of());
                    fwd(req, res, VIEW_FORM);
                }
                case "edit" -> {
                    Integer id = parseIntSafe(req.getParameter("id"));
                    if (id == null) {
                        req.getSession().setAttribute("flash_error", "Thiếu hoặc sai ID chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                        return;
                    }
                    Trip t = service.findById(id);
                    if (t == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                        return;
                    }
                    loadRefs(req);
                    req.setAttribute("t", t);
                    req.setAttribute("departAtInput", toInputValue(t.getDepartAt()));
                    req.setAttribute("arriveAtInput", toInputValue(t.getArriveAt()));

                    List<String> assignedStaffIds = service.getStaffByTrip(id).stream()
                            .map(u -> u.getUserId() == null ? null : u.getUserId().toString())
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    req.setAttribute("assignedStaffIds", assignedStaffIds);

                    fwd(req, res, VIEW_FORM);
                }
                case "view" -> {
                    Integer id = parseIntSafe(req.getParameter("id"));
                    if (id == null) {
                        req.getSession().setAttribute("flash_error", "Thiếu hoặc sai ID chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                        return;
                    }
                    Trip t = service.findById(id);
                    if (t == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                        return;
                    }
                    var routeMeta = routeDao.findById(t.getRouteId());
                    var trainMeta = trainDao.findDetail(t.getTrainId());
                    List<User> staffList = service.getStaffByTrip(id);

                    req.setAttribute("t", t);
                    req.setAttribute("routeMeta", routeMeta);
                    req.setAttribute("trainMeta", trainMeta);
                    req.setAttribute("staffList", staffList);
                    fwd(req, res, VIEW_VIEW);
                }
                default -> {
                    // list
                    req.setAttribute("list", service.findAll());
                    fwd(req, res, VIEW_LIST);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /* =========================
       POST
       ========================= */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // Gate quyền
        HttpSession ss = req.getSession(false);
        if (!isManagerOrAdmin(ss)) {
            res.sendRedirect(req.getContextPath() + "/auth/login?next=/manager/trips");
            return;
        }

        // Nếu bạn thêm CSRF cho form manager, bật block dưới (phải cấp token khi render form)
        /*
        String sTok = (ss == null) ? null : (String) ss.getAttribute("csrfToken");
        String fTok = req.getParameter("_csrf");
        if (sTok != null && (fTok == null || !sTok.equals(fTok))) {
            ss.setAttribute("flash_error", "Phiên không hợp lệ. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
            return;
        }
         */
        String op = Optional.ofNullable(req.getParameter("op")).orElse("save");

        try {
            switch (op) {
                case "save" -> {
                    String idRaw = req.getParameter("trip_id");
                    String routeRaw = req.getParameter("route_id");
                    String trainRaw = req.getParameter("train_id");
                    String departRaw = req.getParameter("depart_at"); // yyyy-MM-ddTHH:mm
                    String arriveRaw = req.getParameter("arrive_at");
                    String status = req.getParameter("status");

                    Integer routeId = parseIntSafe(routeRaw);
                    Integer trainId = parseIntSafe(trainRaw);
                    LocalDateTime departAt = parseDateTimeLocal(departRaw);
                    LocalDateTime arriveAt = parseDateTimeLocal(arriveRaw);

                    String[] staffIds = req.getParameterValues("staff_ids");
                    List<String> assignedStaffIds = (staffIds == null) ? List.of() : Arrays.asList(staffIds);

                    // VALIDATION (bắt buộc)
                    if (routeId == null || trainId == null || departAt == null || arriveAt == null
                            || status == null || status.isBlank()) {

                        Trip t = new Trip();
                        if (idRaw != null && !idRaw.isBlank()) {
                            t.setTripId(Integer.parseInt(idRaw));
                        }
                        t.setRouteId(routeId);
                        t.setTrainId(trainId);
                        t.setDepartAt(departAt);
                        t.setArriveAt(arriveAt);
                        t.setStatus(status);

                        req.setAttribute("error", "Vui lòng chọn Tuyến, Tàu, nhập thời gian đi/đến và trạng thái.");
                        loadRefs(req);
                        req.setAttribute("t", t);
                        req.setAttribute("departAtInput", departRaw);
                        req.setAttribute("arriveAtInput", arriveRaw);
                        req.setAttribute("assignedStaffIds", assignedStaffIds);
                        fwd(req, res, VIEW_FORM);
                        return;
                    }

                    // VALIDATION (logic thời gian)
                    if (!arriveAt.isAfter(departAt)) {
                        Trip t = new Trip(parseIntSafe(idRaw), routeId, trainId, departAt, arriveAt, status);
                        req.setAttribute("error", "Giờ đến phải sau giờ khởi hành.");
                        loadRefs(req);
                        req.setAttribute("t", t);
                        req.setAttribute("departAtInput", departRaw);
                        req.setAttribute("arriveAtInput", arriveRaw);
                        req.setAttribute("assignedStaffIds", assignedStaffIds);
                        fwd(req, res, VIEW_FORM);
                        return;
                    }

                    // CREATE / UPDATE
                    Integer tripId;
                    if (idRaw == null || idRaw.isBlank()) {
                        // CREATE
                        tripId = service.create(routeId, trainId, departAt, arriveAt, status);
                        if (tripId == null) {
                            Trip t = new Trip(null, routeId, trainId, departAt, arriveAt, status);
                            req.setAttribute("error", "Không thể tạo chuyến.");
                            loadRefs(req);
                            req.setAttribute("t", t);
                            req.setAttribute("departAtInput", departRaw);
                            req.setAttribute("arriveAtInput", arriveRaw);
                            req.setAttribute("assignedStaffIds", assignedStaffIds);
                            fwd(req, res, VIEW_FORM);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo chuyến.");
                    } else {
                        // UPDATE
                        tripId = Integer.parseInt(idRaw);
                        Trip t = new Trip(tripId, routeId, trainId, departAt, arriveAt, status);
                        boolean ok = service.update(t);
                        if (!ok) {
                            req.setAttribute("error", "Không thể cập nhật chuyến.");
                            loadRefs(req);
                            req.setAttribute("t", t);
                            req.setAttribute("departAtInput", departRaw);
                            req.setAttribute("arriveAtInput", arriveRaw);
                            req.setAttribute("assignedStaffIds", assignedStaffIds);
                            fwd(req, res, VIEW_FORM);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật chuyến.");
                    }

                    // Gán staff sau khi có tripId
                    service.assignStaff(tripId, assignedStaffIds, "STAFF_FNB");

                    res.sendRedirect(req.getContextPath() + "/manager/trips");
                }
                case "delete" -> {
                    Integer id = parseIntSafe(req.getParameter("id"));
                    if (id == null) {
                        req.getSession().setAttribute("flash_error", "Thiếu hoặc sai ID chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                        return;
                    }
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xoá chuyến.");
                    res.sendRedirect(req.getContextPath() + "/manager/trips");
                }
                default -> {
                    res.sendRedirect(req.getContextPath() + "/manager/trips");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
        } catch (Exception ex) {
            ex.printStackTrace();
            req.getSession().setAttribute("flash_error", "Lỗi không xác định.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
        }
    }
}
