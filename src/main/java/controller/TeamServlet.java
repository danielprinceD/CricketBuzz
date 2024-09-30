package controller;

import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import repository.*;
import utils.PathMatcherUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.*;

public class TeamServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    	
    private TeamDAO teamDAO;
	private final String TEAM_ID = "/([0-9]+)";
	private final String TEAM_ID_PLAYERS = "/([0-9]+)/players";
	private final String TEAM_ID_PLAYERS_ID = "/([0-9]+)/players/([0-9]+)";
	
	
	private final java.util.regex.Pattern TEAM_ID_COMPILE = Pattern.compile(TEAM_ID);
	private final Pattern TEAM_ID_PLAYERS_COMPILE = Pattern.compile(TEAM_ID_PLAYERS);
	private final Pattern TEAM_ID_PLAYERS_ID_COMPILE = Pattern.compile(TEAM_ID_PLAYERS_ID);
	
	
	@Override
	public void init() {
		teamDAO = new TeamDAO();
	}
 	
    @Override 
    protected void doGet(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
    	response.setContentType("application/json");
    	PrintWriter out = response.getWriter();
    	String pathInfo = request.getPathInfo();
    	
    	try {
			
    		if(PathMatcherUtil.matchesPattern(pathInfo, TEAM_ID))
    		{
    			Matcher matcher = TEAM_ID_COMPILE.matcher(pathInfo);
    			if(matcher.find())
    			{
    				Integer teamId = Integer.parseInt(matcher.group(1));
    				TeamVO team = teamDAO.getTeamById(teamId);
    				if(team != null)
    					out.print(new Gson().toJson(team) );
    				else 
    					out.print("No data found");
    			}
    			return;
    		}
    		
    		Extra.sendError(response, out, "Enter valid Path");
    		
		} catch (Exception e) {
			e.printStackTrace();
			Extra.sendError(response, out, e.getMessage());
		}	
    }
    
    @Override 
    protected void doPost(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
    	
    	response.setContentType("application/json");
    	PrintWriter out = response.getWriter();
    	String pathInfo = request.getPathInfo();
    	
    	try {
			
    		
    		String body = Extra.convertToJson(request);
    		
    		if(pathInfo == null)
    		{
    			Boolean isPut = request.getMethod().equalsIgnoreCase("PUT");
    			Boolean status =  teamDAO.addTeamAndPlayers(body , isPut);
    			if(status)
    			{
    				if(isPut)
    					Extra.sendSuccess(response, out, "Data Updated Successfully");
    				else
    					Extra.sendSuccess(response, out, "Data Inserted Successfully");
    				
    				
    			}
    			else {
    				if(isPut)
        				Extra.sendError(response, out, "Data Updation failed");
    				else 
        				Extra.sendError(response, out, "Data insertion failed");
    			}
    			return;
    		}
    		
    		
    		Extra.sendError(response, out, "Enter valid Path");
    		
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage =  Extra.ForeignKeyError(e.getMessage());
			Extra.sendError(response, out, errorMessage);
		}
    	
    	
    }
    
    @Override
    protected void doPut(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException {
    	doPost(request, response);
    }
    
    @Override
    protected void doDelete(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException
    {
    	PrintWriter out = response.getWriter();
    	String pathInfo = request.getPathInfo();
    	try {
    		
    		if(PathMatcherUtil.matchesPattern(pathInfo, TEAM_ID))
    		{
    			Matcher matcher = TEAM_ID_COMPILE.matcher(pathInfo);
    			if(matcher.find())
    			{
    				Integer teamId = Integer.parseInt(matcher.group(1));
    				Boolean status = teamDAO.deleteTeamById(teamId);
    				
    				if(status)
    					Extra.sendSuccess(response, out, "Team Deleted");
    				else 
    					Extra.sendError(response, out, "Deletion Failed");
    			}
    			return;
    		}
    		
    		
    		if(PathMatcherUtil.matchesPattern(pathInfo, TEAM_ID_PLAYERS))
    		{
    			Matcher matcher = TEAM_ID_PLAYERS_COMPILE.matcher(pathInfo);
    			if(matcher.find())
    			{
    				Integer teamId = Integer.parseInt(matcher.group(1));
    				Boolean status = teamDAO.deleteTeamPlayersByTeamId(teamId);
    				
    				if(status)
    					Extra.sendSuccess(response, out, "Team Players Deleted");
    				else 
    					Extra.sendError(response, out, "Team Players Deletion Failed");
    			}
    			return;
    			
    		}
    		
    		
    		if(PathMatcherUtil.matchesPattern(pathInfo, TEAM_ID_PLAYERS_ID))
    		{
    			Matcher matcher = TEAM_ID_PLAYERS_ID_COMPILE.matcher(pathInfo);
    			if(matcher.find())
    			{
    				Integer teamId = Integer.parseInt(matcher.group(1));
    				Integer playerId = Integer.parseInt(matcher.group(2));
    				
    				Boolean status = teamDAO.deleteTeamPlayerByPlayerIdTeamId(teamId, playerId);
    				
    				if(status)
    					Extra.sendSuccess(response, out, "Team Player with ID " + playerId + " is Deleted");
    				else 
    					Extra.sendError(response, out, "Team Player Deletion Failed");
    				
    			}
    			return;
    		}
    		
    		
    		Extra.sendError(response, out, "Enter a Valid Path");
    	}catch (Exception e) {
    		e.printStackTrace();
    		Extra.sendError(response, out, e.getMessage());
		}
    	
    }

    
    
    
}
