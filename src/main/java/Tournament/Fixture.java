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

         String sql = "SELECT * FROM fixture WHERE tour_id = ?";
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
                 fixtureJson.put("team1_id", rs.getInt("team1_id"));
                 fixtureJson.put("team2_id", rs.getInt("team2_id"));
                 fixtureJson.put("winner_id", rs.getInt("winner_id"));
                 fixtureJson.put("venue_id", rs.getInt("venue_id"));
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
