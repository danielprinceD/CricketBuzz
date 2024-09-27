package utils;

import org.json.JSONArray;
import config.RedisConfig;
import redis.clients.jedis.Jedis;

public class OverSummaryRedisUtil {
	
	
	public static JSONArray getByFixtureID(int fixtureId) {
			
			JSONArray overSummaries = new JSONArray();
	
				try (Jedis jedis = RedisConfig.getJedis().getResource()) {
					
					String key = "over_summary:" + fixtureId;
			        
			        String overSummariesJSON = jedis.get(key);
			        
			        if (overSummariesJSON != null) {
			            overSummaries = new JSONArray(overSummariesJSON);
			        }
					return overSummaries;
		    	}
		}
		
		
		public static void setOverSummaryByFixtureId(int fixtureId, JSONArray overSummaryArray) {
		    
			try (Jedis jedis = RedisConfig.getJedis().getResource()) {
		        
		        String key = "over_summary:" + fixtureId;
		        
		        String existingOverSummaryJSON = jedis.get(key);
		        JSONArray existingOverSummaryArray;
		        
		        if (existingOverSummaryJSON != null && !existingOverSummaryJSON.isEmpty()) {
		            existingOverSummaryArray = new JSONArray(existingOverSummaryJSON);
		        } else {
		            existingOverSummaryArray = new JSONArray();
		        }
		        
		        for (int i = 0; i < overSummaryArray.length(); i++) {
		            existingOverSummaryArray.put(overSummaryArray.getJSONObject(i));
		        }
		        
		        String updatedOverSummaryJSON = existingOverSummaryArray.toString();
		        
		        jedis.set(key, updatedOverSummaryJSON);
		        
		    }
		}
	
		public static boolean isCached(int fixtureId) {
			
			try(Jedis jedis = RedisConfig.getJedis().getResource())
			{
				String key = jedis.get("over_summary:" + fixtureId);
				if(key != null && !key.isEmpty()) return true;
			}
			
			return false;
		}
}
