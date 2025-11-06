package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.SeatClassDao;
import vn.ttapp.dao.RouteDao;
import vn.ttapp.model.FareRule;
import vn.ttapp.model.Route;
import vn.ttapp.model.SeatClass;
import vn.ttapp.service.FareRuleService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "FareRuleManagerServlet", urlPatterns = {"/manager/fare-rules"})
public class FareRuleManagerServlet extends HttpServlet {

    private final FareRuleService service = new FareRuleService();
    private final RouteDao routeDao = new RouteDao();
    private final SeatClassDao seatClassDao = new SeatClassDao();

    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<Route> routes = routeDao.findAll();
        List<SeatClass> seatClasses = seatClassDao.findAll();
        req.setAttribute("routes", routes);
        req.setAttribute("seatClasses", seatClasses);
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
                    req.setAttribute("f", new FareRule());
                    req.getRequestDispatcher("/WEB-INF/views/manager/fare_rule_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    FareRule f = service.findById(id);
                    if (f == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy giá tuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/fare-rules");
                        return;
                    }
                    loadRefs(req);
                    req.setAttribute("f", f);
                    req.getRequestDispatcher("/WEB-INF/views/manager/fare_rule_form.jsp").forward(req, res);
                }
                default -> {
                    req.setAttribute("list", service.findAll());
                    req.getRequestDispatcher("/WEB-INF/views/manager/fare_rule_list.jsp").forward(req, res);
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
                    String idRaw = req.getParameter("fare_rule_id");
                    String routeIdRaw = req.getParameter("route_id");
                    String seatClassIdRaw = req.getParameter("seat_class_id");
                    String priceRaw = req.getParameter("base_price");
                    String fromRaw = req.getParameter("effective_from");
                    String toRaw = req.getParameter("effective_to");

                    Integer routeId = (routeIdRaw == null || routeIdRaw.isBlank()) ? null : Integer.parseInt(routeIdRaw);
                    Integer seatClassId = (seatClassIdRaw == null || seatClassIdRaw.isBlank()) ? null : Integer.parseInt(seatClassIdRaw);
                    BigDecimal price = null;
                    if (priceRaw != null && !priceRaw.isBlank()) {
                        try {
                            price = new BigDecimal(priceRaw.trim());
                        } catch (Exception ignore) {
                        }
                    }
                    LocalDate from = null, to = null;
                    try {
                        if (fromRaw != null && !fromRaw.isBlank()) {
                            from = LocalDate.parse(fromRaw);
                        }
                    } catch (Exception ignore) {
                    }
                    try {
                        if (toRaw != null && !toRaw.isBlank()) {
                            to = LocalDate.parse(toRaw);
                        }
                    } catch (Exception ignore) {
                    }

                    if (routeId == null || seatClassId == null || price == null || from == null) {
                        req.setAttribute("error", "Vui lòng chọn Tuyến, Hạng ghế, nhập Giá và Ngày hiệu lực.");
                        FareRule f = new FareRule();
                        if (idRaw != null && !idRaw.isBlank()) {
                            f.setFareRuleId(Integer.parseInt(idRaw));
                        }
                        f.setRouteId(routeId);
                        f.setSeatClassId(seatClassId);
                        f.setBasePrice(price);
                        f.setEffectiveFrom(from);
                        f.setEffectiveTo(to);
                        loadRefs(req);
                        req.setAttribute("f", f);
                        req.getRequestDispatcher("/WEB-INF/views/manager/fare_rule_form.jsp").forward(req, res);
                        return;
                    }
                    if (to != null && to.isBefore(from)) {
                        req.setAttribute("error", "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
                        FareRule f = new FareRule(null, routeId, seatClassId, price, from, to);
                        loadRefs(req);
                        req.setAttribute("f", f);
                        req.getRequestDispatcher("/WEB-INF/views/manager/fare_rule_form.jsp").forward(req, res);
                        return;
                    }

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer newId = service.create(routeId, seatClassId, price, from, to);
                        if (newId == null) {
                            req.setAttribute("error", "Dữ liệu không hợp lệ.");
                            FareRule f = new FareRule(null, routeId, seatClassId, price, from, to);
                            loadRefs(req);
                            req.setAttribute("f", f);
                            req.getRequestDispatcher("/WEB-INF/views/manager/fare_rule_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo giá tuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/fare-rules");
                    } else {
                        FareRule f = new FareRule(Integer.parseInt(idRaw), routeId, seatClassId, price, from, to);
                        boolean ok = service.update(f);
                        if (!ok) {
                            req.setAttribute("error", "Dữ liệu không hợp lệ (giá > 0, ngày hợp lệ).");
                            loadRefs(req);
                            req.setAttribute("f", f);
                            req.getRequestDispatcher("/WEB-INF/views/manager/fare_rule_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật giá tuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/fare-rules");
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa giá tuyến.");
                    res.sendRedirect(req.getContextPath() + "/manager/fare-rules");
                }
                default ->
                    res.sendRedirect(req.getContextPath() + "/manager/fare-rules");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/fare-rules");
        }
    }
}
