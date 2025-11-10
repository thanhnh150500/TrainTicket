package vn.ttapp.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * LogoutServlet: - Xoá sạch session/cookies đăng nhập. - Chặn open redirect
 * (chỉ cho phép đường dẫn nội bộ). - Đặt header no-cache để tránh back hiển thị
 * nội dung private.
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/auth/logout"})
public class LogoutServlet extends HttpServlet {

    /**
     * Các key trong session cần xoá khi đăng xuất.
     */
    private static final String[] CLEAR_ATTRS = {
        // Auth / RBAC
        "authUser", "userId", "roles",
        "isAdmin", "isManager", "isStaff",
        // CSRF & chuyển hướng
        "csrfToken", "targetUrl",
        // App states (tuỳ dự án)
        "cart", "seatSelection", "searchCtx",
        "chosenOutboundTripId", "chosenInboundTripId",
        "lastOrigin", "lastDest", "lastTripType", "lastDepart", "lastReturn"
    };

    /**
     * Tên các cookie thường gặp cần xoá khi đăng xuất.
     */
    private static final String[] CLEAR_COOKIES = {
        "JSESSIONID", // cookie phiên
        "CSRF-TOKEN", "XSRF-TOKEN"
    };

    /* ---------------- Utilities ---------------- */
    /**
     * Chỉ chấp nhận đường dẫn nội bộ dạng tương đối để tránh open redirect.
     */
    private static boolean isSafeInternalPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (!path.startsWith("/")) {
            return false;   // phải là path nội bộ
        }
        if (path.startsWith("//")) {
            return false;   // //evil
        }
        if (path.contains("\\")) {
            return false;   // backslash
        }
        if (path.contains("://")) {
            return false;   // http(s)://
        }
        if (path.indexOf(':') >= 0) {
            return false;   // scheme:...
        }
        return true;
    }

    /**
     * Đặt header no-cache để tránh hồi quy nội dung private qua Back/Cache.
     */
    private static void setNoCache(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP/1.1
        res.setHeader("Pragma", "no-cache");                                    // HTTP/1.0
        res.setDateHeader("Expires", 0);                                        // Proxies
    }

    /**
     * Xoá cookie theo tên (nếu có). Thử xoá với cả path=ctx và path="/" để bao
     * phủ trường hợp JSESSIONID được set ở "/" (Tomcat thường vậy).
     */
    private static void clearCookies(HttpServletRequest req, HttpServletResponse res) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null || cookies.length == 0) {
            return;
        }

        String ctx = req.getContextPath();
        if (ctx == null || ctx.isBlank()) {
            ctx = "/";
        }

        for (Cookie c : cookies) {
            String name = c.getName();
            if (name == null) {
                continue;
            }

            for (String target : CLEAR_COOKIES) {
                if (!target.equalsIgnoreCase(name)) {
                    continue;
                }

                // 1) Xoá theo path hiện tại (ctx)
                Cookie delCtx = new Cookie(c.getName(), "");
                delCtx.setMaxAge(0);
                delCtx.setPath(ctx);
                delCtx.setHttpOnly("JSESSIONID".equalsIgnoreCase(c.getName()));
                delCtx.setSecure(req.isSecure());
                res.addCookie(delCtx);

                // 2) Thử xoá thêm theo path "/" (để chắc chắn)
                if (!"/".equals(ctx)) {
                    Cookie delRoot = new Cookie(c.getName(), "");
                    delRoot.setMaxAge(0);
                    delRoot.setPath("/");
                    delRoot.setHttpOnly("JSESSIONID".equalsIgnoreCase(c.getName()));
                    delRoot.setSecure(req.isSecure());
                    res.addCookie(delRoot);
                }
                break;
            }
        }
    }

    /**
     * Thực thi logout: xoá session, cookie, rồi redirect.
     */
    private void performLogout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        setNoCache(res);

        // 1) Xoá attribute + invalidate session
        HttpSession ss = req.getSession(false);
        if (ss != null) {
            for (String k : CLEAR_ATTRS) {
                try {
                    ss.removeAttribute(k);
                } catch (IllegalStateException ignore) {
                }
            }
            try {
                ss.invalidate();
            } catch (IllegalStateException ignore) {
            }
        }

        // 2) Xoá cookie phiên/CSRF (nếu có)
        clearCookies(req, res);

        // 3) Điều hướng về 'next' hợp lệ nếu có, ngược lại về trang login
        String next = req.getParameter("next");
        String ctx = req.getContextPath();
        if (isSafeInternalPath(next)) {
            res.sendRedirect(ctx + next);
        } else {
            res.sendRedirect(ctx + "/auth/login");
        }
    }

    /* ---------------- GET / POST ---------------- */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        performLogout(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        // Nếu có CSRF filter riêng, form logout có thể cần _csrf; ở đây cho phép GET/POST idempotent.
        performLogout(req, res);
    }

    @Override
    public String getServletInfo() {
        return "Đăng xuất an toàn: xoá session & cookies, chặn open-redirect, đặt no-cache.";
    }
}
