package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.RouteDao;
import vn.ttapp.dao.TrainDao;
import vn.ttapp.model.Route;
import vn.ttapp.model.Trip;
import vn.ttapp.model.Train;
import vn.ttapp.service.TripService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet(name = "TripManagerServlet", urlPatterns = {"/manager/trips"})
public class TripManagerServlet extends HttpServlet {

    private final TripService service = new TripService();
    private final RouteDao routeDao = new RouteDao();
    private final TrainDao trainDao = new TrainDao();

    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<Route> routes = routeDao.findAll();
        List<Train> trains = trainDao.findAll();
        req.setAttribute("routes", routes);
        req.setAttribute("trains", trains);
    }

    private static String toInputValue(LocalDateTime dt) {
        if (dt == null) {
            return "";
        }
        String s = dt.toString(); // 2025-10-27T14:05:23.123
        // chỉ lấy đến phút: yyyy-MM-ddTHH:mm
        int i = s.indexOf(':');
        if (i > 0) {
            int secondSep = s.indexOf(':', i + 1);
            if (secondSep > 0) {
                return s.substring(0, secondSep);
            }
        }
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String op = req.getParameter("op");
        if (op == null) {
            op = "list";
        }

        try {
            switch (op) {
                case "new" -> {
                    loadRefs(req);
                    Trip t = new Trip();
                    req.setAttribute("t", t);
                    req.setAttribute("departAtInput", "");
                    req.setAttribute("arriveAtInput", "");
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
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
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                }
                case "view" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Trip t = service.findById(id);
                    if (t == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                        return;
                    }
                    // Route đã có origin/dest name ngay trong RouteDao.findById()
                    var routeMeta = routeDao.findById(t.getRouteId());
                    // Dùng findDetail để lấy full train meta (toa/ghế)
                    var trainMeta = new TrainDao().findDetail(t.getTrainId());

                    req.setAttribute("t", t);
                    req.setAttribute("routeMeta", routeMeta);
                    req.setAttribute("trainMeta", trainMeta);
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_view.jsp").forward(req, res);
                }
                default -> {
                    req.setAttribute("list", service.findAll());
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_list.jsp").forward(req, res);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException nfe) {
            req.getSession().setAttribute("flash_error", "Tham số không hợp lệ.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String op = req.getParameter("op");
        if (op == null) {
            op = "save";
        }

        try {
            switch (op) {
                case "save" -> {
                    String idRaw = req.getParameter("trip_id");
                    String routeIdRaw = req.getParameter("route_id");
                    String trainIdRaw = req.getParameter("train_id");
                    String departRaw = req.getParameter("depart_at");
                    String arriveRaw = req.getParameter("arrive_at");
                    String status = req.getParameter("status");

                    Integer routeId = (routeIdRaw == null || routeIdRaw.isBlank()) ? null : Integer.parseInt(routeIdRaw);
                    Integer trainId = (trainIdRaw == null || trainIdRaw.isBlank()) ? null : Integer.parseInt(trainIdRaw);

                    LocalDateTime departAt = null, arriveAt = null;
                    try {
                        if (departRaw != null && !departRaw.isBlank()) {
                            departAt = LocalDateTime.parse(departRaw);
                        }
                    } catch (Exception ignore) {
                    }
                    try {
                        if (arriveRaw != null && !arriveRaw.isBlank()) {
                            arriveAt = LocalDateTime.parse(arriveRaw);
                        }
                    } catch (Exception ignore) {
                    }

                    if (routeId == null || trainId == null || departAt == null || arriveAt == null || status == null || status.isBlank()) {
                        req.setAttribute("error", "Vui lòng chọn Tuyến, Tàu, nhập thời gian đi/đến và trạng thái.");
                        Trip t = new Trip();
                        if (idRaw != null && !idRaw.isBlank()) {
                            t.setTripId(Integer.parseInt(idRaw));
                        }
                        t.setRouteId(routeId);
                        t.setTrainId(trainId);
                        t.setDepartAt(departAt);
                        t.setArriveAt(arriveAt);
                        t.setStatus(status);
                        loadRefs(req);
                        req.setAttribute("t", t);
                        req.setAttribute("departAtInput", departRaw == null ? "" : departRaw);
                        req.setAttribute("arriveAtInput", arriveRaw == null ? "" : arriveRaw);
                        req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                        return;
                    }
                    if (!arriveAt.isAfter(departAt)) {
                        req.setAttribute("error", "Giờ đến phải sau giờ khởi hành.");
                        Trip t = new Trip(null, routeId, trainId, departAt, arriveAt, status);
                        loadRefs(req);
                        req.setAttribute("t", t);
                        req.setAttribute("departAtInput", departRaw);
                        req.setAttribute("arriveAtInput", arriveRaw);
                        req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                        return;
                    }

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer newId = service.create(routeId, trainId, departAt, arriveAt, status);
                        if (newId == null) {
                            req.setAttribute("error", "Không thể tạo chuyến (tuyến/tàu không hợp lệ, trạng thái sai hoặc thời gian không hợp lệ).");
                            Trip t = new Trip(null, routeId, trainId, departAt, arriveAt, status);
                            loadRefs(req);
                            req.setAttribute("t", t);
                            req.setAttribute("departAtInput", departRaw);
                            req.setAttribute("arriveAtInput", arriveRaw);
                            req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                    } else {
                        Trip t = new Trip(Integer.parseInt(idRaw), routeId, trainId, departAt, arriveAt, status);
                        boolean ok = service.update(t);
                        if (!ok) {
                            req.setAttribute("error", "Không thể cập nhật chuyến (tuyến/tàu không hợp lệ, trạng thái sai hoặc thời gian không hợp lệ).");
                            loadRefs(req);
                            req.setAttribute("t", t);
                            req.setAttribute("departAtInput", departRaw);
                            req.setAttribute("arriveAtInput", arriveRaw);
                            req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa chuyến.");
                    res.sendRedirect(req.getContextPath() + "/manager/trips");
                }
                default ->
                    res.sendRedirect(req.getContextPath() + "/manager/trips");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
        } catch (NumberFormatException nfe) {
            req.getSession().setAttribute("flash_error", "Tham số không hợp lệ.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
        }
    }
}
