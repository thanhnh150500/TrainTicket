package vn.ttapp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class CsrfFilter implements Filter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private final Set<String> excludePrefixes = new HashSet<>();

    @Override
    public void init(FilterConfig cfg) {
        // File tĩnh của bạn đều dưới /assets/
        excludePrefixes.add("/assets/");
        excludePrefixes.add("/static/"); 
        excludePrefixes.add("/favicon");
        excludePrefixes.add("/robots.txt");

        // cho phép cấu hình thêm qua web.xml (tùy chọn)
        String extra = cfg.getInitParameter("excludePrefixes");
        if (extra != null && !extra.isBlank()) {
            for (String p : extra.split(",")) {
                p = p.trim();
                if (!p.isEmpty()) {
                    excludePrefixes.add(p.startsWith("/") ? p : "/" + p);
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        // --- Luôn có token trong session ---
        HttpSession ss = r.getSession(true);
        if (ss.getAttribute("csrfToken") == null) {
            ss.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());
        }

        // --- Bỏ qua phương thức an toàn ---
        String method = r.getMethod(); // "GET"/"POST"/...
        if (SAFE_METHODS.contains(method)) {
            chain.doFilter(req, res);
            return;
        }

        // --- Bỏ qua tài nguyên tĩnh theo prefix (không kèm contextPath) ---
        String ctx = r.getContextPath();                  // vd: /ttapp
        String uri = r.getRequestURI();                   // vd: /ttapp/assets/js/...
        String path = uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri; // => /assets/js/...
        for (String p : excludePrefixes) {
            if (path.startsWith(p)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // --- Kiểm tra token cho POST/PUT/PATCH/DELETE ---
        String token = r.getHeader("X-CSRF-Token");
        if (token == null || token.isBlank()) {
            token = r.getParameter("_csrf");
        }
        String expect = (String) ss.getAttribute("csrfToken");

        if (expect == null || !expect.equals(token)) {
            // Trả plain text để fetch() đọc dễ hơn
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
