package vn.ttapp.controller.auth;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;

import vn.ttapp.config.Db;
import vn.ttapp.dao.UserDao;
import vn.ttapp.model.User;
import java.sql.Timestamp;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/auth/forgot"})
public class ForgotPasswordServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/auth/forgot.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        if (email == null || email.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập email hợp lệ.");
            doGet(req, res);
            return;
        }

        try {
            String normalized = email.trim().toLowerCase();
            User user = userDao.findByEmail(normalized);

            // Dù có user hay không, ta vẫn trả về message chung để tránh lộ thông tin
            if (user != null && user.getUserId() != null) {
                UUID uid = user.getUserId();
                String token = UUID.randomUUID().toString();
                Instant expiresAt = Instant.now().plusSeconds(30 * 60); // 30 phút

                // 1) Xoá token cũ của user
                String sqlDeleteOld = "DELETE FROM dbo.PasswordResetToken WHERE user_id = ?";

                // 2) Thêm token mới
                String sqlInsert = """
                    INSERT INTO dbo.PasswordResetToken(user_id, token, expires_at, created_at)
                    VALUES (?, ?, ?, SYSUTCDATETIME())
                """;

                try (Connection cn = Db.getConnection()) {
                    cn.setAutoCommit(false);
                    try (PreparedStatement del = cn.prepareStatement(sqlDeleteOld); PreparedStatement ins = cn.prepareStatement(sqlInsert)) {

                        del.setString(1, uid.toString());
                        del.executeUpdate();

                        ins.setString(1, uid.toString());
                        ins.setString(2, token);
                        ins.setTimestamp(3, Timestamp.from(expiresAt));
                        ins.executeUpdate();

                        cn.commit();
                    } catch (SQLException ex) {
                        cn.rollback();
                        throw ex;
                    } finally {
                        cn.setAutoCommit(true);
                    }
                }

                // Build reset link ĐÚNG context path
                String scheme = req.getScheme();
                String host = req.getServerName();
                int port = req.getServerPort();
                String ctx = req.getContextPath(); // ví dụ: /TrainTicketBooking hoặc "" khi chạy root
                String base = scheme + "://" + host + ((port == 80 || port == 443) ? "" : (":" + port)) + ctx;
                String resetLink = base + "/auth/reset?token=" + token;

                System.out.println("[DEBUG] Reset link: " + resetLink);

                try {
                } catch (Exception mailEx) {
                    System.err.println("[MAIL][ERROR] " + mailEx.getClass().getName() + ": " + mailEx.getMessage());
                    mailEx.printStackTrace();
                }
            }

            req.setAttribute("message", "Nếu email hợp lệ, liên kết đặt lại mật khẩu đã được gửi.");
            doGet(req, res);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Đã xảy ra lỗi, vui lòng thử lại sau.");
            doGet(req, res);
        }
    }
}
