package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import repository.*;
import utils.PathMatcherUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import model.*;
import java.sql.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlayerServlet extends HttpServlet {
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
	private static final String USER = "root";
	private static final String PASS = "";
	PlayerDAO playerDAO = new PlayerDAO();
	
	private final String PLAYER_ID = "/([0-9]+)";
	private final Pattern PLAYER_ID_COMPILE = Pattern.compile(PLAYER_ID);

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
		response.setContentType("application/json");
	    PrintWriter out = response.getWriter();
	    String pathinfo = request.getPathInfo();
	    
	    	
	    	
	    	try {
	    		
	    		
	    		if(pathinfo != null && PathMatcherUtil.matchesPattern(pathinfo, PLAYER_ID))
	    		{
	    			Matcher matcher = PLAYER_ID_COMPILE.matcher(pathinfo);
	    			if(matcher.find())
	    			{
	    				Integer playerId = Integer.parseInt(matcher.group(1));
	    				PlayerVO player =  playerDAO.getPlayerById(playerId) ;
	    				if(player == null)
	    				{
	    					Extra.sendError(response, out, "No Player Found");
	    				}
	    				else {
	    					
	    				String playerJson = new Gson().toJson(player);
	    				out.print(playerJson);
	    				}
	    				
	    			}
	    			return;
	    		}
	    		
				
				
			} catch (NumberFormatException | SQLException e) {
				out.print(e.getMessage());
				e.printStackTrace();
			}
	    
	    Extra.sendError(response, out, "Enter a valid Path");
	    
	}

	


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	        throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		String pathInfo = request.getPathInfo();
		if(pathInfo == null)
		{
			
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
			
	    String jsonString = Extra.convertToJson(request);

	    java.lang.reflect.Type listType = new TypeToken<List<PlayerVO>>() {}.getType();
	    List<PlayerVO> playerList = new Gson().fromJson(jsonString.toString(), listType);

	    if (playerList == null || playerList.isEmpty()) {
	        Extra.sendError(response, out, "No player data provided.");
	        return;
	    }

	    PlayerDAO playerDAO = new PlayerDAO();
	    
	    boolean isPut = request.getMethod().equalsIgnoreCase("PUT");

	        conn.setAutoCommit(false);

	        for (PlayerVO player : playerList) {
	            if (player.isValid()) {
	                boolean success;
	                if (isPut) {
	                	
	                	
	                	success = playerDAO.updatePlayer(player);
	                } else {
	                	success = playerDAO.insertPlayer(player);
	                }

	                if (!success) {
	                    conn.rollback();
	                    Extra.sendError(response, out, "Failed to process player data.");
	                    return;
	                }
	            } else {
	                conn.rollback();
	                Extra.sendError(response, out, "Invalid player data.");
	                return;
	            }
	        }

	        conn.commit();
	        Extra.sendSuccess(response, out, "Players processed successfully.");
	        
	    } catch (Exception e) {
	        Extra.sendError(response, out, e.getMessage());
	        e.printStackTrace();
	    }
		return;
		}
		
		Extra.sendError(response, out, "No URL Found");
	    
	}


	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
	        throws ServletException, IOException {

	    response.setContentType("text/plain");
	    PrintWriter out = response.getWriter();
	    
	    String pathInfoString = request.getPathInfo();		
	    String[] pathArray = pathInfoString != null ? pathInfoString.split("/") : null;
	    
	    if (pathArray == null || pathArray.length < 2) {
	        Extra.sendError(response, out, "No ID is mentioned");
	        return;
	    }
	    
	    Integer playerId;
	    try {
	        playerId = Integer.parseInt(pathArray[1]);
	    } catch (NumberFormatException e) {
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        Extra.sendError(response, out, "Invalid player ID format");
	        return;
	    }
	    
	    PlayerDAO playerDAO = new PlayerDAO();
	    
	    try {
	        playerDAO.deletePlayer(playerId);
	        
	         Extra.sendSuccess(response, out, "Deleted Successfully");
	        
	    } catch (SQLException e) {
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        Extra.sendError(response, out, "Database error: " + e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
	        Extra.sendError(response, out, e.getMessage());
	    }
	}

    
    @Override 
    protected void doPut(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException
    {
    	doPost(request, response);
    }

}
