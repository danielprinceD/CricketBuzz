package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class AuthUtil {
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
	public final static  String SECRET_KEY = "auth_salt";
	private static final long EXPIRATION_TIME = 30 * 60 * 1000;
	
	public static String generateToken(String userId , String role) {
		Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
		return JWT.create().withSubject(userId).withClaim("role", role).withIssuedAt(new Date(System.currentTimeMillis() ))
				.withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)).sign(algorithm);
	}
	
	private static DecodedJWT verifyToken(String token)
	{
		Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
		JWTVerifier verifier = JWT.require(algorithm).build();
		return verifier.verify(token);
	}
	
	private static Boolean validateAuthorizationHeader(String authHeader)  {
		
		if(authHeader == null || authHeader.length() <= 7)
			return false;
		return true;
	}
	
	public static Map<String, String> getDetails(HttpServletRequest request) throws Exception{
		
		try {
			
			String token = request.getHeader("Authorization");
			
			if(!validateAuthorizationHeader(token)) 
				return null;
			
			token = token.substring(7);
			
			Map<String, String> details  = new HashMap<>();
			
			DecodedJWT verifier = verifyToken(token);
			
			String id = verifier.getSubject();
			String role = verifier.getClaim("role").asString();
			
			details.put("userId", id);
			details.put("role", role);
			
			return details;
		}
		finally {}
	}
	
	
	public static Boolean isAuthorizedAdmin(HttpServletRequest request ,  String table , String field , Integer id) throws Exception {
		
		String sql = "SELECT  created_by from " + table + " WHERE " + field + " =  ?";
		 
		try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
		         PreparedStatement pstmt = connection.prepareStatement(sql)) {
		        
		        pstmt.setInt(1, id);
		        
		        try (ResultSet resultSet = pstmt.executeQuery()) {
		            if (resultSet.next()) {
		                String createdBy = resultSet.getString("created_by");
		                
		                String userId = getUserId(request);
		                String role = getUserRole(request);
		                
		                if(role != null && role.equals("SUPERUSER"))
		                	return true;
		                
		                if(userId == null || createdBy == null )
		                	return false;
		                
		                return userId.equals(createdBy);
		                
		            } else {
		                return false;
		            }
		        }
		    }
	}
	
	public static String getUserId(HttpServletRequest request) throws Exception
	{
		try {
			
			Map<String , String> details = getDetails(request);
			if(details == null )return null;
			
			String createdBy = details.get("userId");
			if(createdBy == null)
				return null;
			
			return createdBy;
			
		}
		finally {
			
		}
	}
	public static String getUserRole(HttpServletRequest request) throws Exception
	{
		try {
			
			Map<String , String> details = getDetails(request);
			if(details == null )return null;
			
			String createdBy = details.get("role");
			if(createdBy == null)
				return null;
			
			return createdBy;
			
		}
		finally {
			
		}
	}
	
}
