package vn.ttapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuthUtil {

    public static boolean isLoggedIn(HttpServletRequest req) {
        HttpSession ss = req.getSession(false);
        return ss != null && ss.getAttribute("authUser") != null;
    }
}
