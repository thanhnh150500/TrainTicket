package vn.ttapp.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class EmailService {

    private static final String JNDI_NAME = "java:comp/env/mail/Session";
    private static volatile Session CACHED; // tránh lookup nhiều lần

    private Session lookupSession() throws NamingException {
        if (CACHED != null) {
            return CACHED;
        }
        synchronized (EmailService.class) {
            if (CACHED == null) {
                CACHED = (Session) new InitialContext().lookup(JNDI_NAME);
            }
        }
        return CACHED;
    }

    private static String req(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing mail property: " + key);
        }
        return v.trim();
    }

    public void sendResetEmail(String to, String resetLink) throws Exception {
        Session session = lookupSession();
        // Bật debug khi cần
        // session.setDebug(true);

        Properties p = session.getProperties();

        // Các property bắt buộc
        String host = req(p, "mail.smtp.host");
        String port = p.getProperty("mail.smtp.port", "587").trim();
        String user = req(p, "mail.smtp.user");
        String pass = req(p, "mail.smtp.password");

        // STARTTLS vs SSL (465)
        boolean starttls = Boolean.parseBoolean(p.getProperty("mail.smtp.starttls.enable", "true"));
        boolean ssl = Boolean.parseBoolean(p.getProperty("mail.smtp.ssl.enable", "false"))
                || "465".equals(port);

        // “From” nên trùng tài khoản SMTP (đặc biệt Gmail/Outlook)
        String fromAddr = p.getProperty("mail.from", user);

        // Log cấu hình cơ bản
        System.out.printf("[MAIL] host=%s port=%s user=%s starttls=%s ssl=%s%n",
                host, port, user, starttls, ssl);

        // Dựng message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromAddr, "TrainTicket", StandardCharsets.UTF_8.name()));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Objects.requireNonNull(to), false));
        msg.setSubject("Đặt lại mật khẩu - TrainTicket", StandardCharsets.UTF_8.name());

        String html = """
          <div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;">
            <p>Chào bạn,</p>
            <p>Nhấn vào liên kết sau để đặt lại mật khẩu (hết hạn sau 30 phút):</p>
            <p><a href="%s" target="_blank" rel="noopener">%s</a></p>
          </div>
        """.formatted(resetLink, resetLink);
        msg.setContent(html, "text/html; charset=UTF-8");

        // Gửi
        Transport t = session.getTransport("smtp");
        try {
            // Lưu ý: nếu dùng SSL (465), cần mail.smtp.ssl.enable=true trong JNDI
            t.connect(host, Integer.parseInt(port), user, pass);
            t.sendMessage(msg, msg.getAllRecipients());
            System.out.println("[MAIL] sent to " + to);
        } finally {
            try {
                t.close();
            } catch (Exception ignore) {
            }
        }
    }
}
