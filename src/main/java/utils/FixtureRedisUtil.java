package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.security.auth.kerberos.KerberosTicket;

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
	
	public static List<FixtureVO> getFixtureById(int fixtureId){
		List<FixtureVO> result = new ArrayList<>();
		try (Jedis jedis = RedisConfig.getJedis().getResource() ) {
			
			Set<String> Keys = jedis.keys(TOURNAMENT_REDIS_PREFIX + "*");
			
			for (String key : Keys) {
	            String fixtureJSON = jedis.get(key); 
	            if (fixtureJSON != null) {
	                FixtureVO fixture = new Gson().fromJson(fixtureJSON, FixtureVO.class);

	                if (fixture.getFixtureId() == fixtureId) {
	                    result.add(fixture);
	                    System.out.println("GET:ID");
	                }
	            }
	        }
			
		}
		return result; 
	}
	
    public static void setFixtureByTourID(List<FixtureVO> fixtures , int tourId) {
        try (Jedis jedis = RedisConfig.getJedis().getResource() ) {
        	
        	
        	for(FixtureVO fixture : fixtures)
        	{
        		String key = TOURNAMENT_REDIS_PREFIX + tourId + ":" + FIXTURE_REDIS_PREFIX  + fixture.getFixtureId();
        		
        		jedis.set(key , new JSONObject(fixture).toString());
        	}
            System.out.println("SET:FIXTURE_ID");
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
        if(tourid == null)
        	return tournaments;
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            
        	Set<String> keys = jedis.keys(TOURNAMENT_REDIS_PREFIX + tourid + ":" + FIXTURE_REDIS_PREFIX + "*");
            
            for (String key : keys) {
            	
            	String fixtureJSON = jedis.get(key);
            	FixtureVO tournament = new Gson().fromJson(fixtureJSON, FixtureVO.class);
                tournaments.add(tournament);
            }
            
            if(!tournaments.isEmpty())
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
