package Tournament;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import Model.Playing11Model;

import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import Team.Extra;

@WebServlet("/playing_11s")
public class Playing_11 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private void getAll(HttpServletResponse response , PrintWriter out) throws IOException 
    {

        String sql = "SELECT * FROM playing_11";
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
           

            ResultSet rs = pstmt.executeQuery();
            JSONArray playerArray = new JSONArray();

            while (rs.next()) {
                JSONObject playerJson = new JSONObject();
                playerJson.put("fixture_id", rs.getInt("fixture_id"));
                playerJson.put("player_id", rs.getInt("player_id"));
                playerJson.put("role", rs.getString("role"));
                playerJson.put("runs", rs.getInt("runs"));
                playerJson.put("balls_faced", rs.getInt("balls_faced"));
                playerJson.put("fours", rs.getInt("fours"));
                playerJson.put("sixes", rs.getInt("sixes"));
                playerJson.put("fifties", rs.getInt("fifties"));
                playerJson.put("hundreds", rs.getInt("hundreds"));
                playerJson.put("wickets_taken", rs.getInt("wickets_taken"));
                playerJson.put("team_id", rs.getInt("team_id"));
                
                playerArray.put(playerJson);
            }
            if(playerArray.length() > 0)
                out.print(playerArray.toString());
                else out.print("Data is Empty");
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    
    private void getByFixtureTeam(HttpServletResponse response , PrintWriter out , String teamId , String fixtureId) throws ServletException , IOException {

        String sql = "SELECT * FROM playing_11 WHERE team_id = ? AND fixture_id = ?";
       
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
           
            pstmt.setInt(1, Integer.parseInt(teamId) );
            pstmt.setInt(2, Integer.parseInt(fixtureId));

            ResultSet rs = pstmt.executeQuery();
            JSONArray playerArray = new JSONArray();

            while (rs.next()) {
                JSONObject playerJson = new JSONObject();
                playerJson.put("fixture_id", rs.getInt("fixture_id"));
                playerJson.put("player_id", rs.getInt("player_id"));
                playerJson.put("role", rs.getString("role"));
                playerJson.put("runs", rs.getInt("runs"));
                playerJson.put("balls_faced", rs.getInt("balls_faced"));
                playerJson.put("fours", rs.getInt("fours"));
                playerJson.put("sixes", rs.getInt("sixes"));
                playerJson.put("fifties", rs.getInt("fifties"));
                playerJson.put("hundreds", rs.getInt("hundreds"));
                playerJson.put("wickets_taken", rs.getInt("wickets_taken"));
                playerJson.put("team_id", rs.getInt("team_id"));
                
                playerArray.put(playerJson);
            }
            
            if(playerArray.length() > 0)
            out.print(playerArray.toString());
            else out.print("Data is Empty");
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
	}
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	

		response.setContentType("application/json");
		String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		
		PrintWriter out = response.getWriter();
		
		String fixtureId = request.getParameter("fixture_id");
		String teamId = request.getParameter("team_id");
		
		if(fixtureId == null && teamId == null ) {
			getAll(response , out );
		}
		else if(fixtureId != null && teamId != null && !fixtureId.isEmpty() && !teamId.isEmpty())
		{
			getByFixtureTeam(response , out , fixtureId , teamId);
		}
		else
		{
			Extra.sendError(response, out, "Missing Parameters");
		}
		
    	
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

        Type listType = new TypeToken<List<Playing11Model>>() {}.getType();
        List<Playing11Model> playing11List = new Gson().fromJson(jsonString.toString(), listType);

        String sql = "UPDATE playing_11 SET role = ?, runs = ?, balls_faced = ?, fours = ?, sixes = ?, fifties = ?, hundreds = ?, wickets_taken = ? "
                   + "WHERE fixture_id = ? AND player_id = ? AND team_id = ?";

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String fixtureId = request.getParameter("fixture_id");
            String teamId = request.getParameter("team_id");

            for (Playing11Model model : playing11List) {

                    pstmt.setString(1, model.getRole());
                    pstmt.setInt(2, model.getRuns());
                    pstmt.setInt(3, model.getBallsFaced());
                    pstmt.setInt(4, model.getFours());
                    pstmt.setInt(5, model.getSixes());
                    pstmt.setInt(6, model.getFifties());
                    pstmt.setInt(7, model.getHundreds());
                    pstmt.setInt(8, model.getWicketsTaken());
                    pstmt.setInt(9, Integer.parseInt(fixtureId));
                    pstmt.setInt(10, model.getPlayerId());
                    pstmt.setInt(11, Integer.parseInt(teamId));
                    
                    pstmt.addBatch();
               
            }

            int[] rowsAffected = pstmt.executeBatch();

            PrintWriter out = response.getWriter();
            if (rowsAffected.length > 0) {
                out.println("Player data updated successfully.");
            } else {
                out.println("Failed to update player data.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            response.getWriter().println("Invalid number format: " + e.getMessage());
        }
    }

    
    private void deleteByTeam(HttpServletResponse response , int fixtureId , int teamId) throws IOException {
    	String sql = "DELETE FROM playing_11 WHERE fixture_id = ? AND team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, teamId);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Player record deleted successfully.");
            } else {
                out.println("Failed to delete player record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    
    private void deleteByPlayer(HttpServletResponse response , int fixtureId , int teamId , int playerId) throws IOException
    {
    	String sql = "DELETE FROM playing_11 WHERE fixture_id = ? AND team_id = ? AND player_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, teamId);
            pstmt.setInt(3, playerId);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Player record deleted successfully.");
            } else {
                out.println("Failed to delete player record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
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
                deleteByPlayer(response, fixtureId, teamId, playerId);
            } else if (teamId != null && playerId == null) {
                deleteByTeam(response, fixtureId, teamId);
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

        Type listType = new TypeToken<List<Playing11Model>>() {}.getType();
        List<Playing11Model> playing11List = new Gson().fromJson(jsonString.toString(), listType);

        String sql = "INSERT INTO playing_11 (fixture_id, player_id, team_id, role ) "
                   + "VALUES (?, ?, ?, ?)";

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

        	String fixtureId = request.getParameter("fixture_id");
        	String teamId = request.getParameter("team_id");
        	
            for (Playing11Model model : playing11List) {
            	
                    pstmt.setInt(1, Integer.parseInt(fixtureId));
                    pstmt.setInt(2, model.getPlayerId());
                    pstmt.setInt(3, Integer.parseInt(teamId));
                    pstmt.setString(4, model.getRole());
                    pstmt.addBatch();
             }
            

            int[] rowsAffected = pstmt.executeBatch();

            PrintWriter out = response.getWriter();
            if (rowsAffected.length > 0) {
                out.println("Player data inserted successfully.");
            } else {
                out.println("Failed to insert player data.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    
}
