package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.RouteDao;
import vn.ttapp.dao.TrainDao;
import vn.ttapp.dao.TrainDao.TrainMeta;
import vn.ttapp.dao.TrainDao.CarriageMeta;
import vn.ttapp.model.Route;
import vn.ttapp.model.Trip;
import vn.ttapp.model.Train;
import vn.ttapp.service.TripService;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet(name = "TripManagerServlet", urlPatterns = {"/manager/trips"})
public class TripManagerServlet extends HttpServlet {

    private final TripService service = new TripService();
    private final RouteDao routeDao = new RouteDao();
    private final TrainDao trainDao = new TrainDao();

    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<Route> routes = routeDao.findAll(); // có code, originName, destName
        List<Train> trains = trainDao.findAll(); // có trainId, code, name
        req.setAttribute("routes", routes);
        req.setAttribute("trains", trains);
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
                    req.setAttribute("t", new Trip());
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
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                }
                case "metaRoute" -> {
                    res.setCharacterEncoding("UTF-8");
                    res.setContentType("application/json; charset=UTF-8");

                    Integer rid = null;
                    try {
                        String ridRaw = req.getParameter("route_id");
                        if (ridRaw != null && !ridRaw.isBlank()) {
                            rid = Integer.parseInt(ridRaw);
                        }
                    } catch (NumberFormatException ignore) {
                        rid = null;
                    }

                    try (PrintWriter out = res.getWriter()) {
                        vn.ttapp.model.Route r = (rid != null) ? routeDao.findByIdWithStations(rid) : null;
                        if (r == null) {
                            out.print("{}");
                            return;
                        }
                        out.print("{");
                        out.printf("\"routeId\":%d,", r.getRouteId());
                        out.printf("\"code\":\"%s\",", esc(r.getCode()));
                        out.printf("\"originName\":\"%s\",", esc(r.getOriginName()));
                        out.printf("\"destName\":\"%s\"", esc(r.getDestName()));
                        out.print("}");
                    } catch (SQLException e) {
                        try (PrintWriter out = res.getWriter()) {
                            out.print("{}");
                        }
                    }
                }
                case "metaTrain" -> {
                    res.setCharacterEncoding("UTF-8");
                    res.setContentType("application/json; charset=UTF-8");
                    Integer tid = null;
                    try {
                        String tidRaw = req.getParameter("train_id");
                        if (tidRaw != null && !tidRaw.isBlank()) {
                            tid = Integer.parseInt(tidRaw);
                        }
                    } catch (NumberFormatException ignore) {
                        tid = null;
                    }

                    try (PrintWriter out = res.getWriter()) {
                        TrainDao.TrainMeta tm = (tid != null) ? trainDao.findDetail(tid) : null;
                        if (tm == null) {
                            out.print("{}");
                            return;
                        }

                        out.print("{");
                        out.printf("\"trainId\":%d,", tm.trainId);
                        out.printf("\"code\":\"%s\",", esc(tm.code));
                        out.printf("\"name\":\"%s\",", esc(tm.name));
                        out.printf("\"totalCarriages\":%d,", tm.totalCarriages);
                        out.printf("\"totalSeats\":%d,", tm.totalSeats);
                        out.print("\"carriages\":[");
                        for (int i = 0; i < tm.carriages.size(); i++) {
                            var cm = tm.carriages.get(i);
                            if (i > 0) {
                                out.print(",");
                            }
                            out.print("{");
                            out.printf("\"carriageId\":%d,", cm.carriageId);
                            out.printf("\"code\":\"%s\",", esc(cm.carriageCode));
                            out.printf("\"sortOrder\":%d,", cm.sortOrder);
                            out.printf("\"seatClassCode\":\"%s\",", esc(cm.seatClassCode));
                            out.printf("\"seatClassName\":\"%s\",", esc(cm.seatClassName));
                            out.printf("\"seatCount\":%d,", cm.seatCount);

                            out.print("\"seats\":[");
                            for (int j = 0; j < cm.seats.size(); j++) {
                                var sm = cm.seats.get(j);
                                if (j > 0) {
                                    out.print(",");
                                }
                                out.print("{");
                                out.printf("\"seatId\":%d,", sm.seatId);
                                out.printf("\"code\":\"%s\",", esc(sm.code));
                                out.printf("\"seatClassCode\":\"%s\",", esc(sm.seatClassCode));
                                out.printf("\"seatClassName\":\"%s\",", esc(sm.seatClassName));
                                out.printf("\"positionInfo\":\"%s\"", esc(sm.positionInfo));
                                out.print("}");
                            }
                            out.print("]"); // seats
                            out.print("}");
                        }
                        out.print("]}");
                    } catch (SQLException e) {
                        try (PrintWriter out = res.getWriter()) {
                            out.print("{}");
                        }
                    }
                }

                default -> {
                    req.setAttribute("list", service.findAll());
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_list.jsp").forward(req, res);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
                    String departRaw = req.getParameter("depart_at");   // yyyy-MM-ddTHH:mm
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
                        req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                        return;
                    }
                    if (!arriveAt.isAfter(departAt)) {
                        req.setAttribute("error", "Giờ đến phải sau giờ khởi hành.");
                        Trip t = new Trip(null, routeId, trainId, departAt, arriveAt, status);
                        loadRefs(req);
                        req.setAttribute("t", t);
                        req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                        return;
                    }

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer newId = service.create(routeId, trainId, departAt, arriveAt, status);
                        if (newId == null) {
                            req.setAttribute("error", "Không thể tạo chuyến.");
                            Trip t = new Trip(null, routeId, trainId, departAt, arriveAt, status);
                            loadRefs(req);
                            req.setAttribute("t", t);
                            req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                    } else {
                        Trip t = new Trip(Integer.parseInt(idRaw), routeId, trainId, departAt, arriveAt, status);
                        boolean ok = service.update(t);
                        if (!ok) {
                            req.setAttribute("error", "Không thể cập nhật chuyến.");
                            loadRefs(req);
                            req.setAttribute("t", t);
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
        }
    }
}
