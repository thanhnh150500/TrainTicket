package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.StationDao;
import vn.ttapp.model.Route;
import vn.ttapp.model.Station;
import vn.ttapp.service.RouteService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "RouteManagerServlet", urlPatterns = {"/manager/routes"})
public class RouteManagerServlet extends HttpServlet {

    private final RouteService service = new RouteService();
    private final StationDao stationDao = new StationDao();

    private void loadStations(HttpServletRequest req) throws SQLException {
        List<Station> stations = stationDao.findAll(); // đã có code/name/cityName
        req.setAttribute("stations", stations);
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
                    loadStations(req);
                    req.setAttribute("r", new Route());
                    req.getRequestDispatcher("/WEB-INF/views/manager/route_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Route r = service.findById(id);
                    if (r == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy tuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/routes");
                        return;
                    }
                    loadStations(req);
                    req.setAttribute("r", r);
                    req.getRequestDispatcher("/WEB-INF/views/manager/route_form.jsp").forward(req, res);
                }
                default -> {
                    List<Route> list = service.findAll();
                    req.setAttribute("list", list);
                    req.getRequestDispatcher("/WEB-INF/views/manager/route_list.jsp").forward(req, res);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
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
                    String idRaw = req.getParameter("route_id");
                    String orgRaw = req.getParameter("origin_station_id");
                    String dstRaw = req.getParameter("dest_station_id");
                    String code = req.getParameter("code");

                    Integer originId = (orgRaw == null || orgRaw.isBlank()) ? null : Integer.parseInt(orgRaw);
                    Integer destId = (dstRaw == null || dstRaw.isBlank()) ? null : Integer.parseInt(dstRaw);

                    // validate
                    if (originId == null || destId == null || originId.equals(destId)
                            || code == null || code.isBlank()) {
                        req.setAttribute("error", "Vui lòng chọn ga đi/đến (khác nhau) và nhập Code.");
                        Route r = new Route();
                        if (idRaw != null && !idRaw.isBlank()) {
                            r.setRouteId(Integer.parseInt(idRaw));
                        }
                        r.setOriginStationId(originId);
                        r.setDestStationId(destId);
                        r.setCode(code);
                        loadStations(req);
                        req.setAttribute("r", r);
                        req.getRequestDispatcher("/WEB-INF/views/manager/route_form.jsp").forward(req, res);
                        return;
                    }

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer newId = service.create(originId, destId, code.trim());
                        if (newId == null) {
                            req.setAttribute("error", "Code đã tồn tại hoặc dữ liệu không hợp lệ.");
                            Route r = new Route();
                            r.setOriginStationId(originId);
                            r.setDestStationId(destId);
                            r.setCode(code);
                            loadStations(req);
                            req.setAttribute("r", r);
                            req.getRequestDispatcher("/WEB-INF/views/manager/route_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo tuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/routes");
                    } else {
                        Route r = new Route();
                        r.setRouteId(Integer.parseInt(idRaw));
                        r.setOriginStationId(originId);
                        r.setDestStationId(destId);
                        r.setCode(code.trim());
                        boolean ok = service.update(r);
                        if (!ok) {
                            req.setAttribute("error", "Code đã tồn tại ở bản ghi khác hoặc ga đi/đến không hợp lệ.");
                            loadStations(req);
                            req.setAttribute("r", r);
                            req.getRequestDispatcher("/WEB-INF/views/manager/route_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật tuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/routes");
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa tuyến.");
                    res.sendRedirect(req.getContextPath() + "/manager/routes");
                }
                default ->
                    res.sendRedirect(req.getContextPath() + "/manager/routes");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/routes");
        }
    }
}
