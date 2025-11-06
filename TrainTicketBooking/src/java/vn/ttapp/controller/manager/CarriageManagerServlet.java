package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.SeatClassDao;
import vn.ttapp.dao.TrainDao;
import vn.ttapp.model.Carriage;
import vn.ttapp.model.SeatClass;
import vn.ttapp.model.Train;
import vn.ttapp.service.CarriageService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "CarriageManagerServlet", urlPatterns = {"/manager/carriages"})
public class CarriageManagerServlet extends HttpServlet {

    private final CarriageService service = new CarriageService();
    private final TrainDao trainDao = new TrainDao();
    private final SeatClassDao seatClassDao = new SeatClassDao();

    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<Train> trains = trainDao.findAll();
        List<SeatClass> seatClasses = seatClassDao.findAll();
        req.setAttribute("trains", trains);
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
                    req.setAttribute("c", new Carriage());
                    req.getRequestDispatcher("/WEB-INF/views/manager/carriage_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    try {
                        int id = Integer.parseInt(req.getParameter("id"));
                        Carriage c = service.findById(id);
                        if (c == null) {
                            req.getSession().setAttribute("flash_error", "Không tìm thấy toa.");
                            res.sendRedirect(req.getContextPath() + "/manager/carriages");
                            return;
                        }
                        loadRefs(req);
                        req.setAttribute("c", c);
                        req.getRequestDispatcher("/WEB-INF/views/manager/carriage_form.jsp").forward(req, res);
                    } catch (NumberFormatException nfe) {
                        req.getSession().setAttribute("flash_error", "ID không hợp lệ.");
                        res.sendRedirect(req.getContextPath() + "/manager/carriages");
                    }
                }
                default -> {
                    req.setAttribute("list", service.findAll());
                    req.getRequestDispatcher("/WEB-INF/views/manager/carriage_list.jsp").forward(req, res);
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
                    String idRaw = req.getParameter("carriage_id");
                    String trainIdRaw = req.getParameter("train_id");
                    String code = req.getParameter("code");
                    String seatClassIdRaw = req.getParameter("seat_class_id");
                    String sortOrderRaw = req.getParameter("sort_order");

                    Integer trainId = (trainIdRaw == null || trainIdRaw.isBlank()) ? null : Integer.parseInt(trainIdRaw);
                    Integer seatClassId = (seatClassIdRaw == null || seatClassIdRaw.isBlank()) ? null : Integer.parseInt(seatClassIdRaw);
                    Integer sortOrder = (sortOrderRaw == null || sortOrderRaw.isBlank()) ? 0 : Integer.parseInt(sortOrderRaw);

                    if (trainId == null || seatClassId == null || code == null || code.isBlank()) {
                        req.setAttribute("error", "Vui lòng chọn Tàu, Hạng ghế và nhập Code.");
                        Carriage c = new Carriage();
                        if (idRaw != null && !idRaw.isBlank()) {
                            c.setCarriageId(Integer.parseInt(idRaw));
                        }
                        c.setTrainId(trainId);
                        c.setSeatClassId(seatClassId);
                        c.setCode(code);
                        c.setSortOrder(sortOrder);
                        loadRefs(req);
                        req.setAttribute("c", c);
                        req.getRequestDispatcher("/WEB-INF/views/manager/carriage_form.jsp").forward(req, res);
                        return;
                    }

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer newId = service.create(trainId, code.trim(), seatClassId, sortOrder);
                        if (newId == null) {
                            req.setAttribute("error", "Code đã tồn tại trong cùng tàu hoặc dữ liệu không hợp lệ.");
                            Carriage c = new Carriage(null, trainId, code, seatClassId, sortOrder);
                            loadRefs(req);
                            req.setAttribute("c", c);
                            req.getRequestDispatcher("/WEB-INF/views/manager/carriage_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo toa.");
                        res.sendRedirect(req.getContextPath() + "/manager/carriages");
                    } else {
                        Carriage c = new Carriage(Integer.parseInt(idRaw), trainId, code.trim(), seatClassId, sortOrder);
                        boolean ok = service.update(c);
                        if (!ok) {
                            req.setAttribute("error", "Code đã tồn tại ở toa khác trong cùng tàu hoặc dữ liệu không hợp lệ.");
                            loadRefs(req);
                            req.setAttribute("c", c);
                            req.getRequestDispatcher("/WEB-INF/views/manager/carriage_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật toa.");
                        res.sendRedirect(req.getContextPath() + "/manager/carriages");
                    }
                }
                case "delete" -> {
                    try {
                        int id = Integer.parseInt(req.getParameter("id"));
                        service.delete(id);
                        req.getSession().setAttribute("flash_success", "Đã xóa toa.");
                    } catch (NumberFormatException nfe) {
                        req.getSession().setAttribute("flash_error", "ID không hợp lệ.");
                    }
                    res.sendRedirect(req.getContextPath() + "/manager/carriages");
                }
                default ->
                    res.sendRedirect(req.getContextPath() + "/manager/carriages");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/carriages");
        }
    }
}
