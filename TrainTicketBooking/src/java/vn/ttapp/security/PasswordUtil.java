package vn.ttapp.security;
   
import org.mindrot.jbcrypt.BCrypt;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author New User
 */
public class PasswordUtil {
    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(12));
    }
    public static boolean verify(String raw, String hash) {
        if (hash == null || hash.isEmpty()) 
            return false;
        return BCrypt.checkpw(raw, hash);
    }
}
