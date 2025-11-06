/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.service;

import vn.ttapp.config.Db;
import vn.ttapp.dao.UserDao;
import vn.ttapp.model.User;
import vn.ttapp.security.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Service này xử lý nghiệp vụ cho Quản lý User
 */
public class UserService {
    
    private final UserDao userDao = new UserDao();
    
    /**
     * Lấy danh sách user (Nội bộ hoặc Khách hàng)
     */
    public List<User> getAllUsers(String roleType) throws SQLException {
        if ("CUSTOMER".equals(roleType)) {
            return userDao.findAllWithRoles("CUSTOMER");
        }
        return userDao.findAllWithRoles("INTERNAL");
    }

    /**
     * Lấy 1 user (bao gồm cả role)
     */
    public User getUserWithRoles(String userId) throws SQLException {
        return userDao.findUserWithRoles(userId);
    }
    
    /**
     * Admin tạo/cập nhật user (Staff/Manager)
     */
    public boolean saveUser(User user, List<Integer> roleIds, String newPassword) throws SQLException {
        // (1) Kiểm tra Email
        User existing = userDao.findByEmail(user.getEmail());
        if (existing != null && !existing.getUserId().equals(user.getUserId())) {
            // Email này đã tồn tại của 1 user khác
            return false; 
        }

        // (2) Xử lý mật khẩu
        if (newPassword != null && !newPassword.isBlank()) {
            try {
                String hash = PasswordUtil.hash(newPassword);
                user.setPasswordHash(hash); // (Lưu hash vào model)
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // (3) Bắt đầu Transaction
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String userId;
                
                if (user.getUserId() == null) {
                    // (3a) TẠO MỚI
                    if (newPassword == null || newPassword.isBlank()) {
                        // (Bắt buộc phải có pass khi tạo mới)
                        throw new SQLException("Password is required for new user");
                    }
                    userId = userDao.adminCreateUser(user, conn);
                    if (userId == null) throw new SQLException("Failed to create user");
                    // set created id back to model for consistency
                    try {
                        user.setUserId(UUID.fromString(userId));
                    } catch (Exception ignore) {
                    }
                    
                } else {
                    // (3b) CẬP NHẬT
                    userId = user.getUserId().toString();
                    userDao.adminUpdateUser(user, conn);
                }
                
                // (4) Cập nhật Roles
                userDao.adminUpdateRoles(UUID.fromString(userId), roleIds, conn);
                
                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        }
    }
}
