package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import repository.*;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.*;

public class TeamServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    TeamDAO teamDAO = new TeamDAO();
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        
        String pathInfo  = request.getPathInfo();
        
        PrintWriter out = response.getWriter();
        try {
            
        	List<TeamVO> teams = teamDAO.getTeams();
            
            String teamsJson = new Gson().toJson(teams);
            
            if(pathInfo != null)
            {
            	String pathArray[] = pathInfo.split("/");
            	if(pathArray.length == 2)
            	{
            		int teamId = Integer.parseInt(pathArray[1]);
            		TeamVO team =  teamDAO.getTeamByID(teamId);
            		if(team == null)
            		{
            			Extra.sendError(response, out, "No Result Found");
            			return;            			
            		}
            		out.print(new Gson().toJson(team));
            		return;
            	}
            	else {
            		Extra.sendError(response, out, "Enter a Valid Path");
            		return;
            	}
            }
            
            out.print(teamsJson);
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

    	
    	String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();
    	
		
		String playerId = request.getParameter("player_id");
		
		if(pathArray == null || pathArray.length == 1 || pathArray.length > 4)
		{
			Extra.sendError(response, out, "Enter a Valid Endpoint");
			return;
		}
		else if(pathArray.length == 2)
		{
			teamDAO.deleteOneTeam(response , out , pathArray[1]);
		}
		else if(playerId != null)
		{
			teamDAO.deleteOnePlayerFromTeam(response , out , pathArray[1] , playerId);
		}
		else {
			Extra.sendError(response, out, "Enter Valid Path");
		}
		
		
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        Type teamListType = new TypeToken<List<TeamVO>>() {}.getType();
        List<TeamVO> teams = new Gson().fromJson(jsonString.toString(), teamListType);
        
        for (TeamVO teamVO : teams) {
        	
        	if(teamVO == null)
        	{
        		Extra.sendError(response, out, "Not a Valid JSON Body");
        		return;
        	}
        	
            Set<Integer> playerSet = new HashSet<>();
            
            
            
            if (!teamDAO.validatePlayers(teamVO, playerSet, response, out)) return;
           
            String sql =teamDAO.prepareSqlStatement(request, teamVO, response, out);
            
            if (sql == null) return;

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            	
                conn.setAutoCommit(false);
                teamDAO.setPreparedStatementValues(pstmt, teamVO);

                int rowsAffected = pstmt.executeUpdate();
                int teamId = teamVO.getTeamId();
          
                if (teamId < 0 && request.getMethod().equalsIgnoreCase("POST")) {
                    teamId = teamDAO.getGeneratedTeamId(pstmt);
                }

                if (teamId > 0 ){
                    teamDAO.addPlayersToTeam(conn, playerSet, teamId);
                }

                if (rowsAffected > 0) {
                    conn.commit();
                    Extra.sendSuccess(response, out, "Team and players inserted/updated successfully");
                } else {
                    conn.rollback();
                    Extra.sendError(response, out, "Failed to insert/update team");
                }

            } catch (SQLException e) {
                Extra.sendError(response, out, e.getMessage());
                e.printStackTrace();
            }
        }   
    }
    @Override
    protected void doPut(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
    	doPost(request, response);
    }
}
