package utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.RedisConfig;
import model.TeamVO;
import model.TournamentTeamVO;
import redis.clients.jedis.Jedis;

public class TeamRedisUtil {
	
	private static final String TEAM_REDIS_PREFIX = "teams:";
	
	public static List<TournamentTeamVO> getAll(Integer tourId){
		
		List<TournamentTeamVO> teams = new ArrayList<>();
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			String json = jedis.get( "tournaments:" + tourId + ":" + TEAM_REDIS_PREFIX + "all");
			if(json != null)
			{
				Type type = new TypeToken<List<TournamentTeamVO>> () {}.getType();
				teams  = new Gson().fromJson(json, type);
			}
		}
		return teams;
	}
	
	public static void inValidateTeam(Integer teamId) {
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			jedis.del("teams:" + teamId);
		}
	}
	
	
	
	public static TeamVO getOne(int teamId)
	{
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			String teamJSON = jedis.get(TEAM_REDIS_PREFIX + teamId);
			if(teamJSON != null)
				return new Gson().fromJson(teamJSON, TeamVO.class);
		}
		return null;
	}
	
	public static void setAllTeams(List<TournamentTeamVO> teams , Integer tourId) {
	    try (Jedis jedis = RedisConfig.getJedis().getResource()) {
	        
	    	String json = new Gson().toJson(teams);
	    	jedis.set( "tournaments:" + tourId + ":" + TEAM_REDIS_PREFIX + "all" , json );
	        System.out.println("All teams saved to Redis.");
	    } 
	}
	
	
	public static TeamVO getTeamDetails(Integer fixtureId , Integer teamId){
		
		TeamVO teams = null;
			try(Jedis jedis = RedisConfig.getJedis().getResource())
			{
				String json = jedis.get( "fixtures:" + fixtureId + ":" + TEAM_REDIS_PREFIX + teamId + ":playing11s:all");
				if(json != null)
				{
					teams  = new Gson().fromJson(json, TeamVO.class);
				}
				return teams;
			}
		}
	
	public static void setFixtureDetails( TeamVO teams , Integer fixtureId , Integer teamId) {
	    try (Jedis jedis = RedisConfig.getJedis().getResource()) {
	        
	    	String json = new Gson().toJson(teams);
	    	jedis.set(  "fixtures:" + fixtureId + ":" + TEAM_REDIS_PREFIX + teamId + ":playing11s:all" , json);
	        System.out.println("All teams saved to Redis.");
	    } 
	}
	
	public static void setTeamById(TeamVO team , int teamID) {
	    try (Jedis jedis = RedisConfig.getJedis().getResource()) {
	        String teamJSON = new Gson().toJson(team);
	        String redisKey = TEAM_REDIS_PREFIX + teamID;
	        jedis.set(redisKey, teamJSON);
	        System.out.println("Team data for teamId " + team.getTeamId() + " saved to Redis.");
	    }
	}
	
	public static boolean isCached() {
    	try(Jedis jedis = RedisConfig.getJedis().getResource() ) {
			
    		Set<String> cached = jedis.keys(TEAM_REDIS_PREFIX + "*");
    		if(cached != null && !cached.isEmpty())
    			return true;
    		
    		return false;
		}
    }
	
	public static void deleteOne(int teamID) {
		
		try(Jedis jedis = RedisConfig.getJedis().getResource() ) {
					
		    String cached = jedis.get(TEAM_REDIS_PREFIX + teamID);
		    if(cached != null && !cached.isEmpty())
		    	jedis.del(TEAM_REDIS_PREFIX + teamID);
		}
	}

	
}
