/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.model.FnbItem;
import vn.ttapp.model.FnbCategory;
import vn.ttapp.service.FnbItemService;
import vn.ttapp.dao.FnbCategoryDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/manager/fnb-items")
public class FnbItemManagerServlet extends HttpServlet {

    private final FnbItemService service = new FnbItemService();
    private final FnbCategoryDao categoryDao = new FnbCategoryDao();

    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<FnbCategory> categories = categoryDao.findAll();
        req.setAttribute("categories", categories);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String op = req.getParameter("op");
        if (op == null) {
            op = "list";
        }
        try {
            switch (op) {
                case "new" -> {
                    loadRefs(req);
                    req.setAttribute("x", new FnbItem());
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_item_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    FnbItem item = service.getById(id);
                    if (item == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy món.");
                        res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
                        return;
                    }
                    loadRefs(req);
                    req.setAttribute("x", item);
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_item_form.jsp").forward(req, res);
                }
                default -> {
                    req.setAttribute("list", service.getAll());
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_item_list.jsp").forward(req, res);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String op = req.getParameter("op");
        if (op == null) {
            op = "save";
        }
        try {
            switch (op) {
                case "save" -> {
                    String idRaw = req.getParameter("item_id");
                    String categoryRaw = req.getParameter("category_id");
                    String code = req.getParameter("code");
                    String name = req.getParameter("name");
                    String priceRaw = req.getParameter("price");
                    boolean active = "on".equals(req.getParameter("is_active"));

                    FnbItem x = new FnbItem();
                    x.setItemId((idRaw == null || idRaw.isBlank()) ? null : Integer.parseInt(idRaw));
                    x.setCategoryId((categoryRaw == null || categoryRaw.isBlank()) ? null : Integer.parseInt(categoryRaw));
                    x.setCode(code);
                    x.setName(name);
                    x.setPrice(Double.parseDouble(priceRaw));
                    x.setActive(active);

                    if (service.save(x)) {
                        req.getSession().setAttribute("flash_success", "Đã lưu món.");
                        res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
                    } else {
                        req.setAttribute("error", "Dữ liệu không hợp lệ hoặc mã đã tồn tại.");
                        loadRefs(req);
                        req.setAttribute("x", x);
                        req.getRequestDispatcher("/WEB-INF/views/manager/fnb_item_form.jsp").forward(req, res);
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa món.");
                    res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
                }
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Lỗi hệ thống.");
            res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
        }
    }
}
