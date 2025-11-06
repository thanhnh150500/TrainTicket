package vn.ttapp.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.service.AuthService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register", "/auth/register"})
public class RegisterServlet extends HttpServlet {

    private final AuthService auth = new AuthService();

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        // Regex gọn, chấp nhận phần lớn email hợp lệ
        Pattern p = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return p.matcher(email).matches();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());

        final String email = trim(req.getParameter("email"));
        final String password = trim(req.getParameter("password"));
        final String fullName = trim(req.getParameter("fullName"));
        final String confirm = trim(req.getParameter("confirmPassword")); // nếu form có
        final String nextQ = trim(req.getParameter("next"));            // nếu muốn chuyển hướng sau này

        // --- validate tối thiểu ---
        if (email == null || email.isEmpty() || !isValidEmail(email)) {
            req.setAttribute("error", "Email không hợp lệ.");
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            doGet(req, res);
            return;
        }
        if (password == null || password.length() < 8) {
            req.setAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            doGet(req, res);
            return;
        }
        if (fullName == null || fullName.isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập họ và tên.");
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            doGet(req, res);
            return;
        }
        if (confirm != null && !password.equals(confirm)) {
            req.setAttribute("error", "Xác nhận mật khẩu không khớp.");
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            doGet(req, res);
            return;
        }

        // --- thực hiện đăng ký ---
        try {
            String id = auth.register(email, password, fullName);
            if (id == null) {
                // email đã tồn tại
                req.setAttribute("error", "Email đã tồn tại, vui lòng dùng email khác.");
                req.setAttribute("email", email);
                req.setAttribute("fullName", fullName);
                req.setAttribute("next", nextQ);
                doGet(req, res);
                return;
            }

            // thành công -> chuyển sang trang đăng nhập
            String ctx = req.getContextPath();
            StringBuilder redirect = new StringBuilder(ctx)
                    .append("/login?registered=1&email=")
                    .append(java.net.URLEncoder.encode(email, StandardCharsets.UTF_8));

            if (nextQ != null && !nextQ.isBlank()) {
                redirect.append("&next=").append(java.net.URLEncoder.encode(nextQ, StandardCharsets.UTF_8));
            }
            res.sendRedirect(redirect.toString());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public String getServletInfo() {
        return "User registration servlet";
    }
}
