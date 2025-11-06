/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.model.FnbCategory;
import vn.ttapp.service.FnbService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/manager/fnb-categories")
public class FnbManagerServlet extends HttpServlet {

    private final FnbService service = new FnbService();

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
                    req.setAttribute("c", new FnbCategory());
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_category_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    FnbCategory c = service.getCategoryById(id);
                    if (c == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy danh mục.");
                        res.sendRedirect(req.getContextPath() + "/manager/fnb-categories");
                        return;
                    }
                    req.setAttribute("c", c);
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_category_form.jsp").forward(req, res);
                }
                default -> {
                    req.setAttribute("list", service.getAllCategories());
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_category_list.jsp").forward(req, res);
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
                    String idRaw = req.getParameter("category_id");
                    String name = req.getParameter("name");
                    boolean active = "on".equals(req.getParameter("is_active"));

                    FnbCategory c = new FnbCategory(
                            (idRaw == null || idRaw.isBlank()) ? null : Integer.parseInt(idRaw),
                            name, active
                    );

                    if (service.saveCategory(c)) {
                        req.getSession().setAttribute("flash_success", "Đã lưu danh mục.");
                        res.sendRedirect(req.getContextPath() + "/manager/fnb-categories");
                    } else {
                        req.setAttribute("error", "Tên danh mục không hợp lệ.");
                        req.setAttribute("c", c);
                        req.getRequestDispatcher("/WEB-INF/views/manager/fnb_category_form.jsp").forward(req, res);
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.deleteCategory(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa danh mục.");
                    res.sendRedirect(req.getContextPath() + "/manager/fnb-categories");
                }
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Lỗi hệ thống.");
            res.sendRedirect(req.getContextPath() + "/manager/fnb-categories");
        }
    }
}
