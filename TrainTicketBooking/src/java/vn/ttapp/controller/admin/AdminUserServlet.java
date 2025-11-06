/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package vn.ttapp.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.RoleDao;
import vn.ttapp.model.Role;
import vn.ttapp.model.User;
import vn.ttapp.service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servlet này xử lý CRUD cho User (Admin)
 */
@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
    
    private final UserService userService = new UserService();
    private final RoleDao roleDao = new RoleDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        String op = req.getParameter("op");
        if (op == null) op = "list";
        
        try {
            switch (op) {
                case "new" -> showNewForm(req, res);
                case "edit" -> showEditForm(req, res);
                default -> listUsers(req, res); // "list"
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
    
    private void listUsers(HttpServletRequest req, HttpServletResponse res) 
            throws SQLException, ServletException, IOException {
        
        // (Xử lý 2 tab)
        String tab = req.getParameter("tab");
        if (tab == null || tab.isBlank()) {
            tab = "INTERNAL"; // Mặc định là tab "Nhân viên"
        }
        
        List<User> userList = userService.getAllUsers(tab);
        req.setAttribute("userList", userList);
        req.setAttribute("currentTab", tab);
        
        // (Tạo CSRF token)
        String token = UUID.randomUUID().toString();
        req.getSession().setAttribute("csrfToken", token);
        
        req.getRequestDispatcher("/WEB-INF/views/admin/admin_user_list.jsp").forward(req, res);
    }
    
    private void showNewForm(HttpServletRequest req, HttpServletResponse res) 
            throws SQLException, ServletException, IOException {
        
        List<Role> allRoles = roleDao.findInternalRoles(); // Chỉ cho phép tạo Staff/Manager
        
        req.setAttribute("user", new User()); // User rỗng
        req.setAttribute("allRoles", allRoles);
        req.setAttribute("userRoleIds", new ArrayList<Integer>()); // Danh sách rỗng
        
        String token = UUID.randomUUID().toString();
        req.getSession().setAttribute("csrfToken", token);
        
        req.getRequestDispatcher("/WEB-INF/views/admin/admin_user_form.jsp").forward(req, res);
    }
    
    private void showEditForm(HttpServletRequest req, HttpServletResponse res) 
            throws SQLException, ServletException, IOException {
        
        String userId = req.getParameter("id");
        User user = userService.getUserWithRoles(userId); // Lấy user + roles
        
        if (user == null) {
            req.getSession().setAttribute("flash_error", "Không tìm thấy người dùng.");
            res.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        
        List<Role> allRoles = roleDao.findInternalRoles(); // Chỉ cho phép sửa Staff/Manager
        
        // (Lấy danh sách ID của role)
        List<Integer> userRoleIds = new ArrayList<>();
        if (user.getRoles() != null) {
            for (Role r : user.getRoles()) {
                userRoleIds.add(r.getRoleId());
            }
        }
        
        req.setAttribute("user", user);
        req.setAttribute("allRoles", allRoles);
        req.setAttribute("userRoleIds", userRoleIds);
        
        String token = UUID.randomUUID().toString();
        req.getSession().setAttribute("csrfToken", token);
        
        req.getRequestDispatcher("/WEB-INF/views/admin/admin_user_form.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession();
        String sessionToken = (String) session.getAttribute("csrfToken");
        String formToken = req.getParameter("_csrf");
        
        if (sessionToken == null || formToken == null || !sessionToken.equals(formToken)) {
            session.setAttribute("flash_error", "Lỗi thao tác. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        
        String returnUrl = req.getContextPath() + "/admin/users";

        try {
            String op = req.getParameter("op");
            if ("save".equals(op)) {
                // (Lấy data)
                String userId = req.getParameter("userId");
                String email = req.getParameter("email");
                String fullName = req.getParameter("fullName");
                String phone = req.getParameter("phone");
                String address = req.getParameter("address");
                boolean isActive = "on".equals(req.getParameter("isActive"));
                String newPassword = req.getParameter("password");
                String[] roleIdsParam = req.getParameterValues("roleIds");
                
                List<Integer> roleIds = new ArrayList<>();
                if (roleIdsParam != null) {
                    for (String rid : roleIdsParam) {
                        roleIds.add(Integer.parseInt(rid));
                    }
                }
                
                User user = new User();
                if (userId != null && !userId.isBlank()) {
                    try {
                        user.setUserId(UUID.fromString(userId));
                    } catch (IllegalArgumentException ignore) {
                        // leave null if invalid
                    }
                }
                user.setEmail(email);
                user.setFullName(fullName);
                // set phone/address from form (User model contains these fields)
                user.setPhone(phone);
                user.setAddress(address);
                user.setActive(isActive);

                boolean success = userService.saveUser(user, roleIds, newPassword);

                if (success) {
                    session.setAttribute("flash_success", "Đã lưu người dùng thành công.");
                } else {
                    session.setAttribute("flash_error", "Lưu thất bại. Email có thể đã tồn tại.");
                }
                res.sendRedirect(returnUrl);
                
            } else {
                res.sendRedirect(returnUrl);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (Exception e) {
             session.setAttribute("flash_error", "Dữ liệu không hợp lệ.");
             res.sendRedirect(returnUrl);
        }
    }
}