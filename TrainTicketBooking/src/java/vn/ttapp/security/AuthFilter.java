package vn.ttapp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class AuthFilter implements Filter {

    // Đổi ở đây nếu sau này bạn đổi mapping
    private static final String LOGIN_URL = "/auth/login";
    private static final Set<String> PUBLIC_PATHS = Set.of(
            LOGIN_URL,
            "/auth/register",
            "/auth/logout",
            "/api/seatmap" // cho phép xem sơ đồ ghế
    );
    private static final Set<String> PROTECTED_PREFIXES = Set.of(
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

        final String ctx = rq.getContextPath();     // vd: /TrainTicketBooking
        final String uri = rq.getRequestURI();      // vd: /TrainTicketBooking/checkout

        // Bỏ qua file tĩnh
        if (uri.startsWith(ctx + "/assets/")
                || uri.endsWith(".css") || uri.endsWith(".js")
                || uri.endsWith(".png") || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg") || uri.endsWith(".gif") || uri.endsWith(".ico")) {
            chain.doFilter(req, res);
            return;
        }

        // Bỏ qua public paths (khớp tuyệt đối)
        for (String p : PUBLIC_PATHS) {
            if (uri.equals(ctx + p)) {
                chain.doFilter(req, res);
                return;
            }
        }

        boolean needsAuth = PROTECTED_PREFIXES.stream().anyMatch(prefix -> uri.startsWith(ctx + prefix));
        boolean loggedIn = ss != null && ss.getAttribute("authUser") != null;

        if (needsAuth && !loggedIn) {
            String next = rq.getRequestURI();
            if (rq.getQueryString() != null) {
                next += "?" + rq.getQueryString();
            }
            String target = ctx + LOGIN_URL + "?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8);
            rq.getServletContext().log("[AuthFilter] Redirect -> " + target); // log để kiểm tra có /login nào xuất hiện không
            rp.sendRedirect(target);
            return;
        }

        chain.doFilter(req, res);
    }
}
