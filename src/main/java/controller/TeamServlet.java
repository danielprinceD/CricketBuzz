package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import repository.*;
import utils.PathMatcherUtil;
import utils.TeamRedisUtil;

import java.lang.reflect.Type;
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
import model.*;
import redis.clients.jedis.Jedis;

public class TeamServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    	
    private TeamDAO teamDAO;
	private final String TEAM_ID = "/([0-9]+)";
	private final java.util.regex.Pattern TEAM_ID_COMPILE = Pattern.compile(TEAM_ID);
	
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
    
    
}
