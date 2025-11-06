package vn.ttapp.service;

import vn.ttapp.dao.UserDao;
import vn.ttapp.model.Role; // <-- THÊM IMPORT
import vn.ttapp.model.User;
import vn.ttapp.security.PasswordUtil;
import java.util.List; // <-- THÊM IMPORT
import java.sql.SQLException; // <-- THÊM IMPORT

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author New User
 */
public class AuthService {

    private final UserDao userDao = new UserDao();

    public User login(String email, String password) throws Exception {
        User u = userDao.findByEmail(email);
        // If user not found or inactive, return null (login failed)
        if (u == null || !u.isActive()) {
            return null;
        }
        
        if (u.getPasswordHash() == null) {
            return null;
        }

        // Chỉ chạy nếu passwordHash KHÔNG NULL
        if (!PasswordUtil.verify(password, u.getPasswordHash())) {
            return null;
        }
        try {
            List<Role> roles = userDao.findRolesByUserId(u.getUserId());
            u.setRoles(roles); // Gán vai trò vào đối tượng User
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return u;
    }

    public String register(String email, String password, String fullName) throws Exception {
        if (userDao.emailExists(email)) {
            return null;
        }
        String hash = PasswordUtil.hash(password);
        java.util.UUID id = userDao.create(email, hash, fullName);
        return (id != null) ? id.toString() : null;
    }
}
