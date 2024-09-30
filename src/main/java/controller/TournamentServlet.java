package controller;

import java.io.BufferedReader;
import com.google.gson.reflect.TypeToken;

import jakarta.ws.rs.core.NewCookie;
import repository.*;
import utils.PathMatcherUtil;
import utils.TournamentRedisUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.PathMatcher;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private TournamentDAO tournamentDAO;
    private FixtureDAO fixtureDAO;
    
    private final String TOURNAMENT_ID = "/([0-9]+)";
    private final String TOURNAMENT_ID_TEAMS = "/([0-9]+)/teams";
    private final String TOURNAMENT_ID_FIXTURES = "/([0-9]+)/fixtures";
    
    private final Pattern TournamentCompile = Pattern.compile(TOURNAMENT_ID);
    private final Pattern TournamentIdTeams = Pattern.compile(TOURNAMENT_ID_TEAMS);
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
            out.print("{ \"error\": \"" + e.getMessage() + "\" }");
        }
        catch (Exception e) {
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
	        
	        Boolean status = tournamentDAO.insertOrUpdateData(tournamentsVO, isPut);
	        
	        if(status)
	        	Extra.sendSuccess(response, out, "Team and players inserted/updated successfully");
	        else 
	        	Extra.sendError(response, out, "Failed to insert/update team");
		        
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
        		
        			Boolean status = fixtureDAO.addManyFixture( fixtureModelList , tournamentId , request.getMethod());
				
        		
        		if(status)
        			Extra.sendSuccess(response, out,  "Fixtures added/updated successfully.");
        		else  Extra.sendError(response, out, "No fixtures were added/updated.");
        		
        	}
        }
        
        } catch (Exception e) {
        	
        	e.printStackTrace();
        	Extra.sendError(response, out, e.getMessage());
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

        String pathInfoString = request.getPathInfo();
        String[] pathArray = pathInfoString != null ? pathInfoString.split("/") : null;
        PrintWriter out = response.getWriter();

        if (pathArray == null || pathArray.length <= 1) {
            Extra.sendError(response, out, "Tournament ID is required");
            return;
        }

        String tourId = pathArray[1];

        if (pathArray.length == 2) {
           tournamentDAO.deleteTournament(response, out, tourId);
        }
        else if (pathArray.length == 3 && pathArray[2].equals("teams") ) {
        	tournamentDAO.deleteAllTeamFromTour(response, out, tourId);
        }
        else if (pathArray.length == 4 && pathArray[2].equals("teams") ) {
           
        	tournamentDAO.deleteTeamFromTour(response, out, tourId, pathArray[3]);
            
        }else {
            Extra.sendError(response, out, "Invalid request path");
            return;
        }
        
        
    }
    
}

