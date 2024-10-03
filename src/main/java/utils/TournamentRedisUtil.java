package utils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.RedisConfig;
import model.TournamentVO;
import redis.clients.jedis.Jedis;

public class TournamentRedisUtil {
	
	private static final String TOURNAMENT_REDIS_PREFIX = "tournaments:";
	
	public static void invalidateTournamentById(Integer tourId) {
		try (Jedis jedis =  RedisConfig.getJedis().getResource() ){
			System.out.println(1);
			jedis.del(TOURNAMENT_REDIS_PREFIX + tourId + ":teams:all");
		}
	}
	
	public static void invalidateAll() {
		try (Jedis jedis =  RedisConfig.getJedis().getResource() ){
			jedis.del(TOURNAMENT_REDIS_PREFIX + "all");
		}
	}
	
	public static void invalidateFixtures(Integer tourId) {
		try (Jedis jedis =  RedisConfig.getJedis().getResource() ){
			jedis.del(TOURNAMENT_REDIS_PREFIX + tourId + ":fixtures:all");
		}
	}
	
	public static void inValidateTournament(Integer tourId)
	{
		try (Jedis jedis =  RedisConfig.getJedis().getResource() ){
			jedis.del(TOURNAMENT_REDIS_PREFIX + tourId);
		}
	}
	
	private static boolean isCached() {
		
		try (Jedis jedis =  RedisConfig.getJedis().getResource() ){
			Set<String> cachedData = jedis.keys(TOURNAMENT_REDIS_PREFIX + "*");
			if(cachedData != null && !cachedData.isEmpty())
				return true; 
		}
		return false;
	}
	
    public static void setTournaments(List<TournamentVO> tournaments) {
        try (Jedis jedis = RedisConfig.getJedis().getResource() ) {
        	String json = new Gson().toJson(tournaments);
        	if(json != null)
        	{
        		jedis.set( TOURNAMENT_REDIS_PREFIX + "all"  , json);
        		System.out.println("All Data updated in redis");        		
        	}
        }
    }
    
    public static void setTournamentsById(TournamentVO tournamentVO , Integer tourId)
    {
    	try(Jedis jedis = RedisConfig.getJedis().getResource())
    	{    			
    		String key =TOURNAMENT_REDIS_PREFIX + tourId;
    		jedis.set(key  , tournamentVO.toJson());
    		System.out.println("Data is updated in redis");
    	}
    }

    public static List<TournamentVO> getTournaments() {
    	
        List<TournamentVO> tournaments = new ArrayList<>();
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            
        	String json = jedis.get(TOURNAMENT_REDIS_PREFIX + "all");
        	Type type = new TypeToken<List<TournamentVO>>() {}.getType();
        	if(json != null)
        	{
        		tournaments = new Gson().fromJson(json, type);
        		System.out.println("All Data fetched from cache");        		
        	}
        }
        return tournaments;
    }

    public static TournamentVO getTournamentById(int tourId) {
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            String tournamentJson = jedis.get(TOURNAMENT_REDIS_PREFIX + tourId);
            if (tournamentJson != null) {
            	System.out.println("Data fetched from redis by ID");
                return TournamentVO.fromJson(tournamentJson);
            }
        }
        return null;
    }
    
    
    public static void deleteTournamentById(int tourId){
    	
    	try (Jedis jedis = RedisConfig.getJedis().getResource()) {
    		if(isCached())
    		{    			
	    		String key = TOURNAMENT_REDIS_PREFIX + tourId;
	            jedis.del(key);
	            System.out.println("Data deleted from redis");
    		}
        }
    }
    
    
    public static void clearParticipatedTeamsById(int tourId) {
       
    	try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            String key = TOURNAMENT_REDIS_PREFIX + tourId;
            String cachedData = jedis.get(key);
            
            if (cachedData != null && isCached() ) {
                JSONObject tournamentJson = new JSONObject(cachedData);
                tournamentJson.put("participatedTeams", new JSONArray());
                jedis.set(key, tournamentJson.toString());
                System.out.println("Cleared participated teams for tournament in redis" );
            }
    	}
    
    }
    
    
    public static void deleteTeamFromTournament(int tourId, int teamId) {
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            String key = TOURNAMENT_REDIS_PREFIX + tourId;
            String cachedData = jedis.get(key);
            
            if (cachedData != null && isCached()) {
                JSONObject tournamentJson = new JSONObject(cachedData);
                
                JSONArray participatedTeams = tournamentJson.getJSONArray("participatedTeams");
                
                for (int i = 0; i < participatedTeams.length(); i++) {
                    JSONObject team = participatedTeams.getJSONObject(i);
                    if (team.getInt("teamId") == teamId) {
                        participatedTeams.remove(i);
                        break;
                    }
                }

                jedis.set(key, tournamentJson.toString());
                System.out.println("Deleted team with ID: " + teamId + " from tournament with ID: " + tourId + " from redis");
            }
        }
    }
	
}
