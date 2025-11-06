package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.model.SeatClass;
import vn.ttapp.service.SeatClassService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="SeatClassManagerServlet", urlPatterns={"/manager/seat-classes"})
public class SeatClassManagerServlet extends HttpServlet {
    private final SeatClassService service = new SeatClassService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String op = req.getParameter("op");
        if (op == null) op = "list";

        try {
            switch (op) {
                case "new" -> {
                    req.setAttribute("sc", new SeatClass());
                    req.getRequestDispatcher("/WEB-INF/views/manager/seatclass_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    SeatClass sc = service.findById(id);
                    if (sc == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy hạng ghế.");
                        res.sendRedirect(req.getContextPath() + "/manager/seat-classes");
                        return;
                    }
                    req.setAttribute("sc", sc);
                    req.getRequestDispatcher("/WEB-INF/views/manager/seatclass_form.jsp").forward(req, res);
                }
                default -> {
                    List<SeatClass> list = service.findAll();
                    req.setAttribute("list", list);
                    req.getRequestDispatcher("/WEB-INF/views/manager/seatclass_list.jsp").forward(req, res);
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
        if (op == null) op = "save";

        try {
            switch (op) {
                case "save" -> {
                    String idRaw = req.getParameter("seat_class_id");
                    String code = req.getParameter("code");
                    String name = req.getParameter("name");

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer id = service.create(code, name);
                        if (id == null) {
                            req.setAttribute("error", "Code đã tồn tại hoặc dữ liệu không hợp lệ.");
                            SeatClass sc = new SeatClass(null, code, name);
                            req.setAttribute("sc", sc);
                            req.getRequestDispatcher("/WEB-INF/views/manager/seatclass_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo hạng ghế.");
                    } else {
                        SeatClass sc = new SeatClass(Integer.parseInt(idRaw), code, name);
                        boolean ok = service.update(sc);
                        if (!ok) {
                            req.setAttribute("error", "Code đã tồn tại ở bản ghi khác hoặc dữ liệu không hợp lệ.");
                            req.setAttribute("sc", sc);
                            req.getRequestDispatcher("/WEB-INF/views/manager/seatclass_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật hạng ghế.");
                    }
                    res.sendRedirect(req.getContextPath() + "/manager/seat-classes");
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa hạng ghế.");
                    res.sendRedirect(req.getContextPath() + "/manager/seat-classes");
                }
                default -> res.sendRedirect(req.getContextPath() + "/manager/seat-classes");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/seat-classes");
        }
    }
}
