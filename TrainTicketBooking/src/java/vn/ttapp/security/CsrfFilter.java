package vn.ttapp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsrfFilter implements Filter {

    // ---- Configs / constants ----
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final String CSRF_SESSION_KEY = "csrfToken";
    private static final String CSRF_HEADER = "X-CSRF-Token";
    private static final String CSRF_PARAM = "_csrf";
    private static final String CSRF_COOKIE = "CSRF-TOKEN";

    // Excludes mặc định cho static
    private static final Set<String> DEFAULT_EXCLUDE_PREFIXES = Set.of(
            "/assets/", "/static/", "/favicon", "/robots.txt"
    );

    // Excludes nạp từ init-param
    private final Set<String> excludePrefixes = new HashSet<>();
    private final Set<String> excludeExact = new HashSet<>();

    // Tuỳ chọn: có xoay token khi gặp đường dẫn /auth/login|/auth/logout không
    private boolean rotateOnAuth = true;

    @Override
    public void init(FilterConfig cfg) {
        excludePrefixes.addAll(DEFAULT_EXCLUDE_PREFIXES);

        String extraPrefixes = cfg.getInitParameter("excludePrefixes");
        if (extraPrefixes != null && !extraPrefixes.isBlank()) {
            for (String p : extraPrefixes.split(",")) {
                p = p.trim();
                if (!p.isEmpty()) {
                    excludePrefixes.add(p.startsWith("/") ? p : ("/" + p));
                }
            }
        }
        String extraExact = cfg.getInitParameter("excludeExact");
        if (extraExact != null && !extraExact.isBlank()) {
            for (String p : extraExact.split(",")) {
                p = p.trim();
                if (!p.isEmpty()) {
                    excludeExact.add(p.startsWith("/") ? p : ("/" + p));
                }
            }
        }
        String rotate = cfg.getInitParameter("rotateOnAuth");
        if ("false".equalsIgnoreCase(rotate)) {
            rotateOnAuth = false;
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        final String ctx = r.getContextPath();                 // ví dụ: /TrainTicketBooking
        final String uri = r.getRequestURI();                  // ví dụ: /TrainTicketBooking/auth/login
        final String path = uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;

        // 1) Lấy/gán token trong session
        HttpSession ss = r.getSession(true);
        String tokenInSession = (String) ss.getAttribute(CSRF_SESSION_KEY);

        // Tuỳ chọn: xoay token khi vào login/logout (giảm rủi ro fixate)
        if (rotateOnAuth && ("/auth/login".equals(path) || "/auth/logout".equals(path))) {
            tokenInSession = null;
        }
        if (tokenInSession == null || tokenInSession.isBlank()) {
            tokenInSession = UUID.randomUUID().toString();
            ss.setAttribute(CSRF_SESSION_KEY, tokenInSession);
        }

        // 2) Đồng bộ cookie (để JS lấy header/param khi cần)
        Cookie c = new Cookie(CSRF_COOKIE, URLEncoder.encode(tokenInSession, StandardCharsets.UTF_8));
        c.setHttpOnly(false); // để SPA có thể đọc (nếu cần), nếu không cần có thể true
        c.setPath(ctx == null || ctx.isBlank() ? "/" : ctx);
        c.setSecure(r.isSecure());
        // NOTE: muốn SameSite=Strict/Lax → đặt header Set-Cookie thủ công ở đây nếu cần.
        w.addCookie(c);

        // 3) Bỏ qua safe methods
        final String method = r.getMethod();
        if (SAFE_METHODS.contains(method)) {
            chain.doFilter(req, res);
            return;
        }

        // 4) Bỏ qua các path exclude (prefix hoặc exact)
        for (String p : excludePrefixes) {
            if (path.startsWith(p)) {
                chain.doFilter(req, res);
                return;
            }
        }
        if (excludeExact.contains(path)) {
            chain.doFilter(req, res);
            return;
        }

        // 5) Lấy token từ header trước, rồi tới param
        String token = r.getHeader(CSRF_HEADER);
        if (token == null || token.isBlank()) {
            token = r.getParameter(CSRF_PARAM);
        }

        if (token == null || !token.equals(tokenInSession)) {
            w.setStatus(HttpServletResponse.SC_FORBIDDEN);
            w.setContentType("text/plain; charset=UTF-8");
            w.getWriter().write("Invalid CSRF token");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }
}
