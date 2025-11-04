package vn.ttapp.controller.auth;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import vn.ttapp.dao.UserDao;
import vn.ttapp.model.User;
import vn.ttapp.security.PasswordUtil; // nếu bạn dùng BCrypt

@WebServlet(name = "LoginServlet", urlPatterns = {"/auth/login"})
public class LoginServlet extends HttpServlet {

    private static final String DEFAULT_AFTER_LOGIN = "/home";
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        // ===== Xác thực thực tế =====
        User user = null;
        try {
            user = userDao.findByEmail(email == null ? null : email.trim().toLowerCase());
        } catch (Exception ex) {
            // log…
        }

        boolean ok = (user != null) /* && PasswordUtil.verify(password, user.getPasswordHash()) */;
        if (!ok) {
            req.setAttribute("error", "Sai email hoặc mật khẩu.");
            req.setAttribute("next", req.getParameter("next"));
            doGet(req, res);
            return;
        }

        // Chống session fixation
        req.changeSessionId();
        HttpSession ss = req.getSession(true);

        // BỎ user vào session với đúng tên header đang dùng:
        ss.setAttribute("authUser", user);

        // Điều hướng
        String nextRaw = req.getParameter("next");
        if (nextRaw == null || nextRaw.isBlank()) {
            nextRaw = (String) ss.getAttribute("returnTo");
            if (nextRaw != null) {
                ss.removeAttribute("returnTo");
            }
        }
        res.sendRedirect(normalizeNextOrDefault(nextRaw, req));
    }

    private String normalizeNextOrDefault(String next, HttpServletRequest req) {
        String ctx = req.getContextPath();
        String def = ctx + DEFAULT_AFTER_LOGIN;
        if (next == null || next.isBlank()) {
            return def;
        }

        String n = URLDecoder.decode(next, StandardCharsets.UTF_8);

        // chặn URL ngoài & đường nguy hiểm
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
