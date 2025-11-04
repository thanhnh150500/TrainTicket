package vn.ttapp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsrfFilter implements Filter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final Set<String> defaultExcludePrefixes = Set.of(
            "/assets/", "/static/", "/favicon", "/robots.txt"
    );

    private final Set<String> excludePrefixes = new HashSet<>();
    private final Set<String> excludeExact = new HashSet<>();

    private static final String CSRF_SESSION_KEY = "csrfToken";
    private static final String CSRF_HEADER = "X-CSRF-Token";
    private static final String CSRF_PARAM = "_csrf";
    private static final String CSRF_COOKIE = "CSRF-TOKEN";

    @Override
    public void init(FilterConfig cfg) {
        // nạp mặc định
        excludePrefixes.addAll(defaultExcludePrefixes);

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
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        HttpSession ss = r.getSession(true);
        String tokenInSession = (String) ss.getAttribute(CSRF_SESSION_KEY);
        if (tokenInSession == null || tokenInSession.isBlank()) {
            tokenInSession = UUID.randomUUID().toString();
            ss.setAttribute(CSRF_SESSION_KEY, tokenInSession);
        }

        Cookie c = new Cookie(CSRF_COOKIE, URLEncoder.encode(tokenInSession, StandardCharsets.UTF_8));
        c.setHttpOnly(false);
        c.setPath(r.getContextPath().isEmpty() ? "/" : r.getContextPath());
        c.setSecure(r.isSecure()); 
        w.addCookie(c);

        final String method = r.getMethod();
        if (SAFE_METHODS.contains(method)) {
            chain.doFilter(req, res);
            return;
        }

        final String ctx = r.getContextPath();        
        final String uri = r.getRequestURI();           
        final String path = uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;

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

        String token = r.getHeader(CSRF_HEADER);
        if (token == null || token.isBlank()) {
            token = r.getParameter(CSRF_PARAM);
        }

        if (token == null || tokenInSession == null || !tokenInSession.equals(token)) {
            // 403 với thông điệp rõ ràng cho dev
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
