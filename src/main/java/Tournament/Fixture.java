package Tournament;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/fixture")
public class Fixture extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int tourId = Integer.parseInt(request.getParameter("tour_id"));
        int team1Id = Integer.parseInt(request.getParameter("team1_id"));
        int team2Id = Integer.parseInt(request.getParameter("team2_id"));
        int venueId = Integer.parseInt(request.getParameter("venue_id"));
        String matchDate = request.getParameter("match_date");

        String sql = "INSERT INTO fixture (tour_id, team1_id, team2_id, venue_id, match_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tourId);
            pstmt.setInt(2, team1Id);
            pstmt.setInt(3, team2Id);
            pstmt.setInt(4, venueId);
            pstmt.setString(5, matchDate);
            pstmt.executeUpdate();
            response.getWriter().println("Fixture created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	 int tourId = Integer.parseInt(request.getParameter("tour_id"));

    	 String sql = "SELECT IFNULL(md.result , '' ) AS result, f.fixture_id, f.tour_id, tour.name AS tour_name, "
    	            + "t1.team_id AS team1_id, t1.name AS team1_name, p1.name AS team1_captain, "
    	            + "t2.team_id AS team2_id, t2.name AS team2_name, p2.name AS team2_captain, "
    	            + "IFNULL(v.venue_id,'NOT SET') AS venue_id, IFNULL(v.stadium , 'NOT SET') AS venue_name, IFNULL(v.location, 'NOT_SET') AS venue_location, "
    	            + "IFNULL(winner_team.name, 'No winner') AS winner_team_name, f.match_date "
    	            + "FROM fixture f "
    	            + "LEFT JOIN match_details md ON f.fixture_id = md.fixture_id "
    	            + "JOIN tournament tour ON f.tour_id = tour.tour_id "
    	            + "JOIN team t1 ON f.team1_id = t1.team_id "
    	            + "JOIN player p1 ON t1.captain_id = p1.id "
    	            + "JOIN team t2 ON f.team2_id = t2.team_id "
    	            + "JOIN player p2 ON t2.captain_id = p2.id "
    	            + "JOIN venue v ON f.venue_id = v.venue_id "
    	            + "LEFT JOIN team winner_team ON f.winner_id = winner_team.team_id "
    	            + "WHERE f.tour_id = ?";
         
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");

         try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, tourId);
             ResultSet rs = pstmt.executeQuery();

             JSONArray fixturesArray = new JSONArray(); 

             while (rs.next()) {
            	 
                 JSONObject fixtureJson = new JSONObject();
                 
                 fixtureJson.put("fixture_id", rs.getInt("fixture_id"));
                 fixtureJson.put("tour_id", rs.getInt("tour_id"));
                 fixtureJson.put("tour_name", rs.getString("tour_name"));
                 
                 fixtureJson.put("team1_id", rs.getInt("team1_id"));
                 fixtureJson.put("team1_name", rs.getString("team1_name"));
                 fixtureJson.put("team1_captain", rs.getString("team1_captain"));
                 
                 fixtureJson.put("team2_id", rs.getInt("team2_id"));
                 fixtureJson.put("team2_name", rs.getString("team2_name"));
                 fixtureJson.put("team2_captain", rs.getString("team2_captain"));
                 
                 fixtureJson.put("winner_team", rs.getString("winner_team_name") + " "+ rs.getString("result") );
                 
                 fixtureJson.put("venue_id", rs.getInt("venue_id"));
                 fixtureJson.put("venue_name", rs.getString("venue_name"));
                 fixtureJson.put("venue_location", rs.getString("venue_location"));
                 
                 fixtureJson.put("match_date", rs.getDate("match_date").toString());
                 

                 fixturesArray.put(fixtureJson); 
             }

             PrintWriter out = response.getWriter();
             out.print(fixturesArray.toString());  
             out.flush();
         } catch (SQLException e) {
             e.printStackTrace();
             response.getWriter().println("Database error: " + e.getMessage());
         }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
        int tourId = Integer.parseInt(request.getParameter("tour_id"));
        int team1Id = Integer.parseInt(request.getParameter("team1_id"));
        int team2Id = Integer.parseInt(request.getParameter("team2_id"));
        int winnerId = Integer.parseInt(request.getParameter("winner_id"));
        int venueId = Integer.parseInt(request.getParameter("venue_id"));
        String matchDate = request.getParameter("match_date");

        String sql = "UPDATE fixture SET team1_id = ?, team2_id = ?, winner_id = ?, venue_id = ?, match_date = ? WHERE fixture_id = ? AND tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, team1Id);
            pstmt.setInt(2, team2Id);
            pstmt.setInt(3, winnerId);
            pstmt.setInt(4, venueId);
            pstmt.setString(5, matchDate);
            pstmt.setInt(6, fixtureId);
            pstmt.setInt(7, tourId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                response.getWriter().println("Fixture updated successfully.");
            } else {
                response.getWriter().println("No fixture found with the given fixture_id and tour_id.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));

        String sql = "DELETE FROM fixture WHERE fixture_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                response.getWriter().println("Fixture deleted successfully.");
            } else {
                response.getWriter().println("No fixture found with the given fixture_id.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
}
