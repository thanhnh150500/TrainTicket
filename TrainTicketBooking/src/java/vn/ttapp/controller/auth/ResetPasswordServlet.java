package vn.ttapp.controller.auth;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;

import vn.ttapp.config.Db;
import vn.ttapp.security.PasswordUtil;

@WebServlet(name = "ResetPasswordServlet", urlPatterns = {"/auth/reset"})
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String token = req.getParameter("token");
        if (token == null || token.isBlank()) {
            req.setAttribute("error", "Liên kết không hợp lệ.");
            req.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(req, res);
            return;
        }

        // Kiểm tra token còn hạn
        if (!isValidToken(token)) {
            req.setAttribute("error", "Mã đặt lại không hợp lệ hoặc đã hết hạn.");
            req.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(req, res);
            return;
        }

        req.setAttribute("token", token);
        req.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String token = req.getParameter("token");
        String pw = req.getParameter("password");
        String cf = req.getParameter("confirmPassword");

        if (token == null || token.isBlank()) {
            req.setAttribute("error", "Thiếu mã đặt lại.");
            req.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(req, res);
            return;
        }

        if (pw == null || pw.isBlank() || cf == null || cf.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập đủ mật khẩu.");
            req.setAttribute("token", token);
            req.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(req, res);
            return;
        }
        if (!pw.equals(cf)) {
            req.setAttribute("error", "Xác nhận mật khẩu chưa khớp.");
            req.setAttribute("token", token);
            req.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(req, res);
            return;
        }

        String userId = getUserIdFromValidToken(token);
        if (userId == null) {
            req.setAttribute("error", "Mã đặt lại không hợp lệ hoặc đã hết hạn.");
            req.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(req, res);
            return;
        }

        String hash = PasswordUtil.hash(pw);

        String sqlUpdate = "UPDATE dbo.Users SET password_hash = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        String sqlDelete = "DELETE FROM dbo.PasswordResetToken WHERE token = ?";

        try (Connection cn = Db.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement up = cn.prepareStatement(sqlUpdate); PreparedStatement del = cn.prepareStatement(sqlDelete)) {

                up.setString(1, hash);
                up.setString(2, userId);
                up.executeUpdate();

                del.setString(1, token);
                del.executeUpdate();

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            req.setAttribute("token", token);
            req.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(req, res);
            return;
        }

        String ctx = req.getContextPath();
        res.sendRedirect(ctx + "/auth/login?reset=success");
    }


    private boolean isValidToken(String token) {
        String sql = """
            SELECT 1
            FROM dbo.PasswordResetToken
            WHERE token = ?
              AND expires_at > SYSUTCDATETIME()
        """;
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getUserIdFromValidToken(String token) {
        String sql = """
            SELECT CONVERT(varchar(36), user_id) AS user_id
            FROM dbo.PasswordResetToken
            WHERE token = ?
              AND expires_at > SYSUTCDATETIME()
        """;
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
