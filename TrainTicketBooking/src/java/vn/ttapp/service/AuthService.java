package vn.ttapp.service;
import vn.ttapp.dao.UserDao;
import vn.ttapp.model.User;
import vn.ttapp.security.PasswordUtil;

import java.util.UUID;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public User login(String email, String password) throws Exception {
        if (email == null || password == null) {
            return null;
        }
        email = email.trim().toLowerCase();
        if (email.isEmpty() || password.isEmpty()) {
            return null;
        }

        User u = userDao.findByEmail(email);
        if (u == null || !u.isActive()) {
            return null;
        }

        // So khớp với password_hash
        if (!PasswordUtil.verify(password, u.getPasswordHash())) {
            return null;
        }

        return u;
    }

    public String register(String email, String password, String fullName) throws Exception {
        if (email == null || password == null || fullName == null) {
            return null;
        }
        email = email.trim().toLowerCase();
        fullName = fullName.trim();

        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            return null;
        }

        // Có thể bổ sung kiểm tra định dạng email, độ dài password tại đây
        if (userDao.emailExists(email)) {
            return null; // email đã tồn tại
        }

        String hash = PasswordUtil.hash(password); // tạo BCrypt hash
        UUID id = userDao.create(email, hash, fullName);
        return id != null ? id.toString() : null;
    }
}
