package vn.ttapp.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/auth/logout"})
public class LogoutServlet extends HttpServlet {

    private static final String[] CLEAR_ATTRS = {
        "authUser", "userId", "csrfToken", "cart", "seatSelection"
    };

    private void doLogout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // 1) Xoá attribute quan trọng
        HttpSession ss = req.getSession(false);
        if (ss != null) {
            for (String k : CLEAR_ATTRS) {
                ss.removeAttribute(k);
            }
            // 2) Invalidate session
            try {
                ss.invalidate();
            } catch (IllegalStateException ignore) {
            }
        }

        // 3) Xoá cookie phiên (JSESSIONID) + CSRF-TOKEN (nếu có)
        String ctx = req.getContextPath();
        if (ctx == null || ctx.isBlank()) {
            ctx = "/";
        }

        for (Cookie c : req.getCookies() == null ? new Cookie[0] : req.getCookies()) {
            if ("JSESSIONID".equalsIgnoreCase(c.getName()) || "CSRF-TOKEN".equalsIgnoreCase(c.getName())) {
                Cookie del = new Cookie(c.getName(), "");
                del.setMaxAge(0);
                del.setPath(ctx);           // rất quan trọng: đúng path
                del.setHttpOnly("JSESSIONID".equalsIgnoreCase(c.getName()));
                del.setSecure(req.isSecure());
                res.addCookie(del);
            }
        }

        res.sendRedirect(req.getContextPath() + "/auth/login");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doLogout(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doLogout(req, res);
    }
}
