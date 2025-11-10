package vn.ttapp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class AuthFilter implements Filter {

    private static final String LOGIN_URL = "/auth/login";

    /**
     * Các đường dẫn PUBLIC (khớp tuyệt đối, context-relative).
     */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            LOGIN_URL,
            "/auth/register",
            "/auth/logout",
            "/api/seatmap", // nếu muốn public API
            "/",
            "/home"
    );

    /**
     * Các prefix yêu cầu đăng nhập (khu vực khách).
     */
    private static final Set<String> CUSTOMER_PREFIXES = Set.of(
            "/seatmap",
            "/checkout",
            "/confirm-booking",
            "/profile",
            "/booking-history",
            "/payment"
    );

    /**
     * Các khu vực yêu cầu role cụ thể.
     */
    private static final Set<String> ADMIN_PREFIXES = Set.of("/admin");
    private static final Set<String> MANAGER_PREFIXES = Set.of("/manager");
    private static final Set<String> STAFF_PREFIXES = Set.of("/staff");

    /**
     * Static suffixes bỏ qua.
     */
    private static final String[] STATIC_SUFFIXES = {
        ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".svg", ".webp", ".woff", ".woff2", ".ttf", ".map"
    };

    private static boolean isStatic(String uri, String ctx) {
        if (uri.startsWith(ctx + "/assets/")) {
            return true;
        }
        for (String suf : STATIC_SUFFIXES) {
            if (uri.endsWith(suf)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPublicExact(String uri, String ctx) {
        for (String p : PUBLIC_PATHS) {
            if (uri.equals(ctx + p)) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWithAny(String uri, String ctx, Set<String> prefixes) {
        for (String p : prefixes) {
            if (uri.startsWith(ctx + p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ajax/XMLHttpRequest? → ưu tiên 401/403 thay vì redirect vòng lặp.
     */
    private static boolean isAjax(HttpServletRequest rq) {
        String v = rq.getHeader("X-Requested-With");
        return v != null && v.equalsIgnoreCase("XMLHttpRequest");
    }

    /**
     * Lấy path tương đối (bỏ context) để đưa vào next=… an toàn.
     */
    private static String contextRelativePath(HttpServletRequest rq) {
        String ctx = rq.getContextPath();
        String full = rq.getRequestURI();
        String rel = full.startsWith(ctx) ? full.substring(ctx.length()) : full; // “/manager/trips”
        String qs = rq.getQueryString();
        if (qs != null && !qs.isBlank()) {
            rel += "?" + qs;
        }
        return rel;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest rq = (HttpServletRequest) req;
        HttpServletResponse rp = (HttpServletResponse) res;
        HttpSession ss = rq.getSession(false);

        final String ctx = rq.getContextPath();   // vd: /TrainTicketBooking
        final String uri = rq.getRequestURI();    // vd: /TrainTicketBooking/manager/trips

        // 1) Bỏ qua file tĩnh
        if (isStatic(uri, ctx)) {
            chain.doFilter(req, res);
            return;
        }

        // 2) Bỏ qua public (khớp tuyệt đối)
        if (isPublicExact(uri, ctx)) {
            chain.doFilter(req, res);
            return;
        }

        // 3) Trạng thái đăng nhập
        boolean loggedIn = (ss != null && ss.getAttribute("authUser") != null);

        // 4) Xác định có cần đăng nhập không
        boolean needsAuth
                = startsWithAny(uri, ctx, CUSTOMER_PREFIXES)
                || startsWithAny(uri, ctx, ADMIN_PREFIXES)
                || startsWithAny(uri, ctx, MANAGER_PREFIXES)
                || startsWithAny(uri, ctx, STAFF_PREFIXES);

        // 4.1) Chưa login → chuyển /auth/login?next=... (với Ajax → 401)
        if (needsAuth && !loggedIn) {
            if (isAjax(rq)) {
                rp.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401
            } else {
                String relPath = contextRelativePath(rq); // /manager/trips?op=...
                String target = ctx + LOGIN_URL + "?next=" + URLEncoder.encode(relPath, StandardCharsets.UTF_8);
                rq.getServletContext().log("[AuthFilter] Redirect -> " + target);
                rp.sendRedirect(target);
            }
            return;
        }

        // 5) Đã đăng nhập → kiểm tra quyền theo prefix
        if (loggedIn) {
            boolean isAdmin = Boolean.TRUE.equals(ss.getAttribute("isAdmin"));
            boolean isManager = Boolean.TRUE.equals(ss.getAttribute("isManager"));
            boolean isStaff = Boolean.TRUE.equals(ss.getAttribute("isStaff"));

            if (startsWithAny(uri, ctx, ADMIN_PREFIXES) && !isAdmin) {
                rp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang quản trị.");
                return;
            }
            if (startsWithAny(uri, ctx, MANAGER_PREFIXES) && !(isManager || isAdmin)) {
                rp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ quản lý hoặc admin mới được phép truy cập.");
                return;
            }
            if (startsWithAny(uri, ctx, STAFF_PREFIXES) && !(isStaff || isManager || isAdmin)) {
                rp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ nhân viên hoặc cấp cao hơn mới được phép truy cập.");
                return;
            }
        }

        // 6) Cho qua
        chain.doFilter(req, res);
    }
}
