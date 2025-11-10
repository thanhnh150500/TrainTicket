package vn.ttapp.service;

import vn.ttapp.dao.UserDao;
import vn.ttapp.model.Role;
import vn.ttapp.model.User;
import vn.ttapp.security.PasswordUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * AuthService - Đăng nhập: trả về User đã kèm Roles (nếu hợp lệ). - Helper kiểm
 * tra vai trò: isAdmin / isManager / isStaffPrefix. - Biến thể đăng nhập theo
 * vai trò: loginAdmin / loginManager. - Đăng ký: chỉ tạo Users; gán role nên
 * thực hiện ở service quản trị.
 */
public class AuthService {

    private final UserDao userDao = new UserDao();

    /* =============================
       Helpers về vai trò
       ============================= */
    public static boolean isAdmin(User u) {
        if (u == null || u.getRoles() == null) {
            return false;
        }
        for (Role r : u.getRoles()) {
            if (r != null && "ADMIN".equalsIgnoreCase(r.getCode())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isManager(User u) {
        if (u == null || u.getRoles() == null) {
            return false;
        }
        for (Role r : u.getRoles()) {
            if (r != null && "MANAGER".equalsIgnoreCase(r.getCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bất kỳ role bắt đầu bằng STAFF_ (ví dụ STAFF_FNB, STAFF_POS, …)
     */
    public static boolean isStaffPrefix(User u) {
        if (u == null || u.getRoles() == null) {
            return false;
        }
        for (Role r : u.getRoles()) {
            String code = (r == null) ? null : r.getCode();
            if (code != null && code.toUpperCase(Locale.ROOT).startsWith("STAFF_")) {
                return true;
            }
        }
        return false;
    }

    /* =============================
       Đăng nhập
       ============================= */
    /**
     * Đăng nhập mọi loại user.
     *
     * @return User đã nạp roles nếu hợp lệ; null nếu sai thông tin hoặc bị
     * khoá.
     */
    public User login(String email, String password) throws Exception {
        if (email == null || password == null) {
            return null;
        }

        // 1) Chuẩn hoá email
        email = email.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty()) {
            return null;
        }

        // 2) Tìm user theo email
        User u = userDao.findByEmail(email);
        if (u == null) {
            return null;          // không tồn tại
        }
        if (!u.isActive()) {
            return null;      // bị khoá
        }
        // 3) Kiểm tra mật khẩu
        String hash = u.getPasswordHash();
        if (hash == null || hash.isBlank()) {
            return null; // user chưa set mật khẩu / SSO
        }
        if (!PasswordUtil.verify(password, hash)) {
            return null;
        }

        // 4) Nạp roles (bắt buộc để controller/filter phân quyền)
        List<Role> roles;
        try {
            roles = userDao.findRolesByUserId(u.getUserId());
        } catch (SQLException e) {
            // Nếu lỗi DB khi tải role → trả null để tránh phiên không có quyền rõ ràng
            e.printStackTrace();
            return null;
        }
        u.setRoles(roles != null ? roles : new ArrayList<>());

        return u;
    }

    /**
     * Đăng nhập chỉ dành cho ADMIN.
     *
     * @return User nếu đăng nhập đúng và có role ADMIN; null nếu không đủ điều
     * kiện.
     */
    public User loginAdmin(String email, String password) throws Exception {
        User u = login(email, password);
        return (u != null && isAdmin(u)) ? u : null;
    }

    /**
     * Đăng nhập chỉ dành cho MANAGER.
     *
     * @return User nếu đăng nhập đúng và có role MANAGER; null nếu không đủ
     * điều kiện.
     */
    public User loginManager(String email, String password) throws Exception {
        User u = login(email, password);
        return (u != null && isManager(u)) ? u : null;
    }

    /* =============================
       Đăng ký
       ============================= */
    /**
     * Đăng ký tài khoản mới → tạo bản ghi Users.
     *
     * @return userId (UUID.toString) nếu thành công; null nếu trùng email hoặc
     * lỗi.
     *
     * Lưu ý: Hàm này KHÔNG tự gán role. Nếu muốn gán role CUSTOMER mặc định,
     * bạn nên thực hiện ở Admin/UserManagementService (trong transaction) bằng
     * các API như adminUpdateRoles(...) của UserDao.
     */
    public String register(String email, String password, String fullName) throws Exception {
        if (email == null || password == null) {
            return null;
        }

        // Chuẩn hoá email
        email = email.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty()) {
            return null;
        }

        // Không cho trùng email
        if (userDao.emailExists(email)) {
            return null;
        }

        // Hash mật khẩu
        String hash = PasswordUtil.hash(password);

        // Tạo user
        UUID id = userDao.create(email, hash, fullName);
        return (id != null) ? id.toString() : null;
    }
}
