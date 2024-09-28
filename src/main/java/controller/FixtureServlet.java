package controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.sql.SQLException;
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
    
    private final String FIXTURE_ID = "/([0-9]+)";
    private final String FIXTURE_ID_TEAM_ID_PLAYING_11 = "/([0-9]+)/teams/([0-9]+)/playing11s";
    private final String COMMENTARY = "/([0-9]+)/commentaries";
    
    private final Pattern FIXTURE_ID_COMPILE = Pattern.compile(FIXTURE_ID);
    private final Pattern FIXTURE_ID_TEAM_ID_PLAYING_11_COMPILE = Pattern.compile(FIXTURE_ID_TEAM_ID_PLAYING_11);
    private final Pattern COMMENTARIES_COMPILE = Pattern.compile(COMMENTARY);
    
    @Override
    public void init() {
    	fixtureDAO = new FixtureDAO();
    	commentaryDAO = new CommentaryDAO();
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

    	
    	
    	StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while((line = reader.readLine()) != null)
        		jsonString.append(line);
        
        PrintWriter out = response.getWriter();
        java.lang.reflect.Type fixtureListType = new TypeToken<List<FixtureVO>>() {}.getType();
        List<FixtureVO> fixtureModelList = new Gson().fromJson( jsonString.toString() , fixtureListType );
        
        String pathInfoString = request.getPathInfo();		
        String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
        
        if ((pathArray == null || pathArray.length <= 1)) {
             try {
				fixtureDAO.addManyFixture(response, out, fixtureModelList, request.getParameter("tourId"), request.getMethod());
			} catch (ServletException | SQLException e) {
				
				e.printStackTrace();
			}
        } else {
            Extra.sendError(response, out, "Invalid tour_id or missing path parameters.");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
		
		PrintWriter out = response.getWriter();
    	
		try {
			
			fixtureDAO.deleteAllFixture(request ,response , out);
		} catch (Exception e) {
			Extra.sendError(response, out, e.getMessage());
		} 
		
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response)throws IOException , ServletException {
    	doPost(request, response);
    }
    
}

