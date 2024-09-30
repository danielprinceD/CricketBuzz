package utils;

import java.util.Set;
import org.json.JSONArray;
import config.RedisConfig;
import redis.clients.jedis.Jedis;

public class PlayingXIRedisUtil {
	
	
	public static String FIXTURE_REDIX_PREFIX = "playingXI:";
	public static String TEAM_REDIX_PREFIX = "team:";
	
	public static boolean isCached(int fixtureId , int teamId ) {
			
			try (Jedis jedis =  RedisConfig.getJedis().getResource() ){
				Set<String> cachedData = jedis.keys(FIXTURE_REDIX_PREFIX + fixtureId + ":" + TEAM_REDIX_PREFIX + teamId );
				if(cachedData != null && !cachedData.isEmpty())
					return true; 
			}
			return false;
	}
	
	public static void inValidateByFixtureIdTeamId(Integer fixtureId , Integer teamId) {
		try (Jedis jedis =  RedisConfig.getJedis().getResource() ){
			jedis.del("fixtures:" + fixtureId + ":" + TEAM_REDIX_PREFIX + teamId + ":playing11s:all");
		}
	}
	
	public static void inValidatePlaying11sByFixtureId(Integer fixtureId) {
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            String pattern = "fixtures:" + fixtureId + ":*:" + "playing11s:all";
            Set<String> keys = jedis.keys(pattern);
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
                System.out.println("Invalidated playing11s for fixtureId: " + fixtureId);
            } else {
                System.out.println("No keys found to invalidate for fixtureId: " + fixtureId);
            }
        } catch (Exception e) {
            System.err.println("Error invalidating playing11s: " + e.getMessage());
        }
    }
	
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
