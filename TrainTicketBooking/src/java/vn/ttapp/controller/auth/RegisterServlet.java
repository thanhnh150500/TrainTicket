package vn.ttapp.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import vn.ttapp.service.AuthService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register", "/auth/register"})
public class RegisterServlet extends HttpServlet {

    private final AuthService auth = new AuthService();

    /* =========================
       Helpers
       ========================= */
    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        // Regex gọn, bao phủ đa số email hợp lệ
        Pattern p = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return p.matcher(email).matches();
    }

    /**
     * Chỉ cho phép next là đường dẫn nội bộ tương đối, tránh open redirect.
     */
    private static boolean isSafeInternalPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (!path.startsWith("/")) {
            return false;
        }
        if (path.startsWith("//")) {
            return false;
        }
        if (path.contains("\\")) {
            return false;
        }
        if (path.contains("://")) {
            return false;
        }
        if (path.indexOf(':') >= 0) {
            return false;
        }
        return true;
    }

    /**
     * Cấp CSRF token 1 lần cho form.
     */
    private static void issueCsrf(HttpSession ss) {
        ss.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());
    }

    /* =========================
       GET: hiển thị form
       ========================= */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // Cấp CSRF token cho form đăng ký
        issueCsrf(req.getSession(true));
        req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
    }

    /* =========================
       POST: xử lý đăng ký
       ========================= */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        HttpSession ss = req.getSession(true);

        // 1) CSRF check (giống LoginServlet)
        String sessionToken = (String) ss.getAttribute("csrfToken");
        String formToken = req.getParameter("_csrf");
        if (sessionToken == null || formToken == null || !Objects.equals(sessionToken, formToken)) {
            req.setAttribute("error", "Phiên không hợp lệ. Vui lòng thử lại.");
            // cấp lại token để người dùng submit lại form
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
            return;
        }
        // dùng xong xoá token
        ss.removeAttribute("csrfToken");

        // 2) Lấy tham số & chuẩn hoá
        String emailRaw = trim(req.getParameter("email"));
        String password = trim(req.getParameter("password"));
        String confirm = trim(req.getParameter("confirmPassword"));
        String fullName = trim(req.getParameter("fullName"));
        String nextQ = trim(req.getParameter("next"));

        // Chuẩn hoá email: trim + lowercase
        String email = (emailRaw == null) ? null : emailRaw.toLowerCase();

        // 3) Validate cơ bản
        if (!isValidEmail(email)) {
            req.setAttribute("error", "Email không hợp lệ.");
            req.setAttribute("email", emailRaw);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
            return;
        }
        if (password == null || password.length() < 8) {
            req.setAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
            req.setAttribute("email", emailRaw);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
            return;
        }
        if (fullName == null || fullName.isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập họ và tên.");
            req.setAttribute("email", emailRaw);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
            return;
        }
        if (confirm != null && !password.equals(confirm)) {
            req.setAttribute("error", "Xác nhận mật khẩu không khớp.");
            req.setAttribute("email", emailRaw);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
            return;
        }

        // 4) Thực hiện đăng ký
        try {
            String id = auth.register(email, password, fullName);
            if (id == null) {
                // email đã tồn tại
                req.setAttribute("error", "Email đã tồn tại, vui lòng dùng email khác.");
                req.setAttribute("email", emailRaw);
                req.setAttribute("fullName", fullName);
                req.setAttribute("next", nextQ);
                issueCsrf(ss);
                req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
                return;
            }

            // 5) Thành công → quay về trang đăng nhập đúng đường dẫn /auth/login
            String ctx = req.getContextPath();
            StringBuilder redirect = new StringBuilder(ctx)
                    .append("/auth/login?registered=1&email=")
                    .append(URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8));

            // Nếu có next và hợp lệ nội bộ, đính kèm để LoginServlet xử lý tiếp
            if (isSafeInternalPath(nextQ)) {
                redirect.append("&next=").append(java.net.URLEncoder.encode(nextQ, java.nio.charset.StandardCharsets.UTF_8));
            }

            res.sendRedirect(redirect.toString());
        } catch (Exception e) {
            // Log và trả về lỗi tổng quát (tránh lộ chi tiết)
            e.printStackTrace();
            req.setAttribute("error", "Có lỗi khi đăng ký tài khoản. Vui lòng thử lại sau.");
            req.setAttribute("email", emailRaw);
            req.setAttribute("fullName", fullName);
            req.setAttribute("next", nextQ);
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
        }
    }

    @Override
    public String getServletInfo() {
        return "User registration servlet with CSRF + email normalization + safe redirect.";
    }
}
