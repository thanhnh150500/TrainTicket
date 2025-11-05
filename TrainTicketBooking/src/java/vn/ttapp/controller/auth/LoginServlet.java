package vn.ttapp.controller.auth;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
import java.io.IOException;
// import java.io.PrintWriter; // (Xóa)
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.ttapp.model.Role; // <-- THÊM IMPORT
import vn.ttapp.model.User;
import vn.ttapp.service.AuthService;
import java.util.List; // <-- THÊM IMPORT
import java.util.UUID; // <-- THÊM IMPORT

/**
 *
 * @author New User
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/auth/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService auth = new AuthService();

    // (Hàm processRequest không cần thiết, đã xóa)

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
         
        HttpSession session = req.getSession();
        String token = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);
        
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        HttpSession ss = req.getSession(true);

        // (SỬA) Kiểm tra CSRF token
        String sessionToken = (String) ss.getAttribute("csrfToken");
        String formToken = req.getParameter("_csrf"); 

        if (sessionToken == null || formToken == null || !sessionToken.equals(formToken)) {
            req.setAttribute("error", "Phiên đăng nhập không hợp lệ. Vui lòng thử lại.");
            doGet(req, res); // Tải lại form
            return;
        }
        ss.removeAttribute("csrfToken"); // Xóa token sau khi dùng

        String email = req.getParameter("email");
        String pass = req.getParameter("password");
        
        try {
            User u = auth.login(email, pass);
            if (u == null) {
                req.setAttribute("error", "Email hoặc mật khẩu không chính xác");
                doGet(req, res); // Gửi lỗi và tải lại form
                return;
            }
            ss.setAttribute("AUTH_USER", u);

            // Logic chuyển hướng dựa trên vai trò (ưu tiên ADMIN)
            String primaryRoleCode = "CUSTOMER"; // Mặc định
            if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                boolean isAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getCode()));
                boolean isManager = u.getRoles().stream().anyMatch(r -> "MANAGER".equals(r.getCode()));
                boolean isStaff = u.getRoles().stream().anyMatch(r -> r.getCode() != null && r.getCode().startsWith("STAFF_"));

                if (isAdmin) {
                    primaryRoleCode = "ADMIN";
                } else if (isManager) {
                    primaryRoleCode = "MANAGER";
                } else if (isStaff) {
                    primaryRoleCode = "STAFF_POS";
                }
            }

            String targetUrl = (String) ss.getAttribute("targetUrl");

            if (targetUrl != null && !targetUrl.isBlank()) {
                // Ưu tiên 1: Về URL cũ (ví dụ: trang POS)
                ss.removeAttribute("targetUrl");
                res.sendRedirect(targetUrl);
            } else if ("ADMIN".equals(primaryRoleCode)) {
                res.sendRedirect(req.getContextPath() + "/admin");
            } else if ("MANAGER".equals(primaryRoleCode)) {
                res.sendRedirect(req.getContextPath() + "/manager"); // Trang Manager
            } else if ("STAFF_POS".equals(primaryRoleCode)) {
                res.sendRedirect(req.getContextPath() + "/staff/pos"); // Trang Staff
            } else {
                res.sendRedirect(req.getContextPath() + "/home"); // Trang chủ (cho Customer)
            }
        } catch (Exception e) {
            e.printStackTrace(); // (Nên log lỗi)
            throw new ServletException(e);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
