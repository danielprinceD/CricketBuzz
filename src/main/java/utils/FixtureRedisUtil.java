package utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import java.util.Set;
import org.json.JSONObject;
import com.google.gson.Gson;
import config.RedisConfig;
import model.FixtureVO;
import model.TournamentVO;
import redis.clients.jedis.Jedis;

public class FixtureRedisUtil {
	
	private static final String FIXTURE_REDIS_PREFIX = "fixtures:";
	private static final String TOURNAMENT_REDIS_PREFIX = "tournaments:";
	
	public static boolean isCached() {
		
		try (Jedis jedis =  RedisConfig.getJedis().getResource() ){
			Set<String> cachedData = jedis.keys(TOURNAMENT_REDIS_PREFIX + "*" );
			if(cachedData != null && !cachedData.isEmpty() )
				return true; 
		}
		return false;
	}
	
	public static void inValidateFixture(Integer fixtureId)
	{
		try (Jedis jedis = RedisConfig.getJedis().getResource() ) {
			jedis.del("fixtures:" + fixtureId);
		}
	}
	
	public static FixtureVO getFixtureById(int fixtureId){
		try (Jedis jedis = RedisConfig.getJedis().getResource() ) {
			
			String json = jedis.get(FIXTURE_REDIS_PREFIX + fixtureId);
			
			if(json  != null)
			{
				System.out.println("GET : FIXTURE ID");
				return new Gson().fromJson(json, FixtureVO.class);
			}
			
		}
		return null; 
	}
	
	public static void setFixtureId(FixtureVO fixture, Integer fixtureId) {
		try (Jedis jedis = RedisConfig.getJedis().getResource() ) {
			String key = FIXTURE_REDIS_PREFIX + fixtureId;
			jedis.set( key , new JSONObject(fixture).toString());
			System.out.println("SET : FIXTURE ID");
		}
	}
	
    public static void setFixtureByTourID(List<FixtureVO> fixtures , int tourId) {
        try (Jedis jedis = RedisConfig.getJedis().getResource() ) {
        		String key = TOURNAMENT_REDIS_PREFIX + tourId + ":" + FIXTURE_REDIS_PREFIX  + "all";
        		String json = new Gson().toJson(fixtures);
        		if(json != null){
        			jedis.set(key , json);
        			System.out.println("SET:FIXTURE_ID");        			
        		}
        }
    }
    
    public static void setTournamentsById(TournamentVO tournamentVO , int tourId)
    {
    	try(Jedis jedis = RedisConfig.getJedis().getResource())
    	{
    		if(isCached())
    		{    			
	    		String key =FIXTURE_REDIS_PREFIX + tourId;
	    		jedis.set(key  , tournamentVO.toJson());
	    		System.out.println("SET:TOUR_ID");
    		}
    		
    	}
    }

    public static List<FixtureVO> getFixturesByTourId(Integer tourid) {
    	
        List<FixtureVO> tournaments = new ArrayList<>();
        
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            
        	String json = jedis.get(TOURNAMENT_REDIS_PREFIX + tourid + ":" + FIXTURE_REDIS_PREFIX + "all");
        	Type type = new TypeToken <List<FixtureVO>> () {}.getType();
        	if(json != null)
        		tournaments = new Gson().fromJson(json, type);
            
            if(tournaments != null && !tournaments.isEmpty())
            	System.out.println("All Data fetched from cache");
        }
        
        return tournaments;
    }
    
    public static void deleteFixtureById(int fixtureId){
    	
    	try (Jedis jedis = RedisConfig.getJedis().getResource()) {
    		
	    		String KEY = TOURNAMENT_REDIS_PREFIX  + "*" ;
	    		Set<String> keys = jedis.keys(KEY);
	            for(String key : keys)
	            {
	            	if(!key.isEmpty())
	            	{
	            		FixtureVO fixture =  new Gson().fromJson(jedis.get(key) , FixtureVO.class);
	            		if(fixture.getFixtureId() == fixtureId)
	            		{
	            			jedis.del(key);
	            			System.out.println("Data deleted from redis");
	            			break;
	            		}
	            	}
	            }
    		
        }
    }
    
    public static void deleteFixturesByTournamentId(int tourId) {
    	
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            
        	Set<String> keys = jedis.keys(TOURNAMENT_REDIS_PREFIX + tourId + ":" + FIXTURE_REDIS_PREFIX + "*");
            
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
                System.out.println("All Data Deleted from cache");
            } else {
                System.out.println("No Data Found in cache for the given tournament ID");
            }

        }
        
    }
    
    
   
	
}
