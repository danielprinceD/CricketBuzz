package config;

import redis.clients.jedis.JedisPool;

public class RedisConfig {
	
	private static JedisPool jedisPool;

	static {
		jedisPool = new JedisPool("localhost" , 6379);
	}
	
	public static JedisPool  getJedis() {
		return jedisPool;
	}
	
}
