package utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

import config.RedisConfig;
import model.CommentaryVO;
import redis.clients.jedis.Jedis;

public class CommentaryRedisUtil {
	
	public static JSONArray getByFixtureID(int fixtureId) {
		
		JSONArray commentariesArray = new JSONArray();

	    try (Jedis jedis = RedisConfig.getJedis().getResource()) {
	        
	    	String keyPattern = "fixtures:" + fixtureId + ":commentary:*";
	        Set<String> keys = jedis.keys(keyPattern);

	        for (String key : keys) {
	            String commentaryJSON = jedis.get(key);
	            if (commentaryJSON != null) {
	                JSONObject commentaryObject = new JSONObject(commentaryJSON);
	                commentariesArray.put(commentaryObject);
	            }
	        }
	        System.out.println("GET");

	        return commentariesArray;
	    } 
	}
	
	public static JSONArray getByCommentaryID(int commentaryId) {
		
		JSONArray commentaries = new JSONArray();
		
		try (Jedis jedis = RedisConfig.getJedis().getResource()) {
			
			Set<String> keys = jedis.keys("*:commentary:" + commentaryId);
		    if (!keys.isEmpty()) {
	            for (String key : keys) {
	                String commentaryJSON = jedis.get(key);
	                if (commentaryJSON != null) {
	                	commentaries.put(new JSONObject(commentaryJSON));
	                    System.out.println("Commentary fetched by commentary ID " + commentaryId);
	                    System.out.println("GET:ID");
	                    break; 
	                }
	            }
	            
		    }
		    return commentaries;
		}
	}
	
	
	public static void setCommentaryByTourId(int fixtureId, List<CommentaryVO> newCommentaries) {

	    try (Jedis jedis = RedisConfig.getJedis().getResource()) {
	        
	        String keyPattern = "fixtures:" + fixtureId + ":commentary:*";
	        Set<String> existingKeys = jedis.keys(keyPattern);
	        
	        List<CommentaryVO> existingCommentaries = new ArrayList<>();
	        
	        for (String key : existingKeys) {
	            String existingCommentaryJSON = jedis.get(key);
	            if (existingCommentaryJSON != null) {
	                CommentaryVO existingCommentary = new Gson().fromJson(existingCommentaryJSON, CommentaryVO.class);
	                existingCommentaries.add(existingCommentary);
	            }
	        }
	        
	        existingCommentaries.addAll(newCommentaries);
	        
	        for (CommentaryVO commentary : existingCommentaries) {
	            String key = "fixtures:" + fixtureId + ":commentary:" + commentary.getCommentaryId();
	            String commentaryJSON = new Gson().toJson(commentary);
	            jedis.set(key, commentaryJSON);
	        }
	        
	        System.out.println("Commentary data has been updated for fixture ID: " + fixtureId);
	    
	    } 
	}

	public static boolean isCached() {
		
		try(Jedis jedis = RedisConfig.getJedis().getResource())
		{
			Set<String> key = jedis.keys("tour:*");
			if(!key.isEmpty() && key.size() > 0)
				return true;
		}
		
		return false;
	}
	
}
