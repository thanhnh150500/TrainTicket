package vn.ttapp.service;

import jakarta.servlet.http.HttpSession;

public final class SessionUtils {

    private SessionUtils() {
    }

    public static Object getUserPrincipal(HttpSession ss) {
        if (ss == null) {
            return null;
        }
        Object u = ss.getAttribute("user");
        if (u == null) {
            u = ss.getAttribute("authUser");
        }
        if (u == null) {
            u = ss.getAttribute("currentUser");
        }
        return u;
    }
}
