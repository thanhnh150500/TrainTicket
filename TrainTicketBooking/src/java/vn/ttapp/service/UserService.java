/*
 * UserService
 * -----------
 * - Xử lý nghiệp vụ quản trị người dùng (dùng cho trang Admin/Manager).
 * - Phối hợp với UserDao để truy vấn/ghi dữ liệu người dùng và vai trò.
 * - Ghi chú:
 *   + newPassword: nếu TẠO MỚI → bắt buộc; nếu CẬP NHẬT → để trống sẽ giữ nguyên mật khẩu cũ.
 *   + email: chuẩn hoá (trim + toLowerCase) để đảm bảo duy nhất theo email.
 *   + Mọi cập nhật User + Roles đều gộp trong 1 transaction.
 */
package vn.ttapp.service;

import vn.ttapp.config.Db;
import vn.ttapp.dao.UserDao;
import vn.ttapp.model.User;
import vn.ttapp.security.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserService {

    private final UserDao userDao = new UserDao();

    /* READ APIs (LIST / DETAIL) */
    /**
     * Lấy danh sách người dùng theo loại: - "CUSTOMER": chỉ khách hàng (hoặc
     * user chưa có role) - Khác/null: nhóm nội bộ (MANAGER/STAFF/…)
     */
    public List<User> getAllUsers(String roleType) throws SQLException {
        String key = (roleType == null) ? "" : roleType.trim().toUpperCase();
        if ("CUSTOMER".equals(key)) {
            return userDao.findAllWithRoles("CUSTOMER");
        }
        return userDao.findAllWithRoles("INTERNAL");
    }

    public User getUserWithRoles(String userId) throws SQLException {
        return userDao.findUserWithRoles(userId);
    }

    /*WRITE APIs (CREATE / UPDATE)*/
    /**
     * Tạo mới hoặc cập nhật người dùng + cập nhật vai trò trong cùng 1
     * transaction.
     *
     * @param user Model user (nếu userId = null → tạo mới; ngược lại → cập
     * nhật)
     * @param roleIds Danh sách role_id gán cho user (có thể null/empty)
     * @param newPassword Mật khẩu thuần: bắt buộc khi TẠO MỚI; khi CẬP NHẬT để
     * trống sẽ giữ nguyên.
     * @return true nếu thành công, false nếu lỗi/vi phạm ràng buộc
     */
    public boolean saveUser(User user, List<Integer> roleIds, String newPassword) throws SQLException {
        // 0) Chuẩn hoá input
        if (user == null) {
            return false;
        }

        // Chuẩn hoá email: trim + lowercase để đảm bảo duy nhất theo logic ứng dụng
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }
        // Có thể thêm validate định dạng email nếu cần (regex đơn giản)…
        // if (!isValidEmail(user.getEmail())) return false;

        // 1) Kiểm tra trùng email (không tính chính user đang sửa)
        User existedByEmail = userDao.findByEmail(user.getEmail());
        if (existedByEmail != null) {
            // Nếu đang cập nhật: cho phép tự trùng chính mình
            if (user.getUserId() == null || !Objects.equals(existedByEmail.getUserId(), user.getUserId())) {
                // Email đã thuộc về user khác
                return false;
            }
        }

        // 2) Xử lý mật khẩu:
        //    - Tạo mới → bắt buộc newPassword
        //    - Cập nhật → nếu newPassword rỗng thì KHÔNG đổi
        if (user.getUserId() == null) {
            if (newPassword == null || newPassword.isBlank()) {
                // Không thể tạo user mới nếu thiếu mật khẩu
                return false;
            }
            try {
                String hash = PasswordUtil.hash(newPassword);
                user.setPasswordHash(hash);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (newPassword != null && !newPassword.isBlank()) {
                try {
                    String hash = PasswordUtil.hash(newPassword);
                    user.setPasswordHash(hash); // set để Dao cập nhật cột password_hash
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                // Giữ nguyên mật khẩu cũ → set null để Dao bỏ qua cột password_hash
                user.setPasswordHash(null);
            }
        }

        // 3) Transaction: tạo/cập nhật user + cập nhật roles
        try (Connection conn = Db.getConnection()) {
            boolean origAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                UUID uid;

                if (user.getUserId() == null) {
                    // 3a) TẠO MỚI
                    uid = userDao.adminCreateUser(user, conn);
                    if (uid == null) {
                        throw new SQLException("Failed to create user (no id returned)");
                    }
                    user.setUserId(uid);
                } else {
                    // 3b) CẬP NHẬT
                    uid = user.getUserId();
                    userDao.adminUpdateUser(user, conn);
                }

                // 4) Cập nhật vai trò (xoá hết role cũ rồi thêm lại)
                userDao.adminUpdateRoles(uid, roleIds, conn);

                // 5) Commit
                conn.commit();
                conn.setAutoCommit(origAuto);
                return true;

            } catch (Exception ex) {
                // Rollback khi có lỗi
                try {
                    conn.rollback();
                } catch (Exception ignore) {
                }
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ignore) {
                }
                ex.printStackTrace();
                return false;
            }
        }
    }

}
