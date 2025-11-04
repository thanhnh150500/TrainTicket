package vn.ttapp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

public class AuthFilter implements Filter {

    // Các URL cần đăng nhập mới được truy cập
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/seatmap",
            "/checkout",
            "/confirm-booking",
            "/profile",
            "/booking-history",
            "/payment"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest rq = (HttpServletRequest) req;
        HttpServletResponse rp = (HttpServletResponse) res;
        HttpSession ss = rq.getSession(false);

        boolean loggedIn = ss != null && ss.getAttribute("authUser") != null;
        String uri = rq.getRequestURI();
        String ctx = rq.getContextPath();

        // --- Bỏ qua file tĩnh (CSS, JS, ảnh, icon) ---
        if (uri.startsWith(ctx + "/assets/")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg")
                || uri.endsWith(".gif")
                || uri.endsWith(".ico")) {
            chain.doFilter(req, res);
            return;
        }

        // --- Bỏ qua trang đăng nhập / đăng ký ---
        if (uri.equals(ctx + "/auth/login") || uri.equals(ctx + "/auth/register")) {
            chain.doFilter(req, res);
            return;
        }

        // --- Kiểm tra các trang yêu cầu đăng nhập ---
        boolean needsAuth = PROTECTED_PATHS.stream().anyMatch(uri::contains);
        if (needsAuth && !loggedIn) {
            // Chuyển hướng đến login, kèm query next
            String next = rq.getRequestURI();
            if (rq.getQueryString() != null) {
                next += "?" + rq.getQueryString();
            }
            rp.sendRedirect(ctx + "/auth/login?next=" + java.net.URLEncoder.encode(next, "UTF-8"));
            return;
        }

        // --- Nếu đã login hoặc trang không cần login ---
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
