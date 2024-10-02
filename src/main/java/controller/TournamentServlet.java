package controller;

import com.google.gson.reflect.TypeToken;
import repository.*;
import utils.PathMatcherUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import model.*;

public class TournamentServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    private TournamentDAO tournamentDAO;
    private FixtureDAO fixtureDAO;
    
    private final String TOURNAMENT_ID = "/([0-9]+)";
    private final String TOURNAMENT_ID_TEAMS = "/([0-9]+)/teams";
    private final String TOURNAMENT_ID_TEAMS_ID = "/([0-9]+)/teams/([0-9]+)";
    private final String TOURNAMENT_ID_FIXTURES = "/([0-9]+)/fixtures";
    
    
    private final Pattern TournamentCompile = Pattern.compile(TOURNAMENT_ID);
    private final Pattern TournamentIdTeams = Pattern.compile(TOURNAMENT_ID_TEAMS);
    private final Pattern TournamentIdTeamId = Pattern.compile(TOURNAMENT_ID_TEAMS_ID);
    private final Pattern TournamentIdFixtures = Pattern.compile(TOURNAMENT_ID_FIXTURES);
    
    @Override
    public void init() {
    	tournamentDAO = new TournamentDAO();
    	fixtureDAO = new FixtureDAO();
    }
    
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null){
            	
                List<TournamentVO> tournaments  = tournamentDAO.getAllTournaments();
                
                out.print(new Gson().toJson(tournaments));
                return;
            } 
            
            
            
            if(PathMatcherUtil.matchesPattern(pathInfo, TOURNAMENT_ID))
            {
            	Matcher matcher = TournamentCompile.matcher(pathInfo);
            	if(matcher.find())
            	{
            		Integer tourId = Integer.parseInt(matcher.group(1));
            		TournamentVO tournament = tournamentDAO.getTournamentById(tourId);
            		if (tournament != null) {
                        out.print(new Gson().toJson(tournament));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{ \"error\": \"Tournament not found\" }");
                    }
            	}
            	return;
            }
            
            
            if(PathMatcherUtil.matchesPattern(pathInfo, TOURNAMENT_ID_TEAMS))
            {
            	Matcher matcher = TournamentIdTeams.matcher(pathInfo);
            	if(matcher.find())
            	{
            		Integer tourId = Integer.parseInt(matcher.group(1));
            		
            		List<TournamentTeamVO> tournaments = tournamentDAO.getTeamsByTournamentId(tourId);
            		if(tournaments.size() > 0)
            			out.print(new Gson().toJson(tournaments));
            		else out.print("No data found");
            		return;
            	}
            }
             
            if(PathMatcherUtil.matchesPattern(pathInfo, TOURNAMENT_ID_FIXTURES))
            {
            	Matcher matcher = TournamentIdFixtures.matcher(pathInfo);
            	if(matcher.find())
            	{
            		Integer tourId = Integer.parseInt(matcher.group(1));
            		FixtureDAO fixturDao = new FixtureDAO();
            		List<FixtureVO> fixtures = fixturDao.getFixtureByTournamentId(tourId);
            		
            		if(fixtures.size() > 0)
            			out.print(new Gson().toJson(fixtures));
            		else throw new Exception("No Data Found");
            		
            	}
            	return;
            }
            
            Extra.sendError(response, out, "Enter valid path");
            return;
                
            
        } catch (NumberFormatException | SQLException e ) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            out.print("{ \"error\": \"" + e.getMessage() + "\" }");
        }
        catch (Exception e) {
        	e.printStackTrace();
        	Extra.sendError(response, out, e.getMessage());
		}
    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	Boolean isPut = request.getMethod().equalsIgnoreCase("PUT");
    	PrintWriter out = response.getWriter();
    	
    	String pathInfo = request.getPathInfo();
        
		
        try {
			
        
		if(pathInfo == null)
		{

			String jsonString = Extra.convertToJson(request);
			TypeToken<List<TournamentVO>> token = new TypeToken<List<TournamentVO>>() {};
	        List<TournamentVO> tournamentsVO = new Gson().fromJson(jsonString, token.getType());
	        
	        Boolean status = tournamentDAO.insertOrUpdateData( request ,tournamentsVO, isPut);
	        
	        if(status)
	        {
	        	if(isPut)
	        		Extra.sendSuccess(response, out, "Team and players updated successfully");
	        	else 
	        		Extra.sendSuccess(response, out, "Team and players inserted successfully");
	        }
	        else 
	        {
	        	if(isPut)
	        		Extra.sendError(response, out, "Failed to update team");
	        	else 
	        		Extra.sendError(response, out, "Failed to insert team");
	        }
	        	
		        
			return;
		}
		
		
		if(PathMatcherUtil.matchesPattern(pathInfo, TOURNAMENT_ID_FIXTURES))
        {
        	Matcher matcher = TournamentIdFixtures.matcher(pathInfo);
        	if(matcher.find())
        	{
        		String jsonString = Extra.convertToJson(request);
        		
        		java.lang.reflect.Type fixtureListType = new TypeToken<List<FixtureVO>>() {}.getType();
                List<FixtureVO> fixtureModelList = new Gson().fromJson( jsonString.toString() , fixtureListType );
                
        		Integer tournamentId = Integer.parseInt(matcher.group(1));
        		
        			Boolean status = fixtureDAO.addManyFixture(request , fixtureModelList , tournamentId , false);
				
        		
        		if(status)
        			Extra.sendSuccess(response, out,  "Fixtures added/updated successfully.");
        		else  Extra.sendError(response, out, "No fixtures were added/updated.");
        		
        	}
        }
        
        } catch (Exception e) {
        	
        	e.printStackTrace();
        	Extra.sendError(response, out, Extra.ForeignKeyError(e.getMessage()));
		}
        	
           
    }


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       doPost(request, response);
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        
        PrintWriter out = response.getWriter();

        if (pathInfo == null) {
            Extra.sendError(response, out, "Tournament ID is required");
            return;
        }
        
	       try {
			
	    	   if(PathMatcherUtil.matchesPattern(pathInfo, TOURNAMENT_ID))
	           {
	           	Matcher matcher = TournamentCompile.matcher(pathInfo);
		           	if(matcher.find())
		           	{
		           		Integer tourId = Integer.parseInt(matcher.group(1));
		           		Boolean status = tournamentDAO.deleteTournament(request, out, tourId);
		           		if(status)
		           			Extra.sendSuccess(response, out, "Tournament deleted successfully");
		           		else Extra.sendError(response, out, "Tournament deletion failed");
		           		
		           	}
		           	return;
	           }
	    	   
	    	   if(PathMatcherUtil.matchesPattern(pathInfo, TOURNAMENT_ID_TEAMS))
	           {
	           	Matcher matcher = TournamentIdTeams.matcher(pathInfo);
		           	if(matcher.find())
		           	{
		           		Integer tourId = Integer.parseInt(matcher.group(1));
		           		Boolean status = tournamentDAO.deleteAllTeamFromTour( request ,tourId);
		           		if(status)
		           			Extra.sendSuccess(response, out, "All teams deleted from the tournament successfully");
		           		else Extra.sendError(response, out, "No teams found for the provided tournament ID");
		           		
		           	}
		           	return;
	           }
	    	   
	    	   if(PathMatcherUtil.matchesPattern(pathInfo, TOURNAMENT_ID_TEAMS_ID))
	           {
	           	Matcher matcher = TournamentIdTeamId.matcher(pathInfo);
		           	if(matcher.find())
		           	{
		           		Integer tourId = Integer.parseInt(matcher.group(1));
		           		Integer teamId = Integer.parseInt(matcher.group(2));
		           		Boolean status = tournamentDAO.deleteTeamFromTour( request,tourId, teamId );
		           		if(status)
		           			Extra.sendSuccess(response, out, "Team ID "+ teamId +" deleted from the tournament successfully");
		           		else Extra.sendError(response, out, "No team found for the provided tournament ID");
		           		
		           	}
		           	return;
	           }
	    	   
	    	   if(PathMatcherUtil.matchesPattern(pathInfo, TOURNAMENT_ID_FIXTURES))
	           {
		           	Matcher matcher = TournamentIdFixtures.matcher(pathInfo);
		           	if(matcher.find())
		           	{
		           		Integer tourId = Integer.parseInt(matcher.group(1));
		           		
		           		Boolean status = fixtureDAO.deleteAllFixture( request , tourId );
		           		if(status)
		                    Extra.sendSuccess(response, out, "Fixtures Deleted Successfully");
		           		else Extra.sendError(response, out, "No Data Found for the provided parameters");
		           		
		           	}
		           	return;
	           }
	    	   
	    	   
	    	   
	    	   
	    	   
	    	   Extra.sendError(response, out, "Enter a Valid Path");
	    	   
		} catch (Exception e) {
			
			e.printStackTrace();
			Extra.sendError(response, out, Extra.ForeignKeyError(e.getMessage()));
			
		}
        
    }
    
}

