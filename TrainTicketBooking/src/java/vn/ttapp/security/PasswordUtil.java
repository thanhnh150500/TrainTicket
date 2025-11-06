package vn.ttapp.security;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    public static final int BCRYPT_COST = 12;

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be null/blank");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST));
    }
    
    public static boolean verify(String rawPassword, String bcryptHash) {
        if (rawPassword == null || bcryptHash == null) {
            return false;
        }
        if (bcryptHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, bcryptHash);
        } catch (IllegalArgumentException ex) {
            // Trường hợp bcryptHash không đúng format ($2a/$2b/$2y$...)
            return false;
        }
    }

    public static boolean needsRehash(String bcryptHash) {
        if (bcryptHash == null || bcryptHash.isBlank()) {
            return true;
        }
        try {
            int cost = extractCost(bcryptHash);
            return cost < BCRYPT_COST;
        } catch (Exception ignore) {
            return true;
        }
    }

    private static int extractCost(String bcryptHash) {
        int first = bcryptHash.indexOf('$');            // 0
        int second = bcryptHash.indexOf('$', first + 1); // sau "2a"/"2b"/"2y"
        int third = bcryptHash.indexOf('$', second + 1); // sau cost
        if (first != 0 || second < 0 || third < 0) {
            throw new IllegalArgumentException("Invalid bcrypt format");
        }
        String costStr = bcryptHash.substring(second + 1, third);
        return Integer.parseInt(costStr);
    }
}
