package utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import config.RedisConfig;
import model.PlayersStatsVO;
import redis.clients.jedis.Jedis;

public class StatsRedis {
	public final static String STATS_REDIS_PREFIX = "stats:"; 
	
	public static List<PlayersStatsVO> getPlayersStats(){
		try(Jedis jedis = RedisConfig.getJedis().getResource()){
			List<PlayersStatsVO> playersStatsVOs = new ArrayList<>();
			String jsonString = jedis.get(STATS_REDIS_PREFIX + "players:list");
			if(jsonString == null || jsonString.isEmpty())return playersStatsVOs;
			
			Type type =new TypeToken<List<PlayersStatsVO>>() {}.getType();
			
			playersStatsVOs = new Gson().fromJson( jsonString , type);
			return playersStatsVOs;
		}
	}
	
	public static void setPlayersStats(List<PlayersStatsVO> playersStatsVOs) {
		try(Jedis jedis = RedisConfig.getJedis().getResource()){
			String key = STATS_REDIS_PREFIX + "players:list";
			jedis.set(key, new Gson().toJson(playersStatsVOs));
		}
	}
	
	public static void deletePlayerStats() {
		try (Jedis jedis = RedisConfig.getJedis().getResource()){
			jedis.del( STATS_REDIS_PREFIX + "players:list");
		}
	}
	
}
