package Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

@WebServlet("/team_player")
public class Team_Player extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        String teamIdStr = request.getParameter("team_id");

        if (teamIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"team_id parameter is required\"}");
            return;
        }

        int teamId;
        try {
            teamId = Integer.parseInt(teamIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Invalid team_id format\"}");
            return;
        }

        String query = "SELECT p.id, p.name , p.role , p.rating , p.batting_style , p.bowling_style " +
                       "FROM team_player tp " +
                       "JOIN player p ON tp.player_id = p.id " +
                       "WHERE tp.team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();

            JSONArray jsonArray = new JSONArray();

            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("player_id", rs.getInt("id"));
                jsonObject.put("name", rs.getString("name"));
                jsonObject.put("role", rs.getString("role"));
                jsonObject.put("rating", rs.getString("rating"));
                jsonObject.put("batting_style", rs.getString("batting_style"));
                jsonObject.put("bowling_style", rs.getString("bowling_style"));
                jsonArray.put(jsonObject);
            }

            out.println(jsonArray.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String playerIdStr = request.getParameter("player_id");
        String teamIdStr = request.getParameter("team_id");

        if (playerIdStr == null || teamIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("player_id and team_id parameters are required.");
            return;
        }

        int playerId;
        int teamId;

        try {
            playerId = Integer.parseInt(playerIdStr);
            teamId = Integer.parseInt(teamIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid number format for player_id or team_id.");
            return;
        }

        String query = "INSERT INTO team_player (player_id, team_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, playerId);
            pstmt.setInt(2, teamId);

            int rowsAffected = pstmt.executeUpdate();

            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Association created successfully!");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("Failed to create association.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String playerIdStr = request.getParameter("player_id");

        if (playerIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("player_id parameter is required");
            return;
        }

        int playerId = Integer.parseInt(playerIdStr);

        String query = "DELETE FROM team_player WHERE player_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, playerId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                response.getWriter().println("Association deleted successfully!");
            } else {
                response.getWriter().println("Failed to delete association or player_id not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        String p_id = request.getParameter("player_id");
        String t_id = request.getParameter("team_id");
        int playerId = Integer.parseInt(p_id);
        int newTeamId = Integer.parseInt(t_id);
        
        try {
        	
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"player_id and team_id parameters are required\"}");
            return;
        }

        String query = "UPDATE team_player SET team_id = ? WHERE player_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, newTeamId);
            pstmt.setInt(2, playerId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                out.println("{\"message\": \"Association updated successfully!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Player not found or no change in association.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}
