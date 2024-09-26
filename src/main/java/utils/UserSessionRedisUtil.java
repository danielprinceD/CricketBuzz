package utils;

import javax.servlet.http.Cookie;

import org.json.JSONObject;

import config.RedisConfig;
import redis.clients.jedis.Jedis;

public class UserSessionRedisUtil {
	
	private static final String SESSION_PREFIX = "session:";
    private static final int SESSION_TIMEOUT = 3600;
    
    protected Cookie createSession(String sessionId , String userId , String email , String role) {
    	Cookie sessionCookie = new Cookie("SESSIONID", sessionId);
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            
        	JSONObject userDetail = new JSONObject();
        	userDetail.put("email", email);
        	userDetail.put("role", role);
        	
            jedis.setex(SESSION_PREFIX + sessionId, SESSION_TIMEOUT, userDetail.toString() );

            
            sessionCookie.setMaxAge(SESSION_TIMEOUT);
        }
        return sessionCookie;
    }
    
    protected String getSessionData(String sessionId) {
        try (Jedis jedis =  RedisConfig.getJedis().getResource()) {
        	
            if (sessionId != null) {
                return jedis.get(SESSION_PREFIX + sessionId); 
            }
        }
        return null;
    }
	
}
