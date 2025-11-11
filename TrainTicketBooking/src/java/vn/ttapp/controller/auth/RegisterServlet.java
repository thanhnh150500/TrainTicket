package vn.ttapp.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import vn.ttapp.service.AuthService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register", "/auth/register"})
public class RegisterServlet extends HttpServlet {

    private final AuthService auth = new AuthService();

    /*Helpers*/
    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    // Họ & Tên: chỉ chữ Unicode + khoảng trắng, dài 2–60, không cho số/ký tự đặc biệt
    // Yêu cầu đã trim trước khi kiểm tra
    private static final Pattern NAME_RE = Pattern.compile("^[\\p{L}][\\p{L}\\s]{0,58}[\\p{L}]$",
            Pattern.UNICODE_CHARACTER_CLASS);

    private static boolean isValidFullName(String fullName) {
        if (fullName == null) {
            return false;
        }
        String v = fullName.trim();
        if (v.length() < 2 || v.length() > 60) {
            return false;
        }
        return NAME_RE.matcher(v).matches();
    }

    private static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Pattern p = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return p.matcher(email).matches();
    }

    /**Chỉ cho phép 'next' là đường dẫn nội bộ tương đối, tránh open redirect.*/
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

    /**
     * Gán lại các giá trị người dùng đã nhập để hiển thị lại trên form khi có
     * lỗi.
     */
    private static void keepUserInput(HttpServletRequest req, String emailRaw, String fullName, String nextQ) {
        req.setAttribute("email", emailRaw);
        req.setAttribute("fullName", fullName);
        req.setAttribute("next", nextQ);
    }

    /* =========================
       GET: hiển thị form
       ========================= */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
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

        // 1) CSRF
        String sessionToken = (String) ss.getAttribute("csrfToken");
        String formToken = req.getParameter("_csrf");
        if (sessionToken == null || formToken == null || !Objects.equals(sessionToken, formToken)) {
            Map<String, String> errors = new HashMap<>();
            errors.put("global", "Phiên không hợp lệ. Vui lòng thử lại.");
            req.setAttribute("errors", errors);
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
            return;
        }
        ss.removeAttribute("csrfToken");

        // 2) Lấy & chuẩn hoá
        String emailRaw = trim(req.getParameter("email"));
        String password = trim(req.getParameter("password"));
        String confirm = trim(req.getParameter("confirmPassword"));
        String fullName = trim(req.getParameter("fullName"));
        String nextQ = trim(req.getParameter("next"));
        String email = (emailRaw == null) ? null : emailRaw.toLowerCase();

        // 3) Validate theo từng trường
        Map<String, String> errors = new HashMap<>();

        if (!isValidEmail(email)) {
            errors.put("email", "Email không hợp lệ.");
        }
        if (password == null || password.length() < 8) {
            errors.put("password", "Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (confirm == null || !Objects.equals(password, confirm)) {
            errors.put("confirmPassword", "Xác nhận mật khẩu không khớp.");
        }
        if (!isValidFullName(fullName)) {
            errors.put("fullName", "Họ và tên chỉ gồm chữ và khoảng trắng (2–60 ký tự).");
        }

        if (!errors.isEmpty()) {
            keepUserInput(req, emailRaw, fullName, nextQ);
            req.setAttribute("errors", errors);
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
            return;
        }

        // 4) Thực hiện đăng ký
        try {
            String id = auth.register(email, password, fullName);
            if (id == null) {
                // Email tồn tại
                errors.put("email", "Email đã tồn tại, vui lòng dùng email khác.");
                keepUserInput(req, emailRaw, fullName, nextQ);
                req.setAttribute("errors", errors);
                issueCsrf(ss);
                req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
                return;
            }

            // 5) Thành công → về /auth/login
            String ctx = req.getContextPath();
            StringBuilder redirect = new StringBuilder(ctx)
                    .append("/auth/login?registered=1&email=")
                    .append(URLEncoder.encode(email, StandardCharsets.UTF_8));

            if (isSafeInternalPath(nextQ)) {
                redirect.append("&next=")
                        .append(URLEncoder.encode(nextQ, StandardCharsets.UTF_8));
            }
            res.sendRedirect(redirect.toString());

        } catch (Exception e) {
            e.printStackTrace();
            errors.put("global", "Có lỗi khi đăng ký tài khoản. Vui lòng thử lại sau.");
            keepUserInput(req, emailRaw, fullName, nextQ);
            req.setAttribute("errors", errors);
            issueCsrf(ss);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, res);
        }
    }

    @Override
    public String getServletInfo() {
        return "User registration servlet with per-field validation, CSRF, and safe redirect.";
    }
}
