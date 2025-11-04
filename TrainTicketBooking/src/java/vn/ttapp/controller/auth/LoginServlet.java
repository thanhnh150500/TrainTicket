package vn.ttapp.controller.auth;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import vn.ttapp.dao.UserDao;
import vn.ttapp.model.User;
import vn.ttapp.security.PasswordUtil; // ✅ dùng để verify mật khẩu

@WebServlet(name = "LoginServlet", urlPatterns = {"/auth/login"})
public class LoginServlet extends HttpServlet {

    private static final String DEFAULT_AFTER_LOGIN = "/booking-history";

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (req.getParameter("next") != null) {
            req.setAttribute("next", req.getParameter("next"));
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = trimLower(req.getParameter("email"));
        String password = req.getParameter("password");

        User user = null;
        try {
            user = userDao.findByEmail(email);
        } catch (Exception ex) {
            throw new ServletException("Auth error", ex);
        }

        boolean ok = (user != null) && PasswordUtil.verify(password, user.getPasswordHash());
        if (!ok) {
            req.setAttribute("error", "Sai email hoặc mật khẩu.");
            // bảo toàn next nếu có
            req.setAttribute("next", req.getParameter("next"));
            doGet(req, res);
            return;
        }

        req.changeSessionId(); 
        HttpSession ss = req.getSession(true);
        ss.setAttribute("authUser", user);

        String nextRaw = req.getParameter("next");
        if (nextRaw == null || nextRaw.isBlank()) {
            nextRaw = (String) ss.getAttribute("returnTo");
            if (nextRaw != null) {
                ss.removeAttribute("returnTo");
            }
        }

        res.sendRedirect(normalizeNextOrDefault(nextRaw, req));
    }

    private static String trimLower(String s) {
        return (s == null) ? null : s.trim().toLowerCase();
    }

    private String normalizeNextOrDefault(String next, HttpServletRequest req) {
        String ctx = req.getContextPath();
        String def = ctx + DEFAULT_AFTER_LOGIN;

        if (next == null || next.isBlank()) {
            return def;
        }

        String n = URLDecoder.decode(next, StandardCharsets.UTF_8);

        if (n.startsWith("http://") || n.startsWith("https://")
                || n.contains("..") || n.startsWith("/WEB-INF") || n.endsWith(".jsp")) {
            return def;
        }

        if (n.startsWith(ctx + "/")) {
            n = n.substring(ctx.length());
        }
        if (!n.startsWith("/")) {
            n = "/" + n;
        }
        if ("/".equals(n)) {
            n = DEFAULT_AFTER_LOGIN;
        }

        return ctx + n;
    }
}
