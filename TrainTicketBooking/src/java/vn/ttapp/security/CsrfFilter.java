package vn.ttapp.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CsrfFilter implements Filter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private final Set<String> excludePrefixes = new HashSet<>();

    @Override
    public void init(FilterConfig cfg) throws ServletException {
        // Các prefix tài nguyên tĩnh mặc định
        excludePrefixes.add("/assets/");
        excludePrefixes.add("/static/");
        excludePrefixes.add("/favicon");
        excludePrefixes.add("/images/");
        excludePrefixes.add("/css/");
        excludePrefixes.add("/js/");
        excludePrefixes.add("/webjars/");

        String extra = cfg.getInitParameter("excludePrefixes");
        if (extra != null && !extra.isBlank()) {
            for (String p : extra.split(",")) {
                String v = p.trim();
                if (!v.isEmpty()) {
                    excludePrefixes.add(v.startsWith("/") ? v : "/" + v);
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;
        String ctx = r.getContextPath();
        String uri = r.getRequestURI();

        // 1) Bỏ qua các đường dẫn tĩnh/được exclude
        for (String prefix : excludePrefixes) {
            if (uri.startsWith(ctx + prefix)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // 2) Luôn đảm bảo có CSRF token trong session để JSP render form
        HttpSession ss = r.getSession(true);
        if (ss.getAttribute("csrfToken") == null) {
            ss.setAttribute("csrfToken", UUID.randomUUID().toString());
        }

        // 3) Chỉ kiểm tra với các phương thức không an toàn
        String method = r.getMethod();
        if (!SAFE_METHODS.contains(method)) {
            // Token từ form field hoặc từ header
            String tokenFromRequest = r.getParameter("_csrf");
            if (tokenFromRequest == null || tokenFromRequest.isBlank()) {
                tokenFromRequest = r.getHeader("X-CSRF-Token");
            }
            String tokenInSession = (String) ss.getAttribute("csrfToken");

            if (tokenInSession == null || !tokenInSession.equals(tokenFromRequest)) {
                w.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }
}
