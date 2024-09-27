package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import config.RedisConfig;
import model.TeamVO;
import redis.clients.jedis.Jedis;

public class TeamRedisUtil {
	
	private static final String TEAM_REDIS_PREFIX = "teams:";
	
	public static List<TeamVO> getAll(){
		
		List<TeamVO> teams = new ArrayList<>();
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			Set<String> keys = jedis.keys(TEAM_REDIS_PREFIX + "*");
			for (String key : keys) {
	            String teamJSON = jedis.get(key);
	            
	            TeamVO team = new Gson().fromJson(teamJSON, TeamVO.class);
	            
	            teams.add(team);
			}
		}
		return teams;
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
	
	public static void setAllTeams(List<TeamVO> teams) {
	    try (Jedis jedis = RedisConfig.getJedis().getResource()) {
	        for (TeamVO team : teams) {
	            String teamJSON = new Gson().toJson(team);
	            
	            String redisKey = TEAM_REDIS_PREFIX + team.getTeamId();
	            
	            jedis.set(redisKey, teamJSON);
	        }
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
