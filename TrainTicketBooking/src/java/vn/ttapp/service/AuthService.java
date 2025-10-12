package vn.ttapp.service;

import vn.ttapp.dao.UserDao;
import vn.ttapp.model.User;
import vn.ttapp.security.PasswordUtil;

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
        if (u == null || !u.isActive())
            return null;
        if (!PasswordUtil.verify(password, u.getPassword()))
            return null;
        return u;
    }
    
    public String register(String email, String password, String fullName) throws Exception {
        if (userDao.emailExists(email))
            return null;
        String hash = PasswordUtil.hash(password);
        return userDao.create(email, hash, fullName);
    }
}
