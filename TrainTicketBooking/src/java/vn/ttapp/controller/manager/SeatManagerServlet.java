package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.CarriageDao;
import vn.ttapp.dao.SeatClassDao;
import vn.ttapp.model.Seat;
import vn.ttapp.model.Carriage;
import vn.ttapp.model.SeatClass;
import vn.ttapp.service.SeatService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "SeatManagerServlet", urlPatterns = {"/manager/seats"})
public class SeatManagerServlet extends HttpServlet {

    private final SeatService service = new SeatService();
    private final CarriageDao carriageDao = new CarriageDao();
    private final SeatClassDao seatClassDao = new SeatClassDao();

    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<Carriage> carriages = carriageDao.findAll();   // đã kèm trainCode/trainName
        List<SeatClass> seatClasses = seatClassDao.findAll();
        req.setAttribute("carriages", carriages);
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
                    req.setAttribute("s", new Seat());
                    req.getRequestDispatcher("/WEB-INF/views/manager/seat_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Seat s = service.findById(id);
                    if (s == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy ghế.");
                        res.sendRedirect(req.getContextPath() + "/manager/seats");
                        return;
                    }
                    loadRefs(req);
                    req.setAttribute("s", s);
                    req.getRequestDispatcher("/WEB-INF/views/manager/seat_form.jsp").forward(req, res);
                }
                default -> {
                    req.setAttribute("list", service.findAll());
                    req.getRequestDispatcher("/WEB-INF/views/manager/seat_list.jsp").forward(req, res);
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
                    String idRaw = req.getParameter("seat_id");
                    String carriageIdRaw = req.getParameter("carriage_id");
                    String code = req.getParameter("code");
                    String seatClassIdRaw = req.getParameter("seat_class_id");
                    String positionInfo = req.getParameter("position_info");

                    Integer carriageId = (carriageIdRaw == null || carriageIdRaw.isBlank()) ? null : Integer.parseInt(carriageIdRaw);
                    Integer seatClassId = (seatClassIdRaw == null || seatClassIdRaw.isBlank()) ? null : Integer.parseInt(seatClassIdRaw);

                    if (carriageId == null || seatClassId == null || code == null || code.isBlank()) {
                        req.setAttribute("error", "Vui lòng chọn Toa, Hạng ghế và nhập Code ghế.");
                        Seat s = new Seat();
                        if (idRaw != null && !idRaw.isBlank()) {
                            s.setSeatId(Integer.parseInt(idRaw));
                        }
                        s.setCarriageId(carriageId);
                        s.setSeatClassId(seatClassId);
                        s.setCode(code);
                        s.setPositionInfo(positionInfo);
                        loadRefs(req);
                        req.setAttribute("s", s);
                        req.getRequestDispatcher("/WEB-INF/views/manager/seat_form.jsp").forward(req, res);
                        return;
                    }

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer newId = service.create(carriageId, code.trim(), seatClassId,
                                (positionInfo == null || positionInfo.isBlank()) ? null : positionInfo.trim());
                        if (newId == null) {
                            req.setAttribute("error", "Code đã tồn tại trong cùng toa hoặc dữ liệu không hợp lệ.");
                            Seat s = new Seat(null, carriageId, code, seatClassId, positionInfo);
                            loadRefs(req);
                            req.setAttribute("s", s);
                            req.getRequestDispatcher("/WEB-INF/views/manager/seat_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo ghế.");
                        res.sendRedirect(req.getContextPath() + "/manager/seats");
                    } else {
                        Seat s = new Seat(Integer.parseInt(idRaw), carriageId, code.trim(), seatClassId,
                                (positionInfo == null || positionInfo.isBlank()) ? null : positionInfo.trim());
                        boolean ok = service.update(s);
                        if (!ok) {
                            req.setAttribute("error", "Code đã tồn tại ở ghế khác trong cùng toa hoặc dữ liệu không hợp lệ.");
                            loadRefs(req);
                            req.setAttribute("s", s);
                            req.getRequestDispatcher("/WEB-INF/views/manager/seat_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật ghế.");
                        res.sendRedirect(req.getContextPath() + "/manager/seats");
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa ghế.");
                    res.sendRedirect(req.getContextPath() + "/manager/seats");
                }
                default ->
                    res.sendRedirect(req.getContextPath() + "/manager/seats");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/seats");
        }
    }
}
