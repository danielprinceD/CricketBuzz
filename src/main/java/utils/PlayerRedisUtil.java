package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import config.RedisConfig;
import model.PlayerVO;
import redis.clients.jedis.Jedis;

public class PlayerRedisUtil {
	
	private static final String PLAYER_REDIS_PREFIX = "players:";
	
	public static void setPlayerById(PlayerVO player , int playerId) {
		
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			jedis.set(PLAYER_REDIS_PREFIX + playerId  , new JSONObject(player).toString() );
			System.out.println("SET: player ID :" + playerId );
		}
	}
	
	public static void setPlayers(List<PlayerVO> players) {
		
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			if(!players.isEmpty())
			{
				for(PlayerVO player : players)
				{
					jedis.setex(PLAYER_REDIS_PREFIX + player.getId() , 3600 , new JSONObject(player).toString() );
				}
			}
			System.out.println("SET: players");
		}
	}
	
	public static PlayerVO getPlayerById(int playerId) {
		
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			String cachedData =  jedis.get(PLAYER_REDIS_PREFIX + playerId);
			if(cachedData != null)
			{				
				PlayerVO player = new Gson().fromJson(cachedData , PlayerVO.class);
				System.out.println("GET : Players by ID");
				return player;
			}
		}
		return null;
	}
	
	public static List<PlayerVO> getPlayers() {
		
		List<PlayerVO> players = new ArrayList<>();
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			Set<String> playerKeys = jedis.keys(PLAYER_REDIS_PREFIX + "*");
			
			for(String key : playerKeys)
			{
				String playerJSON = jedis.get(key);
				players.add(new Gson().fromJson( new JSONObject(playerJSON).toString() , PlayerVO.class));
			}
			System.out.println("GET : Players");
		}
		return players;
	}
	
	public static boolean isCached() {
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			Set<String> cached = jedis.keys(PLAYER_REDIS_PREFIX + "*" );
			if(cached != null && !cached.isEmpty())
				return true;
		}
		return false;
	}
	
	public static void deletePlayerById(int playerID) {
		
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			jedis.del(PLAYER_REDIS_PREFIX + playerID);
			System.out.println("DEL : PLayer ID:"+playerID );
		}
	}
	
}
