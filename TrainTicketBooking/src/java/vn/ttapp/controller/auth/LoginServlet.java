package vn.ttapp.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Locale;

import vn.ttapp.model.User;
import vn.ttapp.service.AuthService;

/**
 * Đăng nhập: - KHÔNG kiểm tra CSRF tại /auth/login (đã exclude ở CsrfFilter) -
 * Rotate session id (anti fixation) - Đặt cờ isAdmin / isManager / isStaff -
 * Điều hướng theo next (nếu an toàn) hoặc theo vai trò - Ưu tiên đặc biệt:
 * email manager@gmail.com → /manager/trips?op=new
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/auth/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService auth = new AuthService();

    /**
     * Chỉ chấp nhận path nội bộ context-relative để tránh open-redirect.
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // KHÔNG cấp CSRF token thủ công nữa (đã exclude ở filter).
        // Giữ nguyên ?next=... để login.jsp render hidden input nếu cần.
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession ss = req.getSession(true);

        // Lấy input
        String email = req.getParameter("email");
        String pass = req.getParameter("password");

        // Lấy đích điều hướng (ưu tiên session targetUrl, sau đó param next)
        String targetUrl = (String) ss.getAttribute("targetUrl");
        String nextParam = req.getParameter("next");
        if (targetUrl == null || targetUrl.isBlank()) {
            targetUrl = nextParam;
        }

        try {
            // Xác thực
            User u = auth.login(email, pass);
            if (u == null) {
                req.setAttribute("error", "Email hoặc mật khẩu không chính xác, hoặc tài khoản đã bị khóa.");
                req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, res);
                return;
            }

            // Rotate session id
            try {
                req.changeSessionId();
            } catch (UnsupportedOperationException ignore) {
                HttpSession old = ss;
                old.invalidate();
                ss = req.getSession(true);
            }

            // Lưu thông tin đăng nhập vào session
            ss.setAttribute("authUser", u);
            ss.setAttribute("roles", u.getRoles());

            boolean isAdmin = u.getRoles() != null && u.getRoles().stream()
                    .anyMatch(r -> r != null && "ADMIN".equalsIgnoreCase(r.getCode()));
            boolean isManager = u.getRoles() != null && u.getRoles().stream()
                    .anyMatch(r -> r != null && "MANAGER".equalsIgnoreCase(r.getCode()));
            boolean isStaff = u.getRoles() != null && u.getRoles().stream()
                    .anyMatch(r -> r != null && r.getCode() != null
                    && r.getCode().toUpperCase(Locale.ROOT).startsWith("STAFF_"));

            ss.setAttribute("isAdmin", isAdmin);
            ss.setAttribute("isManager", isManager);
            ss.setAttribute("isStaff", isStaff);

            // Điều hướng
            ss.removeAttribute("targetUrl"); // tránh reuse
            String ctx = req.getContextPath();

            // ---- QUY TẮC ƯU TIÊN MỚI ----
            // 1) Nếu là ADMIN/MANAGER: chỉ cho next khi trỏ vào /admin hoặc /manager
            if (isAdmin || isManager) {
                boolean nextOk = isSafeInternalPath(targetUrl)
                        && (targetUrl.startsWith("/admin") || targetUrl.startsWith("/manager"));
                if (nextOk) {
                    res.sendRedirect(ctx + targetUrl);
                    return;
                }

                // Ưu tiên đặc biệt theo email
                if (u.getEmail() != null && u.getEmail().equalsIgnoreCase("manager@gmail.com")) {
                    res.sendRedirect(ctx + "/manager/trips?op=new");
                    return;
                }

                // Mặc định theo role quản trị
                if (isAdmin) {
                    res.sendRedirect(ctx + "/admin");
                } else { // isManager
                    res.sendRedirect(ctx + "/manager/trips?op=new");
                }
                return;
            }

            // 2) Người dùng không có quyền quản trị:
            //    Nếu có next nội bộ hợp lệ → cho đi
            if (isSafeInternalPath(targetUrl)) {
                res.sendRedirect(ctx + targetUrl);
                return;
            }

            // 3) Mặc định cho staff / customer
            if (isStaff) {
                res.sendRedirect(ctx + "/staff/pos");
            } else {
                res.sendRedirect(ctx + "/home");
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Có lỗi khi đăng nhập: " + e.getClass().getSimpleName());
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, res);
        }
    }

    @Override
    public String getServletInfo() {
        return "Login without CSRF check (excluded), session rotation, role flags, and safe redirects (admin/manager only honor next under /admin|/manager).";
    }
}
