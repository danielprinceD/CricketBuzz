package utils;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class AuthUtil {
	
	public final static  String SECRET_KEY = "auth_salt";
	private static final long EXPIRATION_TIME = 30 * 60 * 1000;
	
	public static String generateToken(String userId , String role) {
		Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
		return JWT.create().withSubject(userId).withClaim("role", role).withIssuedAt(new Date(System.currentTimeMillis() ))
				.withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)).sign(algorithm);
	}
	
	public static DecodedJWT verifyToken(String token)
	{
		Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
		JWTVerifier verifier = JWT.require(algorithm).build();
		return verifier.verify(token);
	}
	
}
