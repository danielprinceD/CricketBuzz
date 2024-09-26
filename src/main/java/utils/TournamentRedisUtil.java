package utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import config.RedisConfig;
import model.TournamentVO;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class TournamentRedisUtil {
	
	private static final String TOURNAMENT_REDIS_PREFIX = "tournament:";
	
    public static void setTournaments(List<TournamentVO> tournaments) {
        try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            for (TournamentVO tournament : tournaments) {
                jedis.set( TOURNAMENT_REDIS_PREFIX + tournament.getTourId() , tournament.toJson() );   
            }
            System.out.println("All Data updated in redis");
        }
    }
    
    public static void setTournamentsById(TournamentVO tournamentVO , int tourId)
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
            Set<String> keys = jedis.keys(TOURNAMENT_REDIS_PREFIX + "*");
            for (String key : keys) {
            	String tournamentJson = jedis.get(key);
                TournamentVO tournament = TournamentVO.fromJson(tournamentJson);
                tournaments.add(tournament);
            }
            System.out.println("All Data fetched from cache");
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
            String key = TOURNAMENT_REDIS_PREFIX + tourId;
            jedis.del(key);
            System.out.println("Data deleted from redis");
        }
    }
    
    
    public static void clearParticipatedTeamsById(int tourId) {
       
    	try (Jedis jedis = RedisConfig.getJedis().getResource()) {
            String key = TOURNAMENT_REDIS_PREFIX + tourId;
            String cachedData = jedis.get(key);
            
            if (cachedData != null) {
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
            
            if (cachedData != null) {
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
