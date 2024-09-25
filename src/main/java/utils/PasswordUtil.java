package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtil {
	public static String hashPassword(String password) throws NoSuchAlgorithmException{
		
		MessageDigest digest;
		
		digest = MessageDigest.getInstance("SHA-256");
		
		byte[] encoded = digest.digest(password.getBytes(StandardCharsets.UTF_16));
		
		String hashedPassword = Base64.getEncoder().encodeToString(encoded);
		
		return hashedPassword;
	}
}
