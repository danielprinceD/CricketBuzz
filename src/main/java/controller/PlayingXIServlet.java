package controller;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import repository.*;
import model.*;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PlayingXIServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    PlayingXIDAO playingXIDAO;
    
    @Override
    public void init() {
    	playingXIDAO = new PlayingXIDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	

		response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
		
		 playingXIDAO.getAll(request , response , out );
		
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        Type listType = new TypeToken<List<PlayingXIVO>>() {}.getType();
        List<PlayingXIVO> playing11List = new Gson().fromJson(jsonString.toString(), listType);
        
        String fixtureId = request.getParameter("fixture_id");
        String teamId = request.getParameter("team_id");
        
        
        try {
        	
        	playingXIDAO.updatePlaying11(playing11List, fixtureId, teamId);
        	Extra.sendSuccess(response, response.getWriter(), "Updated Successfully");
        	
        }catch (Exception e) {
        	Extra.sendError(response, response.getWriter(), e.getMessage());
		}
        

    }

    
    
    
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fixtureIdParam = request.getParameter("fixture_id");
        String playerIdParam = request.getParameter("player_id");
        String teamIdParam = request.getParameter("team_id");

        Integer fixtureId = null;
        Integer playerId = null;
        Integer teamId = null;

        try {
            if (fixtureIdParam != null) {
                fixtureId = Integer.parseInt(fixtureIdParam);
            }
            if (playerIdParam != null) {
                playerId = Integer.parseInt(playerIdParam);
            }
            if (teamIdParam != null) {
                teamId = Integer.parseInt(teamIdParam);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid parameter format: " + e.getMessage());
            return;
        }

        if (fixtureId != null) {
            if (playerId != null && teamId != null) {
                playingXIDAO.deleteByPlayer(response, fixtureId, teamId, playerId);
            } else if (teamId != null && playerId == null) {
                playingXIDAO.deleteByTeam(response, fixtureId, teamId);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Missing parameters for deletion.");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("fixture_id is required.");
        }
    }
    
   
    
  
    
    
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        
        Type listType = new TypeToken<List<PlayingXIVO>>() {}.getType();
        List<PlayingXIVO> playing11List = new Gson().fromJson(jsonString.toString(), listType);
        
        
        
        
        if(playing11List.size() > 11)
        {
        	Extra.sendError(response, response.getWriter() , "Playing X1 should not be more than 11 players");
        	return;
        }
        else if(playing11List.size() < 11)
        {
        	Extra.sendError(response, response.getWriter() , "Playing X1 should not be less than 11 players");
        	return;
        }
        
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String fixtureId = request.getParameter("fixtureId");
        String teamId = request.getParameter("teamId");

        try {
            if (fixtureId == null || teamId == null) {
                throw new SQLException("Both Fixture ID and Team ID are required");
            }

            int fixtureIdInt = Integer.parseInt(fixtureId);
            int teamIdInt = Integer.parseInt(teamId);

            playingXIDAO.insertPlaying11(playing11List, fixtureIdInt, teamIdInt);

            PrintWriter out = response.getWriter();
            out.println("{\"message\": \"Player data inserted successfully.\"}");

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            response.getWriter().println("{\"error\": \"Invalid number format: " + e.getMessage() + "\"}");
        }
    }

    
}
