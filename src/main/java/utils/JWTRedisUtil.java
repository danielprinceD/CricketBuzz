package utils;

import config.RedisConfig;
import redis.clients.jedis.Jedis;

public class JWTRedisUtil {
	

    public static void setAccessToken(String token, long expirationTime) {
    	try(Jedis jedis = RedisConfig.getJedis().getResource() )
    	{	
        String redisKey = "tokens:" + token;
	        jedis.hset(redisKey, "isActive", "1"); 
	        jedis.expire(redisKey, (int) (expirationTime / 1000));
    	}
    }

    public static boolean isTokenActive(String token) {
    	try(Jedis jedis = RedisConfig.getJedis().getResource() )
    	{
	        String redisKey = "tokens:" + token;
	        String isActive = jedis.hget(redisKey, "isActive");
	        return "1".equals(isActive);
    	}
    }

    public static void deactivateToken(String token) {
    	try(Jedis jedis = RedisConfig.getJedis().getResource() )
    	{
	        String redisKey = "tokens:" + token;
	        jedis.hset(redisKey, "isActive", "0"); 
    	}
    }
	
}
