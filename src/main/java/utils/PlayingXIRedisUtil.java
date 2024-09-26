package utils;

import org.json.JSONArray;
import config.RedisConfig;
import redis.clients.jedis.Jedis;

public class PlayingXIRedisUtil {
	
	
	public static String FIXTURE_REDIX_PREFIX = "playingXI:";
	public static String TEAM_REDIX_PREFIX = "team:";
	
	public static JSONArray getPlayingXIByFixtureIdByTeamId(int fixtureID , int teamID){
		
		JSONArray playingXIs = new JSONArray();
		
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            String key = FIXTURE_REDIX_PREFIX + fixtureID + ":" + TEAM_REDIX_PREFIX + teamID;
            String cachedData = jedis.get(key);
            if(cachedData != null)
            	playingXIs = new JSONArray(cachedData);
            System.out.println("Data fetched from redis");
        }
        return playingXIs;
	}
	
	public static void setPlayingXIByFixtureIdByTeamId( JSONArray playingXIs  , int fixtureID , int teamID){
		
		try(Jedis jedis = RedisConfig.getJedis().getResource())
    	{
    		String key = FIXTURE_REDIX_PREFIX + fixtureID + ":" + TEAM_REDIX_PREFIX + teamID;
    		jedis.set(key  , playingXIs.toString() );
    		System.out.println("Data update in redis");
    	}
		
	}
	
	
	public static void deleteByFixtureByIdByTeamId(int fixtureID , int teamID) {
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			String key = FIXTURE_REDIX_PREFIX + fixtureID + ":" + TEAM_REDIX_PREFIX + teamID;
			jedis.del(key);
			System.out.println("Data deleted from redis");
		}
	}
	
	
}
