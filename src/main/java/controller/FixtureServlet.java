package controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import repository.*;
import utils.PathMatcherUtil;
import model.*;

public class FixtureServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private static FixtureDAO fixtureDAO;
    private static CommentaryDAO commentaryDAO;
    private static OverSummaryDAO overSummaryDAO;
    private static PlayingXIDAO playingXIDAO;
    private static MatchDetailDAO matchDetailDAO;
    
    private final String FIXTURE_ID = "/([0-9]+)";
    private final String FIXTURE_ID_TEAM_ID_PLAYING_11 = "/([0-9]+)/teams/([0-9]+)/playing11s";
    private final String FIXTURE_ID_PLAYING_11 = "/([0-9]+)/playing11s";
    private final String COMMENTARY = "/([0-9]+)/commentaries";
    private final String OVER_SUMMARIES = "/([0-9]+)/over-summaries";
    private final String FIXTURE_ID_MATCH_DETAILS = "/([0-9]+)/match-details";
    
    private final Pattern FIXTURE_ID_COMPILE = Pattern.compile(FIXTURE_ID);
    private final Pattern FIXTURE_ID_TEAM_ID_PLAYING_11_COMPILE = Pattern.compile(FIXTURE_ID_TEAM_ID_PLAYING_11);
    private final Pattern COMMENTARIES_COMPILE = Pattern.compile(COMMENTARY);
    private final Pattern OVER_SUMMARIES_COMPILE = Pattern.compile(OVER_SUMMARIES);
    private final Pattern FIXTURE_ID_MATCH_DETAILS_COMPILE = Pattern.compile(FIXTURE_ID_MATCH_DETAILS);
    private final Pattern FIXTURE_ID_PLAYING_11_COMPILE = Pattern.compile(FIXTURE_ID_PLAYING_11);
    
    @Override
    public void init() {
    	fixtureDAO = new FixtureDAO();
    	commentaryDAO = new CommentaryDAO();
    	overSummaryDAO = new OverSummaryDAO();
    	playingXIDAO = new PlayingXIDAO();
    	matchDetailDAO = new MatchDetailDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
        	
			if(PathMatcherUtil.matchesPattern(pathInfo, FIXTURE_ID))
			{
				Matcher matcher = FIXTURE_ID_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					FixtureVO fixture = fixtureDAO.getFixtureById(fixtureId);
					
					if(fixture != null)
						out.print(new Gson().toJson( fixture) );
					else throw new Exception("No Data Found");
				}
				return;
			}
			
			if(PathMatcherUtil.matchesPattern(pathInfo, FIXTURE_ID_TEAM_ID_PLAYING_11))
			{
				Matcher matcher = FIXTURE_ID_TEAM_ID_PLAYING_11_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					Integer teamId = Integer.parseInt(matcher.group(2));
					
					TeamVO team = fixtureDAO.getTeamByIdTournamentId(fixtureId, teamId);
					String teamJson = new Gson().toJson(team);
					
					if(teamJson.equalsIgnoreCase("{}"))
						throw new Exception("No Data Found");
					
					out.print(teamJson);
					
				}
				return;
			}
			
			if(PathMatcherUtil.matchesPattern(pathInfo , COMMENTARY))
			{
				Matcher matcher = COMMENTARIES_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					
					List<CommentaryVO> commentaries = commentaryDAO.getCommentariesByFixtureId(fixtureId);
					if(commentaries.size() <= 0)
						throw new Exception("No Data Found");
					
					out.print(new Gson().toJson(commentaries));
				}
				return;
			}
			
			if(PathMatcherUtil.matchesPattern(pathInfo, OVER_SUMMARIES))
			{
				Matcher matcher = OVER_SUMMARIES_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					List<OverSummaryVO> overSummaries = overSummaryDAO.getOverSummariesByFixtureId(fixtureId);
					if(overSummaries.size() <= 0)
						throw new Exception("No Data Found");
					
					out.print(new Gson().toJson(overSummaries));
				}
				return;
			}
			
			Extra.sendError(response, out, "Enter Valid Path");
			
		}
        catch (Exception e) {
        	e.printStackTrace();
        	Extra.sendError(response, out, e.getMessage());
		}
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
    	PrintWriter out = response.getWriter();
    	Boolean isPut = request.getMethod().equalsIgnoreCase("PUT");
    	
		try {
			String jsonString = Extra.convertToJson(request);
			String pathInfo = request.getPathInfo();
			
			if(pathInfo == null && isPut)
			{
				java.lang.reflect.Type fixtureListType = new TypeToken<List<FixtureVO>>() {}.getType();
				List<FixtureVO> fixtureModelList = new Gson().fromJson( jsonString.toString() , fixtureListType );
				Boolean status =  fixtureDAO.addManyFixture(fixtureModelList, null, isPut);
				
				if(status)
					Extra.sendSuccess(response, out, "Fixtures Updated Successfully");
				else 
					Extra.sendError(response, out, "Data not updated");
					
				return;
			}
			
			
			
			if(PathMatcherUtil.matchesPattern(pathInfo, FIXTURE_ID_PLAYING_11))
			{
				Matcher matcher = FIXTURE_ID_PLAYING_11_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					Type listType = new TypeToken<List<PlayingXIVO>>() {}.getType();
			        List<PlayingXIVO> playing11List = new Gson().fromJson(jsonString.toString(), listType);
			        Boolean status = playingXIDAO.updatePlaying11(playing11List, fixtureId);
			        if(status)
			        	Extra.sendSuccess(response, response.getWriter(), "Updated Successfully");
			        else 
			        	Extra.sendError(response, response.getWriter(), "Not Updated");
				}
				return;
			}
			
			if(PathMatcherUtil.matchesPattern(pathInfo, FIXTURE_ID_TEAM_ID_PLAYING_11))
			{
				java.lang.reflect.Type fixtureListType = new TypeToken<List<PlayingXIVO>>() {}.getType();
				List<PlayingXIVO> fixtureModelList = new Gson().fromJson( jsonString.toString() , fixtureListType );
				
				Matcher matcher = FIXTURE_ID_TEAM_ID_PLAYING_11_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					Integer teamId = Integer.parseInt(matcher.group(2));
					
					Boolean status =  playingXIDAO.insertPlaying11(fixtureModelList, fixtureId , teamId );
					
					if(status)
						Extra.sendSuccess(response, out, "Playing 11 Inserted Successfully");
					else 
						Extra.sendError(response, out, "Data not inserted");
				}
				return;
			}
			
			if(PathMatcherUtil.matchesPattern(pathInfo, FIXTURE_ID_MATCH_DETAILS))
			{
				Matcher matcher = FIXTURE_ID_MATCH_DETAILS_COMPILE.matcher(pathInfo);
				
				if(matcher.find())
				{
					
					
					MatchDetailVO matchDetails = new Gson().fromJson( jsonString.toString() , MatchDetailVO.class);
					
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					Boolean status = matchDetailDAO.insert(matchDetails ,  fixtureId , isPut);
					if(status)
						Extra.sendSuccess(response, out, "Match Details inserted successfully");
					else Extra.sendError(response, out, "Data Failed to Insert");
				}
				return;
			}
			
			if(PathMatcherUtil.matchesPattern(pathInfo, OVER_SUMMARIES))
			{
				Matcher matcher = OVER_SUMMARIES_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					
					java.lang.reflect.Type overSummaryType = new TypeToken<List<OverSummaryVO>>() {}.getType();
					List<OverSummaryVO> overSummaryVOs = new Gson().fromJson( jsonString.toString() , overSummaryType );
					
					Boolean status = overSummaryDAO.insert( overSummaryVOs , fixtureId);
					
					if(status)
						Extra.sendSuccess(response , out , "Over Summaries created successfully.");
					else 
						Extra.sendError(response, response.getWriter(), "No records inserted.");
				}
				return;
			}
			
			if(PathMatcherUtil.matchesPattern(pathInfo, COMMENTARY))
			{
				Matcher matcher = COMMENTARIES_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					java.lang.reflect.Type commentaryType = new TypeToken<List<CommentaryVO>>() {}.getType();
					List<CommentaryVO> commentaryVOs = new Gson().fromJson( jsonString.toString() , commentaryType );
					
					Boolean status = commentaryDAO.insert(fixtureId, commentaryVOs);
					if(status)
						Extra.sendSuccess(response, out, "Commentaries inserted Successfully");
					else Extra.sendError(response, out, "Failed to add commentaries");
					
				}
				return;
			}
			
			
			
        
        Extra.sendError(response, out, "Invalid tour_id or missing path parameters.");
        
		} catch (Exception e1) {
			Extra.sendError(response, response.getWriter(), e1.getMessage());
			e1.printStackTrace();
		}
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
		
		PrintWriter out = response.getWriter();
		String pathInfo = request.getPathInfo();
    	
		try {
			if(PathMatcherUtil.matchesPattern(pathInfo, FIXTURE_ID))
			{
				Matcher matcher = FIXTURE_ID_COMPILE.matcher(pathInfo);
				if(matcher.find())
				{
					Integer fixtureId = Integer.parseInt(matcher.group(1));
					
					Boolean status = fixtureDAO.deleteFixtureById(fixtureId);
					if(status)
						Extra.sendError(response, out, "Fixture " + fixtureId  + " deleted");
					else 
						Extra.sendError(response, out, "Fixture deletion failed");
				}
				return;
			}
			
			Extra.sendError(response, out, "Invalid Path");
			
		} catch (Exception e) {
			e.printStackTrace();
			Extra.sendError(response, out, e.getMessage());
		} 
		
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response)throws IOException , ServletException {
    	doPost(request, response);
    }
    
}

