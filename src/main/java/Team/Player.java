package Team;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import java.sql.*;

@WebServlet("/player")
public class Player extends HttpServlet {
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
	private static final String USER = "root";
	private static final String PASS = "";

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		
        PrintWriter out = response.getWriter();
        String playerId = request.getParameter("player_id");
        if (playerId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"player_id parameter is required\"}");
            return;
        }

        String query = "SELECT * FROM player WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Integer.parseInt(playerId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
            	JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", rs.getInt("id"));
                jsonObject.put("name", rs.getString("name"));
                jsonObject.put("role", rs.getString("role"));
                jsonObject.put("address", rs.getString("address"));
                jsonObject.put("gender", rs.getString("gender"));
                jsonObject.put("rating", rs.getInt("rating"));
                jsonObject.put("batting_style", rs.getString("batting_style"));
                jsonObject.put("bowling_style", rs.getString("bowling_style"));
                out.print(jsonObject.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Player not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error fetching data\"}");
        }
	}


    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
        String name = request.getParameter("name");
        String role = request.getParameter("role");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");
        String ratingStr = request.getParameter("rating");
        String battingStyle = request.getParameter("batting_style");
        String bowlingStyle = request.getParameter("bowling_style");

        int rating = 0;
        if (ratingStr != null && !ratingStr.isEmpty()) {
            try {
                rating = Integer.parseInt(ratingStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO player (name, role, address, gender, rating, batting_style, bowling_style) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, role);
                pstmt.setString(3, address);
                pstmt.setString(4, gender);
                pstmt.setInt(5, rating);
                pstmt.setString(6, battingStyle);
                pstmt.setString(7, bowlingStyle);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    response.getWriter().println("Player added successfully!");
                } else {
                    response.getWriter().println("Failed to add player.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String playerId = request.getParameter("player_id");

        if (playerId == null || playerId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("player_id parameter is required.");
            return;
        }

        String sql = "DELETE FROM player WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(playerId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                out.println("Player deleted successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("No player found with the provided ID.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid player_id format.");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String playerId = request.getParameter("player_id");
        String name = request.getParameter("name");
        String role = request.getParameter("role");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");
        String ratingStr = request.getParameter("rating");
        String battingStyle = request.getParameter("batting_style");
        String bowlingStyle = request.getParameter("bowling_style");

        if (playerId == null || playerId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("player_id parameter is required.");
            return;
        }

        if (name == null || role == null || address == null || gender == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Required fields are missing.");
            return;
        }

        int rating = 0;
        if (ratingStr != null && !ratingStr.isEmpty()) {
            try {
                rating = Integer.parseInt(ratingStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Invalid rating format.");
                return;
            }
        }

        String sql = "UPDATE player SET name = ?, role = ?, address = ?, gender = ?, rating = ?, "
                   + "batting_style = ?, bowling_style = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, role);
            pstmt.setString(3, address);
            pstmt.setString(4, gender);
            pstmt.setInt(5, rating);
            pstmt.setString(6, battingStyle);
            pstmt.setString(7, bowlingStyle);
            pstmt.setInt(8, Integer.parseInt(playerId));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("Player updated successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("No player found with the provided ID.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid player_id format.");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
