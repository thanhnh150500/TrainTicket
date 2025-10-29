package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.model.Train;
import vn.ttapp.service.TrainService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "TrainManagerServlet", urlPatterns = {"/manager/trains"})
public class TrainManagerServlet extends HttpServlet {

    private final TrainService service = new TrainService();

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
                    req.setAttribute("t", new Train());
                    req.getRequestDispatcher("/WEB-INF/views/manager/train_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Train t = service.findById(id);
                    if (t == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy tàu.");
                        res.sendRedirect(req.getContextPath() + "/manager/trains");
                        return;
                    }
                    req.setAttribute("t", t);
                    req.getRequestDispatcher("/WEB-INF/views/manager/train_form.jsp").forward(req, res);
                }
                default -> {
                    List<Train> list = service.findAll();
                    req.setAttribute("list", list);
                    req.getRequestDispatcher("/WEB-INF/views/manager/train_list.jsp").forward(req, res);
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
                    String idRaw = req.getParameter("train_id");
                    String code = req.getParameter("code");
                    String name = req.getParameter("name");

                    if (code == null || code.isBlank() || name == null || name.isBlank()) {
                        req.setAttribute("error", "Code và Tên tàu không được để trống.");
                        Train t = new Train();
                        if (idRaw != null && !idRaw.isBlank()) {
                            t.setTrainId(Integer.parseInt(idRaw));
                        }
                        t.setCode(code);
                        t.setName(name);
                        req.setAttribute("t", t);
                        req.getRequestDispatcher("/WEB-INF/views/manager/train_form.jsp").forward(req, res);
                        return;
                    }

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer newId = service.create(code, name);
                        if (newId == null) {
                            req.setAttribute("error", "Mã tàu (code) không hợp lệ hoặc đã tồn tại.");
                            Train t = new Train(null, code, name);
                            req.setAttribute("t", t);
                            req.getRequestDispatcher("/WEB-INF/views/manager/train_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo tàu mới.");
                        res.sendRedirect(req.getContextPath() + "/manager/trains");
                    } else {
                        Train t = new Train();
                        t.setTrainId(Integer.parseInt(idRaw));
                        t.setCode(code);
                        t.setName(name);
                        boolean ok = service.update(t);
                        if (!ok) {
                            req.setAttribute("error", "Mã tàu (code) không hợp lệ hoặc đã tồn tại ở bản ghi khác.");
                            req.setAttribute("t", t);
                            req.getRequestDispatcher("/WEB-INF/views/manager/train_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật tàu.");
                        res.sendRedirect(req.getContextPath() + "/manager/trains");
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa tàu.");
                    res.sendRedirect(req.getContextPath() + "/manager/trains");
                }
                default ->
                    res.sendRedirect(req.getContextPath() + "/manager/trains");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/trains");
        }
    }
}
